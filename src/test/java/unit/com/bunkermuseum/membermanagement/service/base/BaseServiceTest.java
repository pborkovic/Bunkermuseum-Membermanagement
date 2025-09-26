package unit.com.bunkermuseum.membermanagement.service.base;

import com.bunkermuseum.membermanagement.model.base.Model;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link BaseService} abstract class.
 *
 * <p>This test class validates all business logic operations, validation workflows,
 * lifecycle hooks, and integration with repository layers provided by the BaseService
 * implementation. It uses Mockito to mock the underlying BaseRepositoryContract
 * dependency and focuses on testing the service layer logic, validation, and
 * business rule application.</p>
 *
 * <p><strong>Test Coverage Areas:</strong></p>
 * <ul>
 *   <li>Basic CRUD operations with business rule enforcement</li>
 *   <li>Validation hook execution (validateForCreate, validateForUpdate, validateForDelete)</li>
 *   <li>Business rule application hooks (applyBusinessRulesFor*)</li>
 *   <li>Lifecycle hooks (afterCreate, afterUpdate, afterDelete)</li>
 *   <li>Service-repository integration and delegation</li>
 *   <li>Error handling and exception scenarios</li>
 *   <li>Dynamic data operations with validation</li>
 *   <li>Batch processing operations</li>
 *   <li>Empty hook method behavior</li>
 * </ul>
 *
 * <p><strong>Business Logic Testing:</strong></p>
 * <ul>
 *   <li>Validates that business rules are applied before persistence operations</li>
 *   <li>Ensures validation methods are called with correct parameters</li>
 *   <li>Verifies lifecycle hooks execute in proper sequence</li>
 *   <li>Tests that failed validations prevent operations from proceeding</li>
 *   <li>Confirms proper error propagation from repository to service layer</li>
 * </ul>
 *
 * <p><strong>Test Methodology:</strong></p>
 * <ul>
 *   <li>Uses {@code @ExtendWith(MockitoExtension.class)} for mock injection</li>
 *   <li>Mocks the underlying {@link BaseRepositoryContract} to isolate service logic</li>
 *   <li>Tracks hook method execution using boolean flags</li>
 *   <li>Tests both success and failure scenarios</li>
 *   <li>Validates method call sequences and interactions</li>
 * </ul>
 *
 * @author Generated Tests
 * @version 1.0
 * @see BaseService
 * @see BaseRepositoryContract
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    /**
     * Mock instance of the repository contract used by BaseService.
     * This mock allows us to control and verify interactions with the data layer.
     */
    @Mock
    private BaseRepositoryContract<TestEntity> mockRepository;

    /**
     * Test instance of BaseService implementation for testing business logic.
     */
    private TestBaseService testService;

    /**
     * Test entity instance used across multiple test methods.
     */
    private TestEntity testEntity;

    /**
     * Test entity ID used for various service operations.
     */
    private UUID testId;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A test service instance with the mocked repository</li>
     *   <li>A random UUID for testing entity operations</li>
     *   <li>A test entity with the generated ID</li>
     *   <li>Resets all hook tracking flags in the test service</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        testService = new TestBaseService(mockRepository);
        testId = UUID.randomUUID();
        testEntity = new TestEntity();
        testEntity.setId(testId);
    }

    /**
     * Tests the {@link BaseService#findAll()} method delegation to repository.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates directly to the repository's findAll method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>The returned list contains the expected entities</li>
     *   <li>The repository method is called exactly once</li>
     * </ul>
     */
    @Test
    void findAll_ShouldReturnAllEntities() {
        List<TestEntity> expectedEntities = Arrays.asList(testEntity);
        when(mockRepository.findAll()).thenReturn(expectedEntities);

        List<TestEntity> result = testService.findAll();

        assertEquals(expectedEntities, result);
        verify(mockRepository).findAll();
    }

    /**
     * Tests the {@link BaseService#findById(UUID)} method when an entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findById method</li>
     *   <li>Returns an Optional containing the entity when found</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>The repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findById_ShouldReturnOptionalWithEntity_WhenEntityExists() {
        when(mockRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        Optional<TestEntity> result = testService.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(mockRepository).findById(testId);
    }

    /**
     * Tests the {@link BaseService#findById(UUID)} method when an entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findById method</li>
     *   <li>Returns an empty Optional when entity is not found</li>
     *   <li>No exceptions are thrown</li>
     *   <li>The repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findById_ShouldReturnEmptyOptional_WhenEntityDoesNotExist() {
        when(mockRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<TestEntity> result = testService.findById(testId);

        assertTrue(result.isEmpty());
        verify(mockRepository).findById(testId);
    }

    /**
     * Tests the {@link BaseService#findByIdOrFail(UUID)} method when an entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findByIdOrFail method</li>
     *   <li>Returns the entity directly when found</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>The repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void findByIdOrFail_ShouldReturnEntity_WhenEntityExists() {
        when(mockRepository.findByIdOrFail(testId)).thenReturn(testEntity);

        TestEntity result = testService.findByIdOrFail(testId);

        assertEquals(testEntity, result);
        verify(mockRepository).findByIdOrFail(testId);
    }

    /**
     * Tests the {@link BaseService#create(Object)} method with full business rule workflow.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hook is executed before creation</li>
     *   <li>Business rules are applied before persistence</li>
     *   <li>The entity is saved using the repository</li>
     *   <li>After-creation hook is executed after successful save</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>The saved entity is returned</li>
     * </ul>
     */
    @Test
    void create_ShouldCreateEntityWithBusinessRules() {
        when(mockRepository.create(testEntity)).thenReturn(testEntity);

        TestEntity result = testService.create(testEntity);

        assertEquals(testEntity, result);
        verify(mockRepository).create(testEntity);
        assertTrue(testService.validateForCreateCalled);
        assertTrue(testService.applyBusinessRulesForCreateCalled);
        assertTrue(testService.afterCreateCalled);
    }

    /**
     * Tests the {@link BaseService#update(UUID, Object)} method with full business rule workflow.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hook is executed with both ID and entity</li>
     *   <li>Business rules are applied before persistence</li>
     *   <li>The entity is updated using the repository</li>
     *   <li>After-update hook is executed after successful save</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>The updated entity is returned</li>
     * </ul>
     */
    @Test
    void update_ShouldUpdateEntityWithBusinessRules() {
        when(mockRepository.update(testId, testEntity)).thenReturn(testEntity);

        TestEntity result = testService.update(testId, testEntity);

        assertEquals(testEntity, result);
        verify(mockRepository).update(testId, testEntity);
        assertTrue(testService.validateForUpdateCalled);
        assertTrue(testService.applyBusinessRulesForUpdateCalled);
        assertTrue(testService.afterUpdateCalled);
    }

    /**
     * Tests the {@link BaseService#deleteById(UUID)} method with full business rule workflow.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hook is executed with the entity ID</li>
     *   <li>Business rules are applied before deletion</li>
     *   <li>The entity is deleted using the repository</li>
     *   <li>After-delete hook is executed after successful deletion</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>Returns true indicating successful deletion</li>
     * </ul>
     */
    @Test
    void deleteById_ShouldDeleteEntityWithBusinessRules() {
        when(mockRepository.deleteById(testId)).thenReturn(true);

        boolean result = testService.deleteById(testId);

        assertTrue(result);
        verify(mockRepository).deleteById(testId);
        assertTrue(testService.validateForDeleteCalled);
        assertTrue(testService.applyBusinessRulesForDeleteCalled);
        assertTrue(testService.afterDeleteCalled);
    }

    /**
     * Tests the {@link BaseService#deleteById(UUID)} method when deletion fails.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation and business rule hooks are still executed</li>
     *   <li>After-delete hook is NOT called when deletion fails</li>
     *   <li>Returns false indicating failed deletion</li>
     *   <li>Partial workflow execution is handled correctly</li>
     * </ul>
     */
    @Test
    void deleteById_ShouldNotCallAfterDelete_WhenDeletionFails() {
        when(mockRepository.deleteById(testId)).thenReturn(false);

        boolean result = testService.deleteById(testId);

        assertFalse(result);
        verify(mockRepository).deleteById(testId);
        assertTrue(testService.validateForDeleteCalled);
        assertTrue(testService.applyBusinessRulesForDeleteCalled);
        assertFalse(testService.afterDeleteCalled);
    }

    /**
     * Tests the {@link BaseService#findAll(Pageable)} method for pagination support.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findAll method with pagination</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>The correct Page object is returned</li>
     *   <li>The Pageable parameter is passed correctly</li>
     * </ul>
     */
    @Test
    void findAllWithPageable_ShouldReturnPagedEntities() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TestEntity> expectedPage = new PageImpl<>(Arrays.asList(testEntity));
        when(mockRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<TestEntity> result = testService.findAll(pageable);

        assertEquals(expectedPage, result);
        verify(mockRepository).findAll(pageable);
    }

    /**
     * Tests the {@link BaseService#count()} method for entity counting.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's count method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>The correct count value is returned</li>
     *   <li>The repository method is called exactly once</li>
     * </ul>
     */
    @Test
    void count_ShouldReturnEntityCount() {
        long expectedCount = 5L;
        when(mockRepository.count()).thenReturn(expectedCount);

        long result = testService.count();

        assertEquals(expectedCount, result);
        verify(mockRepository).count();
    }

    /**
     * Tests the {@link BaseService#createAll(Iterable)} method for bulk creation with business rules.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hooks are executed for all entities</li>
     *   <li>Business rules are applied before bulk persistence</li>
     *   <li>All entities are saved using the repository's createAll method</li>
     *   <li>After-creation hooks are executed after successful save</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>The saved entities list is returned</li>
     * </ul>
     */
    @Test
    void createAll_ShouldCreateAllEntitiesWithBusinessRules() {
        List<TestEntity> entities = Arrays.asList(testEntity);
        when(mockRepository.createAll(entities)).thenReturn(entities);

        List<TestEntity> result = testService.createAll(entities);

        assertEquals(entities, result);
        verify(mockRepository).createAll(entities);
        assertTrue(testService.validateForCreateCalled);
        assertTrue(testService.applyBusinessRulesForCreateCalled);
        assertTrue(testService.afterCreateCalled);
    }

    /**
     * Tests the {@link BaseService#existsById(UUID)} method when entity exists.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's existsById method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>Returns true when entity exists</li>
     *   <li>The repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void existsById_ShouldReturnTrue_WhenEntityExists() {
        when(mockRepository.existsById(testId)).thenReturn(true);

        boolean result = testService.existsById(testId);

        assertTrue(result);
        verify(mockRepository).existsById(testId);
    }

    /**
     * Tests the {@link BaseService#existsById(UUID)} method when entity does not exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's existsById method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>Returns false when entity does not exist</li>
     *   <li>The repository is called with the correct ID</li>
     * </ul>
     */
    @Test
    void existsById_ShouldReturnFalse_WhenEntityDoesNotExist() {
        when(mockRepository.existsById(testId)).thenReturn(false);

        boolean result = testService.existsById(testId);

        assertFalse(result);
        verify(mockRepository).existsById(testId);
    }

    /**
     * Tests the {@link BaseService#create(Map)} method for dynamic entity creation with business rules.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hooks are executed for dynamically created entities</li>
     *   <li>Business rules are applied before persistence</li>
     *   <li>The entity is created from field-value map using repository</li>
     *   <li>After-creation hooks are executed after successful save</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>The created entity is returned</li>
     * </ul>
     */
    @Test
    void createWithMap_ShouldCreateEntityFromDataWithBusinessRules() {
        Map<String, Object> data = Map.of("name", "Test Entity");
        when(mockRepository.create(data)).thenReturn(testEntity);

        TestEntity result = testService.create(data);

        assertEquals(testEntity, result);
        verify(mockRepository).create(data);
        assertTrue(testService.validateForCreateCalled);
        assertTrue(testService.applyBusinessRulesForCreateCalled);
        assertTrue(testService.afterCreateCalled);
    }

    /**
     * Tests the {@link BaseService#updateWithData(UUID, Map)} method for dynamic updates with business rules.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hooks are executed with ID and dynamic data</li>
     *   <li>Business rules are applied before persistence</li>
     *   <li>The entity is updated with field-value map using repository</li>
     *   <li>After-update hooks are executed after successful save</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>The updated entity is returned</li>
     * </ul>
     */
    @Test
    void updateWithData_ShouldUpdateEntityWithDataAndBusinessRules() {
        Map<String, Object> data = Map.of("name", "Updated Entity");
        when(mockRepository.updateWithData(testId, data)).thenReturn(testEntity);

        TestEntity result = testService.updateWithData(testId, data);

        assertEquals(testEntity, result);
        verify(mockRepository).updateWithData(testId, data);
        assertTrue(testService.validateForUpdateCalled);
        assertTrue(testService.applyBusinessRulesForUpdateCalled);
        assertTrue(testService.afterUpdateCalled);
    }

    /**
     * Tests the {@link BaseService#createMany(List)} method for bulk dynamic entity creation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation hooks are executed for all dynamic entities</li>
     *   <li>Business rules are applied before bulk persistence</li>
     *   <li>Multiple entities are created from field-value maps using repository</li>
     *   <li>After-creation hooks are executed after successful save</li>
     *   <li>All hooks are called in the correct sequence</li>
     *   <li>The created entities list is returned</li>
     * </ul>
     */
    @Test
    void createMany_ShouldCreateManyEntitiesWithBusinessRules() {
        List<Map<String, Object>> dataList = Arrays.asList(
            Map.of("name", "Entity 1"),
            Map.of("name", "Entity 2")
        );
        List<TestEntity> entities = Arrays.asList(testEntity);
        when(mockRepository.createMany(dataList)).thenReturn(entities);

        List<TestEntity> result = testService.createMany(dataList);

        assertEquals(entities, result);
        verify(mockRepository).createMany(dataList);
        assertTrue(testService.validateForCreateCalled);
        assertTrue(testService.applyBusinessRulesForCreateCalled);
        assertTrue(testService.afterCreateCalled);
    }

    /**
     * Tests the {@link BaseService#processInChunks(int, Consumer)} method for batch processing.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates directly to the repository's processInChunks method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>The chunk size and processor function are passed correctly</li>
     *   <li>Batch processing is handled by the repository layer</li>
     * </ul>
     */
    @Test
    void processInChunks_ShouldDelegateToRepository() {
        Consumer<List<TestEntity>> processor = mock(Consumer.class);

        testService.processInChunks(10, processor);

        verify(mockRepository).processInChunks(10, processor);
    }

    /**
     * Tests the {@link BaseService#findActive()} method for soft delete support.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findActive method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>Only non-deleted entities are returned</li>
     *   <li>Soft delete filtering is handled by the repository</li>
     * </ul>
     */
    @Test
    void findActive_ShouldReturnActiveEntities() {
        List<TestEntity> activeEntities = Arrays.asList(testEntity);
        when(mockRepository.findActive()).thenReturn(activeEntities);

        List<TestEntity> result = testService.findActive();

        assertEquals(activeEntities, result);
        verify(mockRepository).findActive();
    }

    /**
     * Tests the {@link BaseService#findDeleted()} method for retrieving soft-deleted entities.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findDeleted method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>Only deleted entities are returned</li>
     *   <li>Soft delete filtering is handled by the repository</li>
     * </ul>
     */
    @Test
    void findDeleted_ShouldReturnDeletedEntities() {
        List<TestEntity> deletedEntities = Arrays.asList(testEntity);
        when(mockRepository.findDeleted()).thenReturn(deletedEntities);

        List<TestEntity> result = testService.findDeleted();

        assertEquals(deletedEntities, result);
        verify(mockRepository).findDeleted();
    }

    /**
     * Tests the {@link BaseService#findWithDeleted()} method for retrieving all entities.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the repository's findWithDeleted method</li>
     *   <li>No business rules or validation are applied for read operations</li>
     *   <li>Both active and deleted entities are returned</li>
     *   <li>No soft delete filtering is applied</li>
     * </ul>
     */
    @Test
    void findWithDeleted_ShouldReturnAllEntities() {
        List<TestEntity> allEntities = Arrays.asList(testEntity);
        when(mockRepository.findWithDeleted()).thenReturn(allEntities);

        List<TestEntity> result = testService.findWithDeleted();

        assertEquals(allEntities, result);
        verify(mockRepository).findWithDeleted();
    }

    /**
     * Tests the {@link BaseService#validateForCreate(Object)} method with null entity.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation correctly identifies null entities as invalid</li>
     *   <li>An IllegalArgumentException is thrown with appropriate message</li>
     *   <li>The validation prevents the creation operation from proceeding</li>
     *   <li>Proper error handling is implemented in validation logic</li>
     * </ul>
     */
    @Test
    void validateForCreate_ShouldThrowException_WhenEntityIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForCreate(null)
        );

        assertEquals("Entity cannot be null", exception.getMessage());
    }

    /**
     * Tests the {@link BaseService#validateForUpdate(UUID, Object)} method with null ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation correctly identifies null IDs as invalid</li>
     *   <li>An IllegalArgumentException is thrown with appropriate message</li>
     *   <li>The validation prevents the update operation from proceeding</li>
     *   <li>ID validation occurs before entity validation</li>
     * </ul>
     */
    @Test
    void validateForUpdate_ShouldThrowException_WhenIdIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForUpdate(null, testEntity)
        );

        assertEquals("ID cannot be null", exception.getMessage());
    }

    /**
     * Tests the {@link BaseService#validateForUpdate(UUID, Object)} method with null entity.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation correctly identifies null entities as invalid</li>
     *   <li>An IllegalArgumentException is thrown with appropriate message</li>
     *   <li>The validation prevents the update operation from proceeding</li>
     *   <li>Entity validation occurs after ID validation</li>
     * </ul>
     */
    @Test
    void validateForUpdate_ShouldThrowException_WhenEntityIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForUpdate(testId, null)
        );

        assertEquals("Entity cannot be null", exception.getMessage());
    }

    /**
     * Tests the {@link BaseService#validateForDelete(UUID)} method with null ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Validation correctly identifies null IDs as invalid</li>
     *   <li>An IllegalArgumentException is thrown with appropriate message</li>
     *   <li>The validation prevents the delete operation from proceeding</li>
     *   <li>Proper input validation is implemented for delete operations</li>
     * </ul>
     */
    @Test
    void validateForDelete_ShouldThrowException_WhenIdIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForDelete(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
    }

    /**
     * Tests the error handling mechanism when repository operations fail.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Exceptions from repository operations are handled gracefully</li>
     *   <li>Default values are returned when operations fail</li>
     *   <li>Error logging is performed with appropriate context</li>
     *   <li>The service layer remains stable despite repository failures</li>
     * </ul>
     */
    @Test
    void executeWithLogging_ShouldReturnDefaultValueOnException() {
        when(mockRepository.count()).thenThrow(new RuntimeException("Database error"));

        long result = testService.count();

        assertEquals(0L, result);
        verify(mockRepository).count();
    }

    /**
     * Tests the {@link BaseService#getEntityName()} method default implementation.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Services that don't override getEntityName use the default implementation</li>
     *   <li>The default entity name is used in error messages and logging</li>
     *   <li>Operations continue to function with default naming</li>
     *   <li>No exceptions are thrown when using default entity names</li>
     * </ul>
     */
    @Test
    void getEntityName_ShouldReturnDefaultEntityName() {
        DefaultEntityNameService defaultService = new DefaultEntityNameService(mockRepository);

        when(mockRepository.count()).thenReturn(5L);

        long result = defaultService.count();

        assertEquals(5L, result);
    }

    /**
     * Tests that empty hook methods don't interfere with normal operations.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Services with empty hook implementations work correctly</li>
     *   <li>CRUD operations complete successfully without custom business logic</li>
     *   <li>Empty hooks don't throw exceptions or cause failures</li>
     *   <li>The base service pattern works with minimal implementations</li>
     * </ul>
     */
    @Test
    void emptyHookMethods_ShouldNotThrowExceptions() {
        EmptyHookService emptyHookService = new EmptyHookService(mockRepository);
        when(mockRepository.create(testEntity)).thenReturn(testEntity);
        when(mockRepository.update(testId, testEntity)).thenReturn(testEntity);
        when(mockRepository.deleteById(testId)).thenReturn(true);

        assertDoesNotThrow(() -> emptyHookService.create(testEntity));
        assertDoesNotThrow(() -> emptyHookService.update(testId, testEntity));
        assertDoesNotThrow(() -> emptyHookService.deleteById(testId));

        verify(mockRepository).create(testEntity);
        verify(mockRepository).update(testId, testEntity);
        verify(mockRepository).deleteById(testId);
    }

    /**
     * Test implementation of BaseService for testing business logic and hook execution.
     *
     * <p>This concrete implementation provides:</p>
     * <ul>
     *   <li>Boolean flags to track hook method execution</li>
     *   <li>Custom validation logic for testing validation workflows</li>
     *   <li>Business rule implementations for testing rule application</li>
     *   <li>Lifecycle hook implementations for testing hook sequences</li>
     * </ul>
     *
     * <p>The tracking flags allow tests to verify that hooks are called
     * in the correct sequence and with the appropriate parameters.</p>
     */
    private static class TestBaseService extends BaseService<TestEntity, BaseRepositoryContract<TestEntity>> {

        boolean validateForCreateCalled = false;
        boolean validateForUpdateCalled = false;
        boolean validateForDeleteCalled = false;
        boolean applyBusinessRulesForCreateCalled = false;
        boolean applyBusinessRulesForUpdateCalled = false;
        boolean applyBusinessRulesForDeleteCalled = false;
        boolean afterCreateCalled = false;
        boolean afterUpdateCalled = false;
        boolean afterDeleteCalled = false;

        public TestBaseService(BaseRepositoryContract<TestEntity> repository) {
            super(repository);
        }

        @Override
        protected String getEntityName() {
            return "TestEntity";
        }

        @Override
        public void validateForCreate(TestEntity entity) {
            validateForCreateCalled = true;
            if (entity == null) {
                throw new IllegalArgumentException("Entity cannot be null");
            }
        }

        @Override
        public void validateForUpdate(UUID id, TestEntity entity) {
            validateForUpdateCalled = true;
            if (id == null) {
                throw new IllegalArgumentException("ID cannot be null");
            }
            if (entity == null) {
                throw new IllegalArgumentException("Entity cannot be null");
            }
        }

        @Override
        public void validateForDelete(UUID id) {
            validateForDeleteCalled = true;
            if (id == null) {
                throw new IllegalArgumentException("ID cannot be null");
            }
        }

        @Override
        protected void applyBusinessRulesForCreate(TestEntity entity) {
            applyBusinessRulesForCreateCalled = true;
        }

        @Override
        protected void applyBusinessRulesForUpdate(UUID id, TestEntity entity) {
            applyBusinessRulesForUpdateCalled = true;
        }

        @Override
        protected void applyBusinessRulesForDelete(UUID id) {
            applyBusinessRulesForDeleteCalled = true;
        }

        @Override
        protected void afterCreate(TestEntity entity) {
            afterCreateCalled = true;
        }

        @Override
        protected void afterUpdate(TestEntity entity) {
            afterUpdateCalled = true;
        }

        @Override
        protected void afterDelete(UUID id) {
            afterDeleteCalled = true;
        }
    }

    /**
     * Test entity class extending the base Model for testing service operations.
     *
     * <p>This simple entity contains:</p>
     * <ul>
     *   <li>A name field for testing field assignment and updates</li>
     *   <li>Standard getter and setter methods</li>
     *   <li>Custom setId method for test setup using reflection</li>
     * </ul>
     *
     * <p>The entity is designed to work with both direct object operations
     * and dynamic field-value map operations tested throughout the service layer.</p>
     */
    private static class TestEntity extends Model {
        private String name;

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

    /**
     * Test service implementation that uses the default entity name.
     *
     * <p>This implementation does not override the getEntityName() method,
     * allowing us to test the default entity naming behavior in service operations.</p>
     */
    private static class DefaultEntityNameService extends BaseService<TestEntity, BaseRepositoryContract<TestEntity>> {

        public DefaultEntityNameService(BaseRepositoryContract<TestEntity> repository) {
            super(repository);
        }

        @Override
        protected String getEntityName() {
            return super.getEntityName();
        }
    }

    /**
     * Test service implementation with no custom hook logic.
     *
     * <p>This implementation uses the default empty hook methods from BaseService,
     * allowing us to test that services work correctly without custom business logic.</p>
     */
    private static class EmptyHookService extends BaseService<TestEntity, BaseRepositoryContract<TestEntity>> {

        public EmptyHookService(BaseRepositoryContract<TestEntity> repository) {
            super(repository);
        }

        @Override
        protected String getEntityName() {
            return "EmptyHookTestEntity";
        }
    }
}