package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.AuthController;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.contract.RoleServiceContract;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController.
 *
 * <p>This test class provides comprehensive unit testing for the AuthController,
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
 *     <li>IP address extraction</li>
 *     <li>Session management</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private UserServiceContract userService;

    @Mock
    private RoleServiceContract roleService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AuthController authController;
    private User testUser;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService, roleService, request);

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

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_ValidCredentials_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "ValidPassword123";
        String clientIp = "192.168.1.1";

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(userService.login(email, password)).thenReturn(testUser);
        when(request.getSession(true)).thenReturn(session);

        // Act
        AuthController.LoginResponse response = authController.login(email, password);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getName(), response.name());
        assertEquals(testUser.getEmail(), response.email());
        assertTrue(response.emailVerified());

        verify(userService).login(email, password);
        verify(request).getSession(true);
        verify(session).setAttribute(anyString(), any(SecurityContext.class));
    }

    @Test
    @DisplayName("Should return null for invalid credentials")
    void testLogin_InvalidCredentials_ReturnsNull() {
        // Arrange
        String email = "test@example.com";
        String password = "WrongPassword";
        String clientIp = "192.168.1.1";

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(userService.login(email, password)).thenReturn(null);

        // Act
        AuthController.LoginResponse response = authController.login(email, password);

        // Assert
        assertNull(response);
        verify(userService).login(email, password);
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("Should throw exception when account is locked")
    void testLogin_AccountLocked_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123";
        String clientIp = "192.168.1.1";

        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(userService.login(email, password))
                .thenThrow(new RuntimeException("Account is temporarily locked due to too many failed login attempts. Please try again later."));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.login(email, password);
        });

        assertTrue(exception.getMessage().contains("locked"));
        verify(userService).login(email, password);
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void testLogin_NullEmail_ThrowsException() {
        // Arrange
        String email = null;
        String password = "Password123";

        when(userService.login(email, password))
                .thenThrow(new IllegalArgumentException("Email must not be null or blank"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Email"));
    }

    @Test
    @DisplayName("Should throw exception for blank password")
    void testLogin_BlankPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "";

        when(userService.login(email, password))
                .thenThrow(new IllegalArgumentException("Password must not be null or blank"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Password"));
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For header")
    void testLogin_WithProxyHeaders_ExtractsCorrectIp() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123";
        String forwardedIp = "203.0.113.1";

        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIp + ", 10.0.0.1");
        when(userService.login(email, password)).thenReturn(testUser);
        when(request.getSession(true)).thenReturn(session);

        // Act
        AuthController.LoginResponse response = authController.login(email, password);

        // Assert
        assertNotNull(response);
        verify(request).getHeader("X-Forwarded-For");
    }

    @Test
    @DisplayName("Should successfully change password with valid credentials")
    void testChangePassword_ValidCredentials_Success() {
        // Arrange
        String currentPassword = "OldPassword123";
        String newPassword = "NewPassword456";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        doNothing().when(userService).changePassword(testUser.getEmail(), currentPassword, newPassword);

        // Act
        authController.changePassword(currentPassword, newPassword);

        // Assert
        verify(userService).changePassword(testUser.getEmail(), currentPassword, newPassword);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when changing password with invalid current password")
    void testChangePassword_InvalidCurrentPassword_ThrowsException() {
        // Arrange
        String currentPassword = "WrongPassword";
        String newPassword = "NewPassword456";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        doThrow(new RuntimeException("Invalid current password"))
                .when(userService).changePassword(testUser.getEmail(), currentPassword, newPassword);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.changePassword(currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("Invalid current password"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when new password doesn't meet requirements")
    void testChangePassword_WeakNewPassword_ThrowsException() {
        // Arrange
        String currentPassword = "OldPassword123";
        String newPassword = "weak";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        doThrow(new IllegalArgumentException("Password validation failed"))
                .when(userService).changePassword(testUser.getEmail(), currentPassword, newPassword);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.changePassword(currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("validation failed"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully delete account with valid password")
    void testDeleteAccount_ValidPassword_Success() {
        // Arrange
        String password = "Password123";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(request.getSession()).thenReturn(session);

        doNothing().when(userService).deleteAccount(testUser.getEmail(), password);

        // Act
        authController.deleteAccount(password);

        // Assert
        verify(userService).deleteAccount(testUser.getEmail(), password);
        verify(session).invalidate();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when deleting account with invalid password")
    void testDeleteAccount_InvalidPassword_ThrowsException() {
        // Arrange
        String password = "WrongPassword";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        doThrow(new RuntimeException("Invalid credentials"))
                .when(userService).deleteAccount(testUser.getEmail(), password);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.deleteAccount(password);
        });

        assertTrue(exception.getMessage().contains("Invalid credentials"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully export user data with valid password")
    void testExportUserData_ValidPassword_ReturnsJson() {
        // Arrange
        String password = "Password123";
        String expectedJson = "{\"personalData\":{\"name\":\"Test User\",\"email\":\"test@example.com\"}}";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.exportUserData(testUser.getEmail(), password)).thenReturn(expectedJson);

        // Act
        String result = authController.exportUserData(password);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("personalData"));
        verify(userService).exportUserData(testUser.getEmail(), password);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when exporting data with invalid password")
    void testExportUserData_InvalidPassword_ThrowsException() {
        // Arrange
        String password = "WrongPassword";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        when(userService.exportUserData(testUser.getEmail(), password))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.exportUserData(password);
        });

        assertTrue(exception.getMessage().contains("Invalid credentials"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when user is not authenticated")
    void testChangePassword_NotAuthenticated_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.changePassword("old", "new");
        });

        assertTrue(exception.getMessage().contains("not authenticated"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create LoginResponse with correct data")
    void testLoginResponse_Creation_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String name = "John Doe";
        String email = "john@example.com";
        boolean emailVerified = true;

        // Act
        AuthController.LoginResponse response = new AuthController.LoginResponse(
                userId.toString(),
                name,
                email,
                emailVerified
        );

        // Assert
        assertEquals(userId.toString(), response.id());
        assertEquals(name, response.name());
        assertEquals(email, response.email());
        assertTrue(response.emailVerified());
    }

    @Test
    @DisplayName("Should handle unverified email in login response")
    void testLogin_UnverifiedEmail_ReturnsFalse() {
        // Arrange
        String email = "unverified@example.com";
        String password = "Password123";
        User unverifiedUser = new User("Unverified User", email, "hashedPassword");
        unverifiedUser.setEmailVerifiedAt(null);

        // Set ID using reflection
        try {
            java.lang.reflect.Field idField = unverifiedUser.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(unverifiedUser, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(userService.login(email, password)).thenReturn(unverifiedUser);
        when(request.getSession(true)).thenReturn(session);

        // Act
        AuthController.LoginResponse response = authController.login(email, password);

        // Assert
        assertNotNull(response);
        assertFalse(response.emailVerified());
    }

    @Test
    @DisplayName("Should extract IP from X-Real-IP header when X-Forwarded-For is absent")
    void testLogin_WithXRealIpHeader_ExtractsCorrectIp() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123";
        String realIp = "198.51.100.1";

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(realIp);
        when(userService.login(email, password)).thenReturn(testUser);
        when(request.getSession(true)).thenReturn(session);

        // Act
        AuthController.LoginResponse response = authController.login(email, password);

        // Assert
        assertNotNull(response);
        verify(request).getHeader("X-Real-IP");
    }

    @Test
    @DisplayName("Should use remote address when no proxy headers present")
    void testLogin_NoProxyHeaders_UsesRemoteAddr() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123";
        String remoteAddr = "10.0.0.1";

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        when(userService.login(email, password)).thenReturn(testUser);
        when(request.getSession(true)).thenReturn(session);

        // Act
        AuthController.LoginResponse response = authController.login(email, password);

        // Assert
        assertNotNull(response);
        verify(request).getRemoteAddr();
    }
}
