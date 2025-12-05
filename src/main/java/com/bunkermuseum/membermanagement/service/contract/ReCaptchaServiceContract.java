package com.bunkermuseum.membermanagement.service.contract;

/**
 * Service contract interface for Google reCAPTCHA v2 verification operations.
 *
 * <p>This interface defines the contract for reCAPTCHA token verification business logic.
 * It provides methods to validate reCAPTCHA tokens received from the frontend
 * by communicating with Google's reCAPTCHA API.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>Token Verification:</strong> Validates reCAPTCHA tokens with Google's API</li>
 *   <li><strong>Security:</strong> Prevents automated bot registrations and form submissions</li>
 *   <li><strong>Error Handling:</strong> Comprehensive error handling and logging</li>
 *   <li><strong>Integration:</strong> Works with both reCAPTCHA v2 checkbox and invisible modes</li>
 * </ul>
 */
public interface ReCaptchaServiceContract {

    /**
     * Verifies a reCAPTCHA token with Google's verification API.
     *
     * <p>This method validates the reCAPTCHA token by making a POST request to
     * Google's siteverify endpoint. It checks the response to ensure the token
     * is valid and hasn't been tampered with.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>User registration form validation</li>
     *   <li>Contact form spam prevention</li>
     *   <li>Login attempt verification</li>
     *   <li>Any user-submitted form requiring bot protection</li>
     * </ul>
     *
     * <p><strong>Verification Process:</strong></p>
     * <ol>
     *   <li>Validates token is not null or empty</li>
     *   <li>Sends POST request to Google's API with secret key and token</li>
     *   <li>Parses response JSON to check success status</li>
     *   <li>Logs error codes if verification fails</li>
     *   <li>Returns true if token is valid, false otherwise</li>
     * </ol>
     *
     * @param token the reCAPTCHA token received from the frontend (g-recaptcha-response)
     *
     * @return true if the token is valid and verified by Google, false otherwise
     *
     * @throws IllegalArgumentException if token is null or blank
     */
    boolean verifyToken(String token);
}
