package com.bunkermuseum.membermanagement.repository.base;

import com.bunkermuseum.membermanagement.model.base.Model;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryInterface;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>This abstract class provides the concrete implementation for the BaseRepositoryInterface,
 * serving as the foundation for all repository implementations in the application.</p>
 *
 * @param <T> The entity type extending Model
 * @param <R> The Spring Data JPA repository type
 */
@Transactional(readOnly = true)
public abstract class BaseRepository<T extends Model, R extends JpaRepository<T, UUID>>
        implements BaseRepositoryInterface<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final R repository;

    /**
     * BaseRepository constructor.
     *
     * @param repository The Spring Data JPA repository instance
     */
    protected BaseRepository(R repository) {
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findAll() {
        return executeWithLogging("Fetching all", () -> repository.findAll());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Optional<T> findById(UUID id) {
        return executeWithLogging("Finding by ID: " + id,
            () -> repository.findById(id),
            Optional.empty());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public T findByIdOrFail(UUID id) {
        return executeWithLogging("Finding by ID or fail: " + id, () -> {
            return repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("%s with ID %s not found", getEntityName(), id)));
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Optional<T> findFirst() {
        return executeWithLogging("Finding first", () -> {
            Page<T> page = repository.findAll(PageRequest.of(0, 1));

            return page.hasContent() ? Optional.of(page.getContent().get(0)) : Optional.empty();
        }, Optional.empty());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public T create(T entity) {
        return executeWithLogging("Creating entity: " + entity.getId(),
            () -> repository.save(entity));
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public T update(UUID id, T entity) {
        return executeWithLogging("Updating entity: " + id, () -> {
            if (!repository.existsById(id)) {
                throw new EntityNotFoundException(
                        String.format("%s with ID %s not found", getEntityName(), id));
            }

            return repository.save(entity);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public boolean deleteById(UUID id) {
        return executeWithLogging("Deleting entity: " + id, () -> {
            if (!repository.existsById(id)) {
                logger.warn("{} entity not found for deletion with ID: {}", getEntityName(), id);
                return false;
            }
            repository.deleteById(id);

            return true;
        }, false);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        return executeWithLogging("Finding with pagination", () -> repository.findAll(pageable));
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public long count() {
        return executeWithLogging("Counting all entities", () -> repository.count(), 0L);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findAllById(List<UUID> ids) {
        return executeWithLogging("Finding by IDs", () -> repository.findAllById(ids));
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public List<T> createAll(List<T> entities) {
        return executeWithLogging("Creating multiple entities", () -> repository.saveAll(entities));
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public boolean existsById(UUID id) {
        return executeWithLogging("Checking existence by ID: " + id,
            () -> repository.existsById(id), false);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public T createAndFlush(T entity) {
        return executeWithLogging("Creating and flushing entity: " + entity.getId(),
            () -> repository.saveAndFlush(entity));
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public void flush() {
        executeWithLogging("Flushing repository", () -> {
            repository.flush();
            return null;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public T create(Map<String, Object> data) {
        return executeWithLogging("Creating entity with data", () -> {
            T entity = createEntityFromData(data);

            return repository.save(entity);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public T updateWithData(UUID id, Map<String, Object> data) {
        return executeWithLogging("Updating entity with data: " + id, () -> {
            T entity = findByIdOrFail(id);
            updateEntityFromData(entity, data);

            return repository.save(entity);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public List<T> createMany(List<Map<String, Object>> dataList) {
        return executeWithLogging("Creating many entities", () -> {
            List<T> entities = dataList.stream()
                .map(this::createEntityFromData)
                .collect(Collectors.toList());

            return repository.saveAll(entities);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public void processInChunks(int chunkSize, Consumer<List<T>> processor) {
        executeWithLogging("Processing in chunks", () -> {
            long totalCount = repository.count();
            int totalPages = (int) Math.ceil((double) totalCount / chunkSize);

            for (int page = 0; page < totalPages; page++) {
                Pageable pageable = PageRequest.of(page, chunkSize);
                Page<T> chunk = repository.findAll(pageable);
                processor.accept(chunk.getContent());
            }

            return null;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findActive() {
        return executeWithLogging("Finding active entities", () -> {
            return repository.findAll().stream()
                    .filter(entity -> entity.deletedAt() == null)
                    .collect(Collectors.toList());
        }, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findDeleted() {
        return executeWithLogging("Finding deleted entities", () -> {
            return repository.findAll().stream()
                    .filter(entity -> entity.deletedAt() != null)
                    .collect(Collectors.toList());
        }, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findWithDeleted() {
        return executeWithLogging("Finding all entities including deleted",
            () -> repository.findAll(), Collections.emptyList());
    }

    /**
     * Gets the entity name for logging and error message purposes.
     *
     * <p>This method provides a human-readable entity name that is used throughout
     * the repository implementation for logging operations, error messages, and
     * debugging information. Concrete repository implementations should override
     * this method to provide meaningful entity-specific names.</p>
     *
     * <p><strong>Default Implementation:</strong> Returns "Entity" as a generic fallback.
     * While functional, this generic name is less helpful for logging and debugging.
     * Concrete implementations are strongly encouraged to override this method.</p>
     *
     * @return The entity name for logging purposes, defaults to "Entity"
     *
     * @see #executeWithLogging(String, RepositoryOperation)
     * @see #executeWithLogging(String, RepositoryOperation, Object)
     *
     * @author Philipp Borkovic
     */
    protected String getEntityName() {
        return "Entity";
    }

    /**
     * Executes a repository operation with consistent logging and error handling.
     *
     * <p>This method provides a standardized approach to executing repository operations
     * with comprehensive logging and error handling. It wraps the operation execution
     * with debug logging for successful operations and error logging for failures,
     * ensuring consistent behavior across all repository methods.</p>
     *
     * <p><strong>Logging Behavior:</strong></p>
     * <ul>
     *   <li><strong>Debug Level:</strong> Logs operation start with entity name and operation description</li>
     *   <li><strong>Error Level:</strong> Logs detailed error information including stack trace on failure</li>
     *   <li><strong>Operation Context:</strong> Uses {@link #getEntityName()} to provide entity-specific context</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Catches all exceptions thrown by the operation</li>
     *   <li>Logs the complete error details including stack trace</li>
     *   <li>Wraps the original exception in a RuntimeException with descriptive message</li>
     *   <li>Preserves the original exception as the cause for debugging</li>
     * </ul>
     *
     * <p><strong>Exception Propagation:</strong> This method will always propagate
     * exceptions as RuntimeExceptions. Use the overloaded version with a default
     * value if you need graceful degradation.</p>
     *
     * @param operation Description of the operation for logging (e.g., "Finding by ID", "Creating entity")
     * @param supplier The repository operation to execute, wrapped in a functional interface
     * @param <R> The return type of the operation
     * @return The result of the operation execution, never null unless operation returns null
     *
     * @throws RuntimeException if the operation fails, wrapping the original exception
     *
     * @see #executeWithLogging(String, RepositoryOperation, Object)
     * @see #getEntityName()
     * @see RepositoryOperation
     *
     * @author Philipp Borkovic
     */
    protected <R> R executeWithLogging(String operation, RepositoryOperation<R> supplier) {
        try {
            logger.debug("{} {} entities", operation, getEntityName());

            return supplier.execute();
        } catch (Exception e) {
            logger.error("Error {} {} entities: {}", operation.toLowerCase(), getEntityName(), e.getMessage(), e);

            throw new RuntimeException("Failed to execute operation: " + operation, e);
        }
    }

    /**
     * Executes a repository operation with consistent logging and error handling,
     * returning a default value on error.
     *
     * <p>This method provides the same standardized logging and error handling as
     * {@link #executeWithLogging(String, RepositoryOperation)}, but offers graceful
     * degradation by returning a specified default value when operations fail instead
     * of throwing exceptions. This approach is useful for operations where failure
     * should not interrupt the application flow.</p>
     *
     * <p><strong>Graceful Degradation Benefits:</strong></p>
     * <ul>
     *   <li>Prevents application crashes from non-critical operation failures</li>
     *   <li>Allows continued execution with sensible fallback values</li>
     *   <li>Maintains system stability in the presence of database issues</li>
     *   <li>Enables defensive programming patterns</li>
     * </ul>
     *
     * <p><strong>Appropriate Use Cases:</strong></p>
     * <ul>
     *   <li>Optional data retrieval where missing data is acceptable</li>
     *   <li>Count operations that can default to 0</li>
     *   <li>Existence checks that can default to false</li>
     *   <li>Cache-like operations where fallback data is available</li>
     * </ul>
     *
     * <p><strong>Error Handling Differences:</strong></p>
     * <ul>
     *   <li>Logs errors but does not propagate exceptions</li>
     *   <li>Returns the provided default value on any failure</li>
     *   <li>Maintains the same debug logging for successful operations</li>
     *   <li>Still preserves error details in logs for debugging</li>
     * </ul>
     *
     * @param operation Description of the operation for logging (e.g., "Checking existence", "Counting entities")
     * @param supplier The repository operation to execute, wrapped in a functional interface
     * @param defaultValue Value to return if the operation fails, should represent a safe fallback
     * @param <R> The return type of the operation and default value
     * @return The result of successful operation execution, or defaultValue if operation fails
     *
     * @see #executeWithLogging(String, RepositoryOperation)
     * @see #getEntityName()
     * @see RepositoryOperation
     *
     * @author Philipp Borkovic
     */
    protected <R> R executeWithLogging(String operation, RepositoryOperation<R> supplier, R defaultValue) {
        try {
            logger.debug("{} {} entities", operation, getEntityName());

            return supplier.execute();
        } catch (Exception e) {
            logger.error("Error {} {} entities: {}", operation.toLowerCase(), getEntityName(), e.getMessage(), e);

            return defaultValue;
        }
    }

    /**
     * Functional interface for repository operations.
     *
     * @author Philipp Borkovic
     */
    @FunctionalInterface
    protected interface RepositoryOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Creates a new entity instance from field-value data using reflection.
     *
     * <p>This method provides a generic approach to creating entity instances from
     * dynamic field-value pairs using Java reflection. It extracts the concrete entity
     * type from generic parameters, instantiates a new entity, and populates its fields
     * with the provided data map.</p>
     *
     * <p><strong>Creation Process:</strong></p>
     * <ol>
     *   <li>Extracts the concrete entity class from generic type parameters</li>
     *   <li>Creates a new instance using the default constructor</li>
     *   <li>Delegates field population to {@link #updateEntityFromData(Object, Map)}</li>
     *   <li>Returns the fully populated entity instance</li>
     * </ol>
     *
     * <p><strong>Type Resolution:</strong></p>
     * <ul>
     *   <li>Uses reflection to determine the concrete entity type at runtime</li>
     *   <li>Extracts type information from the generic superclass declaration</li>
     *   <li>Handles the complexity of Java's type erasure</li>
     *   <li>Requires proper generic type declaration in concrete implementations</li>
     * </ul>
     *
     * <p><strong>Constructor Requirements:</strong></p>
     * <ul>
     *   <li>Entity must have a public or accessible default (no-argument) constructor</li>
     *   <li>Constructor should not throw exceptions during instantiation</li>
     *   <li>Entity should be in a valid state after default construction</li>
     * </ul>
     *
     * @param data Map containing field names as keys and field values as values. Must not be null.
     * @return New entity instance with populated fields, never null
     *
     * @throws IllegalArgumentException if data is null
     * @throws RuntimeException if entity instantiation or field population fails
     *
     * @see #updateEntityFromData(Object, Map)
     * @see #create(Map)
     *
     * @author Philipp Borkovic
     */
    protected T createEntityFromData(Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) ((java.lang.reflect.ParameterizedType)
                getClass().getGenericSuperclass()).getActualTypeArguments()[0];

            T entity = entityClass.getDeclaredConstructor().newInstance();
            updateEntityFromData(entity, data);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity from data", e);
        }
    }

    /**
     * Updates an entity instance from field-value data using reflection.
     *
     * <p>This method provides a generic approach to updating existing entity instances
     * with dynamic field-value pairs using Java reflection. It iterates through the
     * provided data map and attempts to set each field value on the target entity,
     * handling both direct fields and inherited fields from parent classes.</p>
     *
     * <p><strong>Field Update Process:</strong></p>
     * <ol>
     *   <li>Iterates through each field-value pair in the data map</li>
     *   <li>Attempts to find the field in the entity's class</li>
     *   <li>If not found, searches in the entity's superclass (e.g., Model base class)</li>
     *   <li>Makes the field accessible if it's private or protected</li>
     *   <li>Sets the field value directly using reflection</li>
     *   <li>Logs warnings for fields that cannot be found</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Unknown fields generate warning logs but don't stop processing</li>
     *   <li>Field access errors (wrong types, etc.) cause RuntimeException</li>
     *   <li>Preserves partial updates - fields set before error remain changed</li>
     *   <li>Original exception is wrapped with descriptive message</li>
     * </ul>
     *
     * @param entity The entity instance to update with new field values. Must not be null.
     * @param data Map containing field names as keys and new values as values. Must not be null.
     *
     * @throws IllegalArgumentException if entity or data is null
     * @throws RuntimeException if field access or assignment fails
     *
     * @see #createEntityFromData(Map)
     * @see #updateWithData(UUID, Map)
     *
     * @author Philipp Borkovic
     */
    protected void updateEntityFromData(T entity, Map<String, Object> data) {
        try {
            Class<?> entityClass = entity.getClass();

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    Field field = entityClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(entity, value);
                } catch (NoSuchFieldException e) {
                    try {
                        Field field = entityClass.getSuperclass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(entity, value);
                    } catch (NoSuchFieldException ex) {
                        logger.warn("Field '{}' not found in entity class {}", fieldName, entityClass.getSimpleName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update entity from data", e);
        }
    }
}