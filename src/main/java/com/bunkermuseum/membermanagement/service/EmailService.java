package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.EmailRepositoryContract;
import com.bunkermuseum.membermanagement.service.contract.EmailServiceContract;
import jakarta.mail.internet.MimeMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for email sending operations.
 *
 * <p>This service provides business logic for sending emails and automatically
 * logging them to the database for audit and tracking purposes. It integrates
 * with Spring's JavaMailSender for actual email delivery and uses the
 * EmailRepository for database persistence.</p>
 *
 * <h3>Architecture Integration:</h3>
 * <ul>
 *   <li><strong>Email Sending:</strong> Uses JavaMailSender for SMTP communication</li>
 *   <li><strong>Database Logging:</strong> Uses EmailRepositoryContract for persistence</li>
 *   <li><strong>Transaction Management:</strong> All operations are transactional</li>
 *   <li><strong>Error Handling:</strong> Comprehensive logging and exception handling</li>
 * </ul>
 *
 * <h3>Business Logic:</h3>
 * <ul>
 *   <li>Validates all input parameters before sending</li>
 *   <li>Sends emails via configured SMTP server (Mailpit in dev, real SMTP in prod)</li>
 *   <li>Creates Email entity and saves to database after successful sending</li>
 *   <li>Associates emails with users when applicable (user_id field)</li>
 *   <li>Handles both simple text emails and emails with attachments</li>
 * </ul>
 *
 * @see EmailServiceContract
 * @see Email
 * @see EmailRepositoryContract
 * @see JavaMailSender
 */
@Service
@Transactional
public class EmailService implements EmailServiceContract {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailRepositoryContract emailRepository;

    /**
     * Constructs a new EmailService with required dependencies.
     *
     * @param mailSender the JavaMailSender for sending emails
     * @param emailRepository the repository for persisting email records
     *
     * @author Philipp Borkovic
     */
    public EmailService(JavaMailSender mailSender, EmailRepositoryContract emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Email sendSimpleEmail(String from, String to, String subject, String content) {
        return sendSimpleEmail(from, to, subject, content, null);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Email sendSimpleEmail(String from, String to, String subject, String content, @Nullable User user) {
        // Validate input parameters
        validateEmailParameters(from, to, subject, content);

        try {
            logger.info("Sending simple email from {} to {} with subject: {}", from, to, subject);

            // Send the email using JavaMailSender
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            logger.info("Email sent successfully from {} to {}", from, to);

            // Create and save email entity to database
            Email email;
            if (user != null) {
                email = new Email(from, to, subject, content, user);
                logger.debug("Email associated with user: {}", user.getId());
            } else {
                email = new Email(from, to, subject, content);
                logger.debug("Email sent as system email (no user association)");
            }

            Email savedEmail = emailRepository.create(email);
            logger.info("Email record saved to database with ID: {}", savedEmail.getId());

            return savedEmail;

        } catch (Exception e) {
            logger.error("Failed to send email from {} to {}: {}", from, to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Email sendEmailWithAttachment(
        String from,
        String to,
        String subject,
        String content,
        String attachmentName,
        InputStreamSource attachment
    ) {
        return sendEmailWithAttachment(from, to, subject, content, attachmentName, attachment, null);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Email sendEmailWithAttachment(
        String from,
        String to,
        String subject,
        String content,
        String attachmentName,
        InputStreamSource attachment,
        @Nullable User user
    ) {
        // Validate input parameters
        validateEmailParameters(from, to, subject, content);
        validateAttachmentParameters(attachmentName, attachment);

        try {
            logger.info("Sending email with attachment from {} to {} with subject: {}", from, to, subject);
            logger.debug("Attachment name: {}", attachmentName);

            // Create MIME message for attachment support
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);

            // Add the attachment
            helper.addAttachment(attachmentName, attachment);

            mailSender.send(mimeMessage);

            logger.info("Email with attachment sent successfully from {} to {}", from, to);

            // Create and save email entity to database
            // Note: We store the original content, not the attachment data
            Email email;
            if (user != null) {
                email = new Email(from, to, subject, content + "\n[Attachment: " + attachmentName + "]", user);
                logger.debug("Email associated with user: {}", user.getId());
            } else {
                email = new Email(from, to, subject, content + "\n[Attachment: " + attachmentName + "]");
                logger.debug("Email sent as system email (no user association)");
            }

            Email savedEmail = emailRepository.create(email);
            logger.info("Email record with attachment saved to database with ID: {}", savedEmail.getId());

            return savedEmail;

        } catch (Exception e) {
            logger.error("Failed to send email with attachment from {} to {}: {}", from, to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }

    /**
     * Validates common email parameters.
     *
     * @param from the sender's email address
     * @param to the recipient's email address
     * @param subject the email subject
     * @param content the email content
     * @throws IllegalArgumentException if any parameter is null or blank
     *
     * @author Philipp Borkovic
     */
    private void validateEmailParameters(String from, String to, String subject, String content) {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("From address must not be null or blank");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("To address must not be null or blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject must not be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be null or blank");
        }
    }

    /**
     * Validates attachment parameters.
     *
     * @param attachmentName the attachment filename
     * @param attachment the attachment data
     * @throws IllegalArgumentException if any parameter is null or blank
     *
     * @author Philipp Borkovic
     */
    private void validateAttachmentParameters(String attachmentName, InputStreamSource attachment) {
        if (attachmentName == null || attachmentName.isBlank()) {
            throw new IllegalArgumentException("Attachment name must not be null or blank");
        }
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment must not be null");
        }
    }
}
