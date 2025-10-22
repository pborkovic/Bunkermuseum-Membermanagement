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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO implementation of the MinioServiceContract.
 *
 * <p>Provides object storage capabilities using MinIO, an S3-compatible
 * storage system. Handles file uploads, downloads, deletions, and URL
 * generation for profile pictures and other binary data storage.</p>
 *
 * <h3>Configuration:</h3>
 * <ul>
 *     <li><strong>minio.bucket-name:</strong> Storage bucket name (default: "bunkermuseum")</li>
 *     <li><strong>minio.expiry-seconds:</strong> Presigned URL expiry (default: 3600s / 1h)</li>
 * </ul>
 *
 * <h3>Features:</h3>
 * <ul>
 *     <li>Automatic bucket initialization on startup</li>
 *     <li>UUID-based unique file naming</li>
 *     <li>Configurable presigned URL expiry</li>
 *     <li>Comprehensive error logging</li>
 *     <li>Thread-safe operations</li>
 * </ul>
 *
 * @see MinioServiceContract
 */
@Service
public class MinioService implements MinioServiceContract {

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
     * Initializes the MinIO bucket during application startup.
     *
     * <p>This method is automatically invoked by Spring after all dependency injection
     * is complete and all {@code @Value} properties have been resolved. It ensures that
     * the configured MinIO bucket exists before any file operations are attempted.</p>
     *
     * <h3>Initialization Process:</h3>
     * <ol>
     *     <li>Checks if the configured bucket exists using {@link MinioClient#bucketExists}</li>
     *     <li>If the bucket does not exist, creates it using {@link MinioClient#makeBucket}</li>
     *     <li>Logs successful bucket creation or skips if already exists</li>
     *     <li>Throws RuntimeException if initialization fails, preventing application startup</li>
     * </ol>
     *
     * <h3>Error Handling:</h3>
     * <p>If bucket initialization fails, a {@code RuntimeException} is thrown, which:</p>
     * <ul>
     *     <li>Prevents the application from starting with broken storage</li>
     *     <li>Makes configuration errors immediately visible</li>
     *     <li>Fails fast rather than allowing silent errors during runtime</li>
     * </ul>
     *
     * <h3>Configuration Requirements:</h3>
     * <p>Requires the following application properties to be set:</p>
     * <ul>
     *     <li>{@code minio.bucket-name} - The bucket name to initialize (default: "bunkermuseum")</li>
     *     <li>MinIO server must be accessible and credentials must be valid</li>
     * </ul>
     *
     * @throws RuntimeException if bucket existence check fails, bucket creation fails,
     *                          or MinIO server is unreachable. This exception will
     *                          prevent application startup, ensuring fail-fast behavior.
     *
     * @see PostConstruct
     * @see MinioClient#bucketExists(BucketExistsArgs)
     * @see MinioClient#makeBucket(MakeBucketArgs)
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
        } catch (Exception exception) {
            logger.error("Error initializing MinIO bucket: {}", exception.getMessage(), exception);

            throw new RuntimeException("Failed to initialize MinIO bucket", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

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
        } catch (Exception exception) {
            logger.error("Error uploading file to MinIO: {}", exception.getMessage(), exception);

            throw new RuntimeException("Failed to upload file to MinIO", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
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
        } catch (Exception exception) {
            logger.error("Error generating presigned URL: {}", exception.getMessage(), exception);

            throw new RuntimeException("Failed to generate presigned URL", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
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
        } catch (Exception exception) {
            logger.error("Error deleting file from MinIO: {}", exception.getMessage(), exception);

            throw new RuntimeException("Failed to delete file from MinIO", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception exception) {
            logger.error("Error downloading file from MinIO: {}", exception.getMessage(), exception);

            throw new RuntimeException("Failed to download file from MinIO", exception);
        }
    }
}
