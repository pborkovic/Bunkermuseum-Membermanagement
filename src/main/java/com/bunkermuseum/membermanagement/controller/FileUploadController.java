package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.lib.validation.FileContentValidator;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.UserService;
import com.bunkermuseum.membermanagement.service.contract.MinioServiceContract;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for file upload operations.
 *
 * <p>This controller handles file upload functionality for the Bunker Museum
 * member management system, specifically focusing on profile picture management.
 * Files are stored in MinIO object storage with automatic cleanup of old files.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Profile picture upload with validation</li>
 *   <li>Automatic deletion of old profile pictures</li>
 *   <li>File size and type validation</li>
 *   <li>Secure file retrieval with caching</li>
 *   <li>Integration with MinIO object storage</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see MinioServiceContract
 * @see UserService
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/webp"
    };
    private static final String PROFILE_PICTURES_BUCKET = "profile-pictures";
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";
    private static final String CACHE_CONTROL_VALUE = "max-age=3600";
    private final MinioServiceContract minioService;
    private final UserService userService;


    /**
     * Constructs a new FileUploadController with required dependencies.
     *
     * @param minioService Service for MinIO object storage operations
     * @param userService  Service for user database operations
     *
     * @throws IllegalArgumentException if any parameter is null
     *
     * @author Philipp Borkovic
     */
    public FileUploadController(
            MinioServiceContract minioService,
            UserService userService
    ) {
        if (minioService == null) {
            throw new IllegalArgumentException("MinioService cannot be null");
        }
        if (userService == null) {
            throw new IllegalArgumentException("UserService cannot be null");
        }

        this.minioService = minioService;
        this.userService = userService;
    }


    /**
     * Uploads a profile picture for the currently authenticated user.
     *
     * <p>This endpoint performs the following operations:</p>
     * <ol>
     *   <li>Validates user authentication</li>
     *   <li>Validates file size (max 5MB) and type (JPEG/PNG/WebP)</li>
     *   <li>Deletes existing profile picture if present</li>
     *   <li>Uploads new file to MinIO storage</li>
     *   <li>Updates user's avatar path in database</li>
     *   <li>Generates presigned URL for immediate access</li>
     * </ol>
     *
     * <h3>Request Parameters:</h3>
     * <ul>
     *   <li><b>file</b> - Multipart file containing the profile picture</li>
     * </ul>
     *
     *
     * <h3>Error Responses:</h3>
     * <ul>
     *   <li><b>400 Bad Request</b> - Invalid file (empty, too large, wrong type)</li>
     *   <li><b>401 Unauthorized</b> - User not authenticated</li>
     *   <li><b>404 Not Found</b> - User not found in database</li>
     *   <li><b>500 Internal Server Error</b> - Upload or storage failure</li>
     * </ul>
     *
     * @param file The multipart file containing the profile picture to upload
     * @return ResponseEntity containing upload result with file URL or error message
     *
     * @throws IllegalArgumentException if file parameter is null
     *
     * @author Philipp Borkovic
     */
    @PostMapping("/profile-picture")
    @RolesAllowed({"USER", "ADMIN", "MEMBER"})
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            logger.info("Profile picture upload request received");

            User user = getAuthenticatedUser();
            if (user == null) {
                logger.warn("Upload attempt by unauthenticated user");
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }

            logger.info("User authenticated: {} (ID: {})", user.getEmail(), user.getId());

            String validationError = validateFile(file);
            if (validationError != null) {
                logger.warn("File validation failed for user {}: {}", user.getId(), validationError);
                return createErrorResponse(HttpStatus.BAD_REQUEST, validationError);
            }

            logger.info("File validation passed: {} ({} bytes, {})",
                    file.getOriginalFilename(),
                    file.getSize(),
                    file.getContentType());

            deleteOldProfilePicture(user);

            String objectName = minioService.uploadFile(file, PROFILE_PICTURES_BUCKET);
            logger.info("File uploaded to MinIO: {}", objectName);

            user.setAvatarPath(objectName);
            userService.updateUser(user.getId(), user);
            logger.info("User avatar path updated in database");

            String url = minioService.getPresignedUrl(objectName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("objectName", objectName);
            response.put("url", url);

            logger.info("Profile picture upload completed successfully for user {}", user.getId());

            return ResponseEntity.ok(response);
        } catch (Exception exception) {
            logger.error("Unexpected error during profile picture upload: {}",
                    exception.getMessage(), exception);

            return createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Failed to upload profile picture: %s", exception.getMessage())
            );
        }
    }

    /**
     * Retrieves a profile picture for a specific user.
     *
     * <p>This endpoint streams the profile picture directly from MinIO storage
     * and returns it as a byte array with appropriate content type headers.</p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Automatic content type detection based on file extension</li>
     *   <li>HTTP caching enabled (1 hour)</li>
     *   <li>Direct streaming from MinIO storage</li>
     *   <li>Proper error handling for missing files</li>
     * </ul>
     *
     * <h3>URL Path:</h3>
     * <code>GET /api/upload/profile-picture/{userId}</code>
     *
     * <h3>Response Headers:</h3>
     * <ul>
     *   <li><b>Content-Type</b> - image/jpeg, image/png, or image/webp</li>
     *   <li><b>Cache-Control</b> - max-age=3600 (1 hour)</li>
     * </ul>
     *
     * <h3>Error Responses:</h3>
     * <ul>
     *   <li><b>404 Not Found</b> - User not found or no avatar set</li>
     *   <li><b>500 Internal Server Error</b> - Storage retrieval failure</li>
     * </ul>
     *
     * @param userId The UUID of the user whose profile picture to retrieve
     * @return ResponseEntity containing the image bytes or error status
     *
     * @throws IllegalArgumentException if userId is null
     *
     * @author Philipp Borkovic
     */
    @GetMapping("/profile-picture/{userId}")
    @PermitAll
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable UUID userId) {
        User user = null;

        try {
            logger.info("Profile picture retrieval request for user: {}", userId);

            user = userService.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User not found: {}", userId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
                logger.warn("User has no avatar path for user {}", userId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logger.info("Loading profile picture from MinIO: {}", user.getAvatarPath());

            try (var inputStream = minioService.downloadFile(user.getAvatarPath())) {
                byte[] imageBytes = inputStream.readAllBytes();
                logger.info("Successfully loaded image, size: {} bytes", imageBytes.length);

                String contentType = determineContentType(user.getAvatarPath());

                return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .header("Cache-Control", CACHE_CONTROL_VALUE)
                        .body(imageBytes);
            }

        } catch (RuntimeException exception) {
            String errorMsg = exception.getMessage();
            Throwable cause = exception.getCause();
            String causeMsg = cause != null ? cause.getMessage() : null;

            boolean isKeyNotFoundError = (errorMsg != null && errorMsg.contains("does not exist")) ||
                                        (causeMsg != null && causeMsg.contains("does not exist"));

            if (isKeyNotFoundError && user != null) {
                try {
                    user.setAvatarPath(null);
                    userService.updateUser(user.getId(), user);

                    logger.info("Cleared invalid avatar path for user {}", userId);
                } catch (Exception e) {
                    logger.error("Failed to clear invalid avatar path: {}", e.getMessage());
                }

                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logger.error("Error retrieving profile picture for user {}: {}",
                    userId, exception.getMessage(), exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception exception) {
            logger.error("Unexpected error retrieving profile picture for user {}: {}",
                    userId, exception.getMessage(), exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * <p>This method extracts the user from Spring Security's SecurityContextHolder
     * and validates that the authentication is valid and contains a User object.</p>
     *
     * @return The authenticated User entity from the database, or null if not authenticated
     *
     * @author Philipp Borkovic
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User principalUser)) {
            return null;
        }

        return userService.findById(principalUser.getId()).orElse(null);
    }

    /**
     * Validates the uploaded file against size, type, and content constraints.
     *
     * <p>Performs the following validations:</p>
     * <ul>
     *   <li>File is not empty</li>
     *   <li>File size does not exceed {@link #MAX_FILE_SIZE}</li>
     *   <li>File content type is in {@link #ALLOWED_CONTENT_TYPES}</li>
     *   <li>File content matches image format (magic bytes validation)</li>
     * </ul>
     *
     * @param file The multipart file to validate
     * @return Error message if validation fails, null if validation passes
     *
     * @author Philipp Borkovic
     */
    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is empty";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds maximum allowed size of 5MB";
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(ALLOWED_CONTENT_TYPES).contains(contentType)) {
            return "Invalid file type. Only JPEG, PNG, and WebP images are allowed";
        }

        FileContentValidator.ValidationResult contentValidation =
                FileContentValidator.validateImageContent(file);

        if (!contentValidation.isValid()) {
            return contentValidation.message();
        }

        return null;
    }

    /**
     * Deletes the user's existing profile picture from MinIO storage.
     *
     * <p>This method is called before uploading a new profile picture to prevent
     * orphaned files in storage. Failures are logged but do not prevent the upload.</p>
     *
     * @param user The user whose old profile picture should be deleted
     *
     * @author Philipp Borkovic
     */
    private void deleteOldProfilePicture(User user) {
        if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
            logger.debug("No existing profile picture to delete for user {}", user.getId());

            return;
        }

        try {
            minioService.deleteFile(user.getAvatarPath());

            logger.info("Deleted old profile picture: {}", user.getAvatarPath());
        } catch (Exception exception) {
            logger.warn("Failed to delete old profile picture '{}' for user {}: {}",
                    user.getAvatarPath(), user.getId(), exception.getMessage());
        }
    }

    /**
     * Determines the MIME content type based on the file path extension.
     *
     * <p>Supported extensions:</p>
     * <ul>
     *   <li>.png → image/png</li>
     *   <li>.webp → image/webp</li>
     *   <li>.jpg, .jpeg → image/jpeg (default)</li>
     * </ul>
     *
     * @param filePath The file path with extension
     * @return The MIME content type string
     *
     * @author Philipp Borkovic
     */
    private String determineContentType(String filePath) {
        if (filePath == null) {
            return DEFAULT_CONTENT_TYPE;
        }

        String lowerCasePath = filePath.toLowerCase();

        if (lowerCasePath.endsWith(".png")) {
            return "image/png";
        } else if (lowerCasePath.endsWith(".webp")) {
            return "image/webp";
        } else {
            return DEFAULT_CONTENT_TYPE;
        }
    }

    /**
     * Creates a standardized error response with the given status and message.
     *
     * @param status The HTTP status code for the error
     * @param errorMessage The error message to include in the response body
     * @return ResponseEntity containing the error message
     *
     * @author Philipp Borkovic
     */
    private ResponseEntity<Map<String, String>> createErrorResponse(
            HttpStatus status,
            String errorMessage
    ) {
        return ResponseEntity
                .status(status)
                .body(Map.of("error", errorMessage));
    }
}