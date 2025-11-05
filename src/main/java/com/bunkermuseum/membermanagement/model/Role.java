package com.bunkermuseum.membermanagement.model;

import com.bunkermuseum.membermanagement.model.base.Model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity representing system roles.
 *
 * <p>This entity extends the base {@link Model} class to inherit UUID primary keys,
 * automatic timestamps, and soft delete functionality. It provides simple
 * role-based access control (RBAC) functionality for the application.</p>
 *
 * <h3>Database Schema:</h3>
 * <p>The roles table includes the following fields beyond the inherited base fields:</p>
 * <ul>
 *   <li><code>name</code> (VARCHAR(100), UNIQUE, NOT NULL) - Role name (e.g., "ADMIN", "USER")</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see Model
 * @see User
 * @see jakarta.persistence.Entity
 * @see jakarta.validation.constraints
 */
@Entity
@Table(name = "roles",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_roles_name")
    },
    indexes = {
        @Index(name = "idx_roles_name", columnList = "name"),
        @Index(name = "idx_roles_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_roles_name_deleted", columnList = "name, deleted_at")
    }
)
public class Role extends Model {

    /**
     * Protected default constructor for JPA.
     *
     * <p>This constructor is required by JPA specification for entity instantiation
     * during database operations. It should not be called directly by application
     * code. Use the public constructors for creating new role instances.</p>
     *
     * @author Philipp Borkovic
     */
    protected Role() {
        // Default constructor for JPA
    }

    /**
     * The role's unique name identifier.
     *
     * <p>This field stores the role's unique name, typically in uppercase format
     * (e.g., "ADMIN", "USER", "MODERATOR"). It serves as the primary identifier
     * for role-based access control checks throughout the application.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Length must be between 2 and 100 characters</li>
     *   <li>Must be unique across all roles</li>
     *   <li>Whitespace-only strings are not allowed</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'name' column with unique constraint</p>
     * <p><strong>Constraints:</strong> NOT NULL, UNIQUE, VARCHAR(100)</p>
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Role name is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    /**
     * The users who have been assigned this role.
     *
     * <p>This represents the many-to-many relationship between roles and users.
     * Each role can be assigned to multiple users, and each user can have multiple roles.
     * The relationship is managed through a join table 'user_roles'.</p>
     *
     * <p><strong>Relationship Details:</strong></p>
     * <ul>
     *   <li>Bidirectional many-to-many mapping</li>
     *   <li>Join table: user_roles</li>
     *   <li>Foreign keys: user_id, role_id</li>
     *   <li>Lazy loading for performance</li>
     * </ul>
     */
    @JsonBackReference
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    /**
     * Creates a new role with the specified name.
     *
     * @param name The role's unique name identifier. Must not be null or blank.
     *
     * @author Philipp Borkovic
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * Gets the role's unique name identifier.
     *
     * @return The role name, never null for persistent entities
     *
     * @author Philipp Borkovic
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the role's unique name identifier.
     *
     * @param name The new role name. Should not be null, blank, or duplicate existing names.
     *
     * @author Philipp Borkovic
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the users who have been assigned this role.
     *
     * @return Set of users with this role, never null but may be empty
     *
     * @author Philipp Borkovic
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * Sets the users who have been assigned this role.
     *
     * @param users Set of users to assign this role to
     *
     * @author Philipp Borkovic
     */
    public void setUsers(Set<User> users) {
        this.users = users != null ? users : new HashSet<>();
    }

    /**
     * Returns a string representation of the Role entity.
     *
     * @return A string representation of this role
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("Role{id=%s, name='%s'}",
            getId(), name);
    }
}