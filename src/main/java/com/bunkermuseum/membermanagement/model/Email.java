package com.bunkermuseum.membermanagement.model;

import com.bunkermuseum.membermanagement.model.base.Model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Email entity representing emails sent from the system or by users.
 *
 * <p>This entity extends the base {@link Model} class to inherit UUID primary keys,
 * automatic timestamps, and soft delete functionality. It provides comprehensive
 * email tracking for audit trails, debugging, and email history.</p>
 *
 * <h3>Database Schema:</h3>
 * <p>The emails table includes the following fields beyond the inherited base fields:</p>
 * <ul>
 *   <li><code>from_address</code> (VARCHAR(255), NOT NULL) - Sender's email address</li>
 *   <li><code>to_address</code> (VARCHAR(255), NOT NULL) - Recipient's email address</li>
 *   <li><code>subject</code> (VARCHAR(500), NOT NULL) - Email subject line</li>
 *   <li><code>content</code> (TEXT, NOT NULL) - Email body content</li>
 *   <li><code>user_id</code> (UUID, NULLABLE) - Optional reference to user who sent the email</li>
 * </ul>
 *
 * <h3>System vs User Emails:</h3>
 * <p>When <code>user_id</code> is null, the email is considered a system-generated email
 * (e.g., welcome emails, password resets, notifications). When <code>user_id</code> is
 * present, the email was sent by or on behalf of that specific user.</p>
 *
 * @see Model
 * @see User
 * @see jakarta.persistence.Entity
 * @see jakarta.validation.constraints
 */
@Entity
@Table(name = "emails",
    indexes = {
        @Index(name = "idx_emails_user_id", columnList = "user_id"),
        @Index(name = "idx_emails_from_address", columnList = "from_address"),
        @Index(name = "idx_emails_to_address", columnList = "to_address"),
        @Index(name = "idx_emails_created_at", columnList = "created_at"),
        @Index(name = "idx_emails_user_created", columnList = "user_id, created_at")
    }
)
public class Email extends Model {

    /**
     * Protected default constructor for JPA.
     *
     * <p>This constructor is required by JPA specification for entity instantiation
     * during database operations. It should not be called directly by application
     * code. Use the public constructors for creating new email instances.</p>
     *
     * @author Philipp Borkovic
     */
    protected Email() {
        // Default constructor for JPA
    }

    /**
     * Constructs a new system-generated Email (no user association).
     *
     * <p>Use this constructor when the email is sent by the system itself,
     * such as automated notifications, welcome emails, or password resets.</p>
     *
     * @param from the sender's email address
     * @param to the recipient's email address
     * @param subject the email subject line
     * @param content the email body content
     *
     * @author Philipp Borkovic
     */
    public Email(String from, String to, String subject, String content) {
        this.fromAddress = from;
        this.toAddress = to;
        this.subject = subject;
        this.content = content;
        this.user = null;
    }

    /**
     * Constructs a new user-associated Email.
     *
     * <p>Use this constructor when the email is sent by or on behalf of a
     * specific user, allowing for tracking and audit purposes.</p>
     *
     * @param from the sender's email address
     * @param to the recipient's email address
     * @param subject the email subject line
     * @param content the email body content
     * @param user the user who sent or triggered the email
     *
     * @author Philipp Borkovic
     */
    public Email(String from, String to, String subject, String content, User user) {
        this.fromAddress = from;
        this.toAddress = to;
        this.subject = subject;
        this.content = content;
        this.user = user;
    }

    /**
     * The sender's email address.
     *
     * <p>This field stores the email address that appears in the "From" header
     * of the sent email. It can be a system email address (e.g., noreply@bunkermuseum.com)
     * or a user's email address depending on the context.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Maximum length of 255 characters</li>
     *   <li>Should be a valid email format (validated at service layer)</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'from_address' column</p>
     * <p><strong>Constraints:</strong> NOT NULL, VARCHAR(255)</p>
     */
    @Column(name = "from_address", nullable = false, length = 255)
    @NotBlank(message = "From address is required and cannot be blank")
    @Size(max = 255, message = "From address must not exceed 255 characters")
    private String fromAddress;

    /**
     * The recipient's email address.
     *
     * <p>This field stores the email address that appears in the "To" header
     * of the sent email. This is the primary recipient of the email.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Maximum length of 255 characters</li>
     *   <li>Should be a valid email format (validated at service layer)</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'to_address' column</p>
     * <p><strong>Constraints:</strong> NOT NULL, VARCHAR(255)</p>
     */
    @Column(name = "to_address", nullable = false, length = 255)
    @NotBlank(message = "To address is required and cannot be blank")
    @Size(max = 255, message = "To address must not exceed 255 characters")
    private String toAddress;

    /**
     * The email subject line.
     *
     * <p>This field stores the subject line of the email as it appears in
     * the email client. It provides a brief summary of the email's purpose.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Maximum length of 500 characters</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'subject' column</p>
     * <p><strong>Constraints:</strong> NOT NULL, VARCHAR(500)</p>
     */
    @Column(name = "subject", nullable = false, length = 500)
    @NotBlank(message = "Subject is required and cannot be blank")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    /**
     * The email body content.
     *
     * <p>This field stores the full body content of the email, which can be
     * plain text or HTML. The content type should be determined by the email
     * sending service.</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Must not be null or blank</li>
     *   <li>Can contain very long text (stored as TEXT type)</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'content' column</p>
     * <p><strong>Constraints:</strong> NOT NULL, TEXT</p>
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Content is required and cannot be blank")
    private String content;

    /**
     * Optional reference to the user who sent or triggered the email.
     *
     * <p>This field creates a many-to-one relationship with the User entity,
     * allowing tracking of which user sent or triggered the email. When null,
     * the email is considered a system-generated email with no specific user
     * association.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Null: System emails (welcome, password reset, notifications)</li>
     *   <li>Not Null: User-triggered emails (contact forms, invitations)</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'user_id' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, FOREIGN KEY to users.id</p>
     * <p><strong>Fetch Strategy:</strong> LAZY (loaded only when accessed)</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private @Nullable User user;

    /**
     * Gets the sender's email address.
     *
     * @return the from email address
     *
     * @author Philipp Borkovic
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * Sets the sender's email address.
     *
     * @param fromAddress the from email address to set
     *
     * @author Philipp Borkovic
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * Gets the recipient's email address.
     *
     * @return the to email address
     *
     * @author Philipp Borkovic
     */
    public String getToAddress() {
        return toAddress;
    }

    /**
     * Sets the recipient's email address.
     *
     * @param toAddress the to email address to set
     *
     * @author Philipp Borkovic
     */
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    /**
     * Gets the email subject line.
     *
     * @return the email subject
     *
     * @author Philipp Borkovic
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the email subject line.
     *
     * @param subject the subject to set
     *
     * @author Philipp Borkovic
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the email body content.
     *
     * @return the email content
     *
     * @author Philipp Borkovic
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the email body content.
     *
     * @param content the content to set
     *
     * @author Philipp Borkovic
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the user who sent or triggered the email.
     *
     * <p>Returns null if this is a system-generated email with no user association.</p>
     *
     * @return the associated user, or null for system emails
     *
     * @author Philipp Borkovic
     */
    public @Nullable User getUser() {
        return user;
    }

    /**
     * Sets the user who sent or triggered the email.
     *
     * <p>Set to null for system-generated emails.</p>
     *
     * @param user the user to associate with this email, or null for system emails
     *
     * @author Philipp Borkovic
     */
    public void setUser(@Nullable User user) {
        this.user = user;
    }

    /**
     * Checks if this email is a system-generated email.
     *
     * <p>An email is considered system-generated if it has no associated user.</p>
     *
     * @return true if this is a system email, false if user-associated
     *
     * @author Philipp Borkovic
     */
    public boolean isSystemEmail() {
        return user == null;
    }

    /**
     * Checks if this email is associated with a user.
     *
     * <p>An email is considered user-associated if it has a linked user.</p>
     *
     * @return true if this email is user-associated, false if system-generated
     *
     * @author Philipp Borkovic
     */
    public boolean isUserEmail() {
        return user != null;
    }
}
