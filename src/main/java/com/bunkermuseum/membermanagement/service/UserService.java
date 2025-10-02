package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import com.bunkermuseum.membermanagement.validation.PasswordValidator;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for User entity business operations.
 *
 * <p>This service extends {@link BaseService} to inherit standard CRUD operations,
 * validation workflows, transaction management, and error handling while implementing
 * {@link UserServiceContract} to provide the User-specific business logic contract.
 * It follows the established service architecture patterns and provides comprehensive
 * business rule enforcement for User entities.</p>
 *
 * <h3>Architecture Integration:</h3>
 * <ul>
 *   <li><strong>Base Service:</strong> Inherits comprehensive business operations and utilities</li>
 *   <li><strong>Contract Implementation:</strong> Implements UserServiceContract interface</li>
 *   <li><strong>Repository Integration:</strong> Uses UserRepositoryContract for data access</li>
 *   <li><strong>Transaction Management:</strong> Supports transactional business operations</li>
 *   <li><strong>Validation Framework:</strong> Includes validation hooks and business rule enforcement</li>
 * </ul>
 *
 * <h3>Transaction Configuration:</h3>
 * <ul>
 *   <li>Read operations use {@code @Transactional(readOnly = true)} for optimization</li>
 *   <li>Write operations use full transactions with rollback on failure</li>
 *   <li>Complex operations maintain ACID properties across multiple steps</li>
 * </ul>
 *
 *
 * @author Philipp Borkovic
 *
 * @see BaseService
 * @see UserServiceContract
 * @see User
 * @see UserRepositoryContract
 */
@Service
@Transactional(readOnly = true)
public class UserService extends BaseService<User, UserRepositoryContract>
        implements UserServiceContract {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int FAILED_ATTEMPTS_CLEANUP_HOURS = 24;

    private final PasswordEncoder passwordEncoder;
    private final Map<String, LoginAttemptTracker> loginAttempts = new ConcurrentHashMap<>();

    public UserService(UserRepositoryContract repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Internal class to track login attempts and account lockout.
     *
     * @author Philipp Borkovic
     */
    private static class LoginAttemptTracker {
        private int failedAttempts = 0;
        private Instant lockoutUntil = null;
        private Instant lastAttempt = Instant.now();

        boolean isLocked() {
            if (lockoutUntil != null && Instant.now().isBefore(lockoutUntil)) {
                return true;
            }
            if (lockoutUntil != null && Instant.now().isAfter(lockoutUntil)) {
                reset();
            }
            return false;
        }

        void incrementFailedAttempts() {
            failedAttempts++;
            lastAttempt = Instant.now();
            if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
                lockoutUntil = Instant.now().plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES);
            }
        }

        void reset() {
            failedAttempts = 0;
            lockoutUntil = null;
            lastAttempt = Instant.now();
        }

        Instant getLastAttempt() {
            return lastAttempt;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(user.getPassword());

            if (!validationResult.isValid()) {
                String errorMessage = "Password validation failed: " + validationResult.getErrorMessage();
                logger.error("Password validation failed for user: {}", user.getEmail());

                throw new IllegalArgumentException(errorMessage);
            }

            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }

        try {
            User createdUser = repository.create(user);

            if (createdUser == null) {
                logger.warn("Repository returned null when creating user: {}", user.getName());

                throw new RuntimeException("User creation failed");
            }

            return createdUser;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user data provided for username: {}", user.getName(), e);

            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while creating user: {}", user.getName(), e);

            throw new RuntimeException("Error occurred while creating user", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public @Nullable User login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank");
        }

        cleanupOldLoginAttempts();

        LoginAttemptTracker loginAttemptTracker = loginAttempts.computeIfAbsent(email, k -> new LoginAttemptTracker());

        if (loginAttemptTracker.isLocked()) {
            throw new RuntimeException("Account is temporarily locked due to too many failed login attempts. Please try again later.");
        }

        try {
            Optional<User> userOptional = repository.findByEmail(email);

            if (userOptional.isEmpty()) {
                loginAttemptTracker.incrementFailedAttempts();

                return null;
            }

            User user = userOptional.get();

            if (user.getPassword() == null) {
                loginAttemptTracker.incrementFailedAttempts();

                return null;
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Failed login attempt for user: {}", email);
                loginAttemptTracker.incrementFailedAttempts();
                return null;
            }

            loginAttemptTracker.reset();
            logger.info("Successful login for user: {}", email);

            return user;
        } catch (Exception e) {
            logger.error("Error during login for user: {}", email, e);

            throw new RuntimeException("An error occurred during login", e);
        }
    }

    /**
     * Cleans up old login attempt records to prevent memory leaks.
     *
     * <p>This method removes login attempt trackers that haven't been accessed
     * in the past 24 hours to prevent the map from growing indefinitely.</p>
     *
     * @author Philipp Borkovic
     */
    private void cleanupOldLoginAttempts() {
        Instant cutoffTime = Instant.now().minus(FAILED_ATTEMPTS_CLEANUP_HOURS, ChronoUnit.HOURS);

        loginAttempts.entrySet().removeIf(entry ->
            entry.getValue().getLastAttempt().isBefore(cutoffTime)
        );
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password must not be null or blank");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be null or blank");
        }

        User user = login(email, currentPassword);

        if (user == null) {
            logger.warn("Password change attempt with invalid credentials for user: {}", email);

            throw new RuntimeException("Invalid current password");
        }

        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(newPassword);
        if (!validationResult.isValid()) {
            String errorMessage = "New password validation failed: " + validationResult.getErrorMessage();
            logger.error("New password validation failed for user: {}", email);

            throw new IllegalArgumentException(errorMessage);
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("User attempted to reuse current password: {}", email);

            throw new IllegalArgumentException("New password must be different from current password");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        try {
            repository.update(user);

            logger.info("Password changed successfully for user: {}", email);
        } catch (Exception e) {
            logger.error("Error updating password for user: {}", email, e);

            throw new RuntimeException("Failed to update password", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public void deleteAccount(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank");
        }

        User user = login(email, password);

        if (user == null) {
            logger.warn("Account deletion attempt with invalid credentials for user: {}", email);

            throw new RuntimeException("Invalid credentials");
        }

        try {
            repository.delete(user.getId());
            logger.info("Account deleted (soft delete) for user: {}", email);
        } catch (Exception e) {
            logger.error("Error deleting account for user: {}", email, e);

            throw new RuntimeException("Failed to delete account", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public String exportUserData(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank");
        }

        User user = login(email, password);
        if (user == null) {
            logger.warn("Data export attempt with invalid credentials for user: {}", email);

            throw new RuntimeException("Invalid credentials");
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"personalData\": {\n");
        json.append("    \"name\": \"").append(escapeJson(user.getName())).append("\",\n");
        json.append("    \"email\": \"").append(escapeJson(user.getEmail())).append("\",\n");
        json.append("    \"emailVerified\": ").append(user.getEmailVerifiedAt() != null).append(",\n");
        json.append("    \"emailVerifiedAt\": \"").append(user.getEmailVerifiedAt() != null ? user.getEmailVerifiedAt().toString() : "null").append("\",\n");
        json.append("    \"avatarPath\": \"").append(user.getAvatarPath() != null ? escapeJson(user.getAvatarPath()) : "null").append("\"\n");
        json.append("  },\n");
        json.append("  \"accountMetadata\": {\n");
        json.append("    \"createdAt\": \"").append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "null").append("\",\n");
        json.append("    \"updatedAt\": \"").append(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "null").append("\"\n");
        json.append("  },\n");
        json.append("  \"oauthIntegrations\": {\n");
        json.append("    \"googleLinked\": ").append(user.getGoogleId() != null).append(",\n");
        json.append("    \"microsoftLinked\": ").append(user.getMicrosoftId() != null).append("\n");
        json.append("  },\n");
        json.append("  \"gdprNotice\": \"This export contains all personal data stored in our system as per GDPR Article 20 (Right to Data Portability) and Article 15 (Right of Access).\"\n");
        json.append("}");

        logger.info("Data export completed for user: {}", email);

        return json.toString();
    }

    /**
     * Escapes special characters in JSON strings.
     *
     * @param value The string to escape
     * @return Escaped string safe for JSON
     *
     * @author Philipp Borkovic
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

}