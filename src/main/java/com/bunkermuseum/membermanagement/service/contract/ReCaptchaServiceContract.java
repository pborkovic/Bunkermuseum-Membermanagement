package com.bunkermuseum.membermanagement.service.contract;

/**
 * Service contract interface for Google reCAPTCHA v3 verification operations.
 *
 * <p>This interface defines the contract for reCAPTCHA token verification business logic.
 * It provides methods to validate reCAPTCHA tokens received from the frontend
 * by communicating with Google's reCAPTCHA API.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>Token Verification:</strong> Validates reCAPTCHA tokens with Google's API</li>
 *   <li><strong>Score Evaluation:</strong> Evaluates the v3 risk score against a configurable threshold</li>
 *   <li><strong>Action Binding:</strong> Confirms the token was issued for the expected action</li>
 *   <li><strong>Security:</strong> Prevents automated bot registrations and form submissions</li>
 *   <li><strong>Error Handling:</strong> Comprehensive error handling and logging</li>
 * </ul>
 */
public interface ReCaptchaServiceContract {

    /**
     * Verifies a reCAPTCHA v3 token with Google's verification API.
     *
     * <p>This method validates the reCAPTCHA token by making a POST request to
     * Google's siteverify endpoint. Unlike v2 (checkbox), reCAPTCHA v3 returns a
     * risk {@code score} (0.0 - 1.0) and the {@code action} the token was generated
     * for, both of which are validated in addition to the {@code success} flag.</p>
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
     *   <li>If the token is null or blank, verification is skipped (treated as optional)</li>
     *   <li>Sends POST request to Google's API with secret key and token</li>
     *   <li>Parses response JSON to check the success status</li>
     *   <li>Confirms the returned action matches the expected action</li>
     *   <li>Confirms the returned score meets the configured threshold</li>
     *   <li>Returns true if all checks pass, false otherwise</li>
     * </ol>
     *
     * @param token the reCAPTCHA token received from the frontend (g-recaptcha-response)
     * @param expectedAction the action name the token is expected to be bound to
     *                       (e.g. "register"); ignored if null or blank
     *
     * @return true if the token is valid, bound to the expected action, and scores
     *         at or above the configured threshold; false otherwise
     */
    boolean verifyToken(String token, String expectedAction);
}
