package com.bunkermuseum.membermanagement.service.base.contract;

import com.bunkermuseum.membermanagement.model.base.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base service interface defining common business operations for all entities.
 *
 * <p>This interface provides the contract for all concrete services,
 * ensuring consistent business logic patterns across the Bunkermuseum application.
 * It defines standardized methods for entity manipulation with comprehensive
 * validation, business rule enforcement, and transaction management.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>Business Operations:</strong> Complete Create, Read, Update, Delete functionality with business logic</li>
 *   <li><strong>Validation Support:</strong> Built-in validation and business rule enforcement</li>
 *   <li><strong>Pagination Support:</strong> Built-in pagination with Spring Data Page/Pageable</li>
 *   <li><strong>Soft Delete Support:</strong> Compatible with Model's soft delete functionality</li>
 *   <li><strong>UUID Primary Keys:</strong> Full support for UUID-based entity identification</li>
 *   <li><strong>Batch Operations:</strong> Efficient bulk operations with business validation</li>
 *   <li><strong>Transaction Management:</strong> Proper transaction boundaries and rollback handling</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * @Service
 * public class UserServiceImpl extends BaseService<User, UserRepositoryContract> {
 *     public UserServiceImpl(UserRepositoryContract repository) {
 *         super(repository);
 *     }
 *
 *     @Override
 *     protected String getEntityName() {
 *         return "User";
 *     }
 *
 *     @Override
 *     protected void validateForCreate(User entity) {
 *         if (entity.getUsername() == null || entity.getUsername().trim().isEmpty()) {
 *             throw new IllegalArgumentException("Username is required");
 *         }
 *     }
 *
 *     // Add entity-specific business methods here
 *     public List<User> findActiveUsers() {
 *         return repository.findActive();
 *     }
 * }
 * }</pre>
 *
 * <h3>Implementation Notes:</h3>
 * <ul>
 *   <li>All methods include comprehensive validation and business rule enforcement</li>
 *   <li>Transactional annotations are applied appropriately for data consistency</li>
 *   <li>Exception handling provides meaningful business-level error messages</li>
 *   <li>Validation methods can be overridden for entity-specific business rules</li>
 * </ul>
 *
 * @param <T> The entity type extending Model
 *
 * @see com.bunkermuseum.membermanagement.model.base.Model
 * @see com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract
 * @see org.springframework.data.domain.Page
 * @see org.springframework.data.domain.Pageable
 */
public interface BaseServiceContract<T extends Model> {

    /**
     * Retrieves all entities from the database.
     *
     * <p>This method returns all entities of type T that are stored in the database
     * through the underlying repository layer. The operation includes any business
     * logic validation and filtering that may be required.</p>
     *
     * <p><strong>Business Logic:</strong> This method may apply business rules
     * such as access control, data filtering, or other domain-specific logic
     * before returning results.</p>
     *
     * <p><strong>Performance Note:</strong> This method loads all entities into memory
     * at once. For large datasets, consider using pagination methods like
     * {@link #findAll(Pageable)} or chunked processing.</p>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method includes soft-deleted entities.
     * Use {@link #findActive()} to exclude soft-deleted entities.</p>
     *
     * @return List of all entities in the database, or empty list if no entities exist
     *
     * @see #findAll(Pageable)
     * @see #findActive()
     */
    List<T> findAll();

    /**
     * Finds a single entity by its unique identifier.
     *
     * <p>This method searches for an entity using its UUID primary key through
     * the repository layer, applying any necessary business logic validation
     * or access control rules.</p>
     *
     * <p><strong>Business Logic:</strong> May include access control checks,
     * data filtering, or other domain-specific validation before returning results.</p>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method will find entities even if they
     * are soft-deleted. The caller is responsible for checking the entity's deletion
     * status if needed.</p>
     *
     * @param id The UUID identifier of the entity to find. Must not be null.
     * @return Optional containing the entity if found, empty Optional if not found
     *
     * @throws IllegalArgumentException if id is null
     *
     * @see #findByIdOrFail(UUID)
     * @see #existsById(UUID)
     */
    Optional<T> findById(UUID id);

    /**
     * Finds an entity by ID or throws an exception if not found.
     *
     * <p>This method is a convenience wrapper around {@link #findById(UUID)} that
     * throws an exception instead of returning an empty Optional. It includes
     * business logic validation and provides meaningful error messages.</p>
     *
     * <p><strong>Business Logic:</strong> Applies the same business rules as
     * {@link #findById(UUID)} but fails fast with descriptive error messages
     * when entities are not found.</p>
     *
     * @param id The UUID identifier of the entity to find. Must not be null.
     * @return The entity with the specified ID, never null
     *
     * @throws IllegalArgumentException if id is null
     * @throws jakarta.persistence.EntityNotFoundException if no entity exists with the given ID
     *
     * @see #findById(UUID)
     * @see #update(UUID, T)
     */
    T findByIdOrFail(UUID id);

    /**
     * Creates a new entity with full business logic validation.
     *
     * <p>This method creates a new entity through the repository layer while
     * applying comprehensive business logic validation, rule enforcement,
     * and any necessary data transformations.</p>
     *
     * <p><strong>Validation Process:</strong></p>
     * <ol>
     *   <li>Validates entity data using {@link #validateForCreate(T)}</li>
     *   <li>Applies business rules and transformations</li>
     *   <li>Delegates to repository for persistence</li>
     *   <li>Performs any post-creation business logic</li>
     * </ol>
     *
     * <p><strong>Transaction Management:</strong> This method is transactional
     * and will rollback all changes if any step fails.</p>
     *
     * @param entity The entity instance to create. Must not be null and should be in transient state.
     * @return The created entity with assigned ID and audit fields, never null
     *
     * @throws IllegalArgumentException if entity is null or validation fails
     * @throws javax.validation.ConstraintViolationException if entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if business rules are violated
     *
     * @see #createAll(List)
     * @see #validateForCreate(T)
     */
    T create(T entity);

    /**
     * Updates an existing entity with business logic validation.
     *
     * <p>This method updates an existing entity while applying comprehensive
     * business logic validation, rule enforcement, and data transformations.</p>
     *
     * <p><strong>Update Process:</strong></p>
     * <ol>
     *   <li>Verifies the entity exists</li>
     *   <li>Validates updated data using {@link #validateForUpdate(UUID, T)}</li>
     *   <li>Applies business rules and transformations</li>
     *   <li>Delegates to repository for persistence</li>
     *   <li>Performs any post-update business logic</li>
     * </ol>
     *
     * @param id The UUID of the entity to update. Must not be null.
     * @param entity The entity instance containing updated data. Must not be null.
     * @return The updated entity with refreshed audit fields, never null
     *
     * @throws IllegalArgumentException if id or entity is null, or validation fails
     * @throws jakarta.persistence.EntityNotFoundException if no entity exists with the given ID
     * @throws javax.validation.ConstraintViolationException if entity validation fails
     *
     * @see #findByIdOrFail(UUID)
     * @see #validateForUpdate(UUID, T)
     */
    T update(UUID id, T entity);

    /**
     * Deletes an entity from the database with business logic validation.
     *
     * <p>This method performs entity deletion while applying business logic
     * validation and rule enforcement. It may perform hard delete or delegate
     * to soft delete based on business requirements.</p>
     *
     * <p><strong>Business Logic:</strong> May include dependency checks,
     * cascading operations, audit logging, or other domain-specific logic
     * before performing the deletion.</p>
     *
     * @param id The UUID of the entity to delete. Must not be null.
     * @return true if entity was found and deleted, false if entity was not found
     *
     * @throws IllegalArgumentException if id is null or business rules prevent deletion
     *
     * @see #validateForDelete(UUID)
     */
    boolean deleteById(UUID id);

    /**
     * Retrieves entities with pagination support and business logic.
     *
     * <p>This method provides efficient paginated access to entities while
     * applying any necessary business logic, filtering, or access control rules.</p>
     *
     * <p><strong>Business Logic:</strong> May apply data filtering, access control,
     * or other domain-specific logic before returning paginated results.</p>
     *
     * @param pageable Pagination and sorting information. Must not be null.
     * @return Page containing the requested entities with pagination metadata
     *
     * @throws IllegalArgumentException if pageable is null
     *
     * @see org.springframework.data.domain.PageRequest
     * @see org.springframework.data.domain.Sort
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Counts the total number of entities with business logic filtering.
     *
     * <p>This method returns the total count of entities while applying
     * any business logic filtering that may affect the count.</p>
     *
     * <p><strong>Business Logic:</strong> May apply data filtering or access
     * control rules that affect the total count returned.</p>
     *
     * @return The total number of entities accessible through business logic
     *
     * @see #existsById(UUID)
     */
    long count();

    /**
     * Creates multiple entities with business logic validation.
     *
     * <p>This method efficiently creates multiple entities while applying
     * business logic validation to each entity in the batch.</p>
     *
     * <p><strong>Validation:</strong> Each entity in the list is validated
     * using {@link #validateForCreate(T)} before the batch is processed.</p>
     *
     * <p><strong>Transaction Behavior:</strong> All entities are created within
     * a single transaction. If any entity fails validation or persistence,
     * the entire operation is rolled back.</p>
     *
     * @param entities List of entity instances to create. Must not be null, can be empty.
     * @return List of created entities with assigned IDs and audit fields, never null
     *
     * @throws IllegalArgumentException if entities is null or any entity validation fails
     *
     * @see #create(T)
     * @see #validateForCreate(T)
     */
    List<T> createAll(List<T> entities);

    /**
     * Checks whether an entity exists with business logic filtering.
     *
     * <p>This method performs an existence check while applying any business
     * logic rules that may affect entity visibility.</p>
     *
     * <p><strong>Business Logic:</strong> May apply access control or filtering
     * rules that affect whether an entity is considered to "exist" from a
     * business perspective.</p>
     *
     * @param id The UUID identifier to check for existence. Must not be null.
     * @return true if an entity with the given ID exists and is accessible through business logic
     *
     * @throws IllegalArgumentException if id is null
     *
     * @see #findById(UUID)
     */
    boolean existsById(UUID id);

    // Laravel-style additional methods

    /**
     * Creates a new entity from a map of field names to values with business validation.
     *
     * <p>This method provides Laravel-style entity creation from dynamic field-value
     * pairs while applying full business logic validation and rule enforcement.</p>
     *
     * <p><strong>Business Logic Integration:</strong> The created entity is validated
     * using the same business rules as {@link #create(T)}, ensuring data integrity
     * and business rule compliance.</p>
     *
     * @param data Map containing field names as keys and field values as values. Must not be null.
     * @return The created entity with assigned ID and audit fields, never null
     *
     * @throws IllegalArgumentException if data is null or business validation fails
     *
     * @see #create(T)
     * @see #createMany(List)
     */
    T create(Map<String, Object> data);

    /**
     * Updates an existing entity with field-value data and business validation.
     *
     * <p>This method provides Laravel-style entity updates from dynamic field-value
     * pairs while applying comprehensive business logic validation.</p>
     *
     * <p><strong>Business Logic Integration:</strong> The updated entity is validated
     * using {@link #validateForUpdate(UUID, T)} to ensure business rule compliance.</p>
     *
     * @param id The UUID of the entity to update. Must not be null.
     * @param data Map containing field names as keys and new values as values. Must not be null.
     * @return The updated entity with refreshed audit fields, never null
     *
     * @throws IllegalArgumentException if id or data is null, or business validation fails
     * @throws jakarta.persistence.EntityNotFoundException if no entity exists with the given ID
     *
     * @see #update(UUID, T)
     */
    T updateWithData(UUID id, Map<String, Object> data);

    /**
     * Creates multiple entities from field-value data maps with business validation.
     *
     * <p>This method provides Laravel-style batch entity creation from dynamic
     * field-value pairs while applying business logic validation to each entity.</p>
     *
     * <p><strong>Business Logic Integration:</strong> Each entity is validated
     * using the same business rules as {@link #create(T)}, with full transaction
     * rollback if any entity fails validation.</p>
     *
     * @param dataList List of maps, each containing field names to values for one entity. Must not be null.
     * @return List of created entities with assigned IDs and audit fields, never null
     *
     * @throws IllegalArgumentException if dataList is null or any entity validation fails
     *
     * @see #create(Map)
     * @see #createAll(List)
     */
    List<T> createMany(List<Map<String, Object>> dataList);

    /**
     * Processes all entities in memory-efficient chunks with business logic.
     *
     * <p>This method provides memory-efficient processing of large datasets
     * while applying any necessary business logic filtering or access control.</p>
     *
     * <p><strong>Business Logic:</strong> Entities are filtered through business
     * logic before being passed to the processor, ensuring only accessible
     * entities are processed.</p>
     *
     * @param chunkSize Number of entities to process in each chunk. Must be positive.
     * @param processor Consumer function that processes each chunk of entities. Must not be null.
     *
     * @throws IllegalArgumentException if chunkSize <= 0 or processor is null
     *
     * @see #findAll(Pageable)
     */
    void processInChunks(int chunkSize, Consumer<List<T>> processor);

    /**
     * Finds only active entities with business logic filtering.
     *
     * <p>This method retrieves all entities that are currently active (not soft deleted)
     * while applying any additional business logic filtering or access control rules.</p>
     *
     * <p><strong>Business Logic:</strong> May apply additional filtering beyond
     * soft delete status based on business requirements.</p>
     *
     * @return List of active entities accessible through business logic, never null but may be empty
     *
     * @see #findDeleted()
     * @see #findWithDeleted()
     */
    List<T> findActive();

    /**
     * Finds only soft deleted entities with business logic filtering.
     *
     * <p>This method retrieves all entities that have been soft deleted
     * while applying business logic rules for access control and filtering.</p>
     *
     * <p><strong>Business Logic:</strong> May apply access control rules
     * determining which deleted entities are visible to the current context.</p>
     *
     * @return List of soft deleted entities accessible through business logic, never null but may be empty
     *
     * @see #findActive()
     * @see #findWithDeleted()
     */
    List<T> findDeleted();

    /**
     * Finds all entities including both active and soft deleted ones with business logic.
     *
     * <p>This method retrieves the complete dataset of entities while applying
     * business logic filtering and access control rules to both active and
     * deleted entities.</p>
     *
     * <p><strong>Business Logic:</strong> Applies consistent business rules
     * to both active and deleted entities, ensuring proper access control
     * across the complete dataset.</p>
     *
     * @return List of all entities (active and deleted) accessible through business logic, never null but may be empty
     *
     * @see #findActive()
     * @see #findDeleted()
     */
    List<T> findWithDeleted();

    // Validation hooks for business logic

    /**
     * Validates an entity before creation.
     *
     * <p>This method is called before creating new entities to apply business
     * rule validation. Concrete implementations should override this method
     * to provide entity-specific validation logic.</p>
     *
     * <p><strong>Default Implementation:</strong> Performs basic null checks.
     * Concrete services should override to add meaningful business validation.</p>
     *
     * @param entity The entity to validate for creation. Must not be null.
     *
     * @throws IllegalArgumentException if entity is null or validation fails
     *
     * @see #create(T)
     * @see #createAll(List)
     */
    default void validateForCreate(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
    }

    /**
     * Validates an entity before update.
     *
     * <p>This method is called before updating existing entities to apply business
     * rule validation. Concrete implementations should override this method
     * to provide entity-specific validation logic.</p>
     *
     * <p><strong>Default Implementation:</strong> Performs basic null checks.
     * Concrete services should override to add meaningful business validation.</p>
     *
     * @param id The UUID of the entity being updated. Must not be null.
     * @param entity The entity containing updated data. Must not be null.
     *
     * @throws IllegalArgumentException if id or entity is null, or validation fails
     *
     * @see #update(UUID, T)
     */
    default void validateForUpdate(UUID id, T entity) {
        if (id == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
    }

    /**
     * Validates an entity before deletion.
     *
     * <p>This method is called before deleting entities to apply business
     * rule validation. Concrete implementations should override this method
     * to provide entity-specific validation logic.</p>
     *
     * <p><strong>Default Implementation:</strong> Performs basic null checks.
     * Concrete services should override to add meaningful business validation.</p>
     *
     * @param id The UUID of the entity to be deleted. Must not be null.
     *
     * @throws IllegalArgumentException if id is null or business rules prevent deletion
     *
     * @see #deleteById(UUID)
     */
    default void validateForDelete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }
    }
}