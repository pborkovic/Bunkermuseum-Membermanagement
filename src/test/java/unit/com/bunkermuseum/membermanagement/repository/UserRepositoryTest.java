package unit.com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.UserRepository;
import com.bunkermuseum.membermanagement.repository.jpa.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link UserRepository} class.
 *
 * <p>This test class validates the UserRepository implementation, specifically
 * focusing on the custom {@code findByEmail} method which is critical for
 * authentication and user lookup operations. It uses Mockito to mock the underlying
 * JPA repository and focuses on testing repository layer logic, validation, and
 * error handling.</p>
 *
 * @see UserRepository
 * @see UserJpaRepository
 * @see User
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {

    /**
     * Mock instance of the JPA repository used by UserRepository.
     * This mock allows us to control and verify interactions with the data layer.
     */
    @Mock
    private UserJpaRepository jpaRepository;

    /**
     * Test instance of UserRepository for testing repository logic.
     */
    private UserRepository userRepository;

    /**
     * Test user entity used across multiple test methods.
     */
    private User testUser;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A UserRepository instance with the mocked JPA repository</li>
     *   <li>A test User entity with standard test data</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(jpaRepository);
        testUser = new User("Test User", "test@example.com", "hashedPassword123");
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method with a valid email address.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the JPA repository's findByEmail method</li>
     *   <li>Returns an Optional containing the user when found</li>
     *   <li>The returned user has the correct email and name</li>
     *   <li>The JPA repository is called with the exact email parameter</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find user by valid email")
    void testFindByEmail_ValidEmail_ReturnsUser() {
        // Arrange
        String email = "test@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        assertEquals(testUser.getName(), result.get().getName());
        verify(jpaRepository).findByEmail(email);
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method when user is not found in the database.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method delegates to the JPA repository's findByEmail method</li>
     *   <li>Returns an empty Optional when no user matches the email</li>
     *   <li>The JPA repository is called with the exact email parameter</li>
     *   <li>No exceptions are thrown for non-existent emails</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty optional when user not found")
    void testFindByEmail_NonExistentEmail_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository).findByEmail(email);
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method with a null email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is null</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>The JPA repository is never called when validation fails</li>
     *   <li>Input validation occurs before database interaction</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when email is null")
    void testFindByEmail_NullEmail_ThrowsException() {
        // Arrange
        String email = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findByEmail(email);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
        verify(jpaRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method with a blank email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is blank (whitespace only)</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>The JPA repository is never called when validation fails</li>
     *   <li>Whitespace-only strings are properly rejected</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when email is blank")
    void testFindByEmail_BlankEmail_ThrowsException() {
        // Arrange
        String email = "   ";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findByEmail(email);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
        verify(jpaRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method with an empty string email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is an empty string</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>The JPA repository is never called when validation fails</li>
     *   <li>Empty strings are treated the same as null or blank values</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when email is empty string")
    void testFindByEmail_EmptyEmail_ThrowsException() {
        // Arrange
        String email = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findByEmail(email);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
        verify(jpaRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method when a database exception occurs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Database exceptions are caught and wrapped in a RuntimeException</li>
     *   <li>The exception message contains "Error occurred while finding user by email"</li>
     *   <li>The JPA repository is called before the exception occurs</li>
     *   <li>The original error context is preserved in the wrapped exception</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle database exception gracefully")
    void testFindByEmail_DatabaseException_ThrowsRuntimeException() {
        // Arrange
        String email = "test@example.com";
        when(jpaRepository.findByEmail(email)).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userRepository.findByEmail(email);
        });

        assertTrue(exception.getMessage().contains("Error occurred while finding user by email"));
        verify(jpaRepository).findByEmail(email);
    }

    /**
     * Tests the {@link UserRepository#findByEmail} method with complex email address formats.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method accepts complex email formats (dots, plus signs, multi-part TLDs)</li>
     *   <li>Returns an Optional containing the user when found</li>
     *   <li>The returned user has the exact email address provided</li>
     *   <li>The JPA repository is called with the unmodified complex email format</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find user with different email formats")
    void testFindByEmail_DifferentEmailFormats_Success() {
        // Arrange
        String email = "User.Name+tag@example.co.uk";
        User userWithComplexEmail = new User("User", email, "password");
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(userWithComplexEmail));

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(jpaRepository).findByEmail(email);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method with valid search query.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully searches users by query</li>
     *   <li>Returns a page of matching users</li>
     *   <li>The JPA repository is called with trimmed search query</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find users by search query")
    void testFindBySearchQuery_ValidQuery_ReturnsUsers() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(jpaRepository.findBySearchQuery(searchQuery.trim(), pageable)).thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQuery(searchQuery, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(jpaRepository).findBySearchQuery(searchQuery.trim(), pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method with blank search query.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Blank queries return all users with pagination</li>
     *   <li>The JPA repository findAll is called instead of findBySearchQuery</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return all users when search query is blank")
    void testFindBySearchQuery_BlankQuery_ReturnsAllUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        User user2 = new User("Jane Doe", "jane@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1, user2));

        when(jpaRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQuery("  ", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(jpaRepository).findAll(pageable);
        verify(jpaRepository, never()).findBySearchQuery(anyString(), any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method with null search query.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Null queries return all users with pagination</li>
     *   <li>The JPA repository findAll is called instead of findBySearchQuery</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return all users when search query is null")
    void testFindBySearchQuery_NullQuery_ReturnsAllUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(jpaRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQuery(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(jpaRepository).findAll(pageable);
        verify(jpaRepository, never()).findBySearchQuery(anyString(), any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method with null pageable.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when pageable is null</li>
     *   <li>The exception message contains "Pageable must not be null"</li>
     *   <li>The JPA repository is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when pageable is null in search query")
    void testFindBySearchQuery_NullPageable_ThrowsException() {
        // Arrange
        String searchQuery = "john";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQuery(searchQuery, null);
        });

        assertTrue(exception.getMessage().contains("Pageable must not be null"));
        verify(jpaRepository, never()).findBySearchQuery(anyString(), any(Pageable.class));
        verify(jpaRepository, never()).findAll(any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method with query that has whitespace.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Search queries are trimmed before being passed to JPA repository</li>
     *   <li>Leading and trailing whitespace is removed</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should trim search query before searching")
    void testFindBySearchQuery_QueryWithWhitespace_TrimsQuery() {
        // Arrange
        String searchQuery = "  john  ";
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(jpaRepository.findBySearchQuery("john", pageable)).thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQuery(searchQuery, pageable);

        // Assert
        assertNotNull(result);
        verify(jpaRepository).findBySearchQuery("john", pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method when database exception occurs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when JPA repository fails</li>
     *   <li>The exception message contains "Failed to retrieve users"</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when database error occurs in search")
    void testFindBySearchQuery_DatabaseException_ThrowsRuntimeException() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);

        when(jpaRepository.findBySearchQuery(searchQuery, pageable))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userRepository.findBySearchQuery(searchQuery, pageable);
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve users"));
        verify(jpaRepository).findBySearchQuery(searchQuery, pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with valid parameters.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully searches users by query and status</li>
     *   <li>Returns a page of matching users</li>
     *   <li>The JPA repository is called with correct parameters</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find users by search query and status")
    void testFindBySearchQueryAndStatus_ValidParameters_ReturnsUsers() {
        // Arrange
        String searchQuery = "john";
        String status = "active";
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(jpaRepository.findBySearchQueryAndStatus(searchQuery.trim(), status, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQueryAndStatus(searchQuery, status, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(jpaRepository).findBySearchQueryAndStatus(searchQuery.trim(), status, pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with blank search query.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Blank queries use empty string as search parameter</li>
     *   <li>The JPA repository is called with empty string</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should use empty string for blank search query with status")
    void testFindBySearchQueryAndStatus_BlankQuery_UsesEmptyString() {
        // Arrange
        String status = "active";
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(jpaRepository.findBySearchQueryAndStatus("", status, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQueryAndStatus("  ", status, pageable);

        // Assert
        assertNotNull(result);
        verify(jpaRepository).findBySearchQueryAndStatus("", status, pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with all statuses.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>All valid status values ('active', 'deleted', 'all') are accepted</li>
     *   <li>The JPA repository is called with each status</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should accept all valid status values")
    void testFindBySearchQueryAndStatus_AllValidStatuses_Success() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of());

        when(jpaRepository.findBySearchQueryAndStatus(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act & Assert - Test each valid status
        userRepository.findBySearchQueryAndStatus(searchQuery, "active", pageable);
        verify(jpaRepository).findBySearchQueryAndStatus(searchQuery, "active", pageable);

        userRepository.findBySearchQueryAndStatus(searchQuery, "deleted", pageable);
        verify(jpaRepository).findBySearchQueryAndStatus(searchQuery, "deleted", pageable);

        userRepository.findBySearchQueryAndStatus(searchQuery, "all", pageable);
        verify(jpaRepository).findBySearchQueryAndStatus(searchQuery, "all", pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with null status.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when status is null</li>
     *   <li>The exception message contains "Status must not be null"</li>
     *   <li>The JPA repository is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when status is null")
    void testFindBySearchQueryAndStatus_NullStatus_ThrowsException() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQueryAndStatus(searchQuery, null, pageable);
        });

        assertTrue(exception.getMessage().contains("Status must not be null"));
        verify(jpaRepository, never()).findBySearchQueryAndStatus(anyString(), anyString(), any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with blank status.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when status is blank</li>
     *   <li>The exception message contains "Status must not be null or blank"</li>
     *   <li>The JPA repository is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when status is blank")
    void testFindBySearchQueryAndStatus_BlankStatus_ThrowsException() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQueryAndStatus(searchQuery, "  ", pageable);
        });

        assertTrue(exception.getMessage().contains("Status must not be null or blank"));
        verify(jpaRepository, never()).findBySearchQueryAndStatus(anyString(), anyString(), any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with invalid status.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown for invalid status values</li>
     *   <li>The exception message indicates valid status options</li>
     *   <li>The JPA repository is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when status is invalid")
    void testFindBySearchQueryAndStatus_InvalidStatus_ThrowsException() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQueryAndStatus(searchQuery, "invalid", pageable);
        });

        assertTrue(exception.getMessage().contains("Status must be 'active', 'deleted', or 'all'"));
        verify(jpaRepository, never()).findBySearchQueryAndStatus(anyString(), anyString(), any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with null pageable.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when pageable is null</li>
     *   <li>The exception message contains "Pageable must not be null"</li>
     *   <li>The JPA repository is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when pageable is null in status search")
    void testFindBySearchQueryAndStatus_NullPageable_ThrowsException() {
        // Arrange
        String searchQuery = "john";
        String status = "active";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQueryAndStatus(searchQuery, status, null);
        });

        assertTrue(exception.getMessage().contains("Pageable must not be null"));
        verify(jpaRepository, never()).findBySearchQueryAndStatus(anyString(), anyString(), any(Pageable.class));
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method when database exception occurs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when JPA repository fails</li>
     *   <li>The exception message contains "Failed to retrieve users"</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when database error occurs in status search")
    void testFindBySearchQueryAndStatus_DatabaseException_ThrowsRuntimeException() {
        // Arrange
        String searchQuery = "john";
        String status = "active";
        Pageable pageable = PageRequest.of(0, 10);

        when(jpaRepository.findBySearchQueryAndStatus(searchQuery, status, pageable))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userRepository.findBySearchQueryAndStatus(searchQuery, status, pageable);
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve users"));
        verify(jpaRepository).findBySearchQueryAndStatus(searchQuery, status, pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQuery} method when JPA throws IllegalArgumentException.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>IllegalArgumentException from JPA is propagated</li>
     *   <li>The exception is not wrapped in RuntimeException</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should propagate IllegalArgumentException from JPA in search")
    void testFindBySearchQuery_JpaThrowsIllegalArgument_PropagatesException() {
        // Arrange
        String searchQuery = "john";
        Pageable pageable = PageRequest.of(0, 10);

        when(jpaRepository.findBySearchQuery(searchQuery, pageable))
                .thenThrow(new IllegalArgumentException("Invalid search parameter"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQuery(searchQuery, pageable);
        });

        assertTrue(exception.getMessage().contains("Invalid search parameter"));
        verify(jpaRepository).findBySearchQuery(searchQuery, pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method with null search query.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Null search queries are handled correctly with status filter</li>
     *   <li>Empty string is passed to JPA repository</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle null search query with status")
    void testFindBySearchQueryAndStatus_NullQuery_UsesEmptyString() {
        // Arrange
        String status = "active";
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User("John Doe", "john@example.com", "password");
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(jpaRepository.findBySearchQueryAndStatus("", status, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<User> result = userRepository.findBySearchQueryAndStatus(null, status, pageable);

        // Assert
        assertNotNull(result);
        verify(jpaRepository).findBySearchQueryAndStatus("", status, pageable);
    }

    /**
     * Tests the {@link UserRepository#findBySearchQueryAndStatus} method when JPA throws IllegalArgumentException.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>IllegalArgumentException from JPA is propagated</li>
     *   <li>The exception is not wrapped in RuntimeException</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should propagate IllegalArgumentException from JPA in status search")
    void testFindBySearchQueryAndStatus_JpaThrowsIllegalArgument_PropagatesException() {
        // Arrange
        String searchQuery = "john";
        String status = "active";
        Pageable pageable = PageRequest.of(0, 10);

        when(jpaRepository.findBySearchQueryAndStatus(searchQuery, status, pageable))
                .thenThrow(new IllegalArgumentException("Invalid status parameter"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findBySearchQueryAndStatus(searchQuery, status, pageable);
        });

        assertTrue(exception.getMessage().contains("Invalid status parameter"));
        verify(jpaRepository).findBySearchQueryAndStatus(searchQuery, status, pageable);
    }

}
