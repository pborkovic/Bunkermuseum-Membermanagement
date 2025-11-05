package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.dto.UserDTO;
import com.bunkermuseum.membermanagement.dto.mapper.UserMapper;
import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.EmailRepositoryContract;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.contract.EmailServiceContract;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.PermitAll;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Hilla endpoint for email operations in the admin dashboard.
 *
 * <p>This controller provides email-related functionality including:
 * <ul>
 *   <li>Retrieving paginated list of sent emails</li>
 *   <li>Sending new emails to users or custom addresses</li>
 *   <li>Getting all users for email recipient selection</li>
 * </ul>
 *
 * <p>All operations require authentication.
 *
 * @author Philipp Borkovic
 *
 * @see Email
 * @see EmailServiceContract
 * @see EmailRepositoryContract
 */
@Endpoint
@AnonymousAllowed
public class EmailController {

    private final EmailRepositoryContract emailRepository;
    private final EmailServiceContract emailService;
    private final UserRepositoryContract userRepository;

    /**
     * Constructs a new EmailController with required dependencies.
     *
     * @param emailRepository repository for email data access
     * @param emailService service for sending emails
     * @param userRepository repository for user data access
     *
     * @author Philipp Borkovic
     */
    public EmailController(
            EmailRepositoryContract emailRepository,
            EmailServiceContract emailService,
            UserRepositoryContract userRepository
    ) {
        this.emailRepository = emailRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a paginated list of emails.
     *
     * <p>Returns emails ordered by creation date (most recent first)
     * with pagination support for the admin dashboard.
     *
     * @param page the page number (0-indexed)
     * @param size the number of emails per page
     * @return map containing email content and pagination metadata
     *
     * @author Philipp Borkovic
     */
    public Map<String, Object> getEmailsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Email> emailPage = emailRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", emailPage.getContent());
        response.put("number", emailPage.getNumber());
        response.put("size", emailPage.getSize());
        response.put("totalElements", emailPage.getTotalElements());
        response.put("totalPages", emailPage.getTotalPages());
        response.put("first", emailPage.isFirst());
        response.put("last", emailPage.isLast());

        return response;
    }

    /**
     * Sends an email to a specified recipient.
     *
     * <p>Supports two modes:
     * <ul>
     *   <li>Send to existing user: provide userId, email sent to user's email address</li>
     *   <li>Send to custom address: provide customEmail</li>
     * </ul>
     *
     * <p>The email is automatically logged to the database after sending.
     * The email entity tracks the currently logged-in admin user as the sender.
     *
     * @param userId optional ID of user to send email to (mutually exclusive with customEmail)
     * @param customEmail optional custom email address (mutually exclusive with userId)
     * @param subject the email subject line
     * @param content the email HTML content (from rich text editor)
     * @return the sent Email entity with database ID
     * @throws IllegalArgumentException if neither or both userId and customEmail are provided
     * @throws RuntimeException if user is not authenticated
     *
     * @author Philipp Borkovic
     */
    public Email sendEmail(
            @Nullable UUID userId,
            @Nullable String customEmail,
            String subject,
            String content
    ) {
        // Validate that exactly one of userId or customEmail is provided
        if ((userId == null && customEmail == null) || (userId != null && customEmail != null)) {
            throw new IllegalArgumentException(
                    "Bitte wählen Sie entweder einen Benutzer oder geben Sie eine E-Mail-Adresse ein."
            );
        }

        // Get the current logged-in admin user (the sender)
        User currentUser = getCurrentAuthenticatedUser();

        // Determine the recipient's email address
        String toAddress;
        if (userId != null) {
            // Send to existing user
            User recipientUser = userRepository.findByIdOrFail(userId);
            toAddress = recipientUser.getEmail();
        } else {
            // Send to custom email address
            toAddress = customEmail;
        }

        // Send email via EmailService (which logs to database)
        // The currentUser (logged-in admin) is tracked as the sender
        return emailService.sendSimpleEmail(
                "noreply@bunkermuseum.com",
                toAddress,
                subject,
                content,
                currentUser  // Track who sent the email (the logged-in admin)
        );
    }

    /**
     * Retrieves all active users for email recipient selection.
     *
     * <p>Returns a list of active (non-deleted) users to populate
     * the recipient dropdown in the send email modal.
     *
     * @return list of all active users as DTOs (to avoid circular reference issues)
     *
     * @author Philipp Borkovic
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllActiveUsers() {
        List<User> activeUsers = userRepository.findActive();
        if (activeUsers == null || activeUsers.isEmpty()) {
            return new ArrayList<>();
        }

        return activeUsers.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets the current authenticated user from the security context.
     *
     * <p>This method extracts the logged-in admin user who is currently
     * authenticated in the Spring Security context. This user is tracked
     * as the sender when emails are sent through the admin dashboard.</p>
     *
     * @return the current authenticated User entity
     * @throws RuntimeException if user is not authenticated
     *
     * @author Philipp Borkovic
     */
    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new RuntimeException("User not authenticated");
    }
}
