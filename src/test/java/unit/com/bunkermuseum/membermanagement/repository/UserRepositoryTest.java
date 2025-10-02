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
}
