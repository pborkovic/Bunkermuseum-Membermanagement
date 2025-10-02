package unit.com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link UserService} class.
 *
 * <p>This test class validates all authentication, password management, account
 * lifecycle, and GDPR compliance operations provided by the UserService implementation.
 * It uses Mockito to mock repository and password encoding dependencies, focusing on
 * testing business logic, security features, and compliance requirements.</p>
 *
 * <p><strong>Authentication Flow Testing:</strong></p>
 * <p>These tests validate the complete authentication flow including login attempts,
 * password verification, account lockout, and security logging. The tests ensure
 * compliance with OWASP security standards and proper handling of edge cases.</p>
 *
 * @see UserService
 * @see UserRepositoryContract
 * @see PasswordEncoder
 * @see User
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    /**
     * Mock instance of the user repository contract used by UserService.
     * This mock allows us to control and verify data layer interactions.
     */
    @Mock
    private UserRepositoryContract userRepository;

    /**
     * Mock instance of the password encoder for BCrypt operations.
     * This mock allows us to control password hashing and verification.
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * Test instance of UserService for testing business logic and security features.
     */
    private UserService userService;

    /**
     * Test user entity used across multiple test methods.
     */
    private User testUser;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A UserService instance with mocked repository and password encoder</li>
     *   <li>A test User entity with verified email and hashed password</li>
     *   <li>Uses reflection to set the protected ID field in the test user</li>
     * </ul>
     *
     * <p>The reflection approach is necessary because the ID field is protected
     * in the Model base class and cannot be set directly in tests.</p>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
        testUser = new User("Test User", "test@example.com", "hashedPassword123");
        testUser.setEmailVerifiedAt(Instant.now());

        try {
            java.lang.reflect.Field idField = testUser.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test user ID", e);
        }
    }

    /**
     * Tests the {@link UserService#login} method with valid email and password credentials.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully authenticates a user with correct credentials</li>
     *   <li>Returns the authenticated User entity when password matches</li>
     *   <li>The password encoder is used to verify the password hash</li>
     *   <li>The user repository is called with the provided email</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_ValidCredentials_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        User result = userService.login(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    /**
     * Tests the {@link UserService#login} method with an incorrect password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns null when password does not match</li>
     *   <li>The user repository is called to find the user by email</li>
     *   <li>The password encoder is used to verify the password hash</li>
     *   <li>Failed login attempts are tracked for account lockout mechanism</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return null for invalid credentials")
    void testLogin_InvalidCredentials_ReturnsNull() {
        // Arrange
        String email = "test@example.com";
        String password = "WrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // Act
        User result = userService.login(email, password);

        // Assert
        assertNull(result);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    /**
     * Tests the {@link UserService#login} method when the user email does not exist in the database.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns null when no user is found with the provided email</li>
     *   <li>The user repository returns an empty Optional for non-existent emails</li>
     *   <li>The password encoder is never called when user doesn't exist</li>
     *   <li>No exceptions are thrown for non-existent users</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return null when user not found")
    void testLogin_UserNotFound_ReturnsNull() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "Password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        User result = userService.login(email, password);

        // Assert
        assertNull(result);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    /**
     * Tests the {@link UserService#login} method with a null email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is null</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>The user repository is never called when validation fails</li>
     *   <li>Input validation occurs before any database interaction</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null email")
    void testLogin_NullEmail_ThrowsException() {
        // Arrange
        String email = null;
        String password = "Password123";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
        verify(userRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserService#login} method with a blank email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is blank (whitespace only)</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>The user repository is never called when validation fails</li>
     *   <li>Whitespace-only strings are properly rejected</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for blank email")
    void testLogin_BlankEmail_ThrowsException() {
        // Arrange
        String email = "   ";
        String password = "Password123";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
        verify(userRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserService#login} method with a null password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when password is null</li>
     *   <li>The exception message contains "Password must not be null or blank"</li>
     *   <li>The user repository is never called when validation fails</li>
     *   <li>Input validation occurs before any database interaction</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null password")
    void testLogin_NullPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Password must not be null or blank"));
        verify(userRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserService#login} method with a blank password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when password is empty</li>
     *   <li>The exception message contains "Password must not be null or blank"</li>
     *   <li>The user repository is never called when validation fails</li>
     *   <li>Empty strings are treated the same as null values</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for blank password")
    void testLogin_BlankPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Password must not be null or blank"));
        verify(userRepository, never()).findByEmail(any());
    }

    /**
     * Tests the {@link UserService#login} method account lockout mechanism after multiple failed attempts.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The account is locked after 5 consecutive failed login attempts</li>
     *   <li>A RuntimeException is thrown on the 6th attempt with "locked" in the message</li>
     *   <li>The user repository is called 5 times before lockout occurs</li>
     *   <li>The lockout mechanism protects against brute-force attacks</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should lock account after 5 failed login attempts")
    void testLogin_FiveFailedAttempts_LocksAccount() {
        // Arrange
        String email = "test@example.com";
        String wrongPassword = "WrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPassword())).thenReturn(false);

        // Act - Attempt 5 failed logins
        for (int i = 0; i < 5; i++) {
            userService.login(email, wrongPassword);
        }

        // Assert - 6th attempt should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(email, wrongPassword);
        });

        assertTrue(exception.getMessage().contains("locked"));
        verify(userRepository, times(5)).findByEmail(email);
    }

    /**
     * Tests the {@link UserService#login} method failed attempt counter reset on successful login.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Failed login attempt counter is reset to zero on successful authentication</li>
     *   <li>A successful login after failed attempts returns the authenticated user</li>
     *   <li>The user repository is called for each login attempt (failed and successful)</li>
     *   <li>Users can authenticate normally after previous failed attempts</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should reset failed attempts on successful login")
    void testLogin_SuccessfulLogin_ResetsFailedAttempts() {
        // Arrange
        String email = "test@example.com";
        String wrongPassword = "WrongPassword";
        String correctPassword = "CorrectPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.matches(correctPassword, testUser.getPassword())).thenReturn(true);

        // Act - 3 failed attempts, then 1 successful
        for (int i = 0; i < 3; i++) {
            userService.login(email, wrongPassword);
        }
        User result = userService.login(email, correctPassword);

        // Assert - Should be able to login without lockout
        assertNotNull(result);
        verify(userRepository, times(4)).findByEmail(email);
    }

    /**
     * Tests the {@link UserService#login} method when user has no password (OAuth-only account).
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns null when user password is null (OAuth-only account)</li>
     *   <li>The user repository is called to find the user by email</li>
     *   <li>The password encoder is never called when user has no password</li>
     *   <li>OAuth-only accounts cannot authenticate with password-based login</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return null when user has no password set")
    void testLogin_UserWithNoPassword_ReturnsNull() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123";
        User userWithoutPassword = new User("Test User", email, null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithoutPassword));

        // Act
        User result = userService.login(email, password);

        // Assert
        assertNull(result);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    /**
     * Tests the {@link UserService#changePassword} method with valid current password and new password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully changes the password when current password is correct</li>
     *   <li>The current password is verified using the password encoder</li>
     *   <li>The new password is hashed before being saved to the database</li>
     *   <li>The user repository update method is called with the hashed new password</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully change password with valid credentials")
    void testChangePassword_ValidCredentials_Success() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "OldPassword123";
        String newPassword = "NewP@ssw0rd!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPassword");
        when(userRepository.update(any(UUID.class), any(User.class))).thenReturn(testUser);

        // Act
        userService.changePassword(email, currentPassword, newPassword);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, "hashedPassword123");
        verify(passwordEncoder).matches(newPassword, "hashedPassword123");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).update(testUser.getId(), testUser);
    }

    /**
     * Tests the {@link UserService#changePassword} method with an incorrect current password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when current password is incorrect</li>
     *   <li>The exception message contains "Invalid current password"</li>
     *   <li>The user repository update method is never called when verification fails</li>
     *   <li>Password verification occurs before any other validation</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when current password is invalid")
    void testChangePassword_InvalidCurrentPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "WrongPassword";
        String newPassword = "NewPassword456!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(email, currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("Invalid current password"));
        verify(userRepository, never()).update(any(), any());
    }

    /**
     * Tests the {@link UserService#changePassword} method with a weak new password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when new password doesn't meet strength requirements</li>
     *   <li>The exception message contains "validation failed"</li>
     *   <li>The user repository update method is never called when validation fails</li>
     *   <li>Password strength validation occurs after current password verification</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when new password is weak")
    void testChangePassword_WeakPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "OldPassword123";
        String weakPassword = "weak";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(email, currentPassword, weakPassword);
        });

        assertTrue(exception.getMessage().contains("validation failed"));
        verify(userRepository, never()).update(any(), any());
    }

    /**
     * Tests the {@link UserService#changePassword} method when new password is same as current password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when new password matches current password</li>
     *   <li>The exception message contains "different"</li>
     *   <li>The user repository update method is never called for same password</li>
     *   <li>Password reuse prevention is enforced</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when new password equals current password")
    void testChangePassword_SamePassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "SameP@ssw0rd!";
        String newPassword = "SameP@ssw0rd!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(email, currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("different"));
        verify(userRepository, never()).update(any(), any());
    }

    /**
     * Tests the {@link UserService#changePassword} method with a null email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is null</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null email is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null email in change password")
    void testChangePassword_NullEmail_ThrowsException() {
        // Arrange
        String email = null;
        String currentPassword = "OldPassword123";
        String newPassword = "NewPassword456!";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(email, currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#changePassword} method with a null current password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when current password is null</li>
     *   <li>The exception message contains "Current password must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null current password is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null current password in change password")
    void testChangePassword_NullCurrentPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = null;
        String newPassword = "NewPassword456!";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(email, currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("Current password must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#changePassword} method with a null new password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when new password is null</li>
     *   <li>The exception message contains "New password must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null new password is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null new password in change password")
    void testChangePassword_NullNewPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "OldPassword123";
        String newPassword = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(email, currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("New password must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#deleteAccount} method with valid password (GDPR Article 17).
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully deletes the user account when password is correct</li>
     *   <li>Password verification occurs before account deletion</li>
     *   <li>The user repository deleteById method is called with the user's ID</li>
     *   <li>GDPR right to erasure is properly implemented</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully delete account with valid password")
    void testDeleteAccount_ValidPassword_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(userRepository.deleteById(testUser.getId())).thenReturn(true);

        // Act
        userService.deleteAccount(email, password);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(userRepository).deleteById(testUser.getId());
    }

    /**
     * Tests the {@link UserService#deleteAccount} method with an incorrect password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when password verification fails</li>
     *   <li>The exception message contains "Invalid credentials"</li>
     *   <li>The user repository deleteById method is never called when verification fails</li>
     *   <li>Account deletion requires valid password confirmation</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when deleting account with invalid password")
    void testDeleteAccount_InvalidPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "WrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteAccount(email, password);
        });

        assertTrue(exception.getMessage().contains("Invalid credentials"));
        verify(userRepository, never()).deleteById(any());
    }

    /**
     * Tests the {@link UserService#deleteAccount} method with a null email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is null</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null email is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null email in delete account")
    void testDeleteAccount_NullEmail_ThrowsException() {
        // Arrange
        String email = null;
        String password = "Password123";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteAccount(email, password);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#deleteAccount} method with a null password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when password is null</li>
     *   <li>The exception message contains "Password must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null password is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null password in delete account")
    void testDeleteAccount_NullPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteAccount(email, password);
        });

        assertTrue(exception.getMessage().contains("Password must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#exportUserData} method with valid password (GDPR Articles 15 & 20).
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully exports user data in JSON format when password is correct</li>
     *   <li>The JSON contains personalData, accountMetadata, and gdprNotice sections</li>
     *   <li>Password verification occurs before data export</li>
     *   <li>GDPR right to data portability is properly implemented</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully export user data with valid password")
    void testExportUserData_ValidPassword_ReturnsJson() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        String result = userService.exportUserData(email, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("personalData"));
        assertTrue(result.contains("Test User"));
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("accountMetadata"));
        assertTrue(result.contains("gdprNotice"));
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    /**
     * Tests the {@link UserService#exportUserData} method with an incorrect password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when password verification fails</li>
     *   <li>The exception message contains "Invalid credentials"</li>
     *   <li>No user data is exported when verification fails</li>
     *   <li>Data export requires valid password confirmation</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when exporting data with invalid password")
    void testExportUserData_InvalidPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "WrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.exportUserData(email, password);
        });

        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    /**
     * Tests the {@link UserService#exportUserData} method with a null email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is null</li>
     *   <li>The exception message contains "Email must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null email is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null email in export data")
    void testExportUserData_NullEmail_ThrowsException() {
        // Arrange
        String email = null;
        String password = "Password123";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.exportUserData(email, password);
        });

        assertTrue(exception.getMessage().contains("Email must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#exportUserData} method with a null password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when password is null</li>
     *   <li>The exception message contains "Password must not be null or blank"</li>
     *   <li>Input validation occurs before any database or password operations</li>
     *   <li>Null password is rejected at the service layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception for null password in export data")
    void testExportUserData_NullPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.exportUserData(email, password);
        });

        assertTrue(exception.getMessage().contains("Password must not be null or blank"));
    }

    /**
     * Tests the {@link UserService#exportUserData} method when user has OAuth integrations linked.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The exported JSON includes OAuth integration status</li>
     *   <li>Google and Microsoft linked status are correctly set to true</li>
     *   <li>OAuth integration data is included in the GDPR data export</li>
     *   <li>All connected third-party services are disclosed in the export</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should export data with OAuth integrations when present")
    void testExportUserData_WithOAuthIntegrations_IncludesOAuthData() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";
        testUser.setGoogleId("google-123");
        testUser.setMicrosoftId("microsoft-456");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        String result = userService.exportUserData(email, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\"googleLinked\": true"));
        assertTrue(result.contains("\"microsoftLinked\": true"));
    }

    /**
     * Tests the {@link UserService#exportUserData} method when user email is unverified.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The exported JSON shows emailVerified as false</li>
     *   <li>The emailVerifiedAt field shows "null" when email is not verified</li>
     *   <li>Email verification status is accurately reported in data export</li>
     *   <li>Unverified email state is properly disclosed to the user</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should export data with unverified email status")
    void testExportUserData_UnverifiedEmail_ShowsUnverifiedStatus() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";
        testUser.setEmailVerifiedAt(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        String result = userService.exportUserData(email, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\"emailVerified\": false"));
        assertTrue(result.contains("\"emailVerifiedAt\": \"null\""));
    }

    /**
     * Tests the {@link UserService#exportUserData} method with special characters in user data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Special characters like quotes and newlines are properly escaped in JSON</li>
     *   <li>The exported JSON is valid and properly formatted</li>
     *   <li>User data containing special characters is safely encoded</li>
     *   <li>JSON escaping prevents malformed output for edge case data</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should properly escape special characters in exported JSON")
    void testExportUserData_SpecialCharacters_ProperlyEscaped() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";
        User userWithSpecialChars = new User("Test \"User\" \n Name", email, "password");
        userWithSpecialChars.setEmailVerifiedAt(Instant.now());

        try {
            java.lang.reflect.Field idField = userWithSpecialChars.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userWithSpecialChars, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithSpecialChars));
        when(passwordEncoder.matches(password, userWithSpecialChars.getPassword())).thenReturn(true);

        // Act
        String result = userService.exportUserData(email, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\\\""));
        assertTrue(result.contains("\\n"));
    }
}
