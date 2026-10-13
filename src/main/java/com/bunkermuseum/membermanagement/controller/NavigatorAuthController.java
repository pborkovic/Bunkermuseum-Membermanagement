package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * REST API used by the Bunkermuseum Navigator (QR code museum guide) to sign in
 * its owners with their member management credentials.
 *
 * <p>The Navigator sends the email and password entered on its login page to
 * {@code POST /api/navigator/auth/verify}. This controller checks them with the
 * same {@link UserServiceContract#login(String, String)} logic as the member
 * management login, so both applications share the password hashes and the
 * account lockout (5 failed attempts lock the account for 15 minutes).</p>
 *
 * <h3>Security:</h3>
 * <ul>
 *     <li><strong>Server-to-server only:</strong> every request must carry the shared secret
 *     configured as {@code navigator.api-key} (env {@code NAVIGATOR_API_KEY}) in the
 *     {@value #API_KEY_HEADER} header. Without a configured key the endpoint is disabled.</li>
 *     <li><strong>No account enumeration:</strong> unknown emails, wrong passwords and deleted
 *     accounts all produce the same {@link Status#INVALID_CREDENTIALS} response.</li>
 *     <li><strong>No session:</strong> this endpoint never creates an HTTP session or security
 *     context; the Navigator manages its own session.</li>
 * </ul>
 *
 * <h3>Responses:</h3>
 * <ul>
 *     <li>{@code 200} with {@link Status#AUTHENTICATED}, {@link Status#INVALID_CREDENTIALS} or {@link Status#LOCKED}</li>
 *     <li>{@code 400} if email or password is missing</li>
 *     <li>{@code 401} if the API key is missing or wrong</li>
 *     <li>{@code 503} if no API key is configured</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@RestController
@RequestMapping("/api/navigator")
public class NavigatorAuthController {

    private static final Logger logger = LoggerFactory.getLogger(NavigatorAuthController.class);

    public static final String API_KEY_HEADER = "X-Navigator-Api-Key";

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserServiceContract userService;
    private final String apiKey;

    public NavigatorAuthController(
            UserServiceContract userService,
            @Value("${navigator.api-key:}") String apiKey
    ) {
        this.userService = userService;
        this.apiKey = apiKey;
    }

    /**
     * Verifies a user's credentials and reports whether the user is an administrator.
     *
     * <p>Runs in a read-only transaction so the lazily loaded roles of the user can be read.</p>
     *
     * @param providedApiKey The shared secret sent by the Navigator
     * @param request The email and password entered on the Navigator login page
     *
     * @return The verification result (see class documentation for status codes)
     *
     * @author Philipp Borkovic
     */
    @PostMapping("/auth/verify")
    @Transactional(readOnly = true)
    public ResponseEntity<VerifyResponse> verifyCredentials(
            @RequestHeader(value = API_KEY_HEADER, required = false) @Nullable String providedApiKey,
            @RequestBody(required = false) @Nullable VerifyRequest request
    ) {
        if (apiKey.isBlank()) {
            logger.warn("Navigator login attempted, but navigator.api-key is not configured");

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(VerifyResponse.of(Status.DISABLED));
        }

        if (!isValidApiKey(providedApiKey)) {
            logger.warn("Navigator login rejected: missing or invalid API key");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(VerifyResponse.of(Status.INVALID_API_KEY));
        }

        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(VerifyResponse.of(Status.INVALID_REQUEST));
        }

        String email = request.email().trim();

        try {
            User user = userService.login(email, request.password());

            if (user == null || user.isDeleted()) {
                logger.warn("Failed Navigator login attempt for email: {}", email);

                return ResponseEntity.ok(VerifyResponse.of(Status.INVALID_CREDENTIALS));
            }

            boolean admin = user.getRoles().stream()
                    .anyMatch(role -> ADMIN_ROLE.equalsIgnoreCase(role.getName()));

            logger.info("Successful Navigator login for user: {} (admin: {})", email, admin);

            return ResponseEntity.ok(new VerifyResponse(
                    Status.AUTHENTICATED,
                    admin,
                    new NavigatorUser(String.valueOf(user.getId()), user.getName(), user.getEmail())
            ));
        } catch (RuntimeException e) {
            String message = e.getMessage();

            if (message != null && message.toLowerCase().contains("locked")) {
                logger.warn("Navigator login attempt for locked account: {}", email);

                return ResponseEntity.ok(VerifyResponse.of(Status.LOCKED));
            }

            throw e;
        }
    }

    /**
     * Compares the provided API key with the configured one in constant time.
     *
     * @param providedApiKey The key sent by the caller
     *
     * @return true if the key matches
     *
     * @author Philipp Borkovic
     */
    private boolean isValidApiKey(@Nullable String providedApiKey) {
        if (providedApiKey == null) {
            return false;
        }

        return MessageDigest.isEqual(
                providedApiKey.getBytes(StandardCharsets.UTF_8),
                apiKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }

    /**
     * Outcome of a verification request.
     *
     * @author Philipp Borkovic
     */
    public enum Status {
        AUTHENTICATED,
        INVALID_CREDENTIALS,
        LOCKED,
        INVALID_REQUEST,
        INVALID_API_KEY,
        DISABLED
    }

    /**
     * Request body sent by the Navigator.
     *
     * @param email The email address entered on the Navigator login page
     * @param password The password entered on the Navigator login page
     *
     * @author Philipp Borkovic
     */
    public record VerifyRequest(@Nullable String email, @Nullable String password) {}

    /**
     * Minimal user information returned on success. Never contains the password hash.
     *
     * @param id The user's unique identifier (UUID as string)
     * @param name The user's display name
     * @param email The user's email address
     *
     * @author Philipp Borkovic
     */
    public record NavigatorUser(String id, String name, String email) {}

    /**
     * Response returned to the Navigator.
     *
     * @param status The verification outcome
     * @param admin Whether the user has the ADMIN role (always false unless authenticated)
     * @param user The authenticated user, or null if not authenticated
     *
     * @author Philipp Borkovic
     */
    public record VerifyResponse(Status status, boolean admin, @Nullable NavigatorUser user) {

        static VerifyResponse of(Status status) {
            return new VerifyResponse(status, false, null);
        }
    }
}
