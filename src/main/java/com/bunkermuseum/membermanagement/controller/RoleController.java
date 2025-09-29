package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.service.contract.RoleServiceContract;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for Role management operations.
 *
 * <p>This controller provides comprehensive RESTful endpoints for role management,
 * including role creation, modification, deletion, search functionality, and
 * administrative operations. It follows REST conventions and provides proper
 * HTTP status codes and error handling.</p>
 *
 * <h3>Endpoint Categories:</h3>
 * <ul>
 *   <li><strong>Role CRUD:</strong> Create, read, update, delete operations</li>
 *   <li><strong>Role Search:</strong> Find roles by name, type, or search term</li>
 *   <li><strong>Role Management:</strong> System vs custom role operations</li>
 *   <li><strong>Administrative:</strong> Role initialization and validation</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see RoleServiceContract
 * @see Role
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleServiceContract roleService;

    /**
     * Constructs a new RoleController with the provided service.
     *
     * @param roleService The role service for business operations
     *
     * @author Philipp Borkovic
     */
    public RoleController(RoleServiceContract roleService) {
        this.roleService = roleService;
    }

}