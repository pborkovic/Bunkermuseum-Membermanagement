package com.bunkermuseum.membermanagement.model;

import com.bunkermuseum.membermanagement.model.base.Model;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a password setup token for new users.
 *
 * <p>When an admin creates a new user account, a password setup token is generated
 * and emailed to the user. The user clicks the link in the email to access a page
 * where they can set their own password.</p>
 *
 * <h3>Token Lifecycle:</h3>
 * <ol>
 *     <li>Admin creates user account without password</li>
 *     <li>System generates unique token and stores in this table</li>
 *     <li>System sends email with setup link containing token</li>
 *     <li>User clicks link and sets their password</li>
 *     <li>Token is marked as used (used_at timestamp set)</li>
 *     <li>Used or expired tokens cannot be reused</li>
 * </ol>
 *
 * @see User
 * @see Model
 */
@Entity
@Table(name = "password_setup_tokens")
public class PasswordSetupToken extends Model {

    /**
     * The user for whom this password setup token was created.
     *
     * <p>Foreign key relationship to users table.
     * Cascading deletion ensures tokens are removed when user is deleted.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The unique token string sent to the user in the email.
     *
     * <p>This is a cryptographically secure random string that serves as
     * the authentication mechanism for the password setup process.</p>
     *
     * <p>Format: UUID v4 string (36 characters)</p>
     */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Timestamp when the token expires.
     *
     * <p>After this time, the token cannot be used to set a password.
     * Typical expiration period is 24 hours from creation.</p>
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the token was used to set a password.
     *
     * <p>Null if the token hasn't been used yet.
     * Once set, the token cannot be reused.</p>
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Default constructor for JPA.
     */
    public PasswordSetupToken() {
    }

    /**
     * Constructor for creating a new password setup token.
     *
     * @param user the user for whom the token is created
     * @param token the unique token string
     * @param expiresAt when the token expires
     */
    public PasswordSetupToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Checks if the token is still valid for use.
     *
     * <p>A token is valid if it:
     * <ul>
     *     <li>Has not been used yet (usedAt is null)</li>
     *     <li>Has not expired (expiresAt is in the future)</li>
     * </ul>
     *
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return usedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Marks the token as used.
     *
     * <p>Sets the usedAt timestamp to the current time, preventing reuse.</p>
     */
    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
