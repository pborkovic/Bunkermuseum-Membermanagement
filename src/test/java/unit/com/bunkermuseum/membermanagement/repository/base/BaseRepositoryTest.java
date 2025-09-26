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

@ExtendWith(MockitoExtension.class)
class BaseRepositoryTest {

    @Mock
    private JpaRepository<TestEntity, UUID> mockJpaRepository;

    private TestBaseRepository testRepository;
    private TestEntity testEntity;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testRepository = new TestBaseRepository(mockJpaRepository);
        testId = UUID.randomUUID();
        testEntity = new TestEntity();
        testEntity.setId(testId);
    }

    @Test
    void findAll_ShouldReturnAllEntities() {
        List<TestEntity> expectedEntities = Arrays.asList(testEntity);
        when(mockJpaRepository.findAll()).thenReturn(expectedEntities);

        List<TestEntity> result = testRepository.findAll();

        assertEquals(expectedEntities, result);
        verify(mockJpaRepository).findAll();
    }

    @Test
    void findById_ShouldReturnOptionalWithEntity_WhenEntityExists() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        Optional<TestEntity> result = testRepository.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(mockJpaRepository).findById(testId);
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenEntityDoesNotExist() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<TestEntity> result = testRepository.findById(testId);

        assertTrue(result.isEmpty());
        verify(mockJpaRepository).findById(testId);
    }

    @Test
    void findByIdOrFail_ShouldReturnEntity_WhenEntityExists() {
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        TestEntity result = testRepository.findByIdOrFail(testId);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).findById(testId);
    }

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

    @Test
    void findFirst_ShouldReturnFirstEntity_WhenEntitiesExist() {
        Page<TestEntity> page = new PageImpl<>(Arrays.asList(testEntity));
        when(mockJpaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Optional<TestEntity> result = testRepository.findFirst();

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(mockJpaRepository).findAll(PageRequest.of(0, 1));
    }

    @Test
    void findFirst_ShouldReturnEmptyOptional_WhenNoEntitiesExist() {
        Page<TestEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(mockJpaRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        Optional<TestEntity> result = testRepository.findFirst();

        assertTrue(result.isEmpty());
        verify(mockJpaRepository).findAll(PageRequest.of(0, 1));
    }

    @Test
    void create_ShouldSaveAndReturnEntity() {
        when(mockJpaRepository.save(testEntity)).thenReturn(testEntity);

        TestEntity result = testRepository.create(testEntity);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).save(testEntity);
    }

    @Test
    void update_ShouldUpdateAndReturnEntity_WhenEntityExists() {
        when(mockJpaRepository.existsById(testId)).thenReturn(true);
        when(mockJpaRepository.save(testEntity)).thenReturn(testEntity);

        TestEntity result = testRepository.update(testId, testEntity);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository).save(testEntity);
    }

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

    @Test
    void deleteById_ShouldReturnTrue_WhenEntityExists() {
        when(mockJpaRepository.existsById(testId)).thenReturn(true);

        boolean result = testRepository.deleteById(testId);

        assertTrue(result);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository).deleteById(testId);
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenEntityDoesNotExist() {
        when(mockJpaRepository.existsById(testId)).thenReturn(false);

        boolean result = testRepository.deleteById(testId);

        assertFalse(result);
        verify(mockJpaRepository).existsById(testId);
        verify(mockJpaRepository, never()).deleteById(any());
    }

    @Test
    void findAllWithPageable_ShouldReturnPagedEntities() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TestEntity> expectedPage = new PageImpl<>(Arrays.asList(testEntity));
        when(mockJpaRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<TestEntity> result = testRepository.findAll(pageable);

        assertEquals(expectedPage, result);
        verify(mockJpaRepository).findAll(pageable);
    }

    @Test
    void count_ShouldReturnEntityCount() {
        long expectedCount = 5L;
        when(mockJpaRepository.count()).thenReturn(expectedCount);

        long result = testRepository.count();

        assertEquals(expectedCount, result);
        verify(mockJpaRepository).count();
    }

    @Test
    void findAllById_ShouldReturnEntitiesById() {
        List<UUID> ids = Arrays.asList(testId);
        List<TestEntity> expectedEntities = Arrays.asList(testEntity);
        when(mockJpaRepository.findAllById(ids)).thenReturn(expectedEntities);

        List<TestEntity> result = testRepository.findAllById(ids);

        assertEquals(expectedEntities, result);
        verify(mockJpaRepository).findAllById(ids);
    }

    @Test
    void createAll_ShouldSaveAllEntities() {
        List<TestEntity> entities = Arrays.asList(testEntity);
        when(mockJpaRepository.saveAll(entities)).thenReturn(entities);

        List<TestEntity> result = testRepository.createAll(entities);

        assertEquals(entities, result);
        verify(mockJpaRepository).saveAll(entities);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenEntityExists() {
        when(mockJpaRepository.existsById(testId)).thenReturn(true);

        boolean result = testRepository.existsById(testId);

        assertTrue(result);
        verify(mockJpaRepository).existsById(testId);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenEntityDoesNotExist() {
        when(mockJpaRepository.existsById(testId)).thenReturn(false);

        boolean result = testRepository.existsById(testId);

        assertFalse(result);
        verify(mockJpaRepository).existsById(testId);
    }

    @Test
    void createAndFlush_ShouldSaveAndFlushEntity() {
        when(mockJpaRepository.saveAndFlush(testEntity)).thenReturn(testEntity);

        TestEntity result = testRepository.createAndFlush(testEntity);

        assertEquals(testEntity, result);
        verify(mockJpaRepository).saveAndFlush(testEntity);
    }

    @Test
    void flush_ShouldCallRepositoryFlush() {
        testRepository.flush();

        verify(mockJpaRepository).flush();
    }

    @Test
    void createWithMap_ShouldCreateEntityFromData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Entity");

        when(mockJpaRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        TestEntity result = testRepository.create(data);

        assertNotNull(result);
        verify(mockJpaRepository).save(any(TestEntity.class));
    }

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

    @Test
    void getEntityName_ShouldReturnDefaultEntityName() {
        DefaultEntityNameRepository defaultRepo = new DefaultEntityNameRepository(mockJpaRepository);

        // This will trigger the default getEntityName() method
        when(mockJpaRepository.findById(testId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> defaultRepo.findByIdOrFail(testId));

        assertTrue(exception.getCause().getMessage().contains("Entity"));
    }

    @Test
    void executeWithLogging_ShouldReturnDefaultValueOnException() {
        // Create a repository that will throw an exception for count operation
        when(mockJpaRepository.count()).thenThrow(new RuntimeException("Database error"));

        long result = testRepository.count();

        // Should return default value (0L) instead of throwing exception
        assertEquals(0L, result);
        verify(mockJpaRepository).count();
    }

    @Test
    void createEntityFromData_ShouldCreateEntitySuccessfully() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Entity");
        when(mockJpaRepository.save(any(TestEntity.class))).thenReturn(testEntity);

        TestEntity result = testRepository.create(data);

        assertNotNull(result);
        verify(mockJpaRepository).save(any(TestEntity.class));
    }

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

    private static class TestBaseRepository extends BaseRepository<TestEntity, JpaRepository<TestEntity, UUID>> {

        public TestBaseRepository(JpaRepository<TestEntity, UUID> repository) {
            super(repository);
        }

        @Override
        protected String getEntityName() {
            return "TestEntity";
        }
    }

    private static class DefaultEntityNameRepository extends BaseRepository<TestEntity, JpaRepository<TestEntity, UUID>> {

        public DefaultEntityNameRepository(JpaRepository<TestEntity, UUID> repository) {
            super(repository);
        }
    }


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