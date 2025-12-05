package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.AuthController;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.ReCaptchaService;
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
 * Comprehensive unit test suite for the {@link AuthController} class.
 *
 * <p>This test class validates all authentication endpoints, session management,
 * password operations, and GDPR compliance features exposed by the AuthController
 * for Vaadin Hilla frontend integration. It uses Mockito to mock service dependencies
 * and HTTP components, focusing on testing controller logic, response mapping, and
 * security context management.</p>
 *
 * <p><strong>Frontend Integration:</strong></p>
 * <p>These tests ensure the AuthController properly exposes authentication
 * functionality to the Vaadin Hilla TypeScript frontend, with proper DTO mapping,
 * error handling, and session management for a seamless user experience.</p>
 *
 * @see AuthController
 * @see UserServiceContract
 * @see RoleServiceContract
 * @see User
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    /**
     * Mock instance of the user service contract for authentication operations.
     * This mock allows us to control authentication behavior and responses.
     */
    @Mock
    private UserServiceContract userService;

    /**
     * Mock instance of the role service contract for authorization operations.
     * This mock supports future role-based access control features.
     */
    @Mock
    private RoleServiceContract roleService;

    /**
     * Mock instance of the HTTP servlet request for testing HTTP headers and IP extraction.
     */
    @Mock
    private HttpServletRequest request;

    /**
     * Mock instance of the HTTP session for testing session management.
     */
    @Mock
    private HttpSession session;

    /**
     * Mock instance of the Spring Security context for authentication state management.
     */
    @Mock
    private SecurityContext securityContext;

    /**
     * Mock instance of the Spring Security authentication for user principal testing.
     */
    @Mock
    private Authentication authentication;

    /**
     * Mock instance of the ReCaptcha service for endpoints requiring captcha validation.
     */
    @Mock
    private ReCaptchaService reCaptchaService;

    /**
     * Test instance of AuthController for testing endpoint behavior.
     */
    private AuthController authController;

    /**
     * Test user entity used across multiple test methods.
     */
    private User testUser;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>An AuthController instance with mocked service dependencies and HTTP request</li>
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
        authController = new AuthController(userService, roleService, request, reCaptchaService);

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
     * Tests the {@link AuthController#login} method with valid email and password credentials.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully authenticates a user and returns a LoginResponse</li>
     *   <li>The LoginResponse contains correct user name, email, and email verification status</li>
     *   <li>A Spring Security context is created and stored in the HTTP session</li>
     *   <li>The user service login method is called with provided credentials</li>
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

    /**
     * Tests the {@link AuthController#login} method with incorrect password credentials.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns null when authentication fails</li>
     *   <li>The user service login method is called with provided credentials</li>
     *   <li>No HTTP session is created when authentication fails</li>
     *   <li>No SecurityContext is stored when login is unsuccessful</li>
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

    /**
     * Tests the {@link AuthController#login} method when account is locked due to failed login attempts.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when the account is locked</li>
     *   <li>The exception message contains "locked"</li>
     *   <li>The user service login method is called before the exception is thrown</li>
     *   <li>Account lockout exceptions are properly propagated to the frontend</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#login} method with a null email parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when email is null</li>
     *   <li>The exception message contains "Email"</li>
     *   <li>The exception is propagated from the user service layer</li>
     *   <li>Input validation is enforced at the service layer</li>
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

        when(userService.login(email, password))
                .thenThrow(new IllegalArgumentException("Email must not be null or blank"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Email"));
    }

    /**
     * Tests the {@link AuthController#login} method with a blank password parameter.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when password is blank</li>
     *   <li>The exception message contains "Password"</li>
     *   <li>The exception is propagated from the user service layer</li>
     *   <li>Input validation is enforced at the service layer</li>
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

        when(userService.login(email, password))
                .thenThrow(new IllegalArgumentException("Password must not be null or blank"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.login(email, password);
        });

        assertTrue(exception.getMessage().contains("Password"));
    }

    /**
     * Tests the {@link AuthController#login} method IP extraction from X-Forwarded-For header.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The controller successfully extracts the client IP from X-Forwarded-For header</li>
     *   <li>The first IP in the comma-separated list is used as the client IP</li>
     *   <li>Login proceeds normally after IP extraction</li>
     *   <li>Proxy headers are properly handled for logging and security</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#changePassword} method with valid current and new passwords.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully changes the password for an authenticated user</li>
     *   <li>The authenticated user is retrieved from the Spring Security context</li>
     *   <li>The user service changePassword method is called with correct parameters</li>
     *   <li>No exceptions are thrown when password change succeeds</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#changePassword} method with an incorrect current password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when current password is incorrect</li>
     *   <li>The exception message contains "Invalid current password"</li>
     *   <li>The exception is propagated from the user service layer</li>
     *   <li>Password verification failure prevents password change</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#changePassword} method with a weak new password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown when new password is weak</li>
     *   <li>The exception message contains "validation failed"</li>
     *   <li>The exception is propagated from the user service layer</li>
     *   <li>Password strength requirements are enforced</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#deleteAccount} method with valid password (GDPR Article 17).
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully deletes the account for an authenticated user</li>
     *   <li>The user service deleteAccount method is called with correct parameters</li>
     *   <li>The HTTP session is invalidated after successful account deletion</li>
     *   <li>GDPR right to erasure is properly implemented at controller layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#deleteAccount} method with an incorrect password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when password verification fails</li>
     *   <li>The exception message contains "Invalid credentials"</li>
     *   <li>The exception is propagated from the user service layer</li>
     *   <li>Account deletion requires valid password confirmation</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#exportUserData} method with valid password (GDPR Articles 15 & 20).
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully exports user data in JSON format</li>
     *   <li>The JSON contains personalData section with user information</li>
     *   <li>The user service exportUserData method is called with correct parameters</li>
     *   <li>GDPR right to data portability is properly implemented at controller layer</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#exportUserData} method with an incorrect password.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when password verification fails</li>
     *   <li>The exception message contains "Invalid credentials"</li>
     *   <li>The exception is propagated from the user service layer</li>
     *   <li>Data export requires valid password confirmation</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#changePassword} method when user is not authenticated.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when user is not authenticated</li>
     *   <li>The exception message contains "not authenticated"</li>
     *   <li>Anonymous users cannot change passwords</li>
     *   <li>Authentication is required for password change operations</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController.LoginResponse} record creation with valid data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The LoginResponse record correctly stores user ID, name, email, and verification status</li>
     *   <li>All record accessor methods return the correct values</li>
     *   <li>The email verification status is properly represented as a boolean</li>
     *   <li>The DTO is suitable for frontend consumption</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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
                emailVerified,
                java.util.Collections.emptySet()
        );

        // Assert
        assertEquals(userId.toString(), response.id());
        assertEquals(name, response.name());
        assertEquals(email, response.email());
        assertTrue(response.emailVerified());
    }

    /**
     * Tests the {@link AuthController#login} method when user's email is unverified.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns a LoginResponse with emailVerified set to false</li>
     *   <li>Login succeeds even if email is not verified</li>
     *   <li>The email verification status is correctly mapped to the response DTO</li>
     *   <li>Frontend receives accurate email verification information</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#login} method IP extraction from X-Real-IP header fallback.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The controller falls back to X-Real-IP header when X-Forwarded-For is absent</li>
     *   <li>The IP is correctly extracted from X-Real-IP header</li>
     *   <li>Login proceeds normally after IP extraction</li>
     *   <li>Multiple proxy header formats are supported for IP extraction</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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

    /**
     * Tests the {@link AuthController#login} method IP extraction from remote address fallback.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The controller uses getRemoteAddr() when no proxy headers are present</li>
     *   <li>The IP is correctly extracted from the request remote address</li>
     *   <li>Login proceeds normally after IP extraction</li>
     *   <li>Direct connections without proxies are properly handled</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
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
