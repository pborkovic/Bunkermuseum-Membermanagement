package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.PasswordSetupToken;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.PasswordSetupTokenRepositoryContract;
import com.bunkermuseum.membermanagement.repository.jpa.PasswordSetupTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for PasswordSetupToken entity operations.
 *
 * <p>This class provides the concrete implementation for password setup token data access operations.
 * It extends {@link BaseRepository} to inherit standard CRUD operations, validation workflows,
 * transaction management, and error handling while implementing {@link PasswordSetupTokenRepositoryContract}
 * to provide the token-specific repository contract.</p>
 *
 * @see BaseRepository
 * @see PasswordSetupTokenRepositoryContract
 * @see PasswordSetupToken
 */
@Repository
public class PasswordSetupTokenRepository extends BaseRepository<PasswordSetupToken, PasswordSetupTokenJpaRepository>
        implements PasswordSetupTokenRepositoryContract {

    /**
     * Constructs a new PasswordSetupTokenRepository with the provided JPA repository.
     *
     * @param repository The Spring Data JpaRepository for PasswordSetupToken entities
     */
    public PasswordSetupTokenRepository(PasswordSetupTokenJpaRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getEntityName() {
        return "PasswordSetupToken";
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Optional<PasswordSetupToken> findByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }

        try {
            logger.debug("Finding password setup token by token string");
            return repository.findByToken(token);
        } catch (Exception e) {
            logger.error("Error finding password setup token by token", e);
            throw new RuntimeException("Error occurred while finding password setup token by token", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<PasswordSetupToken> findByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        try {
            logger.debug("Finding password setup tokens for user: {}", user.getId());
            return repository.findByUser(user);
        } catch (Exception e) {
            logger.error("Error finding password setup tokens for user: {}", user.getId(), e);
            throw new RuntimeException("Error occurred while finding password setup tokens for user", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<PasswordSetupToken> findExpiredTokens(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Timestamp must not be null");
        }

        try {
            logger.debug("Finding expired password setup tokens");
            return repository.findByExpiresAtBeforeAndUsedAtIsNull(now);
        } catch (Exception e) {
            logger.error("Error finding expired password setup tokens", e);
            throw new RuntimeException("Error occurred while finding expired password setup tokens", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public void deleteByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        try {
            logger.debug("Deleting password setup tokens for user: {}", user.getId());
            repository.deleteByUser(user);
        } catch (Exception e) {
            logger.error("Error deleting password setup tokens for user: {}", user.getId(), e);
            throw new RuntimeException("Error occurred while deleting password setup tokens for user", e);
        }
    }
}
