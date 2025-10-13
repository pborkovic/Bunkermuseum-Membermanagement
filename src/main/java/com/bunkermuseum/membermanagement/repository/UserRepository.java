package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.repository.jpa.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<User> findBySearchQuery(String searchQuery, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            if (searchQuery == null || searchQuery.isBlank()) {
                logger.debug("Fetching all users with pagination: page={}, size={}",
                        pageable.getPageNumber(), pageable.getPageSize());

                return repository.findAll(pageable);
            } else {
                logger.debug("Searching users with query '{}': page={}, size={}",
                        searchQuery, pageable.getPageNumber(), pageable.getPageSize());

                return repository.findBySearchQuery(searchQuery.trim(), pageable);
            }
        } catch (IllegalArgumentException exception) {
            logger.error("Invalid arguments for user search: searchQuery='{}', pageable={}",
                searchQuery, pageable, exception);

            throw exception;
        } catch (Exception exception) {
            logger.error("Database error while finding users with search query: '{}', page={}, size={}",
                searchQuery, pageable.getPageNumber(), pageable.getPageSize(), exception);

            throw new RuntimeException("Failed to retrieve users from database", exception);
        }
    }
}