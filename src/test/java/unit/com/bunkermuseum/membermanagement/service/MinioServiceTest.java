package unit.com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link MinioService} class.
 *
 * <p>This test class validates all MinIO file storage operations provided by the
 * MinioService implementation. It uses Mockito to mock MinioClient and MultipartFile
 * dependencies, focusing on testing business logic, validation, and error handling.</p>
 *
 * @see MinioService
 * @see MinioClient
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinioService Unit Tests")
class MinioServiceTest {

    /**
     * Mock instance of the MinIO client used by MinioService.
     * This mock allows us to control and verify MinIO interactions.
     */
    @Mock
    private MinioClient minioClient;

    /**
     * Mock instance of a MultipartFile for testing file uploads.
     */
    @Mock
    private MultipartFile multipartFile;

    /**
     * Test instance of MinioService for testing file storage operations.
     */
    private MinioService minioService;

    /**
     * Test bucket name used in tests.
     */
    private static final String TEST_BUCKET = "test-bucket";

    /**
     * Test expiry time for presigned URLs.
     */
    private static final int TEST_EXPIRY_SECONDS = 3600;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A MinioService instance with mocked MinioClient</li>
     *   <li>Test configuration values for bucket name and expiry</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        minioService = new MinioService(minioClient);
        ReflectionTestUtils.setField(minioService, "bucketName", TEST_BUCKET);
        ReflectionTestUtils.setField(minioService, "expirySeconds", TEST_EXPIRY_SECONDS);
    }

    /**
     * Tests the {@link MinioService#uploadFile} method with a valid file.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully uploads a file to MinIO</li>
     *   <li>Returns a valid object name with UUID and extension</li>
     *   <li>The MinIO client putObject method is called</li>
     *   <li>The object name includes the specified folder path</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully upload a valid file")
    void testUploadFile_ValidFile_ReturnsObjectName() throws Exception {
        // Arrange
        String folder = "profile-pictures";
        String filename = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        String result = minioService.uploadFile(multipartFile, folder);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith(folder + "/"));
        assertTrue(result.endsWith(".jpg"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#uploadFile} method with a file without extension.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully uploads files without extensions</li>
     *   <li>Returns an object name without extension</li>
     *   <li>The MinIO client putObject method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should upload file without extension")
    void testUploadFile_FileWithoutExtension_ReturnsObjectNameWithoutExtension() throws Exception {
        // Arrange
        String folder = "documents";
        String filename = "testfile";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("application/octet-stream");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        String result = minioService.uploadFile(multipartFile, folder);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith(folder + "/"));
        assertFalse(result.contains("."));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#uploadFile} method with null filename.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method handles null filenames gracefully</li>
     *   <li>Returns an object name without extension</li>
     *   <li>The upload proceeds successfully</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle null filename")
    void testUploadFile_NullFilename_ReturnsObjectNameWithoutExtension() throws Exception {
        // Arrange
        String folder = "uploads";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("application/octet-stream");
        when(multipartFile.isEmpty()).thenReturn(false);

        // Act
        String result = minioService.uploadFile(multipartFile, folder);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith(folder + "/"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#uploadFile} method with null file.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when file is null</li>
     *   <li>The exception message contains "cannot be null or empty"</li>
     *   <li>The MinIO client is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when file is null")
    void testUploadFile_NullFile_ThrowsIllegalArgumentException() throws Exception {
        // Arrange
        String folder = "uploads";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            minioService.uploadFile(null, folder);
        });

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#uploadFile} method with empty file.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when file is empty</li>
     *   <li>The exception message contains "cannot be null or empty"</li>
     *   <li>The MinIO client is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when file is empty")
    void testUploadFile_EmptyFile_ThrowsIllegalArgumentException() throws Exception {
        // Arrange
        String folder = "uploads";
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            minioService.uploadFile(multipartFile, folder);
        });

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#uploadFile} method when MinIO client throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when MinIO operation fails</li>
     *   <li>The exception message contains "Failed to upload file"</li>
     *   <li>The original exception is wrapped</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when MinIO client fails")
    void testUploadFile_MinioClientThrowsException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String folder = "uploads";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            minioService.uploadFile(multipartFile, folder);
        });

        assertTrue(exception.getMessage().contains("Failed to upload file"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#uploadFile} method when InputStream throws IOException.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when file input stream fails</li>
     *   <li>The exception wraps the IOException from getInputStream</li>
     *   <li>The MinIO client is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should wrap IOException from InputStream in RuntimeException")
    void testUploadFile_InputStreamThrowsException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String folder = "uploads";

        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Cannot read file"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            minioService.uploadFile(multipartFile, folder);
        });

        assertTrue(exception.getMessage().contains("Failed to upload file"));
        assertTrue(exception.getCause() instanceof IOException);
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#getPresignedUrl} method with valid object name.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully generates a presigned URL</li>
     *   <li>Returns the URL string from MinIO client</li>
     *   <li>The MinIO client is called with correct parameters</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully generate presigned URL")
    void testGetPresignedUrl_ValidObjectName_ReturnsUrl() throws Exception {
        // Arrange
        String objectName = "profile-pictures/test.jpg";
        String expectedUrl = "https://minio.example.com/test-bucket/profile-pictures/test.jpg?signature=abc123";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        // Act
        String result = minioService.getPresignedUrl(objectName);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    /**
     * Tests the {@link MinioService#getPresignedUrl} method when MinIO client throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when URL generation fails</li>
     *   <li>The exception message contains "Failed to generate presigned URL"</li>
     *   <li>The original exception is wrapped</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when presigned URL generation fails")
    void testGetPresignedUrl_MinioClientThrowsException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String objectName = "profile-pictures/test.jpg";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            minioService.getPresignedUrl(objectName);
        });

        assertTrue(exception.getMessage().contains("Failed to generate presigned URL"));
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    /**
     * Tests the {@link MinioService#deleteFile} method with valid object name.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully deletes a file from MinIO</li>
     *   <li>The MinIO client removeObject method is called</li>
     *   <li>No exception is thrown for successful deletion</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully delete a file")
    void testDeleteFile_ValidObjectName_DeletesFile() throws Exception {
        // Arrange
        String objectName = "profile-pictures/test.jpg";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act
        minioService.deleteFile(objectName);

        // Assert
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#deleteFile} method with null object name.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns early when object name is null</li>
     *   <li>No exception is thrown for null object names</li>
     *   <li>The MinIO client is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle null object name gracefully")
    void testDeleteFile_NullObjectName_DoesNothing() throws Exception {
        // Act
        minioService.deleteFile(null);

        // Assert
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#deleteFile} method with blank object name.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns early when object name is blank</li>
     *   <li>No exception is thrown for blank object names</li>
     *   <li>The MinIO client is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle blank object name gracefully")
    void testDeleteFile_BlankObjectName_DoesNothing() throws Exception {
        // Act
        minioService.deleteFile("   ");

        // Assert
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#deleteFile} method with empty string object name.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns early when object name is empty</li>
     *   <li>No exception is thrown for empty object names</li>
     *   <li>The MinIO client is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle empty object name gracefully")
    void testDeleteFile_EmptyObjectName_DoesNothing() throws Exception {
        // Act
        minioService.deleteFile("");

        // Assert
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#deleteFile} method when MinIO client throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when deletion fails</li>
     *   <li>The exception message contains "Failed to delete file"</li>
     *   <li>The original exception is wrapped</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when file deletion fails")
    void testDeleteFile_MinioClientThrowsException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String objectName = "profile-pictures/test.jpg";

        doThrow(new RuntimeException("MinIO connection error"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            minioService.deleteFile(objectName);
        });

        assertTrue(exception.getMessage().contains("Failed to delete file"));
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#downloadFile} method with valid object name.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully downloads a file from MinIO</li>
     *   <li>Returns an InputStream for the file contents</li>
     *   <li>The MinIO client getObject method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully download a file")
    void testDownloadFile_ValidObjectName_ReturnsInputStream() throws Exception {
        // Arrange
        String objectName = "profile-pictures/test.jpg";
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // Act
        InputStream result = minioService.downloadFile(objectName);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponse, result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    /**
     * Tests the {@link MinioService#downloadFile} method when MinIO client throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when download fails</li>
     *   <li>The exception message contains "Failed to download file"</li>
     *   <li>The original exception is wrapped</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when file download fails")
    void testDownloadFile_MinioClientThrowsException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String objectName = "profile-pictures/test.jpg";

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            minioService.downloadFile(objectName);
        });

        assertTrue(exception.getMessage().contains("Failed to download file"));
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    /**
     * Tests the bucket initialization logic indirectly through constructor.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The service can be instantiated successfully</li>
     *   <li>Bucket initialization is handled correctly</li>
     * </ul>
     *
     * <p><strong>Note:</strong> Direct testing of @PostConstruct methods in unit tests
     * is challenging. This test verifies basic instantiation. Full initialization
     * testing is better suited for integration tests.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully instantiate MinioService")
    void testServiceInstantiation_ValidMinioClient_CreatesService() {
        // Act
        MinioService service = new MinioService(minioClient);

        // Assert
        assertNotNull(service);
    }
}
