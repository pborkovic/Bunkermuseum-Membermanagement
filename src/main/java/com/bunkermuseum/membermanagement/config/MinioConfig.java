package com.bunkermuseum.membermanagement.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for MinIO object storage.
 *
 * <p>This configuration sets up the MinIO client for file storage operations
 * including profile picture uploads and other binary data storage.</p>
 *
 * @author Philipp Borkovic
 */
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin123}")
    private String secretKey;

    /**
     * Creates and configures the MinIO client bean.
     *
     * @return Configured MinIO client instance
     *
     * @author Philipp Borkovic
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
