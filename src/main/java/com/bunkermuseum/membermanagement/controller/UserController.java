package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.dto.PageResponse;
import com.bunkermuseum.membermanagement.dto.UserDTO;
import com.bunkermuseum.membermanagement.dto.mapper.UserMapper;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.UserService;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for User management operations.
 *
 * <p>This controller provides comprehensive RESTful endpoints for user management,
 * including user registration, profile management, authentication support, email
 * verification, OAuth integration, and administrative operations. It follows REST
 * conventions and provides proper HTTP status codes and error handling.</p>
 *
 * @author Philipp Borkovic
 *
 * @see UserServiceContract
 * @see User
 */
@Endpoint
@PermitAll
public class UserController {

    private final UserServiceContract userService;

    /**
     * Constructs a new UserController with the provided service.
     *
     * @param userService The user service for business operations
     *
     * @author Philipp Borkovic
     */
    public UserController(UserServiceContract userService) {
        this.userService = userService;
    }


    /**
     * Creates a new user in the system.
     * <p>
     * This method delegates the creation process to the {@link UserService ::createUser(User)} method.
     * If the input user data is invalid, it returns a {@link HttpStatus#BAD_REQUEST} response.
     * If any other unexpected error occurs during user creation, it returns a
     * {@link HttpStatus#INTERNAL_SERVER_ERROR} response.
     * </p>
     *
     * @param user the {@link User} object containing the details of the user to create.
     *             Must not be {@code null}.
     * @return the created {@link User} object, including any generated fields
     *         such as the unique user ID.
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if the input
     *         user data is invalid (e.g., missing required fields or failing validation).
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during the creation process.
     *
     * @author Philipp Borkovic
     */
    public User createUser(@Nonnull User user) {
        try {
            return userService.createUser(user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user", e);
        }
    }

    /**
     * Retrieves all users from the system.
     *
     * <p>This method fetches all registered users and converts them to DTOs
     * for secure API responses. Sensitive information like passwords and OAuth IDs
     * are excluded from the response.</p>
     *
     * <p><strong>Security:</strong> Returns UserDTOs that exclude sensitive fields
     * and prevent circular reference issues with roles.</p>
     *
     * @return List of all users as DTOs, safe for API responses
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during retrieval.
     *
     * @author Philipp Borkovic
     */
    public List<UserDTO> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();

            return UserMapper.toDTOList(users);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users", exception);
        }
    }

    /**
     * Retrieves users with server-side pagination, optional search filtering, and status filter.
     *
     * <p>This method provides efficient paginated access to users with optional
     * search functionality and status filter. It returns UserDTOs that exclude sensitive fields
     * and prevent circular reference issues.</p>
     *
     * @param page The page number (0-indexed)
     * @param size The number of items per page
     * @param searchQuery Optional search term to filter users
     * @param status Filter status: "active", "deleted", or "all" (defaults to "active")
     *
     * @return PageResponse containing user DTOs and pagination metadata
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if page/size are invalid
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during retrieval
     *
     * @author Philipp Borkovic
     */
    public PageResponse<UserDTO> getUsersPage(int page, int size, @Nullable String searchQuery, @Nullable String status) {
        try {
            if (page < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number must be >= 0, received: " + page);
            }
            if (size < 1 || size > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Page size must be between 1 and 100, received: " + size);
            }

            // Default to "active" if status is not provided
            String filterStatus = (status == null || status.isBlank()) ? "active" : status;

            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

            Page<User> userPage = userService.getUsersPageWithStatus(pageable, searchQuery, filterStatus);

            if (userPage == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service returned null page response");
            }
            if (userPage.getContent() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Service returned null content in page response");
            }

            List<UserDTO> userDTOs = UserMapper.toDTOList(userPage.getContent());

            if (userDTOs == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Mapper returned null DTO list");
            }

            return new PageResponse<>(
                userDTOs,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements()
            );
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid request parameters: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to retrieve users page", exception);
        }
    }

    /**
     * Updates a user's profile information.
     *
     * <p>This method allows updating user profile fields such as name and email.</p>
     *
     * @param userId The ID of the user to update
     * @param name The new name (optional, null to keep existing)
     * @param email The new email (optional, null to keep existing)
     *
     * @return The updated User object
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if userId is invalid
     *         or user not found
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during update
     *
     * @author Philipp Borkovic
     */
    public User updateProfile(@Nonnull UUID userId, String name, String email) {
        try {
            return userService.updateProfile(userId, name, email);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update profile", exception);
        }
    }

    /**
     * Updates comprehensive user information including all profile fields.
     *
     * @param userId The ID of the user to update
     * @param userData User object containing the fields to update
     *
     * @return The updated User object
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if userId is invalid
     *         or user not found
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during update
     *
     * @author Philipp Borkovic
     */
    public User updateUser(@Nonnull UUID userId, @Nonnull User userData) {
        try {
            return userService.updateUser(userId, userData);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user", exception);
        }
    }

}