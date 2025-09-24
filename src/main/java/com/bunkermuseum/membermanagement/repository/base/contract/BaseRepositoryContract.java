package com.bunkermuseum.membermanagement.repository.base.contract;

import com.bunkermuseum.membermanagement.model.base.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Base repository interface defining common CRUD operations for all entities.
 *
 * <p>This interface provides the contract for all concrete repositories,
 * ensuring consistent data access patterns across the Bunkermuseum application.
 * It defines standardized methods for entity manipulation with comprehensive
 * error handling and logging capabilities.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>CRUD Operations:</strong> Complete Create, Read, Update, Delete functionality</li>
 *   <li><strong>Pagination Support:</strong> Built-in pagination with Spring Data Page/Pageable</li>
 *   <li><strong>Specification Queries:</strong> Dynamic queries using JPA Specifications</li>
 *   <li><strong>Soft Delete Support:</strong> Compatible with Model's soft delete functionality</li>
 *   <li><strong>UUID Primary Keys:</strong> Full support for UUID-based entity identification</li>
 *   <li><strong>Batch Operations:</strong> Efficient bulk save and delete operations</li>
 * </ul>
 *
 * <h3>Implementation Notes:</h3>
 * <ul>
 *   <li>All methods include comprehensive error handling and logging</li>
 *   <li>Transactional annotations are applied appropriately for data safety</li>
 *   <li>Exception handling preserves original stack traces for debugging</li>
 *   <li>Specification-based queries provide type-safe dynamic filtering</li>
 * </ul>
 *
 * @param <T> The entity type extending Model
 *
 * @see com.bunkermuseum.membermanagement.model.base.Model
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor
 * @see org.springframework.data.domain.Page
 * @see org.springframework.data.domain.Pageable
 */
public interface BaseRepositoryContract<T extends Model> {

    /**
     * Retrieves all entities from the database.
     *
     * <p>This method returns all entities of type T that are stored in the database.
     * The operation does not apply any filtering or ordering, returning entities
     * in their natural database order.</p>
     *
     * <p><strong>Performance Note:</strong> This method loads all entities into memory
     * at once. For large datasets, consider using pagination methods like
     * {@link #findAll(Pageable)} or chunked processing with {@link #processInChunks(int, Consumer)}.</p>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method includes soft-deleted entities.
     * Use {@link #findActive()} to exclude soft-deleted entities.</p>
     *
     * @return List of all entities in the database, or empty list if no entities exist
     *
     * @see #findAll(Pageable)
     * @see #findActive()
     * @see #processInChunks(int, Consumer)
     */
    List<T> findAll();

    /**
     * Finds a single entity by its unique identifier.
     *
     * <p>This method searches for an entity using its UUID primary key. The search
     * is performed directly against the database using the most efficient query possible.</p>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method will find entities even if they
     * are soft-deleted (deletedAt is not null). The caller is responsible for checking
     * the entity's deletion status if needed.</p>
     *
     * <p><strong>Caching:</strong> Results may be cached by the underlying JPA provider
     * if second-level cache is enabled for the entity.</p>
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
     * throws an exception instead of returning an empty Optional. It's useful when
     * you expect the entity to exist and want to fail fast if it doesn't.</p>
     *
     * <p><strong>Usage Pattern:</strong> This method is commonly used in update and
     * delete operations where the entity must exist for the operation to proceed.</p>
     *
     * <p><strong>Soft Delete Behavior:</strong> Like {@link #findById(UUID)}, this method
     * will find and return soft-deleted entities. Check {@code entity.isDeleted()}
     * if you need to verify the entity's deletion status.</p>
     *
     * @param id The UUID identifier of the entity to find. Must not be null.
     * @return The entity with the specified ID, never null
     *
     * @throws jakarta.persistence.EntityNotFoundException if no entity exists with the given ID
     * @throws IllegalArgumentException if id is null
     *
     * @see #findById(UUID)
     * @see #update(UUID, T)
     */
    T findByIdOrFail(UUID id);

    /**
     * Finds the first entity in the database.
     *
     * <p>This method retrieves the first entity according to the database's natural
     * ordering (typically by primary key or insertion order). It's equivalent to
     * calling {@code findAll(PageRequest.of(0, 1)).getContent().stream().findFirst()}
     * but more efficient as it limits the query to a single result.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Getting a sample entity for testing or demonstration</li>
     *   <li>Checking if any entities exist in the table</li>
     *   <li>Retrieving the oldest entity when combined with appropriate ordering</li>
     * </ul>
     *
     * <p><strong>Ordering:</strong> The "first" entity is determined by the database's
     * default ordering. If you need specific ordering, use {@link #findAll(Pageable)}
     * with a Sort parameter instead.</p>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method includes soft-deleted entities
     * in the search. The first entity returned might be soft-deleted.</p>
     *
     * @return Optional containing the first entity if any exists, empty Optional if table is empty
     *
     * @see #findAll(Pageable)
     * @see #count()
     */
    Optional<T> findFirst();

    /**
     * Creates a new entity in the database.
     *
     * <p>This method persists a new entity to the database, assigning it a new UUID
     * primary key and setting audit timestamps (createdAt, updatedAt). The entity
     * should be in a transient state (not previously persisted).</p>
     *
     * <p><strong>Key Behaviors:</strong></p>
     * <ul>
     *   <li>Assigns a new UUID primary key if not already set</li>
     *   <li>Sets createdAt timestamp automatically via @CreationTimestamp</li>
     *   <li>Sets updatedAt timestamp automatically via @UpdateTimestamp</li>
     *   <li>Validates entity constraints before persistence</li>
     *   <li>Returns the managed (persistent) entity instance</li>
     * </ul>
     *
     * <p><strong>Transaction Requirements:</strong> This method must be called within
     * an active transaction context. The transaction will be committed to make
     * changes permanent.</p>
     *
     * <p><strong>Cascade Operations:</strong> Related entities with cascade settings
     * will be automatically persisted according to their cascade configuration.</p>
     *
     * @param entity The entity instance to create. Must not be null and should be in transient state.
     * @return The created entity with assigned ID and audit fields, never null
     *
     * @throws IllegalArgumentException if entity is null or already persistent
     * @throws javax.validation.ConstraintViolationException if entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     * @throws org.springframework.transaction.TransactionException if no active transaction
     *
     * @see #createAll(List)
     * @see #createAndFlush(T)
     * @see #create(Map)
     */
    T create(T entity);

    /**
     * Updates an existing entity identified by its UUID.
     *
     * <p>This method updates an existing entity in the database with new data.
     * The entity must already exist in the database, or an exception will be thrown.
     * The updatedAt timestamp is automatically set during this operation.</p>
     *
     * <p><strong>Update Process:</strong></p>
     * <ol>
     *   <li>Verifies the entity exists in the database</li>
     *   <li>Updates the entity with the provided data</li>
     *   <li>Automatically sets updatedAt timestamp via @UpdateTimestamp</li>
     *   <li>Validates updated entity constraints</li>
     *   <li>Persists changes to the database</li>
     * </ol>
     *
     * <p><strong>Identity Handling:</strong> The provided entity's ID should match
     * the id parameter. If they differ, the id parameter takes precedence for
     * identifying the entity to update.</p>
     *
     * <p><strong>Optimistic Locking:</strong> If the entity uses version fields
     * for optimistic locking, ensure the entity has the correct version to
     * avoid OptimisticLockingFailureException.</p>
     *
     * @param id The UUID of the entity to update. Must not be null.
     * @param entity The entity instance containing updated data. Must not be null.
     * @return The updated entity with refreshed audit fields, never null
     *
     * @throws jakarta.persistence.EntityNotFoundException if no entity exists with the given ID
     * @throws IllegalArgumentException if id or entity is null
     * @throws javax.validation.ConstraintViolationException if updated entity validation fails
     * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if optimistic locking fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     *
     * @see #findByIdOrFail(UUID)
     * @see #updateWithData(UUID, Map)
     */
    T update(UUID id, T entity);

    /**
     * Deletes an entity from the database by its UUID.
     *
     * <p>This method performs a hard delete, permanently removing the entity
     * from the database. For soft delete functionality, use the entity's
     * {@code delete()} method instead, which sets the deletedAt timestamp.</p>
     *
     * <p><strong>Deletion Process:</strong></p>
     * <ol>
     *   <li>Checks if entity exists in the database</li>
     *   <li>If exists, removes the entity and all its dependent data</li>
     *   <li>If not exists, returns false without throwing an exception</li>
     * </ol>
     *
     * <p><strong>Cascade Behavior:</strong> Related entities will be affected
     * according to their cascade deletion settings. This may result in:
     * <ul>
     *   <li>Automatic deletion of child entities (CASCADE)</li>
     *   <li>Setting foreign key references to null (SET NULL)</li>
     *   <li>Constraint violations if dependent data exists (RESTRICT)</li>
     * </ul>
     *
     * <p><strong>Soft Delete Alternative:</strong> Instead of hard deletion,
     * consider using soft delete by calling {@code entity.delete().save()}
     * to preserve data integrity and enable data recovery.</p>
     *
     * @param id The UUID of the entity to delete. Must not be null.
     * @return true if entity was found and deleted, false if entity was not found
     *
     * @throws IllegalArgumentException if id is null
     * @throws org.springframework.dao.DataIntegrityViolationException if deletion violates database constraints
     * @throws org.springframework.transaction.TransactionException if no active transaction
     *
     * @see com.bunkermuseum.membermanagement.model.base.Model#delete()
     * @see com.bunkermuseum.membermanagement.model.base.Model#restore()
     */
    boolean deleteById(UUID id);

    /**
     * Retrieves entities with pagination support.
     *
     * <p>This method provides efficient paginated access to entities, allowing
     * you to retrieve large datasets in manageable chunks. It supports both
     * offset-based pagination and sorting.</p>
     *
     * <p><strong>Pagination Features:</strong></p>
     * <ul>
     *   <li>Configurable page size (number of entities per page)</li>
     *   <li>Zero-based page indexing (first page is 0)</li>
     *   <li>Multi-field sorting with configurable direction</li>
     *   <li>Total count calculation for UI pagination controls</li>
     *   <li>Efficient database queries using LIMIT/OFFSET</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>Large page sizes may impact memory usage and performance</li>
     *   <li>Deep pagination (high offset values) may be slow on large datasets</li>
     *   <li>Consider using cursor-based pagination for very large datasets</li>
     *   <li>Sorting on indexed columns improves query performance</li>
     * </ul>
     *
     * @param pageable Pagination and sorting information. Must not be null.
     * @return Page containing the requested entities with pagination metadata
     *
     * @throws IllegalArgumentException if pageable is null
     * @throws org.springframework.dao.InvalidDataAccessApiUsageException if sort property doesn't exist
     *
     * @see org.springframework.data.domain.PageRequest
     * @see org.springframework.data.domain.Sort
     * @see #processInChunks(int, Consumer)
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Counts the total number of entities in the database.
     *
     * <p>This method returns the total count of all entities of type T stored
     * in the database, including soft-deleted entities. It performs an efficient
     * COUNT query without loading entity data into memory.</p>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li>Executes a single COUNT(*) SQL query</li>
     *   <li>Does not load entity data, making it memory efficient</li>
     *   <li>Performance depends on table size and database indexing</li>
     *   <li>May be cached by the database query planner</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Calculating pagination metadata (total pages, etc.)</li>
     *   <li>Checking if entities exist before expensive operations</li>
     *   <li>Monitoring and reporting on data volumes</li>
     *   <li>Validating business rules based on entity counts</li>
     * </ul>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method includes soft-deleted
     * entities in the count. Use a custom query method if you need to count
     * only active entities.</p>
     *
     * @return The total number of entities in the database (including soft-deleted), 0 if no entities exist
     *
     * @see #existsById(UUID)
     * @see #findActive()
     */
    long count();

    /**
     * Finds multiple entities by their UUID identifiers.
     *
     * <p>This method efficiently retrieves multiple entities in a single database
     * query using an IN clause. It's more efficient than making multiple individual
     * {@link #findById(UUID)} calls when you need to fetch several entities.</p>
     *
     * <p><strong>Query Optimization:</strong></p>
     * <ul>
     *   <li>Uses SQL IN clause for efficient batch retrieval</li>
     *   <li>Single database round trip regardless of ID count</li>
     *   <li>Respects database query plan caching</li>
     *   <li>May use primary key index for fast lookups</li>
     * </ul>
     *
     * <p><strong>Result Characteristics:</strong></p>
     * <ul>
     *   <li>Returned list may contain fewer entities than requested IDs</li>
     *   <li>Order of returned entities is not guaranteed to match input order</li>
     *   <li>Duplicate IDs in input will not produce duplicate entities in result</li>
     *   <li>Non-existent IDs are silently ignored (no exception thrown)</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>Very large ID lists may hit database query limits</li>
     *   <li>Consider chunking large ID lists (>1000 items) for better performance</li>
     *   <li>Empty input list returns empty result without database query</li>
     * </ul>
     *
     * @param ids List of UUID identifiers to search for. Must not be null, can be empty.
     * @return List of found entities, may contain fewer items than input IDs, never null
     *
     * @throws IllegalArgumentException if ids is null
     *
     * @see #findById(UUID)
     * @see #existsById(UUID)
     */
    List<T> findAllById(List<UUID> ids);

    /**
     * Creates multiple entities in a single batch operation.
     *
     * <p>This method efficiently persists multiple entities to the database using
     * batch processing. It's significantly more efficient than calling {@link #create(Object)}
     * multiple times, especially for large datasets.</p>
     *
     * <p><strong>Batch Processing Benefits:</strong></p>
     * <ul>
     *   <li>Reduced database round trips through batching</li>
     *   <li>Better transaction efficiency and atomicity</li>
     *   <li>Improved performance for bulk insert operations</li>
     *   <li>Optimized memory usage through batch flushing</li>
     * </ul>
     *
     * <p><strong>Entity Processing:</strong></p>
     * <ul>
     *   <li>Each entity receives a new UUID primary key</li>
     *   <li>Audit timestamps (createdAt, updatedAt) are set automatically</li>
     *   <li>Entity validation is performed for each item</li>
     *   <li>Cascade operations are applied according to entity mappings</li>
     * </ul>
     *
     * <p><strong>Transaction Behavior:</strong> All entities are created within
     * a single transaction. If any entity fails validation or persistence,
     * the entire operation is rolled back (all-or-nothing semantics).</p>
     *
     * <p><strong>Performance Recommendations:</strong></p>
     * <ul>
     *   <li>For very large batches (>1000 entities), consider using {@link #processInChunks(int, Consumer)}</li>
     *   <li>Disable automatic flushing for better performance in large batches</li>
     *   <li>Consider using {@link #createAndFlush(Object)} for smaller batches needing immediate persistence</li>
     * </ul>
     *
     * @param entities List of entity instances to create. Must not be null, can be empty.
     * @return List of created entities with assigned IDs and audit fields, never null
     *
     * @throws IllegalArgumentException if entities is null or contains null elements
     * @throws javax.validation.ConstraintViolationException if any entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     * @throws org.springframework.transaction.TransactionException if no active transaction
     *
     * @see #create(T)
     * @see #createMany(List)
     * @see #processInChunks(int, Consumer)
     */
    List<T> createAll(List<T> entities);

    /**
     * Checks whether an entity exists in the database by its UUID.
     *
     * <p>This method performs an efficient existence check without loading the
     * entity data into memory. It typically translates to a SELECT COUNT(*) or
     * SELECT 1 query with a LIMIT 1 clause.</p>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li>Executes lightweight query without loading entity data</li>
     *   <li>Uses primary key index for optimal performance</li>
     *   <li>Minimal memory footprint compared to {@link #findById(UUID)}</li>
     *   <li>May benefit from database query result caching</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Validating entity existence before expensive operations</li>
     *   <li>Implementing conditional logic based on entity presence</li>
     *   <li>Pre-flight checks for update and delete operations</li>
     *   <li>Business rule validation requiring entity existence</li>
     * </ul>
     *
     * <p><strong>Soft Delete Behavior:</strong> This method returns true for
     * soft-deleted entities (where deletedAt is not null). If you need to check
     * for active entities only, use a combination of {@link #findById(UUID)}
     * and {@code entity.isActive()}.</p>
     *
     * @param id The UUID identifier to check for existence. Must not be null.
     * @return true if an entity with the given ID exists in the database, false otherwise
     *
     * @throws IllegalArgumentException if id is null
     *
     * @see #findById(UUID)
     * @see #count()
     */
    boolean existsById(UUID id);

    /**
     * Creates an entity and immediately flushes changes to the database.
     *
     * <p>This method combines entity creation with an immediate flush operation,
     * ensuring that the entity is written to the database right away, even within
     * an ongoing transaction. This is useful when you need the entity to be
     * immediately visible to subsequent database queries within the same transaction.</p>
     *
     * <p><strong>Flush Behavior:</strong></p>
     * <ul>
     *   <li>Forces immediate synchronization with the database</li>
     *   <li>Triggers SQL INSERT statement execution</li>
     *   <li>Makes entity immediately visible to native queries</li>
     *   <li>Updates entity with database-generated values (ID, timestamps)</li>
     * </ul>
     *
     * <p><strong>When to Use:</strong></p>
     * <ul>
     *   <li>Need immediate database-generated values (sequences, triggers)</li>
     *   <li>Following operations require the entity to exist in database</li>
     *   <li>Native SQL queries need to see the newly created entity</li>
     *   <li>Debugging and ensuring immediate persistence</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>More expensive than regular {@link #create(Object)} due to immediate flush</li>
     *   <li>Reduces batch processing efficiency</li>
     *   <li>May impact transaction performance if used frequently</li>
     *   <li>Consider using regular create() for batch operations</li>
     * </ul>
     *
     * <p><strong>Transaction Impact:</strong> The flush operation occurs within
     * the current transaction context and does not commit the transaction.
     * The entity will still be rolled back if the transaction fails later.</p>
     *
     * @param entity The entity instance to create and flush. Must not be null.
     * @return The created entity with all database-generated values, never null
     *
     * @throws IllegalArgumentException if entity is null
     * @throws javax.validation.ConstraintViolationException if entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     * @throws org.springframework.transaction.TransactionException if no active transaction
     *
     * @see #create(T)
     * @see #flush()
     */
    T createAndFlush(T entity);

    /**
     * Flushes all pending changes to the database.
     *
     * <p>This method forces immediate synchronization of all pending entity changes
     * with the database, without committing the current transaction. It triggers
     * the execution of any queued INSERT, UPDATE, and DELETE SQL statements.</p>
     *
     * <p><strong>Flush Operations:</strong></p>
     * <ul>
     *   <li>Executes all pending SQL statements in the persistence context</li>
     *   <li>Synchronizes entity state with database state</li>
     *   <li>Updates entities with database-generated values</li>
     *   <li>Clears the first-level cache after synchronization</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Ensure changes are visible to native SQL queries within same transaction</li>
     *   <li>Force constraint validation before transaction commit</li>
     *   <li>Debugging by making changes immediately visible</li>
     *   <li>Batch processing with periodic synchronization</li>
     *   <li>Obtaining database-generated values during transaction</li>
     * </ul>
     *
     * <p><strong>Performance Impact:</strong></p>
     * <ul>
     *   <li>Can improve performance by clearing memory of managed entities</li>
     *   <li>May reduce performance if called too frequently</li>
     *   <li>Triggers immediate database communication</li>
     *   <li>Best used strategically in batch operations</li>
     * </ul>
     *
     * <p><strong>Transaction Behavior:</strong> Flush does not commit the transaction.
     * All changes can still be rolled back if the transaction fails later.
     * The flush operation respects the current transaction boundaries.</p>
     *
     * @throws org.springframework.dao.DataIntegrityViolationException if flushed changes violate database constraints
     * @throws javax.validation.ConstraintViolationException if entity validation fails during flush
     * @throws org.springframework.transaction.TransactionException if no active transaction
     *
     * @see #createAndFlush(T)
     */
    void flush();

    // Laravel-style additional methods

    /**
     * Creates a new entity from a map of field names to values (Laravel-style).
     *
     * <p>This method provides a convenient way to create entities using dynamic
     * field-value pairs, similar to Laravel's Eloquent ORM. It uses reflection
     * to map the provided data to entity fields and handles the persistence
     * automatically.</p>
     *
     * <p><strong>Field Mapping Process:</strong></p>
     * <ol>
     *   <li>Creates a new entity instance using reflection</li>
     *   <li>Iterates through the provided field-value pairs</li>
     *   <li>Uses reflection to set each field value on the entity</li>
     *   <li>Attempts to find fields in the entity class and its superclasses</li>
     *   <li>Persists the populated entity to the database</li>
     * </ol>
     *
     * <p><strong>Field Resolution:</strong></p>
     * <ul>
     *   <li>Searches for fields in the entity class first</li>
     *   <li>Falls back to searching in superclass (e.g., Model base class)</li>
     *   <li>Logs warnings for fields that cannot be found</li>
     *   <li>Ignores unknown fields rather than throwing exceptions</li>
     * </ul>
     *
     * <p><strong>Data Type Handling:</strong></p>
     * <ul>
     *   <li>Automatic type conversion for compatible types</li>
     *   <li>Null values are handled appropriately</li>
     *   <li>Complex objects should match field types exactly</li>
     *   <li>Audit fields (createdAt, updatedAt, id) are set automatically</li>
     * </ul>
     *
     * <p><strong>Performance Note:</strong> This method uses reflection and is
     * less performant than direct entity creation. For high-performance scenarios
     * or frequent operations, consider using {@link #create(Object)} with
     * properly constructed entity instances.</p>
     *
     * @param data Map containing field names as keys and field values as values. Must not be null.
     * @return The created entity with assigned ID and audit fields, never null
     *
     * @throws IllegalArgumentException if data is null
     * @throws RuntimeException if entity creation or field mapping fails
     * @throws javax.validation.ConstraintViolationException if entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     *
     * @see #create(T)
     * @see #createMany(List)
     * @see #updateWithData(UUID, Map)
     */
    T create(Map<String, Object> data);

    /**
     * Updates an existing entity with field-value data (Laravel-style).
     *
     * <p>This method provides a convenient way to update entities using dynamic
     * field-value pairs, similar to Laravel's Eloquent ORM. It finds the existing
     * entity by ID, applies the field updates using reflection, and persists
     * the changes to the database.</p>
     *
     * <p><strong>Update Process:</strong></p>
     * <ol>
     *   <li>Retrieves the existing entity using {@link #findByIdOrFail(UUID)}</li>
     *   <li>Applies each field-value pair to the entity using reflection</li>
     *   <li>Updates the entity's updatedAt timestamp automatically</li>
     *   <li>Validates the updated entity against constraints</li>
     *   <li>Persists the changes to the database</li>
     * </ol>
     *
     * <p><strong>Field Update Behavior:</strong></p>
     * <ul>
     *   <li>Only provided fields are updated (partial updates supported)</li>
     *   <li>Missing fields in the data map remain unchanged</li>
     *   <li>Null values in the data map will set fields to null</li>
     *   <li>Audit fields (id, createdAt) cannot be overridden</li>
     *   <li>Unknown fields are logged as warnings but don't cause failures</li>
     * </ul>
     *
     * <p><strong>Validation and Constraints:</strong></p>
     * <ul>
     *   <li>Entity validation occurs after all fields are updated</li>
     *   <li>Database constraints are checked during persistence</li>
     *   <li>Failed validation or constraints cause transaction rollback</li>
     *   <li>Original entity state is preserved on failure</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong> This method involves reflection
     * and entity retrieval, making it slower than direct entity updates. For
     * high-performance scenarios, consider using {@link #update(UUID, Object)}
     * with properly constructed entity instances.</p>
     *
     * @param id The UUID of the entity to update. Must not be null.
     * @param data Map containing field names as keys and new values as values. Must not be null.
     * @return The updated entity with refreshed audit fields, never null
     *
     * @throws IllegalArgumentException if id or data is null
     * @throws jakarta.persistence.EntityNotFoundException if no entity exists with the given ID
     * @throws RuntimeException if field mapping fails
     * @throws javax.validation.ConstraintViolationException if updated entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     *
     * @see #update(UUID, T)
     * @see #findByIdOrFail(UUID)
     * @see #create(Map)
     */
    T updateWithData(UUID id, Map<String, Object> data);

    /**
     * Creates multiple entities from a list of field-value data maps (Laravel-style).
     *
     * <p>This method efficiently creates multiple entities using dynamic field-value
     * pairs, combining the convenience of {@link #create(Map)} with the performance
     * benefits of batch processing. Each map in the list represents one entity
     * to be created.</p>
     *
     * <p><strong>Batch Creation Process:</strong></p>
     * <ol>
     *   <li>Iterates through each data map in the provided list</li>
     *   <li>Creates entity instances using reflection for each map</li>
     *   <li>Populates fields using the same logic as {@link #create(Map)}</li>
     *   <li>Batches all entities for efficient database insertion</li>
     *   <li>Assigns UUIDs and timestamps to all entities</li>
     * </ol>
     *
     * <p><strong>Performance Benefits:</strong></p>
     * <ul>
     *   <li>Single database transaction for all entities</li>
     *   <li>Batch INSERT operations reduce database round trips</li>
     *   <li>More efficient than multiple individual {@link #create(Map)} calls</li>
     *   <li>Optimized memory usage through batch processing</li>
     * </ul>
     *
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li>All entities are created or none are (transaction atomicity)</li>
     *   <li>If any entity fails validation, entire operation is rolled back</li>
     *   <li>Each entity receives proper audit timestamps</li>
     *   <li>Database constraints are validated for all entities</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong> If any entity in the batch fails creation
     * (validation, constraints, etc.), the entire operation is rolled back and
     * no entities are persisted to maintain data consistency.</p>
     *
     * @param dataList List of maps, each containing field names to values for one entity. Must not be null.
     * @return List of created entities with assigned IDs and audit fields, never null
     *
     * @throws IllegalArgumentException if dataList is null or contains null maps
     * @throws RuntimeException if entity creation or field mapping fails for any entity
     * @throws javax.validation.ConstraintViolationException if any entity validation fails
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     *
     * @see #create(Map)
     * @see #createAll(List)
     */
    List<T> createMany(List<Map<String, Object>> dataList);

    /**
     * Processes all entities in memory-efficient chunks for large-scale batch operations.
     *
     * <p>This method provides a memory-efficient way to process large numbers of
     * entities by breaking them into smaller, manageable chunks. It's ideal for
     * operations that need to process all entities but want to avoid loading
     * the entire dataset into memory at once.</p>
     *
     * <p><strong>Chunk Processing Flow:</strong></p>
     * <ol>
     *   <li>Determines total entity count in the database</li>
     *   <li>Calculates number of chunks needed based on chunk size</li>
     *   <li>For each chunk, loads entities using pagination</li>
     *   <li>Calls the provided processor function with each chunk</li>
     *   <li>Continues until all entities have been processed</li>
     * </ol>
     *
     * <p><strong>Memory Management Benefits:</strong></p>
     * <ul>
     *   <li>Prevents OutOfMemoryError when processing large datasets</li>
     *   <li>Allows garbage collection between chunks</li>
     *   <li>Configurable chunk size for optimal memory usage</li>
     *   <li>Suitable for datasets of any size</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Data migration and transformation operations</li>
     *   <li>Bulk updates or calculations on large datasets</li>
     *   <li>Report generation from large entity collections</li>
     *   <li>Data export operations</li>
     *   <li>Background batch processing jobs</li>
     * </ul>
     *
     * <p><strong>Chunk Size Considerations:</strong></p>
     * <ul>
     *   <li>Larger chunks = fewer database queries but more memory usage</li>
     *   <li>Smaller chunks = more database queries but less memory usage</li>
     *   <li>Recommended range: 100-1000 entities per chunk</li>
     *   <li>Consider entity size and available memory when choosing chunk size</li>
     * </ul>
     *
     * @param chunkSize Number of entities to process in each chunk. Must be positive.
     * @param processor Consumer function that processes each chunk of entities. Must not be null.
     *
     * @throws IllegalArgumentException if chunkSize <= 0 or processor is null
     *
     * @see #findAll(Pageable)
     * @see #count()
     */
    void processInChunks(int chunkSize, Consumer<List<T>> processor);

    /**
     * Finds only active entities (where deletedAt is null) using soft delete support.
     *
     * <p>This method retrieves all entities that are currently active, meaning they
     * have not been soft deleted (their deletedAt field is null). It filters out
     * any entities that have been marked as deleted using the soft delete functionality.</p>
     *
     * <p><strong>Soft Delete Integration:</strong></p>
     * <ul>
     *   <li>Only returns entities where {@code deletedAt IS NULL}</li>
     *   <li>Automatically excludes soft deleted records</li>
     *   <li>Respects the soft delete pattern without requiring explicit filtering</li>
     *   <li>Maintains referential integrity by preserving deleted entities in database</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Standard business operations that should ignore deleted records</li>
     *   <li>User interfaces that need to show only active entities</li>
     *   <li>Reports and analytics excluding deleted data</li>
     *   <li>API endpoints that should hide soft deleted entities</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>Adds WHERE clause filter which may impact query performance on large tables</li>
     *   <li>Consider adding database index on deletedAt column for better performance</li>
     *   <li>For very large datasets, consider using {@link #findAll(Pageable)} with custom specifications</li>
     * </ul>
     *
     * <p><strong>Alternative Methods:</strong></p>
     * <ul>
     *   <li>Use {@link #findAll()} to include soft deleted entities</li>
     *   <li>Use {@link #findDeleted()} to get only soft deleted entities</li>
     *   <li>Use {@link #findWithDeleted()} for explicit inclusion of both active and deleted entities</li>
     * </ul>
     *
     * @return List of active entities (deletedAt is null), never null but may be empty
     *
     * @see #findDeleted()
     * @see #findWithDeleted()
     * @see com.bunkermuseum.membermanagement.model.base.Model#isActive()
     * @see com.bunkermuseum.membermanagement.model.base.Model#delete()
     */
    List<T> findActive();

    /**
     * Finds only soft deleted entities (where deletedAt is not null).
     *
     * <p>This method retrieves all entities that have been soft deleted, meaning they
     * have a non-null deletedAt timestamp. These entities are typically hidden from
     * normal business operations but preserved for audit trails, data recovery,
     * and compliance purposes.</p>
     *
     * <p><strong>Soft Delete Integration:</strong></p>
     * <ul>
     *   <li>Only returns entities where {@code deletedAt IS NOT NULL}</li>
     *   <li>Shows entities that were deleted using {@code entity.delete()} method</li>
     *   <li>Includes deletion timestamp information for each entity</li>
     *   <li>Preserves complete entity data despite deletion status</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Administrative interfaces for viewing deleted records</li>
     *   <li>Data recovery operations and entity restoration</li>
     *   <li>Audit trails and compliance reporting</li>
     *   <li>Debugging and investigating data integrity issues</li>
     *   <li>Analytics on deleted entities and deletion patterns</li>
     * </ul>
     *
     * <p><strong>Data Recovery Pattern:</strong></p>
     * <pre>{@code
     * // Find deleted entities and restore if needed
     * List<User> deletedUsers = userRepository.findDeleted();
     * for (User user : deletedUsers) {
     *     if (shouldRestore(user)) {
     *         user.restore();
     *         userRepository.update(user.getId(), user);
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>May return fewer results than active entities in typical usage</li>
     *   <li>Consider indexing deletedAt column for optimal query performance</li>
     *   <li>Useful for maintenance operations and administrative tasks</li>
     * </ul>
     *
     * @return List of soft deleted entities (deletedAt is not null), never null but may be empty
     *
     * @see #findActive()
     * @see #findWithDeleted()
     * @see com.bunkermuseum.membermanagement.model.base.Model#isDeleted()
     * @see com.bunkermuseum.membermanagement.model.base.Model#restore()
     */
    List<T> findDeleted();

    /**
     * Finds all entities including both active and soft deleted ones.
     *
     * <p>This method retrieves the complete dataset of entities, including both active
     * entities (deletedAt is null) and soft deleted entities (deletedAt is not null).
     * It provides unrestricted access to all entity data regardless of deletion status.</p>
     *
     * <p><strong>Complete Dataset Access:</strong></p>
     * <ul>
     *   <li>Returns all entities without any deletedAt filtering</li>
     *   <li>Includes both active and soft deleted entities in the result</li>
     *   <li>Equivalent to calling {@link #findAll()} but with explicit intention</li>
     *   <li>Provides full dataset visibility for administrative purposes</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Administrative dashboards showing complete entity counts</li>
     *   <li>Data export operations requiring full dataset</li>
     *   <li>Migration scripts that need to process all entities</li>
     *   <li>Backup and archival operations</li>
     *   <li>Comprehensive reporting including historical data</li>
     *   <li>Database maintenance and cleanup operations</li>
     * </ul>
     *
     * <p><strong>Data Processing Pattern:</strong></p>
     * <pre>{@code
     * // Process all entities with deletion status handling
     * List<User> allUsers = userRepository.findWithDeleted();
     * for (User user : allUsers) {
     *     if (user.isActive()) {
     *         processActiveUser(user);
     *     } else {
     *         processDeletedUser(user);
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>May return large datasets as it includes all entities</li>
     *   <li>Consider using {@link #processInChunks(int, Consumer)} for large datasets</li>
     *   <li>Most efficient method for complete dataset access</li>
     *   <li>No additional WHERE clause filtering improves query performance</li>
     * </ul>
     *
     * <p><strong>Comparison with Other Methods:</strong></p>
     * <ul>
     *   <li>{@link #findActive()} - subset of this method (active entities only)</li>
     *   <li>{@link #findDeleted()} - subset of this method (deleted entities only)</li>
     *   <li>{@link #findAll()} - typically equivalent but less explicit about intention</li>
     * </ul>
     *
     * @return List of all entities both active and deleted, never null but may be empty
     *
     * @see #findActive()
     * @see #findDeleted()
     * @see #findAll()
     * @see #processInChunks(int, Consumer)
     * @see com.bunkermuseum.membermanagement.model.base.Model#isActive()
     * @see com.bunkermuseum.membermanagement.model.base.Model#isDeleted()
     */
    List<T> findWithDeleted();
}