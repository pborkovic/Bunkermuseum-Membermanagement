package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
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
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
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

}