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
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * @Repository
 * public class UserRepositoryImpl extends BaseRepository<User, UserRepository> {
 *     public UserRepositoryImpl(UserRepository repository) {
 *         super(repository);
 *     }
 *
 *     @Override
 *     protected String getEntityName() {
 *         return "User";
 *     }
 *
 *     // Add entity-specific methods here
 *     public List<User> findActiveUsers() {
 *         return findWhere((root, query, criteriaBuilder) ->
 *             criteriaBuilder.isNull(root.get("deletedAt")));
 *     }
 * }
 * }</pre>
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
 * @author Patrick Borkovic
 * @version 1.0
 * @since 1.0
 *
 * @see com.bunkermuseum.membermanagement.model.base.Model
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor
 * @see org.springframework.data.domain.Page
 * @see org.springframework.data.domain.Pageable
 */
public interface BaseRepositoryInterface<T extends Model> {

    /**
     * Retrieves all entities.
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Finds an entity by its ID.
     *
     * @param id The entity ID
     * @return Optional containing the entity if found, empty otherwise
     */
    Optional<T> findById(UUID id);

    /**
     * Finds an entity by ID or throws an exception if not found.
     *
     * @param id The entity ID
     * @return The entity
     * @throws jakarta.persistence.EntityNotFoundException if entity not found
     */
    T findByIdOrFail(UUID id);

    /**
     * Finds the first entity.
     *
     * @return Optional containing the first entity if exists, empty otherwise
     */
    Optional<T> findFirst();

    /**
     * Creates a new entity.
     *
     * @param entity The entity to create
     * @return The created entity
     */
    T create(T entity);

    /**
     * Updates an entity by ID.
     *
     * @param id The entity ID
     * @param entity The entity with updated data
     * @return The updated entity
     * @throws jakarta.persistence.EntityNotFoundException if entity not found
     */
    T update(UUID id, T entity);

    /**
     * Deletes an entity by ID.
     *
     * @param id The entity ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(UUID id);

    /**
     * Retrieves entities with pagination.
     *
     * @param pageable Pagination information
     * @return Page of entities
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Counts all entities.
     *
     * @return Total number of entities
     */
    long count();

    /**
     * Finds entities by their IDs.
     *
     * @param ids List of entity IDs
     * @return List of found entities
     */
    List<T> findAllById(List<UUID> ids);

    /**
     * Creates multiple entities.
     *
     * @param entities List of entities to create
     * @return List of created entities
     */
    List<T> createAll(List<T> entities);

    /**
     * Checks if an entity exists by ID.
     *
     * @param id The entity ID
     * @return true if exists, false otherwise
     */
    boolean existsById(UUID id);

    /**
     * Creates an entity and flushes changes immediately.
     *
     * @param entity The entity to create
     * @return The created entity
     */
    T createAndFlush(T entity);

    /**
     * Flushes all pending changes to the database.
     */
    void flush();

    // Laravel-style additional methods

    /**
     * Creates a new entity from field-value data.
     *
     * @param data Map of field names to values for the new entity
     * @return The created entity
     */
    T create(Map<String, Object> data);

    /**
     * Updates an entity with new field-value data.
     *
     * @param id The entity ID
     * @param data Map of field names to values for updating
     * @return The updated entity
     * @throws jakarta.persistence.EntityNotFoundException if entity not found
     */
    T updateWithData(UUID id, Map<String, Object> data);

    /**
     * Creates multiple entities from a list of field-value data.
     *
     * @param dataList List of maps containing field names to values
     * @return List of created entities
     */
    List<T> createMany(List<Map<String, Object>> dataList);

    /**
     * Processes entities in chunks for memory-efficient batch operations.
     *
     * @param chunkSize Number of entities to process at once
     * @param processor Function to process each chunk
     */
    void processInChunks(int chunkSize, Consumer<List<T>> processor);

    /**
     * Finds only active entities (where deletedAt is null) using soft delete support.
     *
     * @return List of active entities
     */
    List<T> findActive();

    /**
     * Finds only soft deleted entities (where deletedAt is not null).
     *
     * @return List of soft deleted entities
     */
    List<T> findDeleted();

    /**
     * Finds all entities including soft deleted ones.
     *
     * @return List of all entities (active and deleted)
     */
    List<T> findWithDeleted();
}