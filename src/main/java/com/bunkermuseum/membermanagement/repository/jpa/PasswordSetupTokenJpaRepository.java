package com.bunkermuseum.membermanagement.repository.jpa;

import com.bunkermuseum.membermanagement.model.PasswordSetupToken;
import com.bunkermuseum.membermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for PasswordSetupToken entity.
 *
 * <p>This interface provides the underlying Spring Data JPA repository for PasswordSetupToken entities.
 * Spring Data JPA will automatically create the implementation of this interface
 * at runtime with all the standard CRUD operations plus custom query methods.</p>
 *
 * @see PasswordSetupToken
 * @see JpaRepository
 */
@Repository
public interface PasswordSetupTokenJpaRepository extends JpaRepository<PasswordSetupToken, UUID> {

    /**
     * Finds a password setup token by its token string.
     *
     * @param token the token string to search for
     * @return Optional containing the token if found, empty otherwise
     */
    Optional<PasswordSetupToken> findByToken(String token);

    /**
     * Finds all password setup tokens for a specific user.
     *
     * @param user the user to find tokens for
     * @return list of tokens for the user
     */
    List<PasswordSetupToken> findByUser(User user);

    /**
     * Finds all expired tokens that haven't been used yet.
     *
     * <p>Used for cleanup operations to delete old expired tokens.</p>
     *
     * @param now the current timestamp to compare against
     * @return list of expired unused tokens
     */
    List<PasswordSetupToken> findByExpiresAtBeforeAndUsedAtIsNull(LocalDateTime now);

    /**
     * Deletes all tokens for a specific user.
     *
     * <p>Useful when invalidating all password setup tokens for a user.</p>
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);
}
