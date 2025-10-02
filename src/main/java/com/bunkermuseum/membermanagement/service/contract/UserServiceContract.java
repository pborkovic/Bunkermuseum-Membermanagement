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
     * Creates a new user in the system.
     * <p>
     * This method performs several validations and handles possible errors:
     * <ul>
     *     <li>If the input {@code user} is {@code null}, an {@link IllegalArgumentException} is thrown.</li>
     *     <li>If the repository returns {@code null} after attempting to create the user,
     *         a {@link RuntimeException} is thrown indicating failure.</li>
     *     <li>Any {@link IllegalArgumentException} from the repository is logged and rethrown.</li>
     *     <li>Any other unexpected exception is logged and wrapped in a {@link RuntimeException}.</li>
     * </ul>
     * </p>
     *
     * @param user the {@link User} object containing the details of the user to create.
     *             Must not be {@code null}.
     * @return the created {@link User} object returned by the repository.
     * @throws IllegalArgumentException if {@code user} is {@code null} or invalid.
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
}