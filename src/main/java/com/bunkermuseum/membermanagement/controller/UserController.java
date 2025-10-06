package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.UserService;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
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
     * <p>This method fetches all registered users. It's intended for administrative
     * purposes and should be protected by appropriate authorization checks.</p>
     *
     * @return List of all users in the system
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during retrieval.
     *
     * @author Philipp Borkovic
     */
    public List<User> getAllUsers() {
        try {
            return userService.getAllUsers();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users", exception);
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

}