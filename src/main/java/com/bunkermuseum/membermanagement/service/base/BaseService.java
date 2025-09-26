package com.bunkermuseum.membermanagement.service.base;

import com.bunkermuseum.membermanagement.model.base.Model;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.contract.BaseServiceContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Consumer;

/**
 * <p>This abstract class provides the concrete implementation for the BaseServiceContract,
 * serving as the foundation for all service implementations in the application.</p>
 *
 * @param <T> The entity type extending Model
 * @param <R> The repository type extending BaseRepositoryContract
 */
@Transactional(readOnly = true)
public abstract class BaseService<T extends Model, R extends BaseRepositoryContract<T>>
        implements BaseServiceContract<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final R repository;

    /**
     * BaseService constructor.
     *
     * @param repository The repository instance for data access operations
     *
     * @author Philipp Borkovic
     */
    protected BaseService(R repository) {
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
        return executeWithLogging("Finding by ID or fail: " + id,
            () -> repository.findByIdOrFail(id));
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public T create(T entity) {
        return executeWithLogging("Creating entity", () -> {
            validateForCreate(entity);
            applyBusinessRulesForCreate(entity);
            T created = repository.create(entity);
            afterCreate(created);
            return created;
        });
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
            validateForUpdate(id, entity);
            applyBusinessRulesForUpdate(id, entity);
            T updated = repository.update(id, entity);
            afterUpdate(updated);
            return updated;
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
            validateForDelete(id);
            applyBusinessRulesForDelete(id);
            boolean deleted = repository.deleteById(id);
            if (deleted) {
                afterDelete(id);
            }
            return deleted;
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
    @Transactional
    public List<T> createAll(List<T> entities) {
        return executeWithLogging("Creating multiple entities", () -> {
            for (T entity : entities) {
                validateForCreate(entity);
                applyBusinessRulesForCreate(entity);
            }
            List<T> created = repository.createAll(entities);
            for (T entity : created) {
                afterCreate(entity);
            }

            return created;
        });
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
    public T create(Map<String, Object> data) {
        return executeWithLogging("Creating entity with data", () -> {
            T entity = repository.create(data);
            validateForCreate(entity);
            applyBusinessRulesForCreate(entity);
            afterCreate(entity);

            return entity;
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
            T updated = repository.updateWithData(id, data);
            validateForUpdate(id, updated);
            applyBusinessRulesForUpdate(id, updated);
            afterUpdate(updated);

            return updated;
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
            List<T> entities = repository.createMany(dataList);
            for (T entity : entities) {
                validateForCreate(entity);
                applyBusinessRulesForCreate(entity);
                afterCreate(entity);
            }

            return entities;
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
            repository.processInChunks(chunkSize, processor);

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
        return executeWithLogging("Finding active entities",
            () -> repository.findActive(), Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findDeleted() {
        return executeWithLogging("Finding deleted entities",
            () -> repository.findDeleted(), Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<T> findWithDeleted() {
        return executeWithLogging("Finding all entities including deleted",
            () -> repository.findWithDeleted(), Collections.emptyList());
    }

    /**
     * Gets the entity name for logging and error message purposes.
     *
     * <p>This method provides a human-readable entity name that is used throughout
     * the service implementation for logging operations, error messages, and
     * debugging information. Concrete service implementations should override
     * this method to provide meaningful entity-specific names.</p>
     *
     * <p><strong>Implementation Requirements:</strong></p>
     * <ul>
     *   <li>Return a descriptive name for the entity type (e.g., "User", "Product", "Order")</li>
     *   <li>Use singular form ("User" not "Users")</li>
     *   <li>Keep the name concise but descriptive</li>
     *   <li>Avoid technical terms like "Entity" or "Model" in the name</li>
     * </ul>
     *
     * <p><strong>Override Example:</strong></p>
     * <pre>{@code
     * @Override
     * protected String getEntityName() {
     *     return "User";
     * }
     * }</pre>
     *
     * @return The entity name for logging purposes, defaults to "Entity"
     *
     * @see #executeWithLogging(String, ServiceOperation)
     * @see #executeWithLogging(String, ServiceOperation, Object)
     *
     * @author Philipp Borkovic
     */
    protected String getEntityName() {
        return "Entity";
    }

    /**
     * Executes a service operation with consistent logging and error handling.
     *
     * <p>This method provides a standardized approach to executing service operations
     * with comprehensive logging and error handling. It wraps the operation execution
     * with debug logging for successful operations and error logging for failures,
     * ensuring consistent behavior across all service methods.</p>
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
     * <p><strong>Usage Pattern:</strong></p>
     * <pre>{@code
     * return executeWithLogging("Finding by ID", () -> {
     *     return repository.findById(id);
     * });
     * }</pre>
     *
     * <p><strong>Exception Propagation:</strong> This method will always propagate
     * exceptions as RuntimeExceptions. Use the overloaded version with a default
     * value if you need graceful degradation.</p>
     *
     * @param operation Description of the operation for logging (e.g., "Finding by ID", "Creating entity")
     * @param supplier The service operation to execute, wrapped in a functional interface
     * @param <R> The return type of the operation
     * @return The result of the operation execution, never null unless operation returns null
     *
     * @throws RuntimeException if the operation fails, wrapping the original exception
     *
     * @see #executeWithLogging(String, ServiceOperation, Object)
     * @see #getEntityName()
     * @see ServiceOperation
     *
     * @author Philipp Borkovic
     */
    protected <R> R executeWithLogging(String operation, ServiceOperation<R> supplier) {
        try {
            logger.debug("{} {} entities", operation, getEntityName());
            return supplier.execute();
        } catch (Exception e) {
            logger.error("Error {} {} entities: {}", operation.toLowerCase(), getEntityName(), e.getMessage(), e);
            throw new RuntimeException("Failed to execute operation: " + operation, e);
        }
    }

    /**
     * Executes a service operation with consistent logging and error handling,
     * returning a default value on error.
     *
     * <p>This method provides the same standardized logging and error handling as
     * {@link #executeWithLogging(String, ServiceOperation)}, but offers graceful
     * degradation by returning a specified default value when operations fail instead
     * of throwing exceptions. This approach is useful for operations where failure
     * should not interrupt the application flow.</p>
     *
     * <p><strong>Graceful Degradation Benefits:</strong></p>
     * <ul>
     *   <li>Prevents application crashes from non-critical operation failures</li>
     *   <li>Allows continued execution with sensible fallback values</li>
     *   <li>Maintains system stability in the presence of business logic issues</li>
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
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Returns 0 if count operation fails instead of throwing exception
     * return executeWithLogging("Counting entities", () -> {
     *     return repository.count();
     * }, 0L);
     * }</pre>
     *
     * @param operation Description of the operation for logging (e.g., "Checking existence", "Counting entities")
     * @param supplier The service operation to execute, wrapped in a functional interface
     * @param defaultValue Value to return if the operation fails, should represent a safe fallback
     * @param <R> The return type of the operation and default value
     * @return The result of successful operation execution, or defaultValue if operation fails
     *
     * @see #executeWithLogging(String, ServiceOperation)
     * @see #getEntityName()
     * @see ServiceOperation
     *
     * @author Philipp Borkovic
     */
    protected <R> R executeWithLogging(String operation, ServiceOperation<R> supplier, R defaultValue) {
        try {
            logger.debug("{} {} entities", operation, getEntityName());

            return supplier.execute();
        } catch (Exception e) {
            logger.error("Error {} {} entities: {}", operation.toLowerCase(), getEntityName(), e.getMessage(), e);

            return defaultValue;
        }
    }

    /**
     * Functional interface for service operations.
     *
     * @param <T> The return type of the operation
     *
     * @author Philipp Borkovic
     */
    @FunctionalInterface
    protected interface ServiceOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Applies business rules before entity creation.
     *
     * <p>This method is called after basic validation but before persistence
     * to apply any business-specific rules, transformations, or preparations
     * needed for entity creation. Concrete services should override this method
     * to implement entity-specific business logic.</p>
     *
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Setting default values based on business rules</li>
     *   <li>Generating computed fields or codes</li>
     *   <li>Applying business transformations to data</li>
     *   <li>Checking business constraints and dependencies</li>
     * </ul>
     *
     * <p><strong>Default Implementation:</strong> Does nothing. Concrete services
     * should override to provide meaningful business logic.</p>
     *
     * @param entity The entity being created, after basic validation
     *
     * @throws IllegalArgumentException if business rules prevent creation
     *
     * @see #create(T)
     * @see #validateForCreate(T)
     * @see #afterCreate(T)
     *
     * @author Philipp Borkovic
     */
    protected void applyBusinessRulesForCreate(T entity) {
        // Override in concrete services for entity-specific logic
    }

    /**
     * Applies business rules before entity update.
     *
     * <p>This method is called after basic validation but before persistence
     * to apply any business-specific rules, transformations, or preparations
     * needed for entity updates. Concrete services should override this method
     * to implement entity-specific business logic.</p>
     *
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Updating computed fields based on changes</li>
     *   <li>Applying business transformations to modified data</li>
     *   <li>Checking business constraints and dependencies</li>
     *   <li>Logging business-relevant changes</li>
     * </ul>
     *
     * <p><strong>Default Implementation:</strong> Does nothing. Concrete services
     * should override to provide meaningful business logic.</p>
     *
     * @param id The UUID of the entity being updated
     * @param entity The entity containing updated data, after basic validation
     *
     * @throws IllegalArgumentException if business rules prevent update
     *
     * @see #update(UUID, T)
     * @see #validateForUpdate(UUID, T)
     * @see #afterUpdate(T)
     *
     * @author Philipp Borkovic
     */
    protected void applyBusinessRulesForUpdate(UUID id, T entity) {
        // Override in concrete services for entity-specific logic
    }

    /**
     * Applies business rules before entity deletion.
     *
     * <p>This method is called after basic validation but before deletion
     * to apply any business-specific rules or checks needed before removing
     * an entity. Concrete services should override this method to implement
     * entity-specific business logic.</p>
     *
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Checking for dependent entities that would be orphaned</li>
     *   <li>Applying business rules about what can be deleted</li>
     *   <li>Logging business-relevant deletions</li>
     *   <li>Preparing related entities for the deletion</li>
     * </ul>
     *
     * @param id The UUID of the entity being deleted
     *
     * @throws IllegalArgumentException if business rules prevent deletion
     *
     * @see #deleteById(UUID)
     * @see #validateForDelete(UUID)
     * @see #afterDelete(UUID)
     *
     * @author Philipp Borkovic
     */
    protected void applyBusinessRulesForDelete(UUID id) {
        // Override in concrete services for entity-specific logic
    }

    /**
     * Performs post-creation business logic.
     *
     * <p>This method is called after successful entity creation to perform
     * any business logic that should happen after persistence. Concrete services
     * should override this method to implement entity-specific post-creation logic.</p>
     *
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Sending notifications about new entities</li>
     *   <li>Creating related entities or associations</li>
     *   <li>Updating caches or derived data</li>
     *   <li>Logging business events</li>
     * </ul>
     *
     * <p><strong>Default Implementation:</strong> Does nothing. Concrete services
     * should override to provide meaningful business logic.</p>
     *
     * @param entity The successfully created entity with assigned ID
     *
     * @see #create(T)
     * @see #applyBusinessRulesForCreate(T)
     *
     * @author Philipp Borkovic
     */
    protected void afterCreate(T entity) {
        // Override in concrete services for entity-specific logic
    }

    /**
     * Performs post-update business logic.
     *
     * <p>This method is called after successful entity update to perform
     * any business logic that should happen after persistence. Concrete services
     * should override this method to implement entity-specific post-update logic.</p>
     *
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Sending notifications about entity changes</li>
     *   <li>Updating related entities or associations</li>
     *   <li>Invalidating caches or updating derived data</li>
     *   <li>Logging business events</li>
     * </ul>
     *
     * <p><strong>Default Implementation:</strong> Does nothing. Concrete services
     * should override to provide meaningful business logic.</p>
     *
     * @param entity The successfully updated entity
     *
     * @see #update(UUID, T)
     * @see #applyBusinessRulesForUpdate(UUID, T)
     *
     * @author Philipp Borkovic
     */
    protected void afterUpdate(T entity) {
        // Override in concrete services for entity-specific logic
    }

    /**
     * Performs post-deletion business logic.
     *
     * <p>This method is called after successful entity deletion to perform
     * any business logic that should happen after removal. Concrete services
     * should override this method to implement entity-specific post-deletion logic.</p>
     *
     * <p><strong>Common Use Cases:</strong></p>
     * <ul>
     *   <li>Sending notifications about entity deletion</li>
     *   <li>Cleaning up related entities or associations</li>
     *   <li>Updating caches or derived data</li>
     *   <li>Logging business events</li>
     * </ul>
     *
     * <p><strong>Default Implementation:</strong> Does nothing. Concrete services
     * should override to provide meaningful business logic.</p>
     *
     * @param id The UUID of the successfully deleted entity
     *
     * @see #deleteById(UUID)
     * @see #applyBusinessRulesForDelete(UUID)
     *
     * @author Philipp Borkovic
     */
    protected void afterDelete(UUID id) {
        // Override in concrete services for entity-specific logic
    }

}