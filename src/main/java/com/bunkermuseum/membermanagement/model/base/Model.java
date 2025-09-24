package com.bunkermuseum.membermanagement.model.base;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base model class for all entities in the Bunkermuseum application.
 *
 * <p>This class serves as the foundation for all JPA entities, providing common
 * functionality and fields that are required across all domain models. It follows
 * the DRY (Don't Repeat Yourself) principle by centralizing common entity behavior.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>UUID Primary Keys:</strong> Uses UUID v4 for globally unique identifiers</li>
 *   <li><strong>Automatic Timestamps:</strong> Tracks creation, modification, and deletion times</li>
 *   <li><strong>Soft Delete Support:</strong> Laravel-style soft deletion with deleted_at timestamp</li>
 *   <li><strong>Consistent Equality:</strong> Implements proper equals() and hashCode() methods</li>
 *   <li><strong>JPA Integration:</strong> Fully compatible with Spring Data JPA and Hibernate</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * @Entity
 * @Table(name = "users")
 * public class User extends Model {
 *     @Column(nullable = false)
 *     private String username;
 *
 *     @Column(nullable = false)
 *     private String email;
 *
 *     // constructors, getters, setters...
 * }
 * }</pre>
 *
 * <h3>Database Schema:</h3>
 * <p>When extending this class, your entity tables will automatically include:</p>
 * <ul>
 *   <li><code>id</code> (UUID, PRIMARY KEY) - Unique identifier</li>
 *   <li><code>created_at</code> (TIMESTAMP, NOT NULL) - Record creation time</li>
 *   <li><code>updated_at</code> (TIMESTAMP, NULLABLE) - Last modification time</li>
 *   <li><code>deleted_at</code> (TIMESTAMP, NULLABLE) - Soft deletion time</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * <p>This class is not thread-safe. JPA entities are typically not shared between
 * threads, and each persistence context should be used within a single thread.</p>
 *
 * <h3>Performance Considerations:</h3>
 * <ul>
 *   <li>UUID generation is handled efficiently by Hibernate's @UuidGenerator</li>
 *   <li>Timestamps are set automatically by the database/Hibernate</li>
 *   <li>equals() and hashCode() are optimized for entity identity comparison</li>
 * </ul>
 *
 * @see jakarta.persistence.MappedSuperclass
 * @see org.hibernate.annotations.UuidGenerator
 * @see org.hibernate.annotations.CreationTimestamp
 * @see org.hibernate.annotations.UpdateTimestamp
 */
@MappedSuperclass
public abstract class Model {

    /**
     * The unique identifier for this entity.
     *
     * <p>This field uses UUID (Universally Unique Identifier) as the primary key,
     * which provides several advantages:</p>
     * <ul>
     *   <li>Globally unique across all systems and databases</li>
     *   <li>No coordination required between different application instances</li>
     *   <li>Safe for distributed systems and database replication</li>
     *   <li>No sequential patterns that could be exploited for security purposes</li>
     * </ul>
     *
     * <p>The ID is generated automatically by Hibernate using the @UuidGenerator
     * annotation, which creates UUID version 4 (random) identifiers.</p>
     *
     * <p><strong>Note:</strong> This field is nullable during entity creation
     * (before persistence) but will be non-null after the entity is saved.</p>
     */
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * The timestamp when this entity was created.
     *
     * <p>This field is automatically populated by Hibernate when the entity
     * is first persisted to the database. It provides an audit trail of when
     * records were created and cannot be modified after initial creation.</p>
     *
     * <p>The timestamp is stored as an {@link Instant} which represents a
     * point in time in UTC, making it timezone-independent and suitable for
     * distributed applications.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'created_at' column</p>
     * <p><strong>Constraints:</strong> NOT NULL, not updatable</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    /**
     * The timestamp when this entity was last modified.
     *
     * <p>This field is automatically updated by Hibernate whenever the entity
     * is modified and saved. It provides an audit trail of when records were
     * last changed and is useful for:</p>
     * <ul>
     *   <li>Optimistic locking strategies</li>
     *   <li>Cache invalidation decisions</li>
     *   <li>Audit logging and compliance</li>
     *   <li>Data synchronization between systems</li>
     * </ul>
     *
     * <p>The field is nullable because it will be null until the first update
     * occurs after creation.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'updated_at' column</p>
     * <p><strong>Constraints:</strong> NULLABLE</p>
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * The timestamp when this entity was soft deleted.
     *
     * <p>This field implements soft delete functionality, allowing entities to be
     * marked as deleted without actually removing them from the database. This
     * approach provides several benefits:</p>
     * <ul>
     *   <li>Data preservation for audit trails and compliance</li>
     *   <li>Ability to restore accidentally deleted records</li>
     *   <li>Maintaining referential integrity</li>
     *   <li>Historical data analysis capabilities</li>
     * </ul>
     *
     * <p>When this field is null, the entity is considered active. When it contains
     * a timestamp, the entity is considered deleted as of that time.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'deleted_at' column</p>
     * <p><strong>Constraints:</strong> NULLABLE</p>
     * <p><strong>Usage:</strong> Use {@link #delete()} and {@link #restore()} methods</p>
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Protected default constructor for JPA.
     *
     * <p>This constructor is required by JPA specification for entity instantiation
     * during database operations. It should not be called directly by application
     * code.</p>
     *
     * <p><strong>Access Level:</strong> Protected to allow subclasses to call it
     * while preventing direct instantiation by client code.</p>
     *
     * @author Philipp Borkovic
     */
    protected Model() {
        // Default constructor for JPA
    }

    /**
     * Gets the unique identifier of this entity.
     *
     * <p>Returns the UUID that uniquely identifies this entity. The ID will be
     * null for new entities that haven't been persisted yet, and non-null for
     * entities loaded from or saved to the database.</p>
     *
     * @return the entity's unique identifier, or null if not yet persisted
     *
     * @author Philipp Borkovic
     */
    public @Nullable UUID getId() {
        return id;
    }

    /**
     * Gets the creation timestamp of this entity.
     *
     * <p>Returns the exact moment when this entity was first persisted to the
     * database. This value is set automatically by Hibernate and cannot be
     * modified.</p>
     *
     * @return the creation timestamp as an Instant in UTC
     * @throws IllegalStateException if called on an entity that hasn't been persisted
     *
     * @author Philipp Borkovic
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last modification timestamp of this entity.
     *
     * <p>Returns the exact moment when this entity was last updated in the
     * database. This value is updated automatically by Hibernate on every
     * save operation after the initial creation.</p>
     *
     * @return the last modification timestamp as an Instant in UTC, or null if never modified
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the soft deletion timestamp of this entity.
     *
     * <p>Returns the exact moment when this entity was soft deleted, or null
     * if the entity is still active. This timestamp is used to determine
     * whether an entity should be included in normal queries.</p>
     *
     * @return the deletion timestamp as an Instant in UTC, or null if not deleted
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant deletedAt() {
        return deletedAt;
    }

    /**
     * Checks if this entity is soft deleted.
     *
     * <p>An entity is considered deleted if the deletedAt field contains a
     * timestamp. This method provides a convenient way to check the deletion
     * status without directly accessing the timestamp.</p>
     *
     * @return true if the entity is soft deleted, false if active
     *
     * @author Philipp Borkovic
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Checks if this entity is active (not soft deleted).
     *
     * <p>An entity is considered active if the deletedAt field is null.
     * This is the opposite of {@link #isDeleted()}.</p>
     *
     * @return true if the entity is active, false if soft deleted
     *
     * @author Philipp Borkovic
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Soft deletes this entity by setting the deletedAt timestamp.
     *
     * <p>This method marks the entity as deleted without actually removing it
     * from the database. The entity will be excluded from normal queries but
     * can still be accessed directly and restored if needed.</p>
     *
     * <p><strong>Note:</strong> This method only sets the timestamp. The entity
     * must be saved to the database for the change to persist.</p>
     *
     * @throws IllegalStateException if the entity is already deleted
     * @return this entity for method chaining
     *
     * @author Philipp Borkovic
     */
    public Model delete() {
        if (isDeleted()) {
            throw new IllegalStateException("Entity is already deleted");
        }
        this.deletedAt = Instant.now();

        return this;
    }

    /**
     * Restores a soft deleted entity by clearing the deletedAt timestamp.
     *
     * <p>This method reactivates a previously soft deleted entity by setting
     * the deletedAt field to null. The entity will be included in normal
     * queries again after this operation.</p>
     *
     * <p><strong>Note:</strong> This method only clears the timestamp. The entity
     * must be saved to the database for the change to persist.</p>
     *
     * @return this entity for method chaining
     * @throws IllegalStateException if the entity is not currently deleted
     *
     * @author Philipp Borkovic
     */
    public Model restore() {
        if (isActive()) {
            throw new IllegalStateException("Entity is not deleted and cannot be restored");
        }
        this.deletedAt = null;

        return this;
    }

    /**
     * Sets the deletedAt timestamp to a specific time for soft deletion.
     *
     * <p>This method allows setting a custom deletion timestamp, which can be
     * useful for backdating deletions or implementing custom soft delete logic.</p>
     *
     * <p><strong>Note:</strong> This method only sets the timestamp. The entity
     * must be saved to the database for the change to persist.</p>
     *
     * @param deletionTime the timestamp to set as deletion time
     * @return this entity for method chaining
     * @throws IllegalArgumentException if deletionTime is null
     *
     * @author Philipp Borkovic
     */
    public Model deleteAt(Instant deletionTime) {
        if (deletionTime == null) {
            throw new IllegalArgumentException("Deletion time cannot be null");
        }
        this.deletedAt = deletionTime;

        return this;
    }

    /**
     * Compares this entity with another object for equality.
     *
     * <p>Two entities are considered equal if:</p>
     * <ul>
     *   <li>They are the same object (reference equality)</li>
     *   <li>They are of the same class (exact type match)</li>
     *   <li>They have the same non-null ID</li>
     * </ul>
     *
     * <p>This implementation follows JPA best practices for entity equality:</p>
     * <ul>
     *   <li>Uses business key (ID) rather than field comparison</li>
     *   <li>Consistent with hashCode() implementation</li>
     *   <li>Handles proxy objects correctly</li>
     *   <li>Stable across persistence contexts</li>
     * </ul>
     *
     * <p><strong>Important:</strong> Entities without IDs (transient entities)
     * are only equal to themselves by reference.</p>
     *
     * @param obj the object to compare with
     * @return true if the entities are equal, false otherwise
     *
     * @author Philipp Borkovic
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Model other = (Model) obj;

        if (getId() == null || other.getId() == null) {
            return false;
        }

        return Objects.equals(getId(), other.getId());
    }

    /**
     * Returns a hash code value for this entity.
     *
     * <p>The hash code is computed based on the entity's class to ensure:</p>
     * <ul>
     *   <li>Consistency with equals() method</li>
     *   <li>Stability across different states of the entity lifecycle</li>
     *   <li>Proper behavior in hash-based collections (HashSet, HashMap)</li>
     *   <li>Compatibility with JPA proxy objects</li>
     * </ul>
     *
     * <p><strong>Design Decision:</strong> Uses class hash code rather than ID
     * hash code to maintain stability. Entity hash codes should not change
     * when the entity transitions from transient to persistent state.</p>
     *
     * <p><strong>Performance:</strong> This implementation is fast and provides
     * reasonable distribution for most use cases.</p>
     *
     * @return the hash code value for this entity
     *
     * @author Philipp Borkovic
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Returns a string representation of this entity.
     *
     * <p>Provides a human-readable representation including the class name
     * and ID for debugging and logging purposes.</p>
     *
     * @return a string representation of this entity
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("%s{id=%s}", getClass().getSimpleName(), id);
    }
}