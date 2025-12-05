package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.service.contract.ReCaptchaServiceContract;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service implementation for verifying Google reCAPTCHA v2 tokens.
 *
 * <p>This service validates reCAPTCHA tokens by making requests to Google's
 * verification API. It provides protection against automated bot registrations.</p>
 *
 * <h3>Security Features:</h3>
 * <ul>
 *     <li>Verifies reCAPTCHA tokens with Google's API</li>
 *     <li>Validates token presence and format</li>
 *     <li>Handles verification errors gracefully</li>
 *     <li>Provides detailed error logging for debugging</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@Service
public class ReCaptchaService implements ReCaptchaServiceContract {

    private static final Logger logger = LoggerFactory.getLogger(ReCaptchaService.class);

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ReCaptchaService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verifies a reCAPTCHA token with Google's API.
     *
     * <p>This method validates the token by making a POST request to Google's
     * verification endpoint. It checks both the HTTP response and the success
     * field in the JSON response.</p>
     *
     * @param token The reCAPTCHA token from the client
     *
     * @return true if the token is valid, false otherwise
     *
     * @throws IllegalArgumentException if token is null or blank
     *
     * @author Philipp Borkovic
     */
    @Override
    public boolean verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            logger.warn("reCAPTCHA token is null or blank");
            throw new IllegalArgumentException("reCAPTCHA token is required");
        }

        logger.debug("Verifying reCAPTCHA token. Secret key configured: {}, Verify URL: {}",
                     (secretKey != null && !secretKey.isEmpty()), verifyUrl);

        try {
            String requestBody = String.format("secret=%s&response=%s", secretKey, token);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(verifyUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            logger.debug("Sending reCAPTCHA verification request to: {}", verifyUrl);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("reCAPTCHA API response status: {}, body: {}",
                        response.statusCode(), response.body());

            if (response.statusCode() != 200) {
                logger.error("reCAPTCHA verification failed with status code: {}", response.statusCode());
                return false;
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body());
            boolean success = jsonResponse.path("success").asBoolean(false);

            if (!success) {
                JsonNode errorCodes = jsonResponse.path("error-codes");
                String hostname = jsonResponse.path("hostname").asText("unknown");
                logger.warn("reCAPTCHA verification failed. Success: {}, Error codes: {}, Hostname: {}",
                           success, errorCodes, hostname);
                return false;
            }

            String hostname = jsonResponse.path("hostname").asText("unknown");
            logger.info("reCAPTCHA verification successful for hostname: {}", hostname);
            return true;

        } catch (Exception e) {
            logger.error("Error verifying reCAPTCHA token", e);
            return false;
        }
    }
}
