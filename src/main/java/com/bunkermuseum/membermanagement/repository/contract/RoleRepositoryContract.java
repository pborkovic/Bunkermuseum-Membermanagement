package com.bunkermuseum.membermanagement.repository.contract;

import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;

import java.util.Optional;

/**
 * Repository contract interface for Role entity operations.
 *
 * <p>This interface defines the contract for Role-specific data access operations.
 * It extends the base repository contract to inherit standard CRUD operations
 * while adding role-specific query methods for business requirements.</p>
 *
 * <h3>Role-Specific Operations:</h3>
 * <ul>
 *   <li>Find roles by name for permission checks</li>
 *   <li>Query system vs custom roles</li>
 *   <li>Role assignment and management operations</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see Role
 * @see BaseRepositoryContract
 * @see com.bunkermuseum.membermanagement.repository.RoleRepository
 */
public interface RoleRepositoryContract extends BaseRepositoryContract<Role> {

    /**
     * Finds a role by its name.
     *
     * @param name The role name to search for (e.g., "ADMIN", "USER")
     * @return An Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(String name);

}