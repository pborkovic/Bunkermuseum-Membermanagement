package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.MinioServiceContract;
import com.bunkermuseum.membermanagement.service.UserService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for file upload operations.
 *
 * <p>Handles file uploads such as profile pictures, storing them in MinIO
 * object storage and updating user records with the file paths.</p>
 *
 * @author Philipp Borkovic
 */
@RestController
@RequestMapping("/api/upload")
@PermitAll
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/jpg", "image/webp"};

    private final MinioServiceContract minioService;
    private final UserService userService;

    public FileUploadController(MinioServiceContract minioService, UserService userService) {
        this.minioService = minioService;
        this.userService = userService;
    }

    /**
     * Uploads a profile picture for the authenticated user.
     *
     * <p>Validates file size and type, uploads to MinIO, and updates the user's
     * avatar_path field. Deletes the old profile picture if one exists.</p>
     *
     * @param file The multipart file containing the profile picture
     * @return ResponseEntity with upload result and file URL
     *
     * @author Philipp Borkovic
     */
    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof User)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            User principalUser = (User) principal;

            // Fetch user from database to ensure we have a managed entity
            User user = userService.findById(principalUser.getId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File size exceeds maximum allowed size of 5MB"));
            }

            String contentType = file.getContentType();
            boolean validType = false;
            for (String allowedType : ALLOWED_CONTENT_TYPES) {
                if (allowedType.equals(contentType)) {
                    validType = true;
                    break;
                }
            }

            if (!validType) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type. Only JPEG, PNG, and WebP images are allowed"));
            }

            // Delete old profile picture if exists
            if (user.getAvatarPath() != null && !user.getAvatarPath().isBlank()) {
                try {
                    minioService.deleteFile(user.getAvatarPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete old profile picture: {}", e.getMessage());
                }
            }

            // Upload new file
            String objectName = minioService.uploadFile(file, "profile-pictures");

            // Update user record with new avatar path
            user.setAvatarPath(objectName);
            userService.updateUser(user.getId(), user);

            // Generate presigned URL for immediate access
            String url = minioService.getPresignedUrl(objectName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("objectName", objectName);
            response.put("url", url);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading profile picture: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload profile picture: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a presigned URL for accessing a profile picture.
     *
     * @param userId The ID of the user whose profile picture to retrieve
     * @return ResponseEntity with the presigned URL
     *
     * @author Philipp Borkovic
     */
    @GetMapping("/profile-picture/{userId}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable UUID userId) {
        try {
            logger.info("Fetching profile picture for user: {}", userId);

            User user = userService.findById(userId).orElse(null);

            if (user == null) {
                logger.warn("User not found: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
                logger.warn("User {} has no avatar path", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logger.info("Loading image from MinIO: {}", user.getAvatarPath());

            // Stream the file directly from MinIO
            try (var inputStream = minioService.downloadFile(user.getAvatarPath())) {
                byte[] imageBytes = inputStream.readAllBytes();

                logger.info("Successfully loaded image, size: {} bytes", imageBytes.length);

                // Determine content type from file extension
                String contentType = "image/jpeg"; // default
                if (user.getAvatarPath().toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (user.getAvatarPath().toLowerCase().endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .header("Cache-Control", "max-age=3600")
                        .body(imageBytes);
            }

        } catch (Exception e) {
            logger.error("Error retrieving profile picture for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
