package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.InputStreamSource;

/**
 * Service contract interface for email sending operations.
 *
 * <p>This interface defines the contract for email sending business logic,
 * including sending simple emails and emails with attachments. All emails
 * are automatically saved to the database for audit and tracking purposes.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li><strong>Simple Emails:</strong> Send plain text or HTML emails without attachments</li>
 *   <li><strong>Attachment Support:</strong> Send emails with file attachments</li>
 *   <li><strong>System Emails:</strong> Send automated system emails (user_id = null)</li>
 *   <li><strong>User Emails:</strong> Send emails on behalf of users (user_id = user.id)</li>
 *   <li><strong>Database Logging:</strong> All sent emails are saved to database automatically</li>
 *   <li><strong>Error Handling:</strong> Comprehensive error handling and logging</li>
 * </ul>
 *
 * @see Email
 * @see User
 */
public interface EmailServiceContract {

    /**
     * Sends a simple email without attachments (system email).
     *
     * <p>This method sends a plain text or HTML email without any attachments.
     * The email is sent by the system (user_id will be null) and is automatically
     * saved to the database for audit purposes.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Welcome emails to new users</li>
     *   <li>Password reset notifications</li>
     *   <li>System notifications and alerts</li>
     *   <li>Automated reports and summaries</li>
     * </ul>
     *
     * @param from the sender's email address (e.g., "noreply@bunkermuseum.com")
     * @param to the recipient's email address
     * @param subject the email subject line
     * @param content the email body content (plain text or HTML)
     *
     * @return the Email entity that was created and saved to the database
     *
     * @throws IllegalArgumentException if any parameter is null or blank
     * @throws RuntimeException if email sending or database save fails
     */
    Email sendSimpleEmail(String from, String to, String subject, String content);

    /**
     * Sends a simple email without attachments on behalf of a user.
     *
     * <p>This method sends a plain text or HTML email without any attachments.
     * The email is associated with the specified user (user_id will be set) and
     * is automatically saved to the database for audit purposes.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>User-triggered notifications</li>
     *   <li>Contact form submissions</li>
     *   <li>User invitations</li>
     *   <li>Member communications</li>
     * </ul>
     *
     * @param from the sender's email address
     * @param to the recipient's email address
     * @param subject the email subject line
     * @param content the email body content (plain text or HTML)
     * @param user the user sending or triggering the email (can be null for system emails)
     *
     * @return the Email entity that was created and saved to the database
     *
     * @throws IllegalArgumentException if from, to, subject, or content is null or blank
     * @throws RuntimeException if email sending or database save fails
     */
    Email sendSimpleEmail(String from, String to, String subject, String content, @Nullable User user);

    /**
     * Sends an email with an attachment (system email).
     *
     * <p>This method sends an email with a single file attachment. The email is
     * sent by the system (user_id will be null) and is automatically saved to
     * the database for audit purposes.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Sending PDF reports to users</li>
     *   <li>Delivering generated documents</li>
     *   <li>Sharing system-generated files</li>
     *   <li>Automated backup notifications with log files</li>
     * </ul>
     *
     * <p><strong>Attachment Handling:</strong></p>
     * <ul>
     *   <li>Supports any file type via InputStreamSource</li>
     *   <li>Attachment size limited by email server configuration</li>
     *   <li>Multiple calls needed for multiple attachments</li>
     *   <li>Attachment content type is automatically detected when possible</li>
     * </ul>
     *
     * @param from the sender's email address (e.g., "noreply@bunkermuseum.com")
     * @param to the recipient's email address
     * @param subject the email subject line
     * @param content the email body content (plain text or HTML)
     * @param attachmentName the filename of the attachment (e.g., "report.pdf")
     * @param attachment the attachment data as an InputStreamSource
     *
     * @return the Email entity that was created and saved to the database
     *
     * @throws IllegalArgumentException if any parameter is null or blank
     * @throws RuntimeException if email sending or database save fails
     *
     * @author Philipp Borkovic
     */
    Email sendEmailWithAttachment(
        String from,
        String to,
        String subject,
        String content,
        String attachmentName,
        InputStreamSource attachment
    );

    /**
     * Sends an email with an attachment on behalf of a user.
     *
     * <p>This method sends an email with a single file attachment. The email is
     * associated with the specified user (user_id will be set) and is automatically
     * saved to the database for audit purposes.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Users sharing documents with others</li>
     *   <li>Sending booking confirmations with PDF tickets</li>
     *   <li>Member file sharing</li>
     *   <li>Document distribution from user accounts</li>
     * </ul>
     *
     * <p><strong>Attachment Handling:</strong></p>
     * <ul>
     *   <li>Supports any file type via InputStreamSource</li>
     *   <li>Attachment size limited by email server configuration</li>
     *   <li>Multiple calls needed for multiple attachments</li>
     *   <li>Attachment content type is automatically detected when possible</li>
     * </ul>
     *
     * @param from the sender's email address
     * @param to the recipient's email address
     * @param subject the email subject line
     * @param content the email body content (plain text or HTML)
     * @param attachmentName the filename of the attachment (e.g., "document.pdf")
     * @param attachment the attachment data as an InputStreamSource
     * @param user the user sending or triggering the email (can be null for system emails)
     *
     * @return the Email entity that was created and saved to the database
     *
     * @throws IllegalArgumentException if from, to, subject, content, attachmentName, or attachment is null or blank
     * @throws RuntimeException if email sending or database save fails
     *
     * @author Philipp Borkovic
     */
    Email sendEmailWithAttachment(
        String from,
        String to,
        String subject,
        String content,
        String attachmentName,
        InputStreamSource attachment,
        @Nullable User user
    );
}
