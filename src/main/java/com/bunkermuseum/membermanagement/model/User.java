package com.bunkermuseum.membermanagement.model;

import com.bunkermuseum.membermanagement.model.base.Model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import java.time.Instant;

/**
 * User entity representing system users with authentication and profile information.
 *
 * <p>This entity extends the base {@link Model} class to inherit UUID primary keys,
 * automatic timestamps, and soft delete functionality. It provides comprehensive
 * user data storage for traditional email/password authentication and OAuth
 * integration with external providers.</p>
 *
 * <h3>Database Schema:</h3>
 * <p>The users table includes the following fields beyond the inherited base fields:</p>
 * <ul>
 *   <li><code>name</code> (VARCHAR(100), NOT NULL) - User's display name</li>
 *   <li><code>email</code> (VARCHAR(255), UNIQUE, NOT NULL) - User's email address</li>
 *   <li><code>email_verified_at</code> (TIMESTAMP, NULLABLE) - Email verification timestamp</li>
 *   <li><code>password</code> (VARCHAR(255), NULLABLE) - Hashed password for traditional auth</li>
 *   <li><code>avatar_path</code> (VARCHAR(500), NULLABLE) - Path to user's avatar image</li>
 *   <li><code>google_id</code> (VARCHAR(255), UNIQUE, NULLABLE) - Google OAuth user ID</li>
 *   <li><code>microsoft_id</code> (VARCHAR(255), UNIQUE, NULLABLE) - Microsoft OAuth user ID</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see Model
 * @see jakarta.persistence.Entity
 * @see jakarta.validation.constraints
 */
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email"),
        @UniqueConstraint(columnNames = "google_id", name = "uk_users_google_id"),
        @UniqueConstraint(columnNames = "microsoft_id", name = "uk_users_microsoft_id")
    },
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_name", columnList = "name"),
        @Index(name = "idx_users_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_users_name_deleted", columnList = "name, deleted_at"),
    }
)
public class User extends Model {

    /**
     * Protected default constructor for JPA.
     *
     * <p>This constructor is required by JPA specification for entity instantiation
     * during database operations. It should not be called directly by application
     * code. Use the public constructors for creating new user instances.</p>
     *
     * @author Philipp Borkovic
     */
    protected User() {
        // Default constructor for JPA
    }

    /**
     * The user's display name.
     *
     * <p>This field stores the user's full name or preferred display name.
     * It's used throughout the application for user identification and
     * personalization purposes.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Length must be between 2 and 100 characters</li>
     *   <li>Whitespace-only strings are not allowed</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'name' column</p>
     * <p><strong>Constraints:</strong> NOT NULL, VARCHAR(100)</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Name is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    /**
     * The user's email address.
     *
     * <p>This field serves as the primary identifier for user accounts and
     * must be unique across the entire system. It's used for authentication,
     * notifications, and account recovery processes.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Must follow valid email format (validated by @Email annotation)</li>
     *   <li>Must be unique across all users (enforced by database constraint)</li>
     *   <li>Maximum length of 255 characters</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'email' column with unique constraint</p>
     * <p><strong>Constraints:</strong> NOT NULL, UNIQUE, VARCHAR(255)</p>
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    @NotBlank(message = "Email is required and cannot be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * The timestamp when the user's email address was verified.
     *
     * <p>This field tracks when a user completed the email verification process.
     * A null value indicates the email has not been verified yet, while a
     * non-null timestamp shows when verification was successfully completed.</p>
     *
     * <p><strong>Email Verification Workflow:</strong></p>
     * <ul>
     *   <li>New accounts start with emailVerifiedAt = null</li>
     *   <li>System sends verification email to user</li>
     *   <li>User clicks verification link in email</li>
     *   <li>System sets emailVerifiedAt to current timestamp</li>
     * </ul>
     *
     * <p><strong>Business Logic Implications:</strong></p>
     * <ul>
     *   <li>Unverified users may have limited system access</li>
     *   <li>Certain operations may require verified email status</li>
     *   <li>Email changes typically reset this field to null</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'email_verified_at' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, TIMESTAMP</p>
     */
    @Column(name = "email_verified_at")
    private @Nullable Instant emailVerifiedAt;

    /**
     * The user's hashed password for authentication.
     *
     * <p>This field stores the securely hashed password for users who authenticate
     * using traditional email/password credentials. The password is never stored
     * in plain text and should be hashed using strong algorithms like bcrypt.</p>
     *
     * <p><strong>Security Requirements:</strong></p>
     * <ul>
     *   <li>Always store hashed passwords, never plain text</li>
     *   <li>Use secure hashing algorithms (bcrypt, Argon2, PBKDF2)</li>
     *   <li>Include salt to prevent rainbow table attacks</li>
     *   <li>Never log or expose password values</li>
     * </ul>
     *
     * <p><strong>OAuth Users:</strong> This field may be null for users who
     * authenticate exclusively through OAuth providers and have never set
     * a local password.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'password' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "password", length = 255)
    private @Nullable String password;

    /**
     * The file path to the user's avatar image.
     *
     * <p>This field stores the path to the user's profile avatar image.
     * The path can be relative (for local storage), absolute (for file systems),
     * or a URL (for CDN/cloud storage services).</p>
     *
     * <p><strong>Supported Path Types:</strong></p>
     * <ul>
     *   <li>Relative paths: "/uploads/avatars/user-123.jpg"</li>
     *   <li>CDN URLs: "https://cdn.example.com/avatars/user-123.jpg"</li>
     *   <li>Cloud storage: "s3://bucket/avatars/user-123.jpg"</li>
     * </ul>
     *
     * <p><strong>Default Behavior:</strong> When null, the application should
     * display a default avatar or generate one based on user initials.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'avatar_path' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(500)</p>
     */
    @Column(name = "avatar_path", length = 500)
    private @Nullable String avatarPath;

    /**
     * The user's unique identifier from Google OAuth.
     *
     * <p>This field stores the unique user identifier provided by Google during
     * the OAuth authentication process. It enables users to sign in using their
     * Google account and creates a link between the local user account and
     * their Google identity.</p>
     *
     * <p><strong>OAuth Integration:</strong></p>
     * <ul>
     *   <li>Populated during Google OAuth authentication flow</li>
     *   <li>Used to link Google account with local user account</li>
     *   <li>Enables "Sign in with Google" functionality</li>
     *   <li>Must be unique across all users in the system</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'google_id' column with unique constraint</p>
     * <p><strong>Constraints:</strong> NULLABLE, UNIQUE, VARCHAR(255)</p>
     */
    @Column(name = "google_id", unique = true, length = 255)
    private @Nullable String googleId;

    /**
     * The user's unique identifier from Microsoft OAuth.
     *
     * <p>This field stores the unique user identifier provided by Microsoft during
     * the OAuth authentication process. It enables users to sign in using their
     * Microsoft account (including Azure AD, Office 365, Outlook.com) and creates
     * a link between the local user account and their Microsoft identity.</p>
     *
     * <p><strong>OAuth Integration:</strong></p>
     * <ul>
     *   <li>Populated during Microsoft OAuth authentication flow</li>
     *   <li>Used to link Microsoft account with local user account</li>
     *   <li>Enables "Sign in with Microsoft" functionality</li>
     *   <li>Supports both consumer and enterprise Microsoft accounts</li>
     *   <li>Must be unique across all users in the system</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'microsoft_id' column with unique constraint</p>
     * <p><strong>Constraints:</strong> NULLABLE, UNIQUE, VARCHAR(255)</p>
     */
    @Column(name = "microsoft_id", unique = true, length = 255)
    private @Nullable String microsoftId;

    /**
     * Creates a new user with basic authentication information.
     *
     * <p>This constructor creates a user account with the essential information
     * needed for user identification and authentication. Additional fields like
     * OAuth IDs and verification status should be set using the appropriate
     * setter methods.</p>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Create user with hashed password
     * String hashedPassword = passwordEncoder.encode("userPassword123");
     * User user = new User("John Doe", "john@example.com", hashedPassword);
     *
     * // Create OAuth-only user (no password)
     * User oauthUser = new User("Jane Doe", "jane@gmail.com", null);
     * oauthUser.setGoogleId("google_123456789");
     * }</pre>
     *
     * @param name The user's display name. Must not be null or blank.
     * @param email The user's email address. Must not be null, blank, or invalid format.
     * @param password The hashed password. Can be null for OAuth-only users.
     *
     * @author Philipp Borkovic
     */
    public User(String name, String email, @Nullable String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters

    /**
     * Gets the user's display name.
     *
     * @return The user's name, never null for persistent entities
     *
     * @author Philipp Borkovic
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's display name.
     *
     * <p><strong>Validation:</strong> The name will be validated by Jakarta
     * validation annotations when the entity is persisted or updated.</p>
     *
     * @param name The new name. Should not be null, blank, or outside valid length range.
     *
     * @author Philipp Borkovic
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     *
     * @return The user's email, never null for persistent entities
     *
     * @author Philipp Borkovic
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * <p><strong>Important:</strong> Changing the email address typically requires
     * email re-verification in most applications. Consider clearing the email
     * verification status when changing this field.</p>
     *
     * <p><strong>Validation:</strong> The email will be validated by Jakarta
     * validation annotations when the entity is persisted or updated.</p>
     *
     * @param email The new email address. Should not be null, blank, or invalid format.
     *
     * @author Philipp Borkovic
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the email verification timestamp.
     *
     * @return The timestamp when email was verified, or null if not verified
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    /**
     * Sets the email verification timestamp.
     *
     * <p>This method is typically called by the service layer when processing
     * email verification workflows. Setting this to a non-null value indicates
     * the user has successfully verified their email address.</p>
     *
     * @param emailVerifiedAt The verification timestamp, or null to mark as unverified
     *
     * @author Philipp Borkovic
     */
    public void setEmailVerifiedAt(@Nullable Instant emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    /**
     * Gets the hashed password.
     *
     * <p><strong>Security Note:</strong> This method returns the hashed password.
     * Never log or expose this value. Use password verification utilities
     * instead of direct comparison.</p>
     *
     * @return The hashed password, or null for OAuth-only users
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * <p><strong>Security Warning:</strong> The password should be securely hashed
     * before calling this method. Never store plain text passwords.</p>
     *
     * @param password The hashed password, or null for OAuth-only users
     *
     * @author Philipp Borkovic
     */
    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    /**
     * Gets the avatar image path.
     *
     * @return The path to the user's avatar image, or null if no avatar is set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getAvatarPath() {
        return avatarPath;
    }

    /**
     * Sets the avatar image path.
     *
     * @param avatarPath The path to the avatar image, or null to remove avatar
     *
     * @author Philipp Borkovic
     */
    public void setAvatarPath(@Nullable String avatarPath) {
        this.avatarPath = avatarPath;
    }

    /**
     * Gets the Google OAuth user ID.
     *
     * @return The Google user ID, or null if not linked to Google
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getGoogleId() {
        return googleId;
    }

    /**
     * Sets the Google OAuth user ID.
     *
     * <p>This method is typically called during the Google OAuth authentication
     * flow to link the user account with their Google identity.</p>
     *
     * @param googleId The Google user ID, or null to unlink from Google
     *
     * @author Philipp Borkovic
     */
    public void setGoogleId(@Nullable String googleId) {
        this.googleId = googleId;
    }

    /**
     * Gets the Microsoft OAuth user ID.
     *
     * @return The Microsoft user ID, or null if not linked to Microsoft
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getMicrosoftId() {
        return microsoftId;
    }

    /**
     * Sets the Microsoft OAuth user ID.
     *
     * <p>This method is typically called during the Microsoft OAuth authentication
     * flow to link the user account with their Microsoft identity.</p>
     *
     * @param microsoftId The Microsoft user ID, or null to unlink from Microsoft
     *
     * @author Philipp Borkovic
     */
    public void setMicrosoftId(@Nullable String microsoftId) {
        this.microsoftId = microsoftId;
    }

    /**
     * Returns a string representation of the User entity.
     *
     * <p>Provides a human-readable representation including the class name,
     * ID, name, and email for debugging and logging purposes. Sensitive
     * information like passwords and OAuth IDs are not included.</p>
     *
     * @return A string representation of this user
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("User{id=%s, name='%s', email='%s'}",
            getId(), name, email);
    }
}