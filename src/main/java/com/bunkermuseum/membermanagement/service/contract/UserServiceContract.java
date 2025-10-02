package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.model.User;
import org.jspecify.annotations.Nullable;

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
}