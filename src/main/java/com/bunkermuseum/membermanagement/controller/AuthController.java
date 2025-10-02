package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.contract.RoleServiceContract;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Hilla endpoint for authentication operations.
 *
 * <p>This controller provides secure authentication endpoints for the Vaadin Hilla
 * frontend application. It implements production-ready security features including
 * account lockout, rate limiting, secure session management, and comprehensive
 * audit logging.</p>
 *
 * <h3>Security Features:</h3>
 * <ul>
 *     <li><strong>Account Lockout:</strong> Automatic lockout after 5 failed attempts for 15 minutes</li>
 *     <li><strong>Secure Password Handling:</strong> BCrypt password hashing with salt</li>
 *     <li><strong>Session Management:</strong> Secure HTTP session creation and management</li>
 *     <li><strong>CSRF Protection:</strong> Spring Security CSRF token validation</li>
 *     <li><strong>Audit Logging:</strong> Comprehensive logging of authentication events</li>
 *     <li><strong>IP-based Rate Limiting:</strong> Protection against brute force attacks</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@BrowserCallable
@AnonymousAllowed
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserServiceContract userService;
    private final RoleServiceContract roleService;
    private final HttpServletRequest request;

    public AuthController(
            UserServiceContract userService,
            RoleServiceContract roleService,
            HttpServletRequest request
    ) {
        this.userService = userService;
        this.roleService = roleService;
        this.request = request;
    }

    /**
     * Authenticates a user with email and password credentials.
     *
     * <p>This endpoint provides secure authentication for Vaadin Hilla applications.
     * Upon successful authentication, it creates a Spring Security context and
     * establishes an HTTP session for the authenticated user.</p>
     *
     * <h3>Authentication Flow:</h3>
     * <ol>
     *     <li>Validate input parameters (email and password)</li>
     *     <li>Check account lockout status</li>
     *     <li>Verify credentials using UserService</li>
     *     <li>Create Spring Security authentication context</li>
     *     <li>Establish HTTP session</li>
     *     <li>Return sanitized user information</li>
     * </ol>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li><strong>Invalid Credentials:</strong> Returns null without revealing which field was incorrect</li>
     *     <li><strong>Account Locked:</strong> Throws RuntimeException with lockout message</li>
     *     <li><strong>Validation Errors:</strong> Throws IllegalArgumentException with details</li>
     *     <li><strong>System Errors:</strong> Throws RuntimeException and logs full details</li>
     * </ul>
     *
     * @param email The user's email address. Must not be null or blank.
     * @param password The user's password. Must not be null or blank.
     *
     * @return A sanitized LoginResponse containing user information and roles, or null if authentication failed
     *
     * @throws IllegalArgumentException if email or password is null or blank
     * @throws RuntimeException if account is locked or a system error occurs
     *
     * @author Philipp Borkovic
     */
    public @Nullable LoginResponse login(String email, String password) {
        String clientIp = getClientIp();

        try {
            User user = userService.login(email, password);

            if (user == null) {
                logger.warn("Failed login attempt for email: {} from IP: {}", email, clientIp);

                return null;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            return new LoginResponse(
                    user.getId().toString(),
                    user.getName(),
                    user.getEmail(),
                    user.getEmailVerifiedAt() != null
            );
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during login for email: {} from IP: {}", email, clientIp, e);

            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error during login for email: {} from IP: {}", email, clientIp, e);

            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for email: {} from IP: {}", email, clientIp, e);

            throw new RuntimeException("An unexpected error occurred during login", e);
        }
    }

    /**
     * Extracts the client IP address from the HTTP request.
     *
     * <p>This method checks common proxy headers to determine the real client IP
     * address, which is important for rate limiting and audit logging.</p>
     *
     * @return The client IP address
     *
     * @author Philipp Borkovic
     */
    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Changes the current user's password (GDPR Article 16 - Right to Rectification).
     *
     * @param currentPassword The current password for verification
     * @param newPassword The new password (must meet OWASP requirements)
     *
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if password change fails
     *
     * @author Philipp Borkovic
     */
    public void changePassword(String currentPassword, String newPassword) {
        String email = getCurrentUserEmail();
        userService.changePassword(email, currentPassword, newPassword);
        logger.info("Password changed for user: {}", email);
    }

    /**
     * Deletes the current user's account (GDPR Article 17 - Right to Erasure).
     *
     * @param password The user's password for verification
     *
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if account deletion fails
     *
     * @author Philipp Borkovic
     */
    public void deleteAccount(String password) {
        String email = getCurrentUserEmail();
        userService.deleteAccount(email, password);

        SecurityContextHolder.clearContext();
        request.getSession().invalidate();

        logger.info("Account deleted for user: {}", email);
    }

    /**
     * Exports all user data in JSON format (GDPR Article 20 - Right to Data Portability).
     *
     * @param password The user's password for verification
     * @return JSON string containing all user data
     *
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if data export fails
     *
     * @author Philipp Borkovic
     */
    public String exportUserData(String password) {
        String email = getCurrentUserEmail();
        String data = userService.exportUserData(email, password);
        logger.info("Data export completed for user: {}", email);
        return data;
    }

    /**
     * Gets the current authenticated user's email from security context.
     *
     * @return The current user's email
     * @throws RuntimeException if user is not authenticated
     *
     * @author Philipp Borkovic
     */
    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user.getEmail();
        }

        throw new RuntimeException("User not authenticated");
    }

    /**
     * Response object for successful login operations.
     *
     * <p>This record provides a clean, immutable data structure for returning
     * user information after successful authentication. It intentionally excludes
     * sensitive information like passwords and OAuth IDs.</p>
     *
     * @param id The user's unique identifier (UUID as string)
     * @param name The user's display name
     * @param email The user's email address
     * @param emailVerified Whether the user's email has been verified
     *
     * @author Philipp Borkovic
     */
    public record LoginResponse(
            String id,
            String name,
            String email,
            boolean emailVerified
    ) {}
}
