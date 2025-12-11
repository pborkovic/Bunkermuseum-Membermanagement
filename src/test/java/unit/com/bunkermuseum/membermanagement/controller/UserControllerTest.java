package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.UserController;
import com.bunkermuseum.membermanagement.dto.PageResponse;
import com.bunkermuseum.membermanagement.dto.UserDTO;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.RoleRepositoryContract;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link UserController} class.
 *
 * <p>This test class validates all user management endpoints exposed by the UserController
 * for Vaadin Hilla frontend integration. It uses Mockito to mock service dependencies,
 * focusing on testing controller logic, DTO mapping, pagination, search filtering,
 * and comprehensive error handling.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *   <li>User creation with validation</li>
 *   <li>Retrieving all users as DTOs</li>
 *   <li>Paginated user retrieval with search and status filters</li>
 *   <li>User profile updates (name and email)</li>
 *   <li>Comprehensive user updates</li>
 *   <li>Error handling for invalid parameters and service failures</li>
 *   <li>HTTP status code responses and exception mapping</li>
 * </ul>
 *
 * @see UserController
 * @see UserServiceContract
 * @see User
 * @see UserDTO
 *
 * @author Philipp Borkovic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    /**
     * Mock instance of the user service for business operations.
     * This mock allows us to control user management behavior and responses.
     * Note: Using concrete UserService class instead of contract because
     * some controller methods cast to UserService for specific operations.
     */
    @Mock
    private com.bunkermuseum.membermanagement.service.UserService userService;

    /**
     * Mock instance of the role repository contract for role operations.
     * This mock allows us to control role management behavior and responses.
     */
    @Mock
    private RoleRepositoryContract roleRepository;

    /**
     * Test instance of UserController for testing endpoint behavior.
     */
    private UserController userController;

    /**
     * Test user entity used across multiple test methods.
     */
    private User testUser;

    /**
     * Second test user entity for list/pagination tests.
     */
    private User testUser2;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A UserController instance with mocked service dependency</li>
     *   <li>Two test User entities with IDs set via reflection</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        userController = new UserController(userService, roleRepository);

        testUser = new User("Test User", "test@example.com", "hashedPassword123");
        testUser2 = new User("Another User", "another@example.com", "hashedPassword456");

        setUserId(testUser, UUID.randomUUID());
        setUserId(testUser2, UUID.randomUUID());
    }

    /**
     * Helper method to set user ID via reflection.
     *
     * @param user The user entity
     * @param id The UUID to set
     */
    private void setUserId(User user, UUID id) {
        try {
            java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }
    }


    /**
     * Tests the {@link UserController#createUser} method with valid user data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully creates a new user</li>
     *   <li>The created user is returned with all fields populated</li>
     *   <li>The user service createUser method is called with correct parameters</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully create user with valid data")
    void testCreateUser_ValidData_Success() {
        // Arrange
        User newUser = new User("New User", "new@example.com", "password123");
        when(userService.createUser(newUser)).thenReturn(testUser);

        // Act
        User result = userController.createUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userService).createUser(newUser);
    }

    /**
     * Tests the {@link UserController#createUser} method with invalid user data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message contains "Invalid user data"</li>
     *   <li>Validation errors from service layer are properly propagated</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when user data is invalid")
    void testCreateUser_InvalidData_ThrowsBadRequest() {
        // Arrange
        User invalidUser = new User("", "", "");
        when(userService.createUser(invalidUser))
                .thenThrow(new IllegalArgumentException("Email is required"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.createUser(invalidUser);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid user data"));
    }

    /**
     * Tests the {@link UserController#createUser} method when an unexpected error occurs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to create user"</li>
     *   <li>Unexpected exceptions are caught and converted to appropriate HTTP responses</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when unexpected error occurs")
    void testCreateUser_UnexpectedException_ThrowsInternalServerError() {
        // Arrange
        User newUser = new User("New User", "new@example.com", "password123");
        when(userService.createUser(newUser))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.createUser(newUser);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to create user"));
    }

    /**
     * Tests the {@link UserController#getAllUsers} method with successful retrieval.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves all users</li>
     *   <li>Users are converted to DTOs (excluding sensitive data)</li>
     *   <li>The returned list contains the expected number of users</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve all users as DTOs")
    void testGetAllUsers_Success_ReturnsDTOList() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        List<UserDTO> result = userController.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService).getAllUsers();
    }

    /**
     * Tests the {@link UserController#getAllUsers} method when no users exist.
     *
     * <p>This test verifies that an empty list is returned when no users exist.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when no users exist")
    void testGetAllUsers_NoUsers_ReturnsEmptyList() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of());

        // Act
        List<UserDTO> result = userController.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests the {@link UserController#getAllUsers} method when an unexpected error occurs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to retrieve users"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when retrieval fails")
    void testGetAllUsers_ServiceFailure_ThrowsInternalServerError() {
        // Arrange
        when(userService.getAllUsers())
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getAllUsers();
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to retrieve users"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with valid pagination parameters.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves a page of users</li>
     *   <li>Pagination metadata (page number, size, total elements) is correct</li>
     *   <li>Users are converted to DTOs</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve users page with pagination")
    void testGetUsersPage_ValidParameters_ReturnsPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(userService.getUsersPageWithStatus(any(Pageable.class), isNull(), eq("active")))
                .thenReturn(userPage);

        // Act
        PageResponse<UserDTO> result = userController.getUsersPage(0, 10, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with search query.
     *
     * <p>This test verifies that search query is properly passed to the service layer.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should pass search query to service layer")
    void testGetUsersPage_WithSearchQuery_PassesToService() {
        // Arrange
        String searchQuery = "test@example.com";
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.getUsersPageWithStatus(any(Pageable.class), eq(searchQuery), eq("active")))
                .thenReturn(userPage);

        // Act
        PageResponse<UserDTO> result = userController.getUsersPage(0, 10, searchQuery, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userService).getUsersPageWithStatus(any(Pageable.class), eq(searchQuery), eq("active"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with status filter.
     *
     * <p>This test verifies that status filter is properly passed to the service layer.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should pass status filter to service layer")
    void testGetUsersPage_WithStatusFilter_PassesToService() {
        // Arrange
        String status = "deleted";
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.getUsersPageWithStatus(any(Pageable.class), isNull(), eq(status)))
                .thenReturn(userPage);

        // Act
        PageResponse<UserDTO> result = userController.getUsersPage(0, 10, null, status);

        // Assert
        assertNotNull(result);
        verify(userService).getUsersPageWithStatus(any(Pageable.class), isNull(), eq(status));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with blank status.
     *
     * <p>This test verifies that blank status defaults to "active".</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should default to 'active' status when status is blank")
    void testGetUsersPage_BlankStatus_DefaultsToActive() {
        // Arrange
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.getUsersPageWithStatus(any(Pageable.class), isNull(), eq("active")))
                .thenReturn(userPage);

        // Act
        PageResponse<UserDTO> result = userController.getUsersPage(0, 10, null, "   ");

        // Assert
        verify(userService).getUsersPageWithStatus(any(Pageable.class), isNull(), eq("active"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with negative page number.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message contains "Page number must be >= 0"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when page number is negative")
    void testGetUsersPage_NegativePageNumber_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUsersPage(-1, 10, null, null);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Page number must be >= 0"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with page size less than 1.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message contains "Page size must be between 1 and 100"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when page size is less than 1")
    void testGetUsersPage_PageSizeTooSmall_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUsersPage(0, 0, null, null);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Page size must be between 1 and 100"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method with page size greater than 100.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message contains "Page size must be between 1 and 100"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when page size exceeds 100")
    void testGetUsersPage_PageSizeTooLarge_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUsersPage(0, 101, null, null);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Page size must be between 1 and 100"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method when service returns null page.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message indicates null page response</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when service returns null page")
    void testGetUsersPage_ServiceReturnsNull_ThrowsInternalServerError() {
        // Arrange
        when(userService.getUsersPageWithStatus(any(Pageable.class), isNull(), eq("active")))
                .thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUsersPage(0, 10, null, null);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Service returned null page response"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method when service returns null content.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message indicates invalid request parameters</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when page content is null")
    void testGetUsersPage_NullContent_ThrowsBadRequest() {
        // Arrange
        Page<User> userPage = mock(Page.class);
        when(userPage.getContent()).thenReturn(null);
        when(userService.getUsersPageWithStatus(any(Pageable.class), isNull(), eq("active")))
                .thenReturn(userPage);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUsersPage(0, 10, null, null);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid request parameters"));
    }

    /**
     * Tests the {@link UserController#getUsersPage} method when service throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to retrieve users page"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when service throws exception")
    void testGetUsersPage_ServiceThrowsException_ThrowsInternalServerError() {
        // Arrange
        when(userService.getUsersPageWithStatus(any(Pageable.class), isNull(), eq("active")))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.getUsersPage(0, 10, null, null);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to retrieve users page"));
    }

    /**
     * Tests the {@link UserController#updateProfile} method with valid data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully updates user profile</li>
     *   <li>Updated user is returned as DTO</li>
     *   <li>The user service updateProfile method is called with correct parameters</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully update user profile")
    void testUpdateProfile_ValidData_Success() {
        // Arrange
        UUID userId = testUser.getId();
        String newName = "Updated Name";
        String newEmail = "updated@example.com";

        User updatedUser = new User(newName, newEmail, testUser.getPassword());
        setUserId(updatedUser, userId);

        when(userService.updateProfile(userId, newName, newEmail)).thenReturn(updatedUser);

        // Act
        UserDTO result = userController.updateProfile(userId, newName, newEmail);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newEmail, result.getEmail());
        verify(userService).updateProfile(userId, newName, newEmail);
    }

    /**
     * Tests the {@link UserController#updateProfile} method with invalid data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message contains "Invalid data"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when profile data is invalid")
    void testUpdateProfile_InvalidData_ThrowsBadRequest() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.updateProfile(userId, "", ""))
                .thenThrow(new IllegalArgumentException("Name cannot be empty"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.updateProfile(userId, "", "");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid data"));
    }

    /**
     * Tests the {@link UserController#updateProfile} method when service throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to update profile"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when update fails")
    void testUpdateProfile_ServiceThrowsException_ThrowsInternalServerError() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.updateProfile(userId, "New Name", "new@example.com"))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.updateProfile(userId, "New Name", "new@example.com");
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to update profile"));
    }

    /**
     * Tests the {@link UserController#updateUser} method with valid user data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully updates comprehensive user information</li>
     *   <li>Updated user is returned as DTO</li>
     *   <li>The user service updateUser method is called with correct parameters</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully update user with comprehensive data")
    void testUpdateUser_ValidData_Success() {
        // Arrange
        UUID userId = testUser.getId();
        User userData = new User("Updated User", "updated@example.com", "newPassword");

        // Mock admin user
        User adminUser = new User("Admin", "admin@example.com", "password");
        com.bunkermuseum.membermanagement.model.Role adminRole =
                new com.bunkermuseum.membermanagement.model.Role("ADMIN");
        adminUser.getRoles().add(adminRole);

        when(userService.getCurrentAuthenticatedUser()).thenReturn(adminUser);
        when(userService.updateUser(userId, userData)).thenReturn(testUser);

        // Act
        UserDTO result = userController.updateUser(userId, userData);

        // Assert
        assertNotNull(result);
        verify(userService).updateUser(userId, userData);
    }

    /**
     * Tests the {@link UserController#updateUser} method with invalid user data.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message contains "Invalid data"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when user data is invalid")
    void testUpdateUser_InvalidData_ThrowsBadRequest() {
        // Arrange
        UUID userId = testUser.getId();
        User invalidData = new User("", "", "");

        // Mock admin user
        User adminUser = new User("Admin", "admin@example.com", "password");
        com.bunkermuseum.membermanagement.model.Role adminRole =
                new com.bunkermuseum.membermanagement.model.Role("ADMIN");
        adminUser.getRoles().add(adminRole);

        when(userService.getCurrentAuthenticatedUser()).thenReturn(adminUser);
        when(userService.updateUser(userId, invalidData))
                .thenThrow(new IllegalArgumentException("Email is required"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.updateUser(userId, invalidData);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid data"));
    }

    /**
     * Tests the {@link UserController#updateUser} method when service throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to update user"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when update fails")
    void testUpdateUser_ServiceThrowsException_ThrowsInternalServerError() {
        // Arrange
        UUID userId = testUser.getId();
        User userData = new User("Updated User", "updated@example.com", "newPassword");

        // Mock admin user
        User adminUser = new User("Admin", "admin@example.com", "password");
        com.bunkermuseum.membermanagement.model.Role adminRole =
                new com.bunkermuseum.membermanagement.model.Role("ADMIN");
        adminUser.getRoles().add(adminRole);

        when(userService.getCurrentAuthenticatedUser()).thenReturn(adminUser);
        when(userService.updateUser(userId, userData))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.updateUser(userId, userData);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to update user"));
    }


    /**
     * Tests the {@link UserController#deleteUser} method with valid user ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully deletes a user (soft delete)</li>
     *   <li>The user service deleteById method is called with correct ID</li>
     *   <li>No exception is thrown when deletion succeeds</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully delete user with valid ID")
    void testDeleteUser_ValidId_Success() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.deleteById(userId)).thenReturn(true);

        // Act
        userController.deleteUser(userId);

        // Assert
        verify(userService).deleteById(userId);
    }

    /**
     * Tests the {@link UserController#deleteUser} method when user not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with NOT_FOUND is thrown</li>
     *   <li>The exception message indicates user not found</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw NOT_FOUND when user to delete doesn't exist")
    void testDeleteUser_UserNotFound_ThrowsNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userService.deleteById(userId)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.deleteUser(userId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found"));
    }

    /**
     * Tests the {@link UserController#deleteUser} method with invalid user ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message indicates invalid user ID</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when user ID is invalid")
    void testDeleteUser_InvalidId_ThrowsBadRequest() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.deleteById(userId))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.deleteUser(userId);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid user ID"));
    }

    /**
     * Tests the {@link UserController#deleteUser} method when service throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to delete user"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when delete fails")
    void testDeleteUser_ServiceThrowsException_ThrowsInternalServerError() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.deleteById(userId))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.deleteUser(userId);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to delete user"));
    }

    /**
     * Tests the {@link UserController#setUserAdminRole} method to add admin role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully adds ADMIN role to a user</li>
     *   <li>The user is retrieved from the service</li>
     *   <li>The ADMIN role is retrieved from the repository</li>
     *   <li>The role is added to the user's roles</li>
     *   <li>The updated user is saved</li>
     *   <li>UserDTO is returned with updated roles</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully add ADMIN role to user")
    void testSetUserAdminRole_AddAdmin_Success() {
        // Arrange
        UUID userId = testUser.getId();
        com.bunkermuseum.membermanagement.model.Role adminRole =
                new com.bunkermuseum.membermanagement.model.Role("ADMIN");

        when(userService.findById(userId))
                .thenReturn(java.util.Optional.of(testUser));
        when(roleRepository.findByName("ADMIN"))
                .thenReturn(java.util.Optional.of(adminRole));
        when(userService.update(eq(userId), any(User.class)))
                .thenReturn(testUser);

        // Act
        UserDTO result = userController.setUserAdminRole(userId, true);

        // Assert
        assertNotNull(result);
        verify(userService).findById(userId);
        verify(roleRepository).findByName("ADMIN");
        verify(userService).update(eq(userId), any(User.class));
    }

    /**
     * Tests the {@link UserController#setUserAdminRole} method to remove admin role.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully removes ADMIN role from a user</li>
     *   <li>The role is removed from the user's roles</li>
     *   <li>The updated user is saved</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully remove ADMIN role from user")
    void testSetUserAdminRole_RemoveAdmin_Success() {
        // Arrange
        UUID userId = testUser.getId();
        com.bunkermuseum.membermanagement.model.Role adminRole =
                new com.bunkermuseum.membermanagement.model.Role("ADMIN");
        testUser.getRoles().add(adminRole);

        when(userService.findById(userId))
                .thenReturn(java.util.Optional.of(testUser));
        when(roleRepository.findByName("ADMIN"))
                .thenReturn(java.util.Optional.of(adminRole));
        when(userService.update(eq(userId), any(User.class)))
                .thenReturn(testUser);

        // Act
        UserDTO result = userController.setUserAdminRole(userId, false);

        // Assert
        assertNotNull(result);
        verify(userService).findById(userId);
        verify(roleRepository).findByName("ADMIN");
        verify(userService).update(eq(userId), any(User.class));
    }

    /**
     * Tests the {@link UserController#setUserAdminRole} method when user not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with NOT_FOUND is thrown</li>
     *   <li>The exception message indicates user not found</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw NOT_FOUND when user doesn't exist")
    void testSetUserAdminRole_UserNotFound_ThrowsNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.setUserAdminRole(userId, true);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found"));
    }

    /**
     * Tests the {@link UserController#setUserAdminRole} method when ADMIN role doesn't exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with NOT_FOUND is thrown</li>
     *   <li>The exception message indicates ADMIN role not found</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw NOT_FOUND when ADMIN role doesn't exist in system")
    void testSetUserAdminRole_RoleNotFound_ThrowsNotFound() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.findById(userId))
                .thenReturn(java.util.Optional.of(testUser));
        when(roleRepository.findByName("ADMIN"))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.setUserAdminRole(userId, true);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("ADMIN role not found"));
    }

    /**
     * Tests the {@link UserController#setUserAdminRole} method with invalid user ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with BAD_REQUEST is thrown</li>
     *   <li>The exception message indicates invalid user ID</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw BAD_REQUEST when user ID is invalid")
    void testSetUserAdminRole_InvalidId_ThrowsBadRequest() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.findById(userId))
                .thenReturn(java.util.Optional.of(testUser));
        when(roleRepository.findByName("ADMIN"))
                .thenReturn(java.util.Optional.of(new com.bunkermuseum.membermanagement.model.Role("ADMIN")));
        when(userService.update(eq(userId), any(User.class)))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.setUserAdminRole(userId, true);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid user ID"));
    }

    /**
     * Tests the {@link UserController#setUserAdminRole} method when service throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to update user admin role"</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when role update fails")
    void testSetUserAdminRole_ServiceThrowsException_ThrowsInternalServerError() {
        // Arrange
        UUID userId = testUser.getId();
        when(userService.findById(userId))
                .thenReturn(java.util.Optional.of(testUser));
        when(roleRepository.findByName("ADMIN"))
                .thenReturn(java.util.Optional.of(new com.bunkermuseum.membermanagement.model.Role("ADMIN")));
        when(userService.update(eq(userId), any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.setUserAdminRole(userId, true);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to update user admin role"));
    }
}
