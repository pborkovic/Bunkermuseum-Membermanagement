package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository implementation for User entity operations.
 *
 * <p>This repository extends {@link BaseRepository} to inherit standard CRUD operations,
 * error handling, logging, and transaction management while implementing
 * {@link UserRepositoryContract} to provide the User-specific data access contract.
 * It uses Spring Data JPA for database operations and follows the established
 * repository architecture patterns.</p>
 *
 * @author Philipp Borkovic
 * @see BaseRepository
 * @see UserRepositoryContract
 * @see User
 * @see JpaRepository
 */
@Repository
public class UserRepository extends BaseRepository<User, JpaRepository<User, UUID>>
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
    public UserRepository(JpaRepository<User, UUID> repository) {
        super(repository);
    }
}