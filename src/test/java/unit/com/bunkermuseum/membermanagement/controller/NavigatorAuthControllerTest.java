package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.NavigatorAuthController;
import com.bunkermuseum.membermanagement.controller.NavigatorAuthController.Status;
import com.bunkermuseum.membermanagement.controller.NavigatorAuthController.VerifyRequest;
import com.bunkermuseum.membermanagement.controller.NavigatorAuthController.VerifyResponse;
import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for the {@link NavigatorAuthController} class.
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *   <li>API key handling (not configured, missing, wrong)</li>
 *   <li>Request validation (missing email or password)</li>
 *   <li>Successful login for admins and non-admins</li>
 *   <li>Invalid credentials, deleted accounts and locked accounts</li>
 * </ul>
 *
 * @see NavigatorAuthController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NavigatorAuthController Unit Tests")
class NavigatorAuthControllerTest {

    private static final String API_KEY = "test-navigator-api-key";
    private static final String EMAIL = "admin@example.com";
    private static final String PASSWORD = "CorrectPassword123!";

    @Mock
    private UserServiceContract userService;

    private NavigatorAuthController controller;

    @BeforeEach
    void setUp() {
        controller = new NavigatorAuthController(userService, API_KEY);
    }

    private static User createUser(String... roleNames) {
        User user = new User("Test Admin", EMAIL, "hashedPassword");

        try {
            java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test user ID", e);
        }

        Set<Role> roles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            roles.add(new Role(roleName));
        }
        user.setRoles(roles);

        return user;
    }

    @Test
    @DisplayName("Should return 503 when no API key is configured")
    void testVerify_NoApiKeyConfigured_ReturnsServiceUnavailable() {
        controller = new NavigatorAuthController(userService, "");

        ResponseEntity<VerifyResponse> response =
                controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, PASSWORD));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Status.DISABLED, response.getBody().status());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 401 when the API key header is missing")
    void testVerify_MissingApiKey_ReturnsUnauthorized() {
        ResponseEntity<VerifyResponse> response =
                controller.verifyCredentials(null, new VerifyRequest(EMAIL, PASSWORD));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Status.INVALID_API_KEY, response.getBody().status());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 401 when the API key is wrong")
    void testVerify_WrongApiKey_ReturnsUnauthorized() {
        ResponseEntity<VerifyResponse> response =
                controller.verifyCredentials("wrong-key", new VerifyRequest(EMAIL, PASSWORD));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return 400 when email or password is missing")
    void testVerify_MissingFields_ReturnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, " ")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.verifyCredentials(API_KEY, new VerifyRequest(null, PASSWORD)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.verifyCredentials(API_KEY, null).getStatusCode());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Should authenticate an admin and report admin rights")
    void testVerify_AdminUser_ReturnsAuthenticatedAdmin() {
        User admin = createUser("ADMIN", "USER");
        when(userService.login(EMAIL, PASSWORD)).thenReturn(admin);

        ResponseEntity<VerifyResponse> response =
                controller.verifyCredentials(API_KEY, new VerifyRequest("  " + EMAIL + " ", PASSWORD));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        VerifyResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(Status.AUTHENTICATED, body.status());
        assertTrue(body.admin());
        assertNotNull(body.user());
        assertEquals(EMAIL, body.user().email());
        assertEquals("Test Admin", body.user().name());
        assertEquals(String.valueOf(admin.getId()), body.user().id());
    }

    @Test
    @DisplayName("Should match the admin role case-insensitively")
    void testVerify_LowercaseAdminRole_ReturnsAdmin() {
        when(userService.login(EMAIL, PASSWORD)).thenReturn(createUser("admin"));

        VerifyResponse body = controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, PASSWORD)).getBody();

        assertNotNull(body);
        assertTrue(body.admin());
    }

    @Test
    @DisplayName("Should authenticate a member without admin rights")
    void testVerify_MemberUser_ReturnsAuthenticatedNonAdmin() {
        when(userService.login(EMAIL, PASSWORD)).thenReturn(createUser("USER"));

        VerifyResponse body = controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, PASSWORD)).getBody();

        assertNotNull(body);
        assertEquals(Status.AUTHENTICATED, body.status());
        assertFalse(body.admin());
    }

    @Test
    @DisplayName("Should report invalid credentials without user details")
    void testVerify_WrongPassword_ReturnsInvalidCredentials() {
        when(userService.login(EMAIL, "wrong")).thenReturn(null);

        ResponseEntity<VerifyResponse> response =
                controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, "wrong"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        VerifyResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(Status.INVALID_CREDENTIALS, body.status());
        assertFalse(body.admin());
        assertNull(body.user());
    }

    @Test
    @DisplayName("Should reject soft-deleted accounts like invalid credentials")
    void testVerify_DeletedUser_ReturnsInvalidCredentials() {
        User deleted = createUser("ADMIN");
        deleted.delete();
        when(userService.login(EMAIL, PASSWORD)).thenReturn(deleted);

        VerifyResponse body = controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, PASSWORD)).getBody();

        assertNotNull(body);
        assertEquals(Status.INVALID_CREDENTIALS, body.status());
        assertFalse(body.admin());
        assertNull(body.user());
    }

    @Test
    @DisplayName("Should report locked accounts")
    void testVerify_LockedAccount_ReturnsLocked() {
        when(userService.login(EMAIL, PASSWORD)).thenThrow(new RuntimeException(
                "Account is temporarily locked due to too many failed login attempts. Please try again later."));

        VerifyResponse body = controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, PASSWORD)).getBody();

        assertNotNull(body);
        assertEquals(Status.LOCKED, body.status());
    }

    @Test
    @DisplayName("Should propagate unexpected errors")
    void testVerify_UnexpectedError_Propagates() {
        when(userService.login(EMAIL, PASSWORD)).thenThrow(new RuntimeException("An error occurred during login"));

        assertThrows(RuntimeException.class,
                () -> controller.verifyCredentials(API_KEY, new VerifyRequest(EMAIL, PASSWORD)));
    }
}
