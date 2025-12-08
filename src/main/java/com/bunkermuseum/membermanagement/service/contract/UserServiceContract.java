package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.model.User;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service contract interface for User entity business operations.
 *
 * <p>This interface defines the contract for User-specific business logic operations.
 * It serves as a clean contract without extending any base interfaces, allowing
 * for maximum flexibility in implementation while maintaining clear separation
 * of concerns.</p>
 *
 * @author Philipp Borkovic
 * @see User
 * @see com.bunkermuseum.membermanagement.service.base.BaseService
 */
public interface UserServiceContract {

    /**
     * Creates a new user in the system with password validation.
     * <p>
     * This method performs several validations and handles possible errors:
     * <ul>
     *     <li>If the input {@code user} is {@code null}, an {@link IllegalArgumentException} is thrown.</li>
     *     <li>Password is validated against OWASP ASVS requirements before hashing.</li>
     *     <li>Password is hashed using BCrypt before storage.</li>
     *     <li>If the repository returns {@code null} after attempting to create the user,
     *         a {@link RuntimeException} is thrown indicating failure.</li>
     *     <li>Any {@link IllegalArgumentException} from the repository is logged and rethrown.</li>
     *     <li>Any other unexpected exception is logged and wrapped in a {@link RuntimeException}.</li>
     * </ul>
     * </p>
     *
     * @param user the {@link User} object containing the details of the user to create.
     *             Must not be {@code null}. Password must meet OWASP requirements.
     * @return the created {@link User} object returned by the repository.
     * @throws IllegalArgumentException if {@code user} is {@code null}, invalid, or password doesn't meet requirements.
     * @throws RuntimeException if the repository fails to create the user or any unexpected
     *         error occurs during creation.
     */
    User createUser(User user);

    /**
     * Registers a new user with all registration information.
     *
     * <p>This method creates a new user account with all provided registration data.
     * It validates input, checks for duplicate emails, hashes the password securely,
     * and stores all user information in the database.</p>
     *
     * @param name The user's full name
     * @param email The user's email address
     * @param password The user's password (will be hashed)
     * @param salutation The user's salutation/gender
     * @param academicTitle The user's academic title (optional)
     * @param rank The user's rank (optional)
     * @param birthday The user's date of birth
     * @param phone The user's phone number
     * @param street The user's street address
     * @param city The user's city
     * @param postalCode The user's postal code
     *
     * @return The created User object
     *
     * @throws IllegalArgumentException if validation fails or email already exists
     * @throws RuntimeException if user creation fails
     */
    User register(
            String name,
            String email,
            String password,
            String salutation,
            String academicTitle,
            String rank,
            java.time.LocalDate birthday,
            String phone,
            String street,
            String city,
            String postalCode,
            String country
    );

    /**
     * Authenticates a user with email and password credentials.
     *
     * <p>This method provides secure authentication with the following features:</p>
     * <ul>
     *     <li>Password validation using BCrypt</li>
     *     <li>Account lockout after failed attempts</li>
     *     <li>Rate limiting to prevent brute force attacks</li>
     *     <li>Secure logging that never exposes credentials</li>
     * </ul>
     *
     * @param email The user's email address. Must not be null or blank.
     * @param password The user's plain text password. Must not be null or blank.
     *
     * @return The authenticated User object if credentials are valid, null if invalid
     *
     * @throws IllegalArgumentException if email or password is null or blank
     * @throws RuntimeException if account is locked or rate limit exceeded
     */
    @Nullable User login(String email, String password);

    /**
     * Changes a user's password with OWASP ASVS validation.
     *
     * <p>This method provides secure password change functionality for GDPR compliance
     * (Right to rectification). It validates the current password, validates the new
     * password against OWASP requirements, and updates the password securely.</p>
     *
     * <h3>GDPR Compliance:</h3>
     * <ul>
     *     <li>Article 16 - Right to rectification</li>
     *     <li>Article 32 - Security of processing (secure password storage)</li>
     * </ul>
     *
     * @param email The user's email address
     * @param currentPassword The current password for verification
     * @param newPassword The new password (must meet OWASP requirements)
     *
     * @throws IllegalArgumentException if validation fails or passwords don't meet requirements
     * @throws RuntimeException if user not found or password update fails
     */
    void changePassword(String email, String currentPassword, String newPassword);

    /**
     * Deletes a user account (GDPR Right to Erasure - "Right to be Forgotten").
     *
     * <p>This method implements soft delete to comply with GDPR Article 17 while
     * maintaining referential integrity and audit trails as required by legal
     * and compliance obligations.</p>
     *
     * <h3>GDPR Compliance:</h3>
     * <ul>
     *     <li>Article 17 - Right to erasure ("right to be forgotten")</li>
     *     <li>Soft delete preserves audit trails for legal compliance</li>
     *     <li>Personal data is anonymized upon deletion</li>
     * </ul>
     *
     * @param email The user's email address
     * @param password The user's password for verification
     *
     * @throws IllegalArgumentException if email or password is null/blank
     * @throws RuntimeException if user not found or authentication fails
     */
    void deleteAccount(String email, String password);

    /**
     * Exports all user data in JSON format (GDPR Right to Data Portability).
     *
     * <p>This method implements GDPR Article 20 by providing users with a complete
     * export of their personal data in a structured, commonly used, and machine-readable
     * format (JSON).</p>
     *
     * <h3>GDPR Compliance:</h3>
     * <ul>
     *     <li>Article 20 - Right to data portability</li>
     *     <li>Article 15 - Right of access by the data subject</li>
     *     <li>Returns data in JSON format (machine-readable)</li>
     *     <li>Excludes sensitive security data (password hashes)</li>
     * </ul>
     *
     * <h3>Exported Data:</h3>
     * <ul>
     *     <li>User profile information (name, email)</li>
     *     <li>Account metadata (creation date, verification status)</li>
     *     <li>OAuth linkages (if any)</li>
     *     <li>Excludes: Password hashes, internal IDs, security tokens</li>
     * </ul>
     *
     * @param email The user's email address
     * @param password The user's password for verification
     *
     * @return JSON string containing all user data
     *
     * @throws IllegalArgumentException if email or password is null/blank
     * @throws RuntimeException if user not found or authentication fails
     */
    String exportUserData(String email, String password);

    /**
     * Retrieves all users from the system.
     *
     * <p>This method fetches all registered users from the database.
     * It's intended for administrative purposes and should be protected
     * by appropriate authorization checks.</p>
     *
     * @return List of all users in the system
     *
     * @throws RuntimeException if retrieval fails
     *
     * @author Philipp Borkovic
     */
    List<User> getAllUsers();

    /**
     * Retrieves users with pagination and optional search filtering.
     *
     * <p>This method provides efficient paginated access to users with optional
     * search functionality. It's designed for administrative interfaces where
     * displaying all users at once would be impractical.</p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Server-side pagination for better performance</li>
     *   <li>Optional search across name, email, and phone fields</li>
     *   <li>Configurable page size</li>
     *   <li>Sorted results</li>
     * </ul>
     *
     * @param pageable Pagination parameters (page number, size, sort)
     * @param searchQuery Optional search term to filter users (searches name, email, phone)
     *
     * @return Page of users matching the criteria
     *
     * @throws RuntimeException if retrieval fails
     *
     * @author Philipp Borkovic
     */
    Page<User> getUsersPage(Pageable pageable, @Nullable String searchQuery);

    /**
     * Retrieves users with pagination, optional search filtering, and status filter.
     *
     * <p>This method provides efficient paginated access to users with optional
     * search functionality and the ability to filter by user status (active/deleted/all).
     * It's designed for administrative interfaces where displaying all users at once
     * would be impractical.</p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Server-side pagination for better performance</li>
     *   <li>Optional search across name, email, and phone fields</li>
     *   <li>Status filter: active, deleted, or all users</li>
     *   <li>Configurable page size</li>
     *   <li>Sorted results</li>
     * </ul>
     *
     * @param pageable Pagination parameters (page number, size, sort)
     * @param searchQuery Optional search term to filter users (searches name, email, phone)
     * @param status Filter status: "active", "deleted", or "all"
     *
     * @return Page of users matching the criteria
     *
     * @throws IllegalArgumentException if status is invalid
     * @throws RuntimeException if retrieval fails
     *
     * @author Philipp Borkovic
     */
    Page<User> getUsersPageWithStatus(Pageable pageable, @Nullable String searchQuery, String status);

    /**
     * Updates a user's profile information.
     *
     * <p>This method allows updating user profile fields such as name and email.
     * It validates the input and ensures data integrity.</p>
     *
     * @param userId The ID of the user to update
     * @param name The new name (optional, null to keep existing)
     * @param email The new email (optional, null to keep existing)
     *
     * @return The updated User object
     *
     * @throws IllegalArgumentException if userId is null or user not found
     * @throws RuntimeException if update fails
     *
     * @author Philipp Borkovic
     */
    User updateProfile(UUID userId, @Nullable String name, @Nullable String email);

    /**
     * Updates comprehensive user information.
     *
     * <p>This method allows updating all user profile fields including personal information,
     * contact details, and address information. It validates the input and ensures data integrity.</p>
     *
     * @param userId The ID of the user to update
     * @param userData User object containing the fields to update
     *
     * @return The updated User object
     *
     * @throws IllegalArgumentException if userId is null or user not found
     * @throws RuntimeException if update fails
     *
     * @author Philipp Borkovic
     */
    User updateUser(UUID userId, User userData);

    /**
     * Finds a user by their unique identifier.
     *
     * <p>This method retrieves a user from the database by their UUID.
     * It ensures the user is properly loaded within a transaction context
     * to allow access to lazy-loaded relationships.</p>
     *
     * <h3>Caching Strategy:</h3>
     * <ul>
     *     <li>Results are cached for 10 minutes to reduce database load</li>
     *     <li>Frequently accessed in authentication, navbar, settings pages</li>
     *     <li>Cache is evicted when user profile is updated</li>
     * </ul>
     *
     * @param userId The UUID of the user to find
     * @return Optional containing the user if found, empty if not found
     *
     * @throws IllegalArgumentException if userId is null
     * @throws RuntimeException if database access fails
     */
    Optional<User> findById(UUID userId);

    /**
     * Finds a user by their email address.
     *
     * <p>This method retrieves a user from the database by their email address.
     * Email addresses are case-insensitive and normalized to lowercase during
     * registration and lookup.</p>
     *
     * <h3>Caching Strategy:</h3>
     * <ul>
     *     <li>Results are cached for 10 minutes to improve authentication performance</li>
     *     <li>Frequently accessed during login, user lookups, and authentication</li>
     *     <li>Cache is evicted when user email is updated</li>
     * </ul>
     *
     * <h3>Use Cases:</h3>
     * <ul>
     *     <li>Authentication and login flows</li>
     *     <li>User profile lookups</li>
     *     <li>Email uniqueness validation</li>
     *     <li>Password reset operations</li>
     * </ul>
     *
     * @param email The email address of the user to find (case-insensitive)
     * @return Optional containing the user if found, empty if not found
     *
     * @throws IllegalArgumentException if email is null or blank
     * @throws RuntimeException if database access fails
     */
    Optional<User> findByEmail(String email);

    /**
     * Sets up a user's password using a password setup token.
     *
     * <p>This method is used when an admin creates a user account without a password.
     * The user receives an email with a token link, and uses this endpoint to set
     * their password for the first time.</p>
     *
     * <h3>Process:</h3>
     * <ul>
     *     <li>Validates the token is valid (not used, not expired)</li>
     *     <li>Validates the password meets OWASP ASVS requirements</li>
     *     <li>Hashes and sets the user's password</li>
     *     <li>Marks the token as used to prevent reuse</li>
     * </ul>
     *
     * @param token The unique password setup token from the email
     * @param password The new password (must meet OWASP requirements)
     *
     * @throws IllegalArgumentException if token is invalid, expired, or password doesn't meet requirements
     * @throws RuntimeException if password setup fails
     */
    void setupPasswordWithToken(String token, String password);
}