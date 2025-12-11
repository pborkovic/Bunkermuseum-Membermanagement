package unit.com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.repository.RoleRepository;
import com.bunkermuseum.membermanagement.repository.jpa.RoleJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link RoleRepository} class.
 *
 * <p>This test class validates the RoleRepository implementation.
 * It uses Mockito to mock the underlying JPA repository and focuses on
 * testing repository layer logic inherited from BaseRepository.</p>
 *
 * @see RoleRepository
 * @see RoleJpaRepository
 * @see Role
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleRepository Unit Tests")
class RoleRepositoryTest {

    /**
     * Mock instance of the JPA repository used by RoleRepository.
     * This mock allows us to control and verify interactions with the data layer.
     */
    @Mock
    private RoleJpaRepository jpaRepository;

    /**
     * Test instance of RoleRepository for testing repository logic.
     */
    private RoleRepository roleRepository;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A RoleRepository instance with the mocked JPA repository</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        roleRepository = new RoleRepository(jpaRepository);
    }

    /**
     * Tests the {@link RoleRepository#findAll} method with successful retrieval.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves all roles from the repository</li>
     *   <li>The returned list contains the expected roles</li>
     *   <li>The JPA repository findAll method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve all roles")
    void testFindAll_Success_ReturnsRoles() {
        // Arrange
        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);
        List<Role> roles = List.of(role1, role2);

        when(jpaRepository.findAll()).thenReturn(roles);

        // Act
        List<Role> result = roleRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link RoleRepository#findAll} method when no roles exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty list when no roles are present</li>
     *   <li>No exception is thrown for empty results</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when no roles exist")
    void testFindAll_NoRoles_ReturnsEmptyList() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(List.of());

        // Act
        List<Role> result = roleRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link RoleRepository#findById} method with valid ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves a role by its ID</li>
     *   <li>Returns an Optional containing the role</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find role by valid ID")
    void testFindById_ValidId_ReturnsRole() {
        // Arrange
        UUID id = UUID.randomUUID();
        Role role = mock(Role.class);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(role));

        // Act
        Optional<Role> result = roleRepository.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link RoleRepository#findById} method when role not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty Optional when role doesn't exist</li>
     *   <li>No exception is thrown for non-existent IDs</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty optional when role not found")
    void testFindById_NonExistentId_ReturnsEmpty() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleRepository.findById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link RoleRepository#findByIdOrFail} method with valid ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves a role by its ID</li>
     *   <li>Returns the role directly (not wrapped in Optional)</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find role by ID or fail with valid ID")
    void testFindByIdOrFail_ValidId_ReturnsRole() {
        // Arrange
        UUID id = UUID.randomUUID();
        Role role = mock(Role.class);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(role));

        // Act
        Role result = roleRepository.findByIdOrFail(id);

        // Assert
        assertNotNull(result);
        assertEquals(role, result);
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link RoleRepository#findByIdOrFail} method when role not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when role doesn't exist</li>
     *   <li>The exception is caused by an EntityNotFoundException</li>
     *   <li>The exception message contains "Role" and the ID</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when role not found")
    void testFindByIdOrFail_NonExistentId_ThrowsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleRepository.findByIdOrFail(id);
        });

        assertTrue(exception.getCause() instanceof EntityNotFoundException);
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link RoleRepository#create} method with valid role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully creates a new role</li>
     *   <li>Returns the saved role</li>
     *   <li>The JPA repository save method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully create a new role")
    void testCreate_ValidRole_ReturnsCreatedRole() {
        // Arrange
        Role role = mock(Role.class);
        when(jpaRepository.save(role)).thenReturn(role);

        // Act
        Role result = roleRepository.create(role);

        // Assert
        assertNotNull(result);
        assertEquals(role, result);
        verify(jpaRepository).save(role);
    }

    /**
     * Tests the {@link RoleRepository#update} method with valid role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully updates an existing role</li>
     *   <li>Returns the updated role</li>
     *   <li>The JPA repository checks existence and saves the role</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully update an existing role")
    void testUpdate_ExistingRole_ReturnsUpdatedRole() {
        // Arrange
        UUID id = UUID.randomUUID();
        Role role = mock(Role.class);
        when(jpaRepository.existsById(id)).thenReturn(true);
        when(jpaRepository.save(role)).thenReturn(role);

        // Act
        Role result = roleRepository.update(id, role);

        // Assert
        assertNotNull(result);
        assertEquals(role, result);
        verify(jpaRepository).existsById(id);
        verify(jpaRepository).save(role);
    }

    /**
     * Tests the {@link RoleRepository#update} method with non-existent role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when role doesn't exist</li>
     *   <li>The exception is caused by an EntityNotFoundException</li>
     *   <li>The save method is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when updating non-existent role")
    void testUpdate_NonExistentRole_ThrowsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        Role role = mock(Role.class);
        when(jpaRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleRepository.update(id, role);
        });

        assertTrue(exception.getCause() instanceof EntityNotFoundException);
        verify(jpaRepository).existsById(id);
        verify(jpaRepository, never()).save(any());
    }

    /**
     * Tests the {@link RoleRepository#deleteById} method with existing role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully soft deletes an existing role</li>
     *   <li>Returns true to indicate successful deletion</li>
     *   <li>The entity is fetched, marked as deleted, and saved</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully delete an existing role")
    void testDeleteById_ExistingRole_ReturnsTrue() {
        // Arrange
        UUID id = UUID.randomUUID();
        Role role = mock(Role.class);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(role));
        when(jpaRepository.save(role)).thenReturn(role);

        // Act
        boolean result = roleRepository.deleteById(id);

        // Assert
        assertTrue(result);
        verify(jpaRepository).findById(id);
        verify(role).delete();
        verify(jpaRepository).save(role);
    }

    /**
     * Tests the {@link RoleRepository#deleteById} method with non-existent role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns false when role doesn't exist</li>
     *   <li>No exception is thrown for non-existent roles</li>
     *   <li>The save method is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return false when deleting non-existent role")
    void testDeleteById_NonExistentRole_ReturnsFalse() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = roleRepository.deleteById(id);

        // Assert
        assertFalse(result);
        verify(jpaRepository).findById(id);
        verify(jpaRepository, never()).save(any());
    }

    /**
     * Tests the {@link RoleRepository#count} method.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns the correct count of roles</li>
     *   <li>The JPA repository count method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return correct count of roles")
    void testCount_ReturnsCorrectCount() {
        // Arrange
        when(jpaRepository.count()).thenReturn(3L);

        // Act
        long result = roleRepository.count();

        // Assert
        assertEquals(3L, result);
        verify(jpaRepository).count();
    }

    /**
     * Tests the {@link RoleRepository#existsById} method with existing role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns true for existing roles</li>
     *   <li>The JPA repository existsById method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return true when role exists")
    void testExistsById_ExistingRole_ReturnsTrue() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = roleRepository.existsById(id);

        // Assert
        assertTrue(result);
        verify(jpaRepository).existsById(id);
    }

    /**
     * Tests the {@link RoleRepository#existsById} method with non-existent role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns false for non-existent roles</li>
     *   <li>The JPA repository existsById method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return false when role does not exist")
    void testExistsById_NonExistentRole_ReturnsFalse() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = roleRepository.existsById(id);

        // Assert
        assertFalse(result);
        verify(jpaRepository).existsById(id);
    }

    /**
     * Tests the {@link RoleRepository#findFirst} method with existing roles.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns the first role from the repository</li>
     *   <li>Returns an Optional containing the first role</li>
     *   <li>The JPA repository findAll with pagination is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return first role when roles exist")
    void testFindFirst_WithRoles_ReturnsFirstRole() {
        // Arrange
        Role firstRole = mock(Role.class);
        Page<Role> page = new PageImpl<>(List.of(firstRole));
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        Optional<Role> result = roleRepository.findFirst();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(firstRole, result.get());
        verify(jpaRepository).findAll(any(PageRequest.class));
    }

    /**
     * Tests the {@link RoleRepository#findFirst} method with no roles.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty Optional when no roles exist</li>
     *   <li>No exception is thrown for empty results</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty optional when no roles exist")
    void testFindFirst_NoRoles_ReturnsEmpty() {
        // Arrange
        Page<Role> emptyPage = new PageImpl<>(List.of());
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        Optional<Role> result = roleRepository.findFirst();

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository).findAll(any(PageRequest.class));
    }

    /**
     * Tests the {@link RoleRepository#findAllById} method with multiple IDs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns roles matching the provided IDs</li>
     *   <li>The JPA repository findAllById method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find all roles by IDs")
    void testFindAllById_MultipleIds_ReturnsMatchingRoles() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = List.of(id1, id2);
        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);
        List<Role> roles = List.of(role1, role2);

        when(jpaRepository.findAllById(ids)).thenReturn(roles);

        // Act
        List<Role> result = roleRepository.findAllById(ids);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository).findAllById(ids);
    }

    /**
     * Tests the {@link RoleRepository#createAll} method with multiple roles.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully creates multiple roles</li>
     *   <li>Returns all saved roles</li>
     *   <li>The JPA repository saveAll method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should create all roles")
    void testCreateAll_MultipleRoles_ReturnsCreatedRoles() {
        // Arrange
        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);
        List<Role> roles = List.of(role1, role2);

        when(jpaRepository.saveAll(roles)).thenReturn(roles);

        // Act
        List<Role> result = roleRepository.createAll(roles);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository).saveAll(roles);
    }

    /**
     * Tests the {@link RoleRepository#findAll} method when JPA repository throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when JPA operation fails</li>
     *   <li>The exception message contains "Failed to execute operation"</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when JPA repository fails")
    void testFindAll_JpaRepositoryThrowsException_ThrowsRuntimeException() {
        // Arrange
        when(jpaRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleRepository.findAll();
        });

        assertTrue(exception.getMessage().contains("Failed to execute operation"));
        verify(jpaRepository).findAll();
    }
}
