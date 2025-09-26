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

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock
    private BaseRepositoryContract<TestEntity> mockRepository;

    private TestBaseService testService;
    private TestEntity testEntity;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testService = new TestBaseService(mockRepository);
        testId = UUID.randomUUID();
        testEntity = new TestEntity();
        testEntity.setId(testId);
    }

    @Test
    void findAll_ShouldReturnAllEntities() {
        List<TestEntity> expectedEntities = Arrays.asList(testEntity);
        when(mockRepository.findAll()).thenReturn(expectedEntities);

        List<TestEntity> result = testService.findAll();

        assertEquals(expectedEntities, result);
        verify(mockRepository).findAll();
    }

    @Test
    void findById_ShouldReturnOptionalWithEntity_WhenEntityExists() {
        when(mockRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        Optional<TestEntity> result = testService.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(mockRepository).findById(testId);
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenEntityDoesNotExist() {
        when(mockRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<TestEntity> result = testService.findById(testId);

        assertTrue(result.isEmpty());
        verify(mockRepository).findById(testId);
    }

    @Test
    void findByIdOrFail_ShouldReturnEntity_WhenEntityExists() {
        when(mockRepository.findByIdOrFail(testId)).thenReturn(testEntity);

        TestEntity result = testService.findByIdOrFail(testId);

        assertEquals(testEntity, result);
        verify(mockRepository).findByIdOrFail(testId);
    }

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

    @Test
    void findAllWithPageable_ShouldReturnPagedEntities() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TestEntity> expectedPage = new PageImpl<>(Arrays.asList(testEntity));
        when(mockRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<TestEntity> result = testService.findAll(pageable);

        assertEquals(expectedPage, result);
        verify(mockRepository).findAll(pageable);
    }

    @Test
    void count_ShouldReturnEntityCount() {
        long expectedCount = 5L;
        when(mockRepository.count()).thenReturn(expectedCount);

        long result = testService.count();

        assertEquals(expectedCount, result);
        verify(mockRepository).count();
    }

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

    @Test
    void existsById_ShouldReturnTrue_WhenEntityExists() {
        when(mockRepository.existsById(testId)).thenReturn(true);

        boolean result = testService.existsById(testId);

        assertTrue(result);
        verify(mockRepository).existsById(testId);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenEntityDoesNotExist() {
        when(mockRepository.existsById(testId)).thenReturn(false);

        boolean result = testService.existsById(testId);

        assertFalse(result);
        verify(mockRepository).existsById(testId);
    }

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

    @Test
    void processInChunks_ShouldDelegateToRepository() {
        Consumer<List<TestEntity>> processor = mock(Consumer.class);

        testService.processInChunks(10, processor);

        verify(mockRepository).processInChunks(10, processor);
    }

    @Test
    void findActive_ShouldReturnActiveEntities() {
        List<TestEntity> activeEntities = Arrays.asList(testEntity);
        when(mockRepository.findActive()).thenReturn(activeEntities);

        List<TestEntity> result = testService.findActive();

        assertEquals(activeEntities, result);
        verify(mockRepository).findActive();
    }

    @Test
    void findDeleted_ShouldReturnDeletedEntities() {
        List<TestEntity> deletedEntities = Arrays.asList(testEntity);
        when(mockRepository.findDeleted()).thenReturn(deletedEntities);

        List<TestEntity> result = testService.findDeleted();

        assertEquals(deletedEntities, result);
        verify(mockRepository).findDeleted();
    }

    @Test
    void findWithDeleted_ShouldReturnAllEntities() {
        List<TestEntity> allEntities = Arrays.asList(testEntity);
        when(mockRepository.findWithDeleted()).thenReturn(allEntities);

        List<TestEntity> result = testService.findWithDeleted();

        assertEquals(allEntities, result);
        verify(mockRepository).findWithDeleted();
    }

    @Test
    void validateForCreate_ShouldThrowException_WhenEntityIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForCreate(null)
        );

        assertEquals("Entity cannot be null", exception.getMessage());
    }

    @Test
    void validateForUpdate_ShouldThrowException_WhenIdIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForUpdate(null, testEntity)
        );

        assertEquals("ID cannot be null", exception.getMessage());
    }

    @Test
    void validateForUpdate_ShouldThrowException_WhenEntityIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForUpdate(testId, null)
        );

        assertEquals("Entity cannot be null", exception.getMessage());
    }

    @Test
    void validateForDelete_ShouldThrowException_WhenIdIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> testService.validateForDelete(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
    }

    @Test
    void executeWithLogging_ShouldReturnDefaultValueOnException() {
        when(mockRepository.count()).thenThrow(new RuntimeException("Database error"));

        long result = testService.count();

        assertEquals(0L, result);
        verify(mockRepository).count();
    }

    @Test
    void getEntityName_ShouldReturnDefaultEntityName() {
        DefaultEntityNameService defaultService = new DefaultEntityNameService(mockRepository);

        when(mockRepository.count()).thenReturn(5L);

        long result = defaultService.count();

        assertEquals(5L, result);
    }

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

    private static class DefaultEntityNameService extends BaseService<TestEntity, BaseRepositoryContract<TestEntity>> {

        public DefaultEntityNameService(BaseRepositoryContract<TestEntity> repository) {
            super(repository);
        }

        @Override
        protected String getEntityName() {
            return super.getEntityName();
        }
    }

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