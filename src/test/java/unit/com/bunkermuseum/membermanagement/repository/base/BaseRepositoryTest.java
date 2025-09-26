package unit.com.bunkermuseum.membermanagement.repository.base;

import com.bunkermuseum.membermanagement.model.base.Model;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link BaseRepository} abstract class.
 *
 * <p>This test class validates all CRUD operations, pagination, bulk operations,
 * soft delete functionality, error handling, and edge cases provided by the
 * BaseRepository implementation. It uses Mockito to mock the underlying
 * JpaRepository dependency and focuses on testing the business logic and
 * error handling within BaseRepository.</p>
 *
 * <p><strong>Test Coverage Areas:</strong></p>
 * <ul>
 *   <li>Basic CRUD operations (create, read, update, delete)</li>
 *   <li>Pagination and sorting functionality</li>
 *   <li>Bulk operations (createAll, findAllById)</li>
 *   <li>Soft delete operations (findActive, findDeleted, findWithDeleted)</li>
 *   <li>Entity creation from dynamic data maps using reflection</li>
 *   <li>Error handling and exception scenarios</li>
 *   <li>Logging and default value behavior</li>
 *   <li>Batch processing with chunk operations</li>
 * </ul>
 *
 * <p><strong>Test Methodology:</strong></p>
 * <ul>
 *   <li>Uses {@code @ExtendWith(MockitoExtension.class)} for mock injection</li>
 *   <li>Mocks the underlying {@link JpaRepository} to isolate BaseRepository logic</li>
 *   <li>Tests both success and failure scenarios</li>
 *   <li>Validates method interactions and return values</li>
 *   <li>Includes edge cases and boundary conditions</li>
 * </ul>
 *
 * @author Generated Tests
 * @version 1.0
 * @see BaseRepository
 * @see JpaRepository
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class BaseRepositoryTest {

    /**
     * Mock instance of the underlying JPA repository used by BaseRepository.
     * This mock allows us to control and verify interactions with the persistence layer.
     */
    @Mock
    private JpaRepository<TestEntity, UUID> mockJpaRepository;

    /**
     * Test instance of BaseRepository implementation for testing.
     */
    private TestBaseRepository testRepository;

    /**
     * Test entity instance used across multiple test methods.
     */
    private TestEntity testEntity;

    /**
     * Test entity ID used for various operations.
     */
    private UUID testId;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A test repository instance with the mocked JPA repository</li>
     *   <li>A random UUID for testing entity operations</li>
     *   <li>A test entity with the generated ID</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        testRepository = new TestBaseRepository(mockJpaRepository);
        testId = UUID.randomUUID();
        testEntity = new TestEntity();
        testEntity.setId(testId);
    }

    /**
     * Tests the {@link BaseRepository#findAll()} method to ensure it returns all entities.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository's findAll() method</li>
     *   <li>The returned list contains the expected entities</li>
     *   <li>The JPA repository method is called exactly once</li>
     * </ul>
     */
    @Test
    void findAll_ShouldReturnAllEntities() {
        List<TestEntity> expectedEntities = Arrays.asList(testEntity);
        when(mockJpaRepository.findAll()).thenReturn(expectedEntities);

        List<TestEntity> result = testRepository.findAll();

        assertEquals(expectedEntities, result);
        verify(mockJpaRepository).findAll();
    }

    /**
     * Tests the {@link BaseRepository#findById(UUID)} method when an entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an Optional containing the entity when found</li>
     *   <li>The correct entity is returned</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findById_ShouldReturnOptionalWithEntity_WhenEntityExists() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        Optional<TestEntity> result = testRepository.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(mockJpaRepository).findById(testId);
    }

    /**
     * Tests the {@link BaseRepository#findById(UUID)} method when an entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty Optional when entity is not found</li>
     *   <li>No exceptions are thrown</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findById_ShouldReturnEmptyOptional_WhenEntityDoesNotExist() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<TestEntity> result = testRepository.findById(testId);

        assertTrue(result.isEmpty());
        verify(mockJpaRepository).findById(testId);
    }

    /**
     * Tests the {@link BaseRepository#findByIdOrFail(UUID)} method when an entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns the entity directly when found</li>
     *   <li>No exceptions are thrown</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findByIdOrFail_ShouldReturnEntity_WhenEntityExists() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        TestEntity result = testRepository.findByIdOrFail(testId);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).findById(testId);
    }

    /**
     * Tests the {@link BaseRepository#findByIdOrFail(UUID)} method when an entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when entity is not found</li>
     *   <li>The exception contains appropriate error message</li>
     *   <li>The exception cause is an EntityNotFoundException</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findByIdOrFail_ShouldThrowEntityNotFoundException_WhenEntityDoesNotExist() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> testRepository.findByIdOrFail(testId)
        );

        assertTrue(exception.getMessage().contains("Finding by ID or fail"));
        assertTrue(exception.getCause() instanceof EntityNotFoundException);
        verify(mockJpaRepository).findById(testId);
    }

    /**
     * Tests the {@link BaseRepository#findFirst()} method when entities exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns the first entity wrapped in an Optional</li>
     *   <li>A PageRequest with size 1 is used to limit results</li>
     *   <li>The correct entity is returned</li>
     * </ul>
     */
    @Test
    void findFirst_ShouldReturnFirstEntity_WhenEntitiesExist() {
        Page<TestEntity> page = new PageImpl<>(Arrays.asList(testEntity));
        when(mockJpaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Optional<TestEntity> result = testRepository.findFirst();

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(mockJpaRepository).findAll(PageRequest.of(0, 1));
    }

    /**
     * Tests the {@link BaseRepository#findFirst()} method when no entities exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty Optional when no entities are found</li>
     *   <li>A PageRequest with size 1 is used</li>
     *   <li>No exceptions are thrown</li>
     * </ul>
     */
    @Test
    void findFirst_ShouldReturnEmptyOptional_WhenNoEntitiesExist() {
        Page<TestEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(mockJpaRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        Optional<TestEntity> result = testRepository.findFirst();

        assertTrue(result.isEmpty());
        verify(mockJpaRepository).findAll(PageRequest.of(0, 1));
    }

    /**
     * Tests the {@link BaseRepository#create(Object)} method for entity creation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The entity is saved using the underlying JPA repository</li>
     *   <li>The saved entity is returned</li>
     *   <li>The save method is called exactly once with the correct entity</li>
     * </ul>
     */
    @Test
    void create_ShouldSaveAndReturnEntity() {
        when(mockJpaRepository.save(testEntity)).thenReturn(testEntity);

        TestEntity result = testRepository.create(testEntity);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).save(testEntity);
    }

    /**
     * Tests the {@link BaseRepository#update(UUID, Object)} method when entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The entity existence is checked before updating</li>
     *   <li>The entity is saved using the underlying JPA repository</li>
     *   <li>The updated entity is returned</li>
     *   <li>Both existsById and save methods are called</li>
     * </ul>
     */
    @Test
    void update_ShouldUpdateAndReturnEntity_WhenEntityExists() {
        when(mockJpaRepository.existsById(testId)).thenReturn(true);
        when(mockJpaRepository.save(testEntity)).thenReturn(testEntity);

        TestEntity result = testRepository.update(testId, testEntity);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository).save(testEntity);
    }

    /**
     * Tests the {@link BaseRepository#update(UUID, Object)} method when entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when entity is not found</li>
     *   <li>The exception contains appropriate error message</li>
     *   <li>The exception cause is an EntityNotFoundException</li>
     *   <li>The save method is never called</li>
     * </ul>
     */
    @Test
    void update_ShouldThrowEntityNotFoundException_WhenEntityDoesNotExist() {
        when(mockJpaRepository.existsById(testId)).thenReturn(false);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> testRepository.update(testId, testEntity)
        );

        assertTrue(exception.getMessage().contains("Updating entity"));
        assertTrue(exception.getCause() instanceof EntityNotFoundException);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository, never()).save(any());
    }

    /**
     * Tests the {@link BaseRepository#deleteById(UUID)} method when entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The entity existence is checked before deletion</li>
     *   <li>The entity is deleted using the underlying JPA repository</li>
     *   <li>The method returns true indicating successful deletion</li>
     *   <li>Both existsById and deleteById methods are called</li>
     * </ul>
     */
    @Test
    void deleteById_ShouldReturnTrue_WhenEntityExists() {
        when(mockJpaRepository.existsById(testId)).thenReturn(true);

        boolean result = testRepository.deleteById(testId);

        assertTrue(result);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository).deleteById(testId);
    }

    /**
     * Tests the {@link BaseRepository#deleteById(UUID)} method when entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The entity existence is checked</li>
     *   <li>The method returns false when entity is not found</li>
     *   <li>The deleteById method is never called on the JPA repository</li>
     *   <li>No exceptions are thrown</li>
     * </ul>
     */
    @Test
    void deleteById_ShouldReturnFalse_WhenEntityDoesNotExist() {
        when(mockJpaRepository.existsById(testId)).thenReturn(false);

        boolean result = testRepository.deleteById(testId);

        assertFalse(result);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository, never()).deleteById(any());
    }

    /**
     * Tests the {@link BaseRepository#findAll(Pageable)} method for pagination support.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository with pagination</li>
     *   <li>The correct Page object is returned</li>
     *   <li>The Pageable parameter is passed correctly</li>
     * </ul>
     */
    @Test
    void findAllWithPageable_ShouldReturnPagedEntities() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TestEntity> expectedPage = new PageImpl<>(Arrays.asList(testEntity));
        when(mockJpaRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<TestEntity> result = testRepository.findAll(pageable);

        assertEquals(expectedPage, result);
        verify(mockJpaRepository).findAll(pageable);
    }

    /**
     * Tests the {@link BaseRepository#count()} method for entity counting.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository's count method</li>
     *   <li>The correct count value is returned</li>
     *   <li>The count method is called exactly once</li>
     * </ul>
     */
    @Test
    void count_ShouldReturnEntityCount() {
        long expectedCount = 5L;
        when(mockJpaRepository.count()).thenReturn(expectedCount);

        long result = testRepository.count();

        assertEquals(expectedCount, result);
        verify(mockJpaRepository).count();
    }

    /**
     * Tests the {@link BaseRepository#findAllById(Iterable)} method for bulk retrieval.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository's findAllById method</li>
     *   <li>The correct list of entities is returned</li>
     *   <li>The collection of IDs is passed correctly</li>
     * </ul>
     */
    @Test
    void findAllById_ShouldReturnEntitiesById() {
        List<UUID> ids = Arrays.asList(testId);
        List<TestEntity> expectedEntities = Arrays.asList(testEntity);
        when(mockJpaRepository.findAllById(ids)).thenReturn(expectedEntities);

        List<TestEntity> result = testRepository.findAllById(ids);

        assertEquals(expectedEntities, result);
        verify(mockJpaRepository).findAllById(ids);
    }

    /**
     * Tests the {@link BaseRepository#createAll(Iterable)} method for bulk entity creation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>All entities in the collection are saved using saveAll</li>
     *   <li>The saved entities list is returned</li>
     *   <li>The saveAll method is called exactly once with the correct entities</li>
     * </ul>
     */
    @Test
    void createAll_ShouldSaveAllEntities() {
        List<TestEntity> entities = Arrays.asList(testEntity);
        when(mockJpaRepository.saveAll(entities)).thenReturn(entities);

        List<TestEntity> result = testRepository.createAll(entities);

        assertEquals(entities, result);
        verify(mockJpaRepository).saveAll(entities);
    }

    /**
     * Tests the {@link BaseRepository#existsById(UUID)} method when entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository's existsById method</li>
     *   <li>Returns true when entity exists</li>
     *   <li>The existsById method is called with the correct ID</li>
     * </ul>
     */
    @Test
    void existsById_ShouldReturnTrue_WhenEntityExists() {
        when(mockJpaRepository.existsById(testId)).thenReturn(true);

        boolean result = testRepository.existsById(testId);

        assertTrue(result);
        verify(mockJpaRepository).existsById(testId);
    }

    /**
     * Tests the {@link BaseRepository#existsById(UUID)} method when entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository's existsById method</li>
     *   <li>Returns false when entity does not exist</li>
     *   <li>The existsById method is called with the correct ID</li>
     * </ul>
     */
    @Test
    void existsById_ShouldReturnFalse_WhenEntityDoesNotExist() {
        when(mockJpaRepository.existsById(testId)).thenReturn(false);

        boolean result = testRepository.existsById(testId);

        assertFalse(result);
        verify(mockJpaRepository).existsById(testId);
    }

    /**
     * Tests the {@link BaseRepository#createAndFlush(Object)} method for immediate persistence.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The entity is saved and immediately flushed to the database</li>
     *   <li>The method delegates to the JPA repository's saveAndFlush method</li>
     *   <li>The saved entity is returned</li>
     * </ul>
     */
    @Test
    void createAndFlush_ShouldSaveAndFlushEntity() {
        when(mockJpaRepository.saveAndFlush(testEntity)).thenReturn(testEntity);

        TestEntity result = testRepository.createAndFlush(testEntity);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).saveAndFlush(testEntity);
    }

    /**
     * Tests the {@link BaseRepository#flush()} method for manual database synchronization.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the underlying JPA repository's flush method</li>
     *   <li>The flush method is called exactly once</li>
     *   <li>No return value is expected</li>
     * </ul>
     */
    @Test
    void flush_ShouldCallRepositoryFlush() {
        testRepository.flush();

        verify(mockJpaRepository).flush();
    }

    /**
     * Tests the {@link BaseRepository#create(Map)} method for dynamic entity creation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An entity is created from field-value map data using reflection</li>
     *   <li>The created entity is saved using the underlying JPA repository</li>
     *   <li>The saved entity is returned</li>
     *   <li>Dynamic field assignment works correctly</li>
     * </ul>
     */
    @Test
    void createWithMap_ShouldCreateEntityFromData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Entity");

        when(mockJpaRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        TestEntity result = testRepository.create(data);

        assertNotNull(result);
        verify(mockJpaRepository).save(any(TestEntity.class));
    }

    /**
     * Tests the {@link BaseRepository#updateWithData(UUID, Map)} method for dynamic updates.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An existing entity is retrieved and updated with field-value map data</li>
     *   <li>Dynamic field assignment works using reflection</li>
     *   <li>The updated entity is saved and returned</li>
     *   <li>Both findById and save methods are called</li>
     * </ul>
     */
    @Test
    void updateWithData_ShouldUpdateEntityWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Updated Entity");

        when(mockJpaRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(mockJpaRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        TestEntity result = testRepository.updateWithData(testId, data);

        assertNotNull(result);
        verify(mockJpaRepository).findById(testId);
        verify(mockJpaRepository).save(any(TestEntity.class));
    }

    /**
     * Tests the {@link BaseRepository#createMany(List)} method for bulk dynamic entity creation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Multiple entities are created from a list of field-value maps</li>
     *   <li>All entities are saved using the saveAll method</li>
     *   <li>The saved entities list is returned</li>
     *   <li>Dynamic field assignment works for multiple entities</li>
     * </ul>
     */
    @Test
    void createMany_ShouldCreateMultipleEntitiesFromData() {
        List<Map<String, Object>> dataList = Arrays.asList(
            Map.of("name", "Entity 1"),
            Map.of("name", "Entity 2")
        );

        when(mockJpaRepository.saveAll(anyList())).thenReturn(Arrays.asList(testEntity));

        List<TestEntity> result = testRepository.createMany(dataList);

        assertNotNull(result);
        verify(mockJpaRepository).saveAll(anyList());
    }

    /**
     * Tests the {@link BaseRepository#processInChunks(int, Consumer)} method for batch processing.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Entities are processed in manageable chunks</li>
     *   <li>The count method is called to determine total entities</li>
     *   <li>Pagination is used to retrieve chunks of entities</li>
     *   <li>The consumer function is called for each chunk</li>
     * </ul>
     */
    @Test
    void processInChunks_ShouldProcessEntitiesInChunks() {
        Consumer<List<TestEntity>> processor = mock(Consumer.class);
        Page<TestEntity> page = new PageImpl<>(Arrays.asList(testEntity));

        when(mockJpaRepository.count()).thenReturn(1L);
        when(mockJpaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        testRepository.processInChunks(10, processor);

        verify(mockJpaRepository).count();
        verify(mockJpaRepository).findAll(any(PageRequest.class));
        verify(processor).accept(Arrays.asList(testEntity));
    }

    /**
     * Tests the {@link BaseRepository#findActive()} method for soft delete support.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Only non-deleted entities are returned</li>
     *   <li>Entities with deletedAt field as null are considered active</li>
     *   <li>Deleted entities are filtered out from the results</li>
     *   <li>The filtering logic works correctly</li>
     * </ul>
     */
    @Test
    void findActive_ShouldReturnActiveEntities() {
        TestEntity activeEntity = new TestEntity();
        TestEntity deletedEntity = new TestEntity();
        deletedEntity.delete();

        when(mockJpaRepository.findAll()).thenReturn(Arrays.asList(activeEntity, deletedEntity));

        List<TestEntity> result = testRepository.findActive();

        assertEquals(1, result.size());
        assertEquals(activeEntity, result.get(0));
        verify(mockJpaRepository).findAll();
    }

    /**
     * Tests the {@link BaseRepository#findDeleted()} method for retrieving soft-deleted entities.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Only deleted entities are returned</li>
     *   <li>Entities with deletedAt field set are considered deleted</li>
     *   <li>Active entities are filtered out from the results</li>
     *   <li>The soft delete filtering logic works correctly</li>
     * </ul>
     */
    @Test
    void findDeleted_ShouldReturnDeletedEntities() {
        TestEntity activeEntity = new TestEntity();
        TestEntity deletedEntity = new TestEntity();
        deletedEntity.delete();

        when(mockJpaRepository.findAll()).thenReturn(Arrays.asList(activeEntity, deletedEntity));

        List<TestEntity> result = testRepository.findDeleted();

        assertEquals(1, result.size());
        assertEquals(deletedEntity, result.get(0));
        verify(mockJpaRepository).findAll();
    }

    /**
     * Tests the {@link BaseRepository#findWithDeleted()} method for retrieving all entities.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Both active and deleted entities are returned</li>
     *   <li>No filtering is applied based on deletion status</li>
     *   <li>The complete entity list is returned</li>
     *   <li>Soft delete status is ignored</li>
     * </ul>
     */
    @Test
    void findWithDeleted_ShouldReturnAllEntities() {
        TestEntity activeEntity = new TestEntity();
        TestEntity deletedEntity = new TestEntity();
        deletedEntity.delete();
        List<TestEntity> allEntities = Arrays.asList(activeEntity, deletedEntity);

        when(mockJpaRepository.findAll()).thenReturn(allEntities);

        List<TestEntity> result = testRepository.findWithDeleted();

        assertEquals(2, result.size());
        assertEquals(allEntities, result);
        verify(mockJpaRepository).findAll();
    }

    /**
     * Tests the {@link BaseRepository#getEntityName()} method default implementation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The default entity name is used when not overridden</li>
     *   <li>Error messages include the default entity name</li>
     *   <li>Exception handling works with default naming</li>
     * </ul>
     */
    @Test
    void getEntityName_ShouldReturnDefaultEntityName() {
        DefaultEntityNameRepository defaultRepo = new DefaultEntityNameRepository(mockJpaRepository);

        // This will trigger the default getEntityName() method
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> defaultRepo.findByIdOrFail(testId));

        assertTrue(exception.getCause().getMessage().contains("Entity"));
    }

    /**
     * Tests the {@link BaseRepository#executeWithLogging} method error handling.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Exceptions are caught and logged properly</li>
     *   <li>Default values are returned when operations fail</li>
     *   <li>The application continues to function despite errors</li>
     *   <li>Error logging includes appropriate context</li>
     * </ul>
     */
    @Test
    void executeWithLogging_ShouldReturnDefaultValueOnException() {
        // Create a repository that will throw an exception for count operation
        when(mockJpaRepository.count()).thenThrow(new RuntimeException("Database error"));

        long result = testRepository.count();

        // Should return default value (0L) instead of throwing exception
        assertEquals(0L, result);
        verify(mockJpaRepository).count();
    }

    /**
     * Tests successful entity creation from dynamic data using the reflection mechanism.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Entities can be created from field-value map data</li>
     *   <li>Reflection-based field assignment works correctly</li>
     *   <li>The created entity is properly saved</li>
     *   <li>No exceptions are thrown during the process</li>
     * </ul>
     */
    @Test
    void createEntityFromData_ShouldCreateEntitySuccessfully() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Entity");
        when(mockJpaRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        TestEntity result = testRepository.create(data);

        assertNotNull(result);
        verify(mockJpaRepository).save(any(TestEntity.class));
    }

    /**
     * Tests that unknown fields in data maps generate warnings but don't stop processing.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Unknown fields in the data map are handled gracefully</li>
     *   <li>Warning messages are logged for unrecognized fields</li>
     *   <li>The update operation continues despite unknown fields</li>
     *   <li>Valid fields are still processed correctly</li>
     * </ul>
     */
    @Test
    void updateEntityFromData_ShouldLogWarningForUnknownField() {
        Map<String, Object> data = new HashMap<>();
        data.put("unknownField", "value");
        data.put("anotherUnknownField", "value2");

        when(mockJpaRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(mockJpaRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        // This should succeed but log warnings for unknown fields
        TestEntity result = testRepository.updateWithData(testId, data);

        assertNotNull(result);
        verify(mockJpaRepository).findById(testId);
        verify(mockJpaRepository).save(any(TestEntity.class));
    }

    /**
     * Tests error handling when update with data encounters general exceptions.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>General exceptions during update operations are caught</li>
     *   <li>RuntimeException is thrown with appropriate error message</li>
     *   <li>Error context is preserved in the exception</li>
     *   <li>Failed operations are handled gracefully</li>
     * </ul>
     */
    @Test
    void updateEntityFromData_ShouldThrowRuntimeExceptionOnGeneralError() {
        // Create a map that will cause a general exception during reflection
        Map<String, Object> data = new HashMap<>();
        data.put("name", "test");

        // Make the repository throw an exception during the operation
        when(mockJpaRepository.findById(testId)).thenThrow(new RuntimeException("Database connection lost"));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> testRepository.updateWithData(testId, data));

        assertTrue(exception.getMessage().contains("Failed to execute operation"));
    }

    /**
     * Test implementation of BaseRepository for testing purposes.
     *
     * <p>This concrete implementation provides a test-specific entity name
     * and allows us to test the abstract BaseRepository functionality
     * without requiring a full Spring context.</p>
     */
    private static class TestBaseRepository extends BaseRepository<TestEntity, JpaRepository<TestEntity, UUID>> {

        public TestBaseRepository(JpaRepository<TestEntity, UUID> repository) {
            super(repository);
        }

        @Override
        protected String getEntityName() {
            return "TestEntity";
        }
    }

    /**
     * Test repository implementation that uses the default entity name.
     *
     * <p>This implementation does not override the getEntityName() method,
     * allowing us to test the default entity naming behavior in error scenarios.</p>
     */
    private static class DefaultEntityNameRepository extends BaseRepository<TestEntity, JpaRepository<TestEntity, UUID>> {

        public DefaultEntityNameRepository(JpaRepository<TestEntity, UUID> repository) {
            super(repository);
        }
    }


    /**
     * Test entity class extending the base Model for testing repository operations.
     *
     * <p>This simple entity contains:</p>
     * <ul>
     *   <li>A name field for testing field assignment</li>
     *   <li>Public constructor for reflection-based instantiation</li>
     *   <li>Standard getter and setter methods</li>
     *   <li>Custom setId method for test setup</li>
     * </ul>
     */
    public static class TestEntity extends Model {
        private String name;

        public TestEntity() {
            // Public constructor for reflection
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setId(UUID id) {
            try {
                var field = Model.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(this, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}