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
 * Unit tests for UserService.
 *
 * <p>This test class provides comprehensive unit testing for the UserService,
 * covering authentication, password management, account deletion, and data export
 * functionality with security and GDPR compliance verification.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *     <li>Login with valid credentials</li>
 *     <li>Login with invalid credentials</li>
 *     <li>Account lockout scenarios</li>
 *     <li>Password change functionality</li>
 *     <li>Account deletion (GDPR Article 17)</li>
 *     <li>Data export (GDPR Article 20)</li>
 *     <li>Password validation enforcement</li>
 *     <li>Rate limiting and security features</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepositoryContract userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private User testUser;

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

    // ==================== LOGIN TESTS ====================

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

    // ==================== CHANGE PASSWORD TESTS ====================

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

    // ==================== DELETE ACCOUNT TESTS ====================

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

    // ==================== EXPORT USER DATA TESTS ====================

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
