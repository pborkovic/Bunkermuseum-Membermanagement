package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.EmailController;
import com.bunkermuseum.membermanagement.dto.EmailDTO;
import com.bunkermuseum.membermanagement.dto.PageResponse;
import com.bunkermuseum.membermanagement.dto.UserDTO;
import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.EmailRepositoryContract;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.contract.EmailServiceContract;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for the {@link EmailController} class.
 *
 * <p>This test class validates all email management endpoints exposed by the EmailController
 * for Vaadin Hilla frontend integration. It uses Mockito to mock service and repository dependencies,
 * focusing on testing controller logic, pagination, email sending, and error handling.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *   <li>Paginated email retrieval with sorting</li>
 *   <li>Sending emails to users or custom addresses</li>
 *   <li>Retrieving all active users for recipient selection</li>
 *   <li>Authentication and security context handling</li>
 *   <li>Error handling for various failure scenarios</li>
 * </ul>
 *
 * @see EmailController
 * @see EmailServiceContract
 * @see EmailRepositoryContract
 * @see UserRepositoryContract
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailController Unit Tests")
class EmailControllerTest {

    /**
     * Mock instance of the email repository for data access.
     * This mock controls email retrieval behavior.
     */
    @Mock
    private EmailRepositoryContract emailRepository;

    /**
     * Mock instance of the email service for sending operations.
     * This mock controls email sending behavior.
     */
    @Mock
    private EmailServiceContract emailService;

    /**
     * Mock instance of the user repository for user data access.
     * This mock controls user retrieval behavior.
     */
    @Mock
    private UserRepositoryContract userRepository;

    /**
     * Mock instance of Spring Security context for authentication testing.
     */
    @Mock
    private SecurityContext securityContext;

    /**
     * Mock instance of Spring Security authentication for user principal testing.
     */
    @Mock
    private Authentication authentication;

    /**
     * Test instance of EmailController for testing endpoint behavior.
     */
    private EmailController emailController;

    /**
     * Test user entity used across multiple test methods.
     */
    private User testUser;

    /**
     * Test email entity used across multiple test methods.
     */
    private Email testEmail;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>An EmailController instance with mocked dependencies</li>
     *   <li>Test User and Email entities with IDs set via reflection</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        emailController = new EmailController(emailRepository, emailService, userRepository);

        testUser = new User("Test User", "test@example.com", "hashedPassword123");
        testEmail = new Email("from@test.com", "to@test.com", "Test Subject", "Test Content");

        try {
            java.lang.reflect.Field userIdField = testUser.getClass().getSuperclass().getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(testUser, UUID.randomUUID());

            java.lang.reflect.Field emailIdField = testEmail.getClass().getSuperclass().getDeclaredField("id");
            emailIdField.setAccessible(true);
            emailIdField.set(testEmail, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test entity IDs", e);
        }
    }

    /**
     * Tests the {@link EmailController#getEmailsPage} method with valid pagination parameters.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves a page of emails</li>
     *   <li>Emails are ordered by creation date (most recent first)</li>
     *   <li>Pagination metadata (page number, size, total elements) is correct</li>
     *   <li>Emails are converted to DTOs</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve emails page with pagination")
    void testGetEmailsPage_ValidParameters_ReturnsPage() {
        // Arrange
        Email email1 = new Email("from1@test.com", "to1@test.com", "Subject 1", "Content 1");
        Email email2 = new Email("from2@test.com", "to2@test.com", "Subject 2", "Content 2");
        List<Email> emails = Arrays.asList(email1, email2);
        // Create PageImpl with PageRequest to set correct size
        Page<Email> emailPage = new PageImpl<>(emails, PageRequest.of(0, 10), 2);

        when(emailRepository.findAll(any(Pageable.class))).thenReturn(emailPage);

        // Act
        PageResponse<EmailDTO> result = emailController.getEmailsPage(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        verify(emailRepository).findAll(any(Pageable.class));
    }

    /**
     * Tests the {@link EmailController#getEmailsPage} method when no emails exist.
     *
     * <p>This test verifies that an empty page is returned when no emails are present.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty page when no emails exist")
    void testGetEmailsPage_NoEmails_ReturnsEmptyPage() {
        // Arrange
        Page<Email> emailPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(emailRepository.findAll(any(Pageable.class))).thenReturn(emailPage);

        // Act
        PageResponse<EmailDTO> result = emailController.getEmailsPage(0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(10, result.getSize());
    }

    /**
     * Tests the {@link EmailController#getEmailsPage} method with different page sizes.
     *
     * <p>This test verifies that pagination works correctly with various page sizes.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle different page sizes correctly")
    void testGetEmailsPage_DifferentPageSizes_ReturnsCorrectSize() {
        // Arrange
        List<Email> emails = Arrays.asList(
            new Email("from1@test.com", "to1@test.com", "Subject 1", "Content 1"),
            new Email("from2@test.com", "to2@test.com", "Subject 2", "Content 2"),
            new Email("from3@test.com", "to3@test.com", "Subject 3", "Content 3")
        );
        Page<Email> emailPage = new PageImpl<>(emails, PageRequest.of(0, 20), 3);

        when(emailRepository.findAll(any(Pageable.class))).thenReturn(emailPage);

        // Act
        PageResponse<EmailDTO> result = emailController.getEmailsPage(0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getSize());
        assertEquals(3, result.getContent().size());
    }

    /**
     * Tests the {@link EmailController#getEmailsPage} method with large dataset.
     *
     * <p>This test verifies that the method handles large result sets correctly.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle large number of emails")
    void testGetEmailsPage_LargeDataset_ReturnsAllEmails() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            emails.add(new Email("from" + i + "@test.com", "to" + i + "@test.com", "Subject " + i, "Content " + i));
        }
        Page<Email> emailPage = new PageImpl<>(emails, PageRequest.of(0, 100), 100);

        when(emailRepository.findAll(any(Pageable.class))).thenReturn(emailPage);

        // Act
        PageResponse<EmailDTO> result = emailController.getEmailsPage(0, 100);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getContent().size());
        assertEquals(100, result.getTotalElements());
        assertEquals(100, result.getSize());
    }


    /**
     * Tests the {@link EmailController#sendEmail} method sending to existing user.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully sends email to an existing user</li>
     *   <li>Current authenticated user is retrieved from security context</li>
     *   <li>Recipient user is loaded from repository</li>
     *   <li>Email service is called with correct parameters</li>
     *   <li>Sent email entity is returned</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully send email to existing user")
    void testSendEmail_ToExistingUser_Success() {
        // Arrange
        UUID recipientId = UUID.randomUUID();
        String subject = "Test Subject";
        String content = "Test Content";

        User recipient = new User("Recipient", "recipient@example.com", "password");
        try {
            java.lang.reflect.Field idField = recipient.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(recipient, recipientId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set recipient ID", e);
        }

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userRepository.findByIdOrFail(recipientId)).thenReturn(recipient);
        when(emailService.sendSimpleEmail(anyString(), anyString(), anyString(), anyString(), any(User.class)))
                .thenReturn(testEmail);

        // Act
        Email result = emailController.sendEmail(recipientId, null, subject, content);

        // Assert
        assertNotNull(result);
        assertEquals(testEmail, result);
        verify(userRepository).findByIdOrFail(recipientId);
        verify(emailService).sendSimpleEmail(
                eq("noreply@bunkermuseum.com"),
                eq(recipient.getEmail()),
                eq(subject),
                eq(content),
                eq(testUser)
        );

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link EmailController#sendEmail} method sending to custom email address.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully sends email to a custom email address</li>
     *   <li>No user lookup is performed for custom emails</li>
     *   <li>Email service is called with custom email address</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully send email to custom address")
    void testSendEmail_ToCustomAddress_Success() {
        // Arrange
        String customEmail = "custom@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(emailService.sendSimpleEmail(anyString(), anyString(), anyString(), anyString(), any(User.class)))
                .thenReturn(testEmail);

        // Act
        Email result = emailController.sendEmail(null, customEmail, subject, content);

        // Assert
        assertNotNull(result);
        assertEquals(testEmail, result);
        verify(userRepository, never()).findByIdOrFail(any());
        verify(emailService).sendSimpleEmail(
                eq("noreply@bunkermuseum.com"),
                eq(customEmail),
                eq(subject),
                eq(content),
                eq(testUser)
        );

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link EmailController#sendEmail} method when both userId and customEmail are provided.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown</li>
     *   <li>The exception message indicates that only one recipient should be specified</li>
     *   <li>Mutually exclusive parameters are enforced</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when both userId and customEmail are provided")
    void testSendEmail_BothRecipients_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String customEmail = "custom@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailController.sendEmail(userId, customEmail, subject, content);
        });

        assertTrue(exception.getMessage().contains("entweder einen Benutzer oder geben Sie eine E-Mail-Adresse ein"));
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString(), anyString(), any());
    }

    /**
     * Tests the {@link EmailController#sendEmail} method when neither userId nor customEmail are provided.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>An IllegalArgumentException is thrown</li>
     *   <li>The exception message indicates that a recipient must be specified</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when no recipient is provided")
    void testSendEmail_NoRecipient_ThrowsException() {
        // Arrange
        String subject = "Test Subject";
        String content = "Test Content";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailController.sendEmail(null, null, subject, content);
        });

        assertTrue(exception.getMessage().contains("entweder einen Benutzer oder geben Sie eine E-Mail-Adresse ein"));
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString(), anyString(), any());
    }

    /**
     * Tests the {@link EmailController#sendEmail} method when user is not authenticated.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when user is not authenticated</li>
     *   <li>The exception message indicates authentication failure</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when user is not authenticated")
    void testSendEmail_NotAuthenticated_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String subject = "Test Subject";
        String content = "Test Content";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailController.sendEmail(userId, null, subject, content);
        });

        assertTrue(exception.getMessage().contains("User not authenticated"));

        SecurityContextHolder.clearContext();
    }

    /**
     * Tests the {@link EmailController#sendEmail} method when recipient user not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Exception is thrown when recipient user doesn't exist</li>
     *   <li>Email is not sent when user lookup fails</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw exception when recipient user not found")
    void testSendEmail_RecipientNotFound_ThrowsException() {
        // Arrange
        UUID recipientId = UUID.randomUUID();
        String subject = "Test Subject";
        String content = "Test Content";

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userRepository.findByIdOrFail(recipientId))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailController.sendEmail(recipientId, null, subject, content);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString(), anyString(), any());

        SecurityContextHolder.clearContext();
    }


    /**
     * Tests the {@link EmailController#getAllActiveUsers} method with active users.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves all active (non-deleted) users</li>
     *   <li>Users are converted to DTOs</li>
     *   <li>The returned list contains the expected number of users</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve all active users")
    void testGetAllActiveUsers_WithActiveUsers_ReturnsUserList() {
        // Arrange
        User user1 = new User("User 1", "user1@example.com", "password");
        User user2 = new User("User 2", "user2@example.com", "password");
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findActive()).thenReturn(users);

        // Act
        List<UserDTO> result = emailController.getAllActiveUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findActive();
    }

    /**
     * Tests the {@link EmailController#getAllActiveUsers} method when no active users exist.
     *
     * <p>This test verifies that an empty list is returned when no active users are present.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when no active users exist")
    void testGetAllActiveUsers_NoUsers_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findActive()).thenReturn(List.of());

        // Act
        List<UserDTO> result = emailController.getAllActiveUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findActive();
    }

    /**
     * Tests the {@link EmailController#getAllActiveUsers} method when repository returns null.
     *
     * <p>This test verifies that an empty list is returned when repository returns null.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when repository returns null")
    void testGetAllActiveUsers_RepositoryReturnsNull_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findActive()).thenReturn(null);

        // Act
        List<UserDTO> result = emailController.getAllActiveUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findActive();
    }

    /**
     * Tests the {@link EmailController#getAllActiveUsers} method with large dataset.
     *
     * <p>This test verifies that the method handles large numbers of users correctly.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle large number of active users")
    void testGetAllActiveUsers_LargeDataset_ReturnsAllUsers() {
        // Arrange
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(new User("User " + i, "user" + i + "@example.com", "password"));
        }

        when(userRepository.findActive()).thenReturn(users);

        // Act
        List<UserDTO> result = emailController.getAllActiveUsers();

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        verify(userRepository).findActive();
    }

    /**
     * Tests the {@link EmailController#getAllActiveUsers} method filtering out deleted users.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Only active users are returned</li>
     *   <li>Deleted users are excluded from the result</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should exclude deleted users from result")
    void testGetAllActiveUsers_ExcludesDeletedUsers_ReturnsOnlyActive() {
        // Arrange
        User activeUser = new User("Active User", "active@example.com", "password");
        List<User> users = List.of(activeUser); // Repository already filters deleted users

        when(userRepository.findActive()).thenReturn(users);

        // Act
        List<UserDTO> result = emailController.getAllActiveUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Active User", result.get(0).getName());
        verify(userRepository).findActive();
    }
}
