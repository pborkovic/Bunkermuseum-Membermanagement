package com.bunkermuseum.membermanagement.dto;

import java.util.UUID;

/**
 * Data Transfer Object for Role entity.
 *
 * <p>This DTO provides a lightweight representation of roles for API responses.
 * It includes only the essential information needed by clients, avoiding
 * circular references and unnecessary data exposure.</p>
 *
 * @see com.bunkermuseum.membermanagement.model.Role
 */
public class RoleDTO {

    /**
     * The unique identifier for this role.
     */
    private final UUID id;

    /**
     * The role's unique name identifier (e.g., "ADMIN", "USER", "MODERATOR").
     */
    private final String name;

    /**
     * Constructs a new RoleDTO with the specified values.
     *
     * @param id The unique identifier for this role
     * @param name The role's unique name identifier
     *
     * @author Philipp Borkovic
     */
    public RoleDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the unique identifier for this role.
     *
     * @return The role's unique identifier
     *
     * @author Philipp Borkovic
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the role's unique name identifier.
     *
     * @return The role name (e.g., "ADMIN", "USER")
     *
     * @author Philipp Borkovic
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of the RoleDTO.
     *
     * @return A string representation of this role DTO
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("RoleDTO{id=%s, name='%s'}", id, name);
    }
}
