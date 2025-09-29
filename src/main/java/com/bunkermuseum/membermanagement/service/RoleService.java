package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.repository.contract.RoleRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.RoleServiceContract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Role entity business operations.
 *
 * <p>This service extends {@link BaseService} to inherit standard CRUD operations,
 * validation workflows, transaction management, and error handling while implementing
 * {@link RoleServiceContract} to provide the Role-specific business logic contract.
 * It follows the established service architecture patterns and provides comprehensive
 * business rule enforcement for Role entities.</p>
 *
 * <h3>Transaction Configuration:</h3>
 * <ul>
 *   <li>Read operations use {@code @Transactional(readOnly = true)} for optimization</li>
 *   <li>Write operations use full transactions with rollback on failure</li>
 *   <li>Complex operations maintain ACID properties across multiple steps</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see BaseService
 * @see RoleServiceContract
 * @see Role
 * @see RoleRepositoryContract
 */
@Service
@Transactional(readOnly = true)
public class RoleService extends BaseService<Role, RoleRepositoryContract>
        implements RoleServiceContract {

    /**
     * Constructs a new RoleService with the provided repository.
     *
     * <p>This constructor injects the RoleRepositoryContract dependency
     * and passes it to the parent BaseService for standard operations.
     * The repository provides the data access capabilities while BaseService
     * adds business logic, validation, and transaction management.</p>
     *
     * @param repository The role repository for data access operations
     *
     * @author Philipp Borkovic
     */
    public RoleService(RoleRepositoryContract repository) {
        super(repository);
    }

}