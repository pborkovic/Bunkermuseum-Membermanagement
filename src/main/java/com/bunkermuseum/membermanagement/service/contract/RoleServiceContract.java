package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service contract interface for Role entity business operations.
 *
 * <p>This interface defines the contract for Role-specific business logic operations.
 * It provides methods for role management, assignment, and access control functionality
 * while maintaining clear separation of concerns from the data access layer.</p>
 *
 * <h3>Caching Strategy:</h3>
 * <p>All role operations utilize caching to optimize performance:</p>
 * <ul>
 *     <li><strong>Read operations:</strong> Cached for 24 hours (roles rarely change)</li>
 *     <li><strong>Write operations:</strong> Automatically evict the entire role cache</li>
 *     <li><strong>Cache size:</strong> Maximum 100 entries (typically only 3-5 roles exist)</li>
 * </ul>
 *
 * @author Philipp Borkovic
 * @see Role
 * @see com.bunkermuseum.membermanagement.service.RoleService
 */
public interface RoleServiceContract {

    /**
     * Retrieves all roles from the system.
     *
     * <p>This method returns all available roles in the system. The results are cached
     * for 24 hours to optimize performance, as roles rarely change in production.</p>
     *
     * <h3>Caching:</h3>
     * <ul>
     *     <li>Cache name: {@code roles}</li>
     *     <li>Cache key: {@code 'all'}</li>
     *     <li>TTL: 24 hours</li>
     * </ul>
     *
     * <h3>Use Cases:</h3>
     * <ul>
     *     <li>Populating role selection dropdowns</li>
     *     <li>Authorization checks across the application</li>
     *     <li>User role assignment operations</li>
     * </ul>
     *
     * @return List of all roles, never null but may be empty
     * @throws RuntimeException if the database operation fails
     */
    List<Role> findAll();

    /**
     * Retrieves a role by its unique identifier.
     *
     * <p>This method performs a lookup by UUID and returns the role if found.
     * Results are cached for 24 hours to reduce database load during frequent
     * authorization checks.</p>
     *
     * <h3>Caching:</h3>
     * <ul>
     *     <li>Cache name: {@code roles}</li>
     *     <li>Cache key: Role UUID</li>
     *     <li>TTL: 24 hours</li>
     * </ul>
     *
     * @param id The unique identifier of the role, must not be null
     * @return Optional containing the role if found, empty Optional otherwise
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if the database operation fails
     */
    Optional<Role> findById(UUID id);

    /**
     * Creates a new role in the system.
     *
     * <p>This method persists a new role and evicts the entire role cache to ensure
     * consistency. The cache eviction is necessary because the 'all roles' cache
     * would become stale after a new role is added.</p>
     *
     * <h3>Transaction Management:</h3>
     * <ul>
     *     <li>Runs in a write transaction</li>
     *     <li>Rolls back on any exception</li>
     *     <li>Evicts all role caches upon successful commit</li>
     * </ul>
     *
     * <h3>Cache Eviction:</h3>
     * <ul>
     *     <li>Evicts all entries from {@code roles} cache</li>
     *     <li>Forces fresh database queries for subsequent reads</li>
     * </ul>
     *
     * @param role The role to create, must not be null
     * @return The created role with populated ID and timestamps
     * @throws IllegalArgumentException if role is null or invalid
     * @throws RuntimeException if persistence fails
     */
    Role create(Role role);

    /**
     * Updates an existing role in the system.
     *
     * <p>This method updates the specified role and evicts the entire role cache
     * to maintain data consistency across all cached entries.</p>
     *
     * <h3>Transaction Management:</h3>
     * <ul>
     *     <li>Runs in a write transaction</li>
     *     <li>Rolls back on any exception</li>
     *     <li>Evicts all role caches upon successful commit</li>
     * </ul>
     *
     * <h3>Cache Eviction:</h3>
     * <ul>
     *     <li>Evicts all entries from {@code roles} cache</li>
     *     <li>Ensures subsequent reads fetch updated data</li>
     * </ul>
     *
     * @param id The unique identifier of the role to update, must not be null
     * @param role The role data with updated fields, must not be null
     * @return The updated role with new timestamps
     * @throws IllegalArgumentException if id or role is null, or role not found
     * @throws RuntimeException if update fails
     */
    Role update(UUID id, Role role);

    /**
     * Deletes a role from the system by its unique identifier.
     *
     * <p>This method performs a soft delete (setting deleted flag) or hard delete
     * depending on the repository implementation, and evicts the entire role cache
     * to ensure consistency.</p>
     *
     * <h3>Transaction Management:</h3>
     * <ul>
     *     <li>Runs in a write transaction</li>
     *     <li>Rolls back on any exception</li>
     *     <li>Evicts all role caches upon successful commit</li>
     * </ul>
     *
     * <h3>Cache Eviction:</h3>
     * <ul>
     *     <li>Evicts all entries from {@code roles} cache</li>
     *     <li>Ensures deleted role is not served from cache</li>
     * </ul>
     *
     * <h3>Important Notes:</h3>
     * <ul>
     *     <li>Consider impact on users with this role before deletion</li>
     *     <li>May fail if role is referenced by active users</li>
     * </ul>
     *
     * @param id The unique identifier of the role to delete, must not be null
     * @return true if deletion was successful, false otherwise
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if deletion fails due to database constraints
     */
    boolean deleteById(UUID id);
}