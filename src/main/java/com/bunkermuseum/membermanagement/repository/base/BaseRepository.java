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
 * {@inheritDoc}
 *
 * <p>This abstract class provides the concrete implementation for the BaseRepositoryInterface,
 * serving as the foundation for all repository implementations in the application.</p>
 *
 * @param <T> The entity type extending Model
 * @param <R> The Spring Data JPA repository type
 *
 * @author Patrick Borkovic
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

    @Override
    public List<T> findAll() {
        return executeWithLogging("Fetching all", () -> repository.findAll());
    }

    @Override
    public Optional<T> findById(UUID id) {
        return executeWithLogging("Finding by ID: " + id,
            () -> repository.findById(id),
            Optional.empty());
    }

    @Override
    public T findByIdOrFail(UUID id) {
        return executeWithLogging("Finding by ID or fail: " + id, () -> {
            return repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("%s with ID %s not found", getEntityName(), id)));
        });
    }

    @Override
    public Optional<T> findFirst() {
        return executeWithLogging("Finding first", () -> {
            Page<T> page = repository.findAll(PageRequest.of(0, 1));
            return page.hasContent() ? Optional.of(page.getContent().get(0)) : Optional.empty();
        }, Optional.empty());
    }

    @Override
    @Transactional
    public T create(T entity) {
        return executeWithLogging("Creating entity: " + entity.getId(),
            () -> repository.save(entity));
    }

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

    @Override
    public Page<T> findAll(Pageable pageable) {
        return executeWithLogging("Finding with pagination", () -> repository.findAll(pageable));
    }

    @Override
    public long count() {
        return executeWithLogging("Counting all entities", () -> repository.count(), 0L);
    }

    @Override
    public List<T> findAllById(List<UUID> ids) {
        return executeWithLogging("Finding by IDs", () -> repository.findAllById(ids));
    }

    @Override
    @Transactional
    public List<T> createAll(List<T> entities) {
        return executeWithLogging("Creating multiple entities", () -> repository.saveAll(entities));
    }

    @Override
    public boolean existsById(UUID id) {
        return executeWithLogging("Checking existence by ID: " + id,
            () -> repository.existsById(id), false);
    }

    @Override
    @Transactional
    public T createAndFlush(T entity) {
        return executeWithLogging("Creating and flushing entity: " + entity.getId(),
            () -> repository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public void flush() {
        executeWithLogging("Flushing repository", () -> {
            repository.flush();
            return null;
        });
    }

    // Laravel-style additional methods implementation

    @Override
    @Transactional
    public T create(Map<String, Object> data) {
        return executeWithLogging("Creating entity with data", () -> {
            T entity = createEntityFromData(data);
            return repository.save(entity);
        });
    }

    @Override
    @Transactional
    public T updateWithData(UUID id, Map<String, Object> data) {
        return executeWithLogging("Updating entity with data: " + id, () -> {
            T entity = findByIdOrFail(id);
            updateEntityFromData(entity, data);
            return repository.save(entity);
        });
    }

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

    @Override
    public List<T> findActive() {
        return executeWithLogging("Finding active entities", () -> {
            return repository.findAll().stream()
                    .filter(entity -> entity.deletedAt() == null)
                    .collect(Collectors.toList());
        }, Collections.emptyList());
    }

    @Override
    public List<T> findDeleted() {
        return executeWithLogging("Finding deleted entities", () -> {
            return repository.findAll().stream()
                    .filter(entity -> entity.deletedAt() != null)
                    .collect(Collectors.toList());
        }, Collections.emptyList());
    }

    @Override
    public List<T> findWithDeleted() {
        return executeWithLogging("Finding all entities including deleted",
            () -> repository.findAll(), Collections.emptyList());
    }

    /**
     * Gets the entity name for logging purposes.
     * Override this method in concrete repositories to provide specific entity names.
     *
     * @return The entity name
     */
    protected String getEntityName() {
        return "Entity";
    }

    /**
     * Executes a repository operation with consistent logging and error handling.
     *
     * @param operation Description of the operation for logging
     * @param supplier The operation to execute
     * @param <R> The return type
     * @return The result of the operation
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
     * @param operation Description of the operation for logging
     * @param supplier The operation to execute
     * @param defaultValue Value to return on error
     * @param <R> The return type
     * @return The result of the operation or default value on error
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
     */
    @FunctionalInterface
    protected interface RepositoryOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Creates a new entity instance from field-value data using reflection.
     * Override this method in concrete repositories for custom entity creation logic.
     *
     * @param data Map of field names to values
     * @return New entity instance
     */
    protected T createEntityFromData(Map<String, Object> data) {
        try {
            // This is a basic implementation using reflection
            // Override in concrete repositories for better performance and type safety
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
     * Override this method in concrete repositories for custom update logic.
     *
     * @param entity The entity to update
     * @param data Map of field names to values
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
                    // Try superclass fields
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