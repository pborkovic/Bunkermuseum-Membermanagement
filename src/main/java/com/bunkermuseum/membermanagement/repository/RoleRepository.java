package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.RoleRepositoryContract;
import com.bunkermuseum.membermanagement.repository.jpa.RoleJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for Role entity operations.
 *
 * <p>This class provides the concrete implementation for Role-specific data access operations.
 * It extends {@link BaseRepository} to inherit standard CRUD operations, validation workflows,
 * transaction management, and error handling while implementing {@link RoleRepositoryContract}
 * to provide the Role-specific repository contract.</p>
 *
 * @author Philipp Borkovic
 * @see BaseRepository
 * @see RoleRepositoryContract
 * @see Role
 */
@Repository
public class RoleRepository extends BaseRepository<Role, RoleJpaRepository>
        implements RoleRepositoryContract {

    /**
     * Constructs a new RoleRepository with the provided JPA repository.
     *
     * <p>This constructor injects the Spring Data JpaRepository dependency
     * and passes it to the parent BaseRepository for standard CRUD operations.
     * The JpaRepository provides the actual database interaction capabilities
     * while BaseRepository adds logging, error handling, and additional utilities.</p>
     *
     * @param repository The Spring Data JpaRepository for Role entities
     *
     * @author Philipp Borkovic
     */
    public RoleRepository(RoleJpaRepository repository) {
        super(repository);
    }

}