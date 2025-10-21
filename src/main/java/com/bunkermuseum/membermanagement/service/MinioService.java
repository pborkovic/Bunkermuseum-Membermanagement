package com.bunkermuseum.membermanagement.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing file operations with MinIO object storage.
 *
 * <p>Handles file uploads, downloads, deletions, and URL generation for
 * profile pictures and other binary data storage.</p>
 *
 * @author Philipp Borkovic
 */
@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:bunkermuseum}")
    private String bucketName;

    @Value("${minio.expiry-seconds:3600}")
    private int expirySeconds;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Initializes the MinIO bucket if it doesn't exist.
     * Called after dependency injection is complete.
     *
     * @author Philipp Borkovic
     */
    @PostConstruct
    private void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                logger.info("Created MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("Error initializing MinIO bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }

    /**
     * Uploads a file to MinIO and returns the object name.
     *
     * @param file The multipart file to upload
     * @param folder The folder/prefix within the bucket (e.g., "profile-pictures")
     * @return The object name (path) of the uploaded file
     *
     * @throws IOException if file reading fails
     * @throws RuntimeException if upload fails
     *
     * @author Philipp Borkovic
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = folder + "/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            logger.info("Uploaded file to MinIO: {}", objectName);
            return objectName;

        } catch (Exception e) {
            logger.error("Error uploading file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    /**
     * Generates a presigned URL for accessing a file.
     *
     * @param objectName The object name (path) in MinIO
     * @return Presigned URL valid for configured expiry time
     *
     * @throws RuntimeException if URL generation fails
     *
     * @author Philipp Borkovic
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Deletes a file from MinIO.
     *
     * @param objectName The object name (path) to delete
     *
     * @throws RuntimeException if deletion fails
     *
     * @author Philipp Borkovic
     */
    public void deleteFile(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            logger.info("Deleted file from MinIO: {}", objectName);

        } catch (Exception e) {
            logger.error("Error deleting file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    /**
     * Downloads a file from MinIO.
     *
     * @param objectName The object name (path) to download
     * @return InputStream of the file
     *
     * @throws RuntimeException if download fails
     *
     * @author Philipp Borkovic
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error downloading file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }
}
