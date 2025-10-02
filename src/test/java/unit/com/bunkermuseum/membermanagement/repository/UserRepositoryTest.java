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
 * Unit tests for UserRepository.
 *
 * <p>This test class provides comprehensive unit testing for the UserRepository,
 * focusing on the findByEmail method and its error handling capabilities.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *     <li>Find user by valid email</li>
 *     <li>Find user by non-existent email</li>
 *     <li>Find user with null email validation</li>
 *     <li>Find user with blank email validation</li>
 *     <li>Exception handling during database operations</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {

    @Mock
    private UserJpaRepository jpaRepository;

    private UserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(jpaRepository);
        testUser = new User("Test User", "test@example.com", "hashedPassword123");
    }

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
