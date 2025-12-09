package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.lib.helper.LoginAttemptTracker;
import com.bunkermuseum.membermanagement.lib.validation.PasswordValidator;
import com.bunkermuseum.membermanagement.model.PasswordSetupToken;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.PasswordSetupTokenRepositoryContract;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.EmailServiceContract;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    private static final int FAILED_ATTEMPTS_CLEANUP_HOURS = 24;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceContract emailService;
    private final PasswordSetupTokenRepositoryContract tokenRepository;
    private final Map<String, LoginAttemptTracker> loginAttempts = new ConcurrentHashMap<>();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UserService(
            UserRepositoryContract repository,
            PasswordEncoder passwordEncoder,
            EmailServiceContract emailService,
            PasswordSetupTokenRepositoryContract tokenRepository
    ) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
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

        boolean passwordProvided = user.getPassword() != null && !user.getPassword().isBlank();

        if (passwordProvided) {
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
            if (!passwordProvided) {
                sendPasswordSetupEmail(createdUser);
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
     * Sends a password setup email to a newly created user.
     *
     * <p>This method generates a unique, time-limited token and sends an email
     * to the user with a link to set their password. The token expires after 24 hours.</p>
     *
     * <p>The email contains:
     * <ul>
     *     <li>A personalized greeting with the user's name</li>
     *     <li>A unique password setup link</li>
     *     <li>Instructions for setting up their account</li>
     *     <li>Expiration information (24 hours)</li>
     * </ul>
     *
     * @param user the newly created user to send the setup email to
     * @throws RuntimeException if email sending fails
     *
     * @author Philipp Borkovic
     */
    private void sendPasswordSetupEmail(User user) {
        try {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
            PasswordSetupToken setupToken = new PasswordSetupToken(user, token, expiresAt);

            tokenRepository.create(setupToken);

            String setupUrl = baseUrl + "/setup-password?token=" + token;

            String subject = "Willkommen - Richten Sie Ihr Passwort ein";
            String content = String.format("""
                    <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                            <h2 style="color: #2c3e50;">Willkommen beim Bunkermuseum!</h2>

                            <p>Hallo %s,</p>

                            <p>Ein Administrator hat ein Konto für Sie erstellt. Um Ihr Konto zu aktivieren,
                            müssen Sie zunächst ein Passwort festlegen.</p>

                            <p>Bitte klicken Sie auf den folgenden Link, um Ihr Passwort einzurichten:</p>

                            <p style="margin: 30px 0;">
                                <a href="%s"
                                   style="background-color: #3498db; color: white; padding: 12px 24px;
                                          text-decoration: none; border-radius: 4px; display: inline-block;">
                                    Passwort einrichten
                                </a>
                            </p>

                            <p style="color: #7f8c8d; font-size: 14px;">
                                Oder kopieren Sie diesen Link in Ihren Browser:<br>
                                <a href="%s" style="color: #3498db;">%s</a>
                            </p>

                            <p style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ecf0f1; color: #7f8c8d; font-size: 12px;">
                                <strong>Wichtig:</strong> Dieser Link ist 24 Stunden gültig.<br>
                                Falls Sie dieses Konto nicht angefordert haben, können Sie diese E-Mail ignorieren.
                            </p>
                        </div>
                    </body>
                    </html>
                    """,
                    user.getName(),
                    setupUrl,
                    setupUrl,
                    setupUrl
            );

            emailService.sendSimpleEmail("noreply@bunkermuseum.com", user.getEmail(), subject, content, null);

            logger.info("Password setup email sent to user: {} ({})", user.getName(), user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password setup email to user: {} ({})", user.getName(), user.getEmail(), e);
            throw new RuntimeException("Failed to send password setup email", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public User register(
            String name,
            String email,
            String password,
            String salutation,
            String academicTitle,
            String rank,
            LocalDate birthday,
            String phone,
            String street,
            String city,
            String postalCode,
            String country
    ) {
        validateRequiredFields(name, email, password);

        String normalizedEmail = email.trim().toLowerCase();

        if (repository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("This email address is already in use");
        }

        validatePassword(password);

        User user = buildUser(
                name.trim(),
                normalizedEmail,
                password,
                salutation,
                academicTitle,
                rank,
                birthday,
                phone,
                street,
                city,
                postalCode,
                country
        );

        return persistUser(user, normalizedEmail);
    }

    /**
     * Validates required registration fields.
     *
     * @param name User's name
     * @param email User's email
     * @param password User's password
     *
     * @throws IllegalArgumentException if any required field is null or blank
     *
     * @author Philipp Borkovic
     */
    private void validateRequiredFields(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    /**
     * Validates password strength using PasswordValidator.
     *
     * @param password The password to validate
     *
     * @throws IllegalArgumentException if password validation fails
     *
     * @author Philipp Borkovic
     */
    private void validatePassword(String password) {
        PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(password);

        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.getErrorMessage());
        }
    }

    /**
     * Builds a User object with all registration data.
     *
     * @return Configured User object with hashed password
     *
     * @author Philipp Borkovic
     */
    private User buildUser(
            String name,
            String email,
            String password,
            String salutation,
            String academicTitle,
            String rank,
            LocalDate birthday,
            String phone,
            String street,
            String city,
            String postalCode,
            String country
    ) {
        User user = new User(name, email, null);
        user.setSalutation(salutation);
        user.setAcademicTitle(academicTitle);
        user.setRank(rank);
        user.setBirthday(birthday);
        user.setPhone(phone);
        user.setStreet(street);
        user.setCity(city);
        user.setPostalCode(postalCode);
        user.setCountry(country);
        user.setPassword(passwordEncoder.encode(password));

        return user;
    }

    /**
     * Persists user to database with error handling.
     *
     * @param user User to persist
     * @param email User's email (for logging)
     *
     * @return Persisted User object
     *
     * @throws RuntimeException if persistence fails
     *
     * @author Philipp Borkovic
     */
    private User persistUser(User user, String email) {
        try {
            User createdUser = repository.create(user);

            if (createdUser == null) {
                logger.warn("Repository returned null when creating user: {}", email);
                throw new RuntimeException("User could not be created");
            }
            logger.info("User registered successfully: {}", email);

            return createdUser;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during registration for email: {}", email, e);

            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration for email: {}", email, e);

            throw new RuntimeException("Registration failed. Please try again.", e);
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
            repository.update(user.getId(), user);

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
            repository.deleteById(user.getId());
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

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<User> getAllUsers() {
        try {
            return repository.findAll();
        } catch (Exception exception) {
            logger.error("Error retrieving all users", exception);
            throw new RuntimeException("Failed to retrieve users", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<User> getUsersPage(Pageable pageable, @Nullable String searchQuery) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Fetching users page: page={}, size={}, searchQuery='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), searchQuery);

            Page<User> result = repository.findBySearchQuery(searchQuery, pageable);

            if (result == null) {
                logger.error("Repository returned null for getUsersPage");

                throw new RuntimeException("Failed to retrieve users: null result from repository");
            }

            logger.debug("Successfully retrieved {} users out of {} total",
                result.getNumberOfElements(), result.getTotalElements());

            return result;
        } catch (IllegalArgumentException exception) {
            logger.error("Invalid arguments for getUsersPage: pageable={}, searchQuery='{}'",
                pageable, searchQuery, exception);

            throw exception;
        } catch (Exception exception) {
            logger.error("Error retrieving users page: page={}, size={}, searchQuery='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), searchQuery, exception);

            throw new RuntimeException("Failed to retrieve users", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<User> getUsersPageWithStatus(Pageable pageable, @Nullable String searchQuery, String status) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status must not be null or blank");
        }

        try {
            logger.debug("Fetching users page: page={}, size={}, searchQuery='{}', status='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), searchQuery, status);

            Page<User> result = repository.findBySearchQueryAndStatus(searchQuery, status, pageable);

            if (result == null) {
                logger.error("Repository returned null for getUsersPageWithStatus");

                throw new RuntimeException("Failed to retrieve users: null result from repository");
            }

            logger.debug("Successfully retrieved {} users out of {} total (status: {})",
                result.getNumberOfElements(), result.getTotalElements(), status);

            return result;
        } catch (IllegalArgumentException exception) {
            logger.error("Invalid arguments for getUsersPageWithStatus: pageable={}, searchQuery='{}', status='{}'",
                pageable, searchQuery, status, exception);

            throw exception;
        } catch (Exception exception) {
            logger.error("Error retrieving users page: page={}, size={}, searchQuery='{}', status='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), searchQuery, status, exception);

            throw new RuntimeException("Failed to retrieve users", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "usersById", key = "#userId"),
        @CacheEvict(value = "usersByEmail", key = "#result.email", condition = "#result != null")
    })
    public User updateProfile(UUID userId, @Nullable String name, @Nullable String email) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        try {
            Optional<User> optionalUser = repository.findById(userId);

            if (optionalUser.isEmpty()) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            User user = optionalUser.get();
            String oldEmail = user.getEmail();

            if (name != null && !name.isBlank()) {
                user.setName(name);
            }

            if (email != null && !email.isBlank()) {
                user.setEmail(email);
            }

            User updatedUser = repository.update(userId, user);

            if (!oldEmail.equals(updatedUser.getEmail())) {
                logger.info("Email changed - evicting old email cache: {}", oldEmail);
            }

            logger.info("Profile updated for user: {} and cache evicted", userId);

            return updatedUser;
        } catch (IllegalArgumentException exception) {
            logger.error("Invalid user data for profile update: {}", userId, exception);

            throw exception;
        } catch (Exception exception) {
            logger.error("Failed to update user profile: {}", userId, exception);

            throw new RuntimeException("Failed to update user profile", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "usersById", key = "#userId"),
        @CacheEvict(value = "usersByEmail", key = "#result.email", condition = "#result != null")
    })
    public User updateUser(UUID userId, User userData) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (userData == null) {
            throw new IllegalArgumentException("User data must not be null");
        }

        try {
            User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            List<String> changes = new ArrayList<>();

            updateStringField(userData.getName(), user::getName, user::setName, "Name", changes);
            updateStringField(userData.getEmail(), user::getEmail, user::setEmail, "E-Mail", changes);

            updateField(userData.getSalutation(), user::getSalutation, user::setSalutation, "Anrede", changes);
            updateField(userData.getAcademicTitle(), user::getAcademicTitle, user::setAcademicTitle, "Akademischer Titel", changes);
            updateField(userData.getRank(), user::getRank, user::setRank, "Dienstgrad", changes);
            updateDateField(userData.getBirthday(), user::getBirthday, user::setBirthday, "Geburtsdatum", changes);
            updateField(userData.getPhone(), user::getPhone, user::setPhone, "Telefon", changes);
            updateField(userData.getStreet(), user::getStreet, user::setStreet, "Straße", changes);
            updateField(userData.getCity(), user::getCity, user::setCity, "Stadt", changes);
            updateField(userData.getPostalCode(), user::getPostalCode, user::setPostalCode, "Postleitzahl", changes);
            updateField(userData.getCountry(), user::getCountry, user::setCountry, "Land", changes);

            User updatedUser = repository.update(userId, user);

            if (!changes.isEmpty()) {
                notifyAdminsOfProfileChanges(updatedUser, changes);
            }

            return updatedUser;
        } catch (IllegalArgumentException exception) {
            logger.error("Invalid user data for update: {}", userId, exception);

            throw exception;
        } catch (Exception exception) {
            logger.error("Failed to update user: {}", userId, exception);

            throw new RuntimeException("Failed to update user profile", exception);
        }
    }

    /**
     * Updates a string field if the new value is not null and not blank.
     * Tracks the change if the value differs from the current value.
     *
     * @param newValue the new value to set (null or blank values are ignored)
     * @param getter the getter method reference to retrieve current value
     * @param setter the setter method reference to set new value
     * @param fieldName the display name of the field for change tracking
     * @param changes the list to add change messages to
     *
     * @author Philipp Borkovic
     */
    private void updateStringField(
            @Nullable String newValue,
            Supplier<String> getter,
            Consumer<String> setter,
            String fieldName,
            List<String> changes
    ) {
        if (newValue != null && !newValue.isBlank()) {
            String currentValue = getter.get();

            if (!newValue.equals(currentValue)) {
                changes.add(buildChangeMessage(fieldName, currentValue, newValue));
            }

            setter.accept(newValue);
        }
    }

    /**
     * Updates a generic field if the new value is not null.
     * Tracks the change if the value differs from the current value.
     *
     * @param newValue the new value to set (null values are ignored)
     * @param getter the getter method reference to retrieve current value
     * @param setter the setter method reference to set new value
     * @param fieldName the display name of the field for change tracking
     * @param changes the list to add change messages to
     * @param <T> the type of the field value
     *
     * @author Philipp Borkovic
     */
    private <T> void updateField(
            @Nullable T newValue,
            Supplier<T> getter,
            Consumer<T> setter,
            String fieldName,
            List<String> changes
    ) {
        if (newValue != null) {
            T currentValue = getter.get();

            if (!Objects.equals(newValue, currentValue)) {
                String oldValueStr = currentValue != null ? currentValue.toString() : null;
                String newValueStr = newValue.toString();

                changes.add(buildChangeMessage(fieldName, oldValueStr, newValueStr));
            }

            setter.accept(newValue);
        }
    }

    /**
     * Updates a LocalDate field if the new value is not null.
     * Tracks the change if the value differs from the current value.
     * Converts LocalDate to String for change message display.
     *
     * @param newValue the new date value to set (null values are ignored)
     * @param getter the getter method reference to retrieve current LocalDate value
     * @param setter the setter method reference to set new LocalDate value
     * @param fieldName the display name of the field for change tracking
     * @param changes the list to add change messages to
     *
     * @author Philipp Borkovic
     */
    private void updateDateField(
            @Nullable LocalDate newValue,
            Supplier<LocalDate> getter,
            Consumer<LocalDate> setter,
            String fieldName,
            List<String> changes
    ) {
        if (newValue != null) {
            LocalDate currentValue = getter.get();

            if (!Objects.equals(newValue, currentValue)) {
                String oldValueStr = currentValue != null ? currentValue.toString() : null;
                String newValueStr = newValue.toString();

                changes.add(buildChangeMessage(fieldName, oldValueStr, newValueStr));
            }

            setter.accept(newValue);
        }
    }

    /**
     * Builds a formatted change message showing old value to new value.
     *
     * @param fieldName the name of the field that changed
     * @param oldValue the old value (null if not set)
     * @param newValue the new value
     * @return formatted change message
     *
     * @author Philipp Borkovic
     */
    private String buildChangeMessage(String fieldName, @Nullable String oldValue, String newValue) {
        String oldValueDisplay = (oldValue == null || oldValue.isBlank()) ? "(leer)" : oldValue;
        String newValueDisplay = (newValue == null || newValue.isBlank()) ? "(leer)" : newValue;

        return fieldName + ": " + oldValueDisplay + " → " + newValueDisplay;
    }

    /**
     * Notifies all admin users about profile changes made by a member.
     *
     * <p>This method sends an email notification to all users with the ADMIN role,
     * informing them about changes made to a member's profile. The email includes:
     * <ul>
     *     <li>The name and email of the user who made changes</li>
     *     <li>A detailed list of all changed fields with old and new values</li>
     *     <li>Timestamp of when the changes occurred</li>
     * </ul>
     *
     * <p>The notification is sent asynchronously to avoid blocking the update operation.
     * If email sending fails, the error is logged but does not affect the profile update.</p>
     *
     * @param user the user whose profile was updated
     * @param changes list of change descriptions (field: oldValue → newValue)
     *
     * @author Philipp Borkovic
     */
    private void notifyAdminsOfProfileChanges(User user, List<String> changes) {
        try {
            List<User> adminUsers = repository.findActive().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName())))
                .toList();

            if (adminUsers.isEmpty()) {
                logger.warn("No admin users found to notify about profile changes for user: {}", user.getId());

                return;
            }

            String subject = "Mitglied hat Profildaten geändert - " + user.getName();

            StringBuilder changesHtml = new StringBuilder();
            for (String change : changes) {
                changesHtml.append("<li style='margin: 8px 0; padding: 8px; background-color: #f8f9fa; border-left: 3px solid #3498db;'>")
                    .append(change)
                    .append("</li>");
            }

            String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px;">
                            Profildatenänderung durch Mitglied
                        </h2>

                        <div style="background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Mitglied:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>E-Mail:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Zeitpunkt:</strong> %s</p>
                        </div>

                        <h3 style="color: #2c3e50; margin-top: 30px;">Geänderte Felder:</h3>
                        <ul style="list-style: none; padding: 0;">
                            %s
                        </ul>

                        <p style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ecf0f1; color: #7f8c8d; font-size: 12px;">
                            Diese automatische Benachrichtigung wurde vom Bunkermuseum-Verwaltungssystem gesendet.<br>
                            Sie können die vollständigen Mitgliederdaten im Admin-Dashboard einsehen.
                        </p>
                    </div>
                </body>
                </html>
                """,
                user.getName(),
                user.getEmail(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                changesHtml.toString()
            );

            for (User admin : adminUsers) {
                try {
                    emailService.sendSimpleEmail(
                        "noreply@bunkermuseum.com",
                        admin.getEmail(),
                        subject,
                        content,
                        null
                    );
                    logger.info("Profile change notification sent to admin: {} ({})", admin.getName(), admin.getEmail());
                } catch (Exception e) {
                    logger.error("Failed to send profile change notification to admin: {} ({})",
                        admin.getName(), admin.getEmail(), e);
                }
            }

            logger.info("Profile change notifications sent to {} admin(s) for user: {} ({})",
                adminUsers.size(), user.getName(), user.getEmail());

        } catch (Exception e) {
            logger.error("Error while notifying admins of profile changes for user: {} ({})",
                    user.getName(), user.getEmail(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Cacheable(value = "usersById", key = "#userId")
    public Optional<User> findById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        try {
            logger.debug("Cache miss - loading user {} from database", userId);
            return repository.findById(userId);
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", userId, e);
            throw new RuntimeException("Failed to find user", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Cacheable(value = "usersByEmail", key = "#email")
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        try {
            logger.debug("Cache miss - loading user by email {} from database", email);
            return repository.findByEmail(email);
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Failed to find user", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public void setupPasswordWithToken(String token, String password) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be null or blank");
        }

        try {
            Optional<PasswordSetupToken> tokenOpt = tokenRepository.findByToken(token);
            if (tokenOpt.isEmpty()) {
                logger.warn("Password setup attempted with invalid token");

                throw new IllegalArgumentException("Invalid or expired password setup token");
            }

            PasswordSetupToken setupToken = tokenOpt.get();

            if (!setupToken.isValid()) {
                logger.warn("Password setup attempted with expired or used token for user: {}",
                    setupToken.getUser().getEmail());

                throw new IllegalArgumentException("Invalid or expired password setup token");
            }

            PasswordValidator.ValidationResult validationResult = PasswordValidator.validate(password);

            if (!validationResult.isValid()) {
                String errorMessage = "Password validation failed: " + validationResult.getErrorMessage();

                throw new IllegalArgumentException(errorMessage);
            }

            User user = setupToken.getUser();
            String hashedPassword = passwordEncoder.encode(password);
            user.setPassword(hashedPassword);

            setupToken.markAsUsed();

            repository.update(user.getId(), user);
            tokenRepository.update(setupToken.getId(), setupToken);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during password setup", e);

            throw new RuntimeException("Failed to set up password", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new RuntimeException("User not authenticated");
    }

}