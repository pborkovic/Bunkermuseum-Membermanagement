package com.bunkermuseum.membermanagement.repository.contract;

import com.bunkermuseum.membermanagement.model.PasswordSetupToken;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository contract interface for PasswordSetupToken entity operations.
 *
 * <p>This interface defines the contract for password setup token data access operations.
 * It extends the base repository contract to inherit standard CRUD operations
 * while adding token-specific query methods for password setup functionality.</p>
 *
 * <h3>Token-Specific Operations:</h3>
 * <ul>
 *   <li>Find tokens by token string for verification</li>
 *   <li>Find tokens by user for management</li>
 *   <li>Find expired tokens for cleanup</li>
 *   <li>Delete tokens by user for invalidation</li>
 * </ul>
 *
 * @see PasswordSetupToken
 * @see BaseRepositoryContract
 */
public interface PasswordSetupTokenRepositoryContract extends BaseRepositoryContract<PasswordSetupToken> {

    /**
     * Finds a password setup token by its token string.
     *
     * @param token the token string to search for
     * @return Optional containing the token if found, empty otherwise
     * @throws IllegalArgumentException if token is null or blank
     */
    Optional<PasswordSetupToken> findByToken(String token);

    /**
     * Finds all password setup tokens for a specific user.
     *
     * @param user the user to find tokens for
     * @return list of tokens for the user
     * @throws IllegalArgumentException if user is null
     */
    List<PasswordSetupToken> findByUser(User user);

    /**
     * Finds all expired tokens that haven't been used yet.
     *
     * <p>Used for cleanup operations to delete old expired tokens.</p>
     *
     * @param now the current timestamp to compare against
     * @return list of expired unused tokens
     * @throws IllegalArgumentException if now is null
     */
    List<PasswordSetupToken> findExpiredTokens(LocalDateTime now);

    /**
     * Deletes all tokens for a specific user.
     *
     * <p>Useful when invalidating all password setup tokens for a user.</p>
     *
     * @param user the user whose tokens should be deleted
     * @throws IllegalArgumentException if user is null
     */
    void deleteByUser(User user);
}
