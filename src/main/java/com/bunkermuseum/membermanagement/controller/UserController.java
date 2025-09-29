package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("/api/users")
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

}