package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository implementation for User entity operations.
 *
 * <p>This class provides the concrete implementation for User-specific data access operations.
 * It extends {@link BaseRepository} to inherit standard CRUD operations, validation workflows,
 * transaction management, and error handling while implementing {@link UserRepositoryContract}
 * to provide the User-specific repository contract.</p>
 *
 * @author Philipp Borkovic
 * @see BaseRepository
 * @see UserRepositoryContract
 * @see User
 */
@Repository
public class UserRepository extends BaseRepository<User, UserJpaRepository>
        implements UserRepositoryContract {

    /**
     * Constructs a new UserRepository with the provided JPA repository.
     *
     * <p>This constructor injects the Spring Data JpaRepository dependency
     * and passes it to the parent BaseRepository for standard CRUD operations.
     * The JpaRepository provides the actual database interaction capabilities
     * while BaseRepository adds logging, error handling, and additional utilities.</p>
     *
     * @param repository The Spring Data JpaRepository for User entities
     *
     * @author Philipp Borkovic
     */
    public UserRepository(UserJpaRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getEntityName() {
        return "User";
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        try {
            return repository.findByEmail(email);
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);

            throw new RuntimeException("Error occurred while finding user by email", e);
        }
    }
}