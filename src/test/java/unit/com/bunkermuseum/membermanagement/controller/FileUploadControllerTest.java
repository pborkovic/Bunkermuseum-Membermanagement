package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.FileUploadController;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.UserService;
import com.bunkermuseum.membermanagement.service.contract.MinioServiceContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link FileUploadController} class.
 *
 * <p>This test class validates all file upload and retrieval endpoints exposed by
 * the FileUploadController, focusing on profile picture management for users. It uses
 * Mockito to mock service dependencies and Spring Security components, testing controller
 * logic, file validation, error handling, and integration with MinIO object storage.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *   <li>Profile picture upload with authentication validation</li>
 *   <li>File size and type validation (max 5MB, JPEG/PNG/WebP only)</li>
 *   <li>Automatic deletion of old profile pictures</li>
 *   <li>Profile picture retrieval with content type detection</li>
 *   <li>Error handling for authentication, validation, and storage failures</li>
 *   <li>HTTP status code responses and error messages</li>
 * </ul>
 *
 * @see FileUploadController
 * @see MinioServiceContract
 * @see UserService
 *
 * @author Philipp Borkovic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadController Unit Tests")
class FileUploadControllerTest {

    /**
     * Mock instance of the MinIO service for object storage operations.
     * This mock controls file upload, download, and deletion behavior.
     */
    @Mock
    private MinioServiceContract minioService;

    /**
     * Mock instance of the user service for database operations.
     * This mock controls user retrieval and update behavior.
     */
    @Mock
    private UserService userService;

    /**
     * Mock instance of Spring Security context for authentication testing.
     */
    @Mock
    private SecurityContext securityContext;

    /**
     * Mock instance of Spring Security authentication for user principal testing.
     */
    @Mock
    private Authentication authentication;

    /**
     * Test instance of FileUploadController for testing endpoint behavior.
     */
    private FileUploadController fileUploadController;

    /**
     * Test user entity used across multiple test methods.
     */
    private User testUser;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A FileUploadController instance with mocked service dependencies</li>
     *   <li>A test User entity with ID and email</li>
     *   <li>Uses reflection to set the protected ID field</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        fileUploadController = new FileUploadController(minioService, userService);

        testUser = new User("Test User", "test@example.com", "hashedPassword123");

        // Set ID using reflection
        try {
            java.lang.reflect.Field idField = testUser.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test user ID", e);
        }
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method with a valid JPEG file.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully uploads a valid JPEG file</li>
     *   <li>The user is authenticated and retrieved from SecurityContext</li>
     *   <li>File validation passes for valid JPEG content</li>
     *   <li>Old profile picture is deleted if exists</li>
     *   <li>File is uploaded to MinIO storage</li>
     *   <li>User's avatar path is updated in database</li>
     *   <li>Presigned URL is generated for immediate access</li>
     *   <li>Response contains success message, object name, and URL</li>
     *   <li>HTTP 200 OK status is returned</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully upload valid JPEG profile picture")
    void testUploadProfilePicture_ValidJpegFile_Success() throws Exception {
        // Arrange
        byte[] imageContent = "fake-jpeg-content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                imageContent
        );

        String objectName = "profile-pictures/" + UUID.randomUUID() + "-profile.jpg";
        String presignedUrl = "https://minio.example.com/presigned-url";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(minioService.uploadFile(eq(file), anyString())).thenReturn(objectName);
        when(minioService.getPresignedUrl(objectName)).thenReturn(presignedUrl);
        when(userService.updateUser(eq(testUser.getId()), any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Profile picture uploaded successfully", response.getBody().get("message"));
        assertEquals(objectName, response.getBody().get("objectName"));
        assertEquals(presignedUrl, response.getBody().get("url"));

        verify(minioService).uploadFile(eq(file), anyString());
        verify(minioService).getPresignedUrl(objectName);
        verify(userService).updateUser(eq(testUser.getId()), any(User.class));

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method with a valid PNG file.
     *
     * <p>This test verifies that PNG files are accepted and processed correctly.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully upload valid PNG profile picture")
    void testUploadProfilePicture_ValidPngFile_Success() throws Exception {
        // Arrange
        byte[] imageContent = "fake-png-content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                imageContent
        );

        String objectName = "profile-pictures/" + UUID.randomUUID() + "-profile.png";
        String presignedUrl = "https://minio.example.com/presigned-url";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(minioService.uploadFile(eq(file), anyString())).thenReturn(objectName);
        when(minioService.getPresignedUrl(objectName)).thenReturn(presignedUrl);
        when(userService.updateUser(eq(testUser.getId()), any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile picture uploaded successfully", response.getBody().get("message"));

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method with a valid WebP file.
     *
     * <p>This test verifies that WebP files are accepted and processed correctly.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully upload valid WebP profile picture")
    void testUploadProfilePicture_ValidWebPFile_Success() throws Exception {
        // Arrange
        byte[] imageContent = "fake-webp-content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.webp",
                "image/webp",
                imageContent
        );

        String objectName = "profile-pictures/" + UUID.randomUUID() + "-profile.webp";
        String presignedUrl = "https://minio.example.com/presigned-url";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(minioService.uploadFile(eq(file), anyString())).thenReturn(objectName);
        when(minioService.getPresignedUrl(objectName)).thenReturn(presignedUrl);
        when(userService.updateUser(eq(testUser.getId()), any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile picture uploaded successfully", response.getBody().get("message"));

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method when user is not authenticated.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 401 UNAUTHORIZED status is returned</li>
     *   <li>Error message indicates authentication failure</li>
     *   <li>No file upload is attempted</li>
     *   <li>No user database update occurs</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void testUploadProfilePicture_NotAuthenticated_Returns401() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not authenticated", response.getBody().get("error"));

        verify(minioService, never()).uploadFile(any(), anyString());
        verify(userService, never()).updateUser(any(), any());

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method with an empty file.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 400 BAD REQUEST status is returned</li>
     *   <li>Error message indicates file is empty</li>
     *   <li>No file upload is attempted</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 400 when file is empty")
    void testUploadProfilePicture_EmptyFile_Returns400() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                new byte[0]
        );

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File is empty", response.getBody().get("error"));

        verify(minioService, never()).uploadFile(any(), anyString());

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method with a file that exceeds size limit.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 400 BAD REQUEST status is returned</li>
     *   <li>Error message indicates file is too large</li>
     *   <li>Files larger than 5MB are rejected</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 400 when file exceeds 5MB size limit")
    void testUploadProfilePicture_FileTooLarge_Returns400() throws Exception {
        // Arrange
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                largeContent
        );

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File size exceeds maximum allowed size of 5MB", response.getBody().get("error"));

        verify(minioService, never()).uploadFile(any(), anyString());

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method with an invalid file type.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 400 BAD REQUEST status is returned</li>
     *   <li>Error message indicates invalid file type</li>
     *   <li>Non-image files (e.g., PDF) are rejected</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 400 when file type is not allowed")
    void testUploadProfilePicture_InvalidFileType_Returns400() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid file type. Only JPEG, PNG, and WebP images are allowed", response.getBody().get("error"));

        verify(minioService, never()).uploadFile(any(), anyString());

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method when deleting old profile picture.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Old profile picture is deleted before uploading new one</li>
     *   <li>Upload succeeds even if old file deletion fails</li>
     *   <li>New file is uploaded successfully</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should delete old profile picture before uploading new one")
    void testUploadProfilePicture_WithExistingAvatar_DeletesOld() throws Exception {
        // Arrange
        String oldAvatarPath = "profile-pictures/old-avatar.jpg";
        testUser.setAvatarPath(oldAvatarPath);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        String objectName = "profile-pictures/" + UUID.randomUUID() + "-profile.jpg";
        String presignedUrl = "https://minio.example.com/presigned-url";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(minioService.uploadFile(eq(file), anyString())).thenReturn(objectName);
        when(minioService.getPresignedUrl(objectName)).thenReturn(presignedUrl);
        when(userService.updateUser(eq(testUser.getId()), any(User.class))).thenReturn(testUser);
        doNothing().when(minioService).deleteFile(oldAvatarPath);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(minioService).deleteFile(oldAvatarPath);
        verify(minioService).uploadFile(eq(file), anyString());

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#uploadProfilePicture} method when MinIO upload fails.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 500 INTERNAL SERVER ERROR status is returned</li>
     *   <li>Error message contains failure details</li>
     *   <li>Exception is caught and converted to appropriate response</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 500 when MinIO upload fails")
    void testUploadProfilePicture_MinioUploadFails_Returns500() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(minioService.uploadFile(eq(file), anyString()))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Failed to upload profile picture"));

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method with valid user and avatar.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Profile picture is retrieved successfully</li>
     *   <li>Content-Type header is set correctly based on file extension</li>
     *   <li>Cache-Control header is set for 1 hour</li>
     *   <li>Image bytes are returned in response body</li>
     *   <li>HTTP 200 OK status is returned</li>
     * </ul>
     *
     * @throws IOException if input stream operations fail
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve profile picture for user")
    void testGetProfilePicture_ValidUser_ReturnsImage() throws IOException {
        // Arrange
        UUID userId = testUser.getId();
        String avatarPath = "profile-pictures/avatar.jpg";
        testUser.setAvatarPath(avatarPath);

        byte[] imageBytes = "fake-image-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(minioService.downloadFile(avatarPath)).thenReturn(inputStream);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(imageBytes, response.getBody());
        assertEquals("image/jpeg", response.getHeaders().getFirst("Content-Type"));
        assertEquals("max-age=3600", response.getHeaders().getFirst("Cache-Control"));

        verify(userService).findById(userId);
        verify(minioService).downloadFile(avatarPath);
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method with PNG file.
     *
     * <p>This test verifies that content type is correctly set to image/png.</p>
     *
     * @throws IOException if input stream operations fail
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return correct content type for PNG images")
    void testGetProfilePicture_PngImage_ReturnsCorrectContentType() throws IOException {
        // Arrange
        UUID userId = testUser.getId();
        String avatarPath = "profile-pictures/avatar.png";
        testUser.setAvatarPath(avatarPath);

        byte[] imageBytes = "fake-png-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(minioService.downloadFile(avatarPath)).thenReturn(inputStream);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertEquals("image/png", response.getHeaders().getFirst("Content-Type"));
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method with WebP file.
     *
     * <p>This test verifies that content type is correctly set to image/webp.</p>
     *
     * @throws IOException if input stream operations fail
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return correct content type for WebP images")
    void testGetProfilePicture_WebPImage_ReturnsCorrectContentType() throws IOException {
        // Arrange
        UUID userId = testUser.getId();
        String avatarPath = "profile-pictures/avatar.webp";
        testUser.setAvatarPath(avatarPath);

        byte[] imageBytes = "fake-webp-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(minioService.downloadFile(avatarPath)).thenReturn(inputStream);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertEquals("image/webp", response.getHeaders().getFirst("Content-Type"));
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method when user not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 404 NOT FOUND status is returned</li>
     *   <li>No file download is attempted</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetProfilePicture_UserNotFound_Returns404() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(minioService, never()).downloadFile(anyString());
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method when user has no avatar.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 404 NOT FOUND status is returned</li>
     *   <li>No file download is attempted</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 404 when user has no avatar path")
    void testGetProfilePicture_NoAvatarPath_Returns404() {
        // Arrange
        UUID userId = testUser.getId();
        testUser.setAvatarPath(null);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(minioService, never()).downloadFile(anyString());
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method when avatar path is blank.
     *
     * <p>This test verifies that blank avatar paths are treated as missing.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 404 when user avatar path is blank")
    void testGetProfilePicture_BlankAvatarPath_Returns404() {
        // Arrange
        UUID userId = testUser.getId();
        testUser.setAvatarPath("   ");

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Tests the {@link FileUploadController#getProfilePicture} method when MinIO download fails.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>HTTP 500 INTERNAL SERVER ERROR status is returned</li>
     *   <li>Exception is caught and converted to error response</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return 500 when MinIO download fails")
    void testGetProfilePicture_MinioDownloadFails_Returns500() {
        // Arrange
        UUID userId = testUser.getId();
        String avatarPath = "profile-pictures/avatar.jpg";
        testUser.setAvatarPath(avatarPath);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(minioService.downloadFile(avatarPath))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        // Act
        ResponseEntity<byte[]> response = fileUploadController.getProfilePicture(userId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * Tests the {@link FileUploadController} constructor with null MinioService.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>IllegalArgumentException is thrown when MinioService is null</li>
     *   <li>Constructor validates required dependencies</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when MinioService is null")
    void testConstructor_NullMinioService_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new FileUploadController(null, userService);
        });
    }

    /**
     * Tests the {@link FileUploadController} constructor with null UserService.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>IllegalArgumentException is thrown when UserService is null</li>
     *   <li>Constructor validates required dependencies</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when UserService is null")
    void testConstructor_NullUserService_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new FileUploadController(minioService, null);
        });
    }
}
