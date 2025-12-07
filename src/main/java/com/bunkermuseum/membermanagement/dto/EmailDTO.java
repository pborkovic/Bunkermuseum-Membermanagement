package com.bunkermuseum.membermanagement.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for Email entity.
 *
 * <p>This DTO is used to transfer email data between the backend and frontend,
 * avoiding serialization issues with Hibernate lazy-loaded relationships and
 * preventing circular references.</p>
 *
 * @author Philipp Borkovic
 */
public class EmailDTO {

    private UUID id;
    private String fromAddress;
    private String toAddress;
    private String subject;
    private String content;
    private UserDTO user;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    /**
     * Default constructor for Jackson deserialization.
     */
    public EmailDTO() {
    }

    /**
     * Constructor with all fields.
     *
     * @param id the email ID
     * @param fromAddress the sender's email address
     * @param toAddress the recipient's email address
     * @param subject the email subject
     * @param content the email content
     * @param user the user who sent the email (null for system emails)
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     * @param deletedAt the deletion timestamp (null if not deleted)
     */
    public EmailDTO(UUID id, String fromAddress, String toAddress, String subject,
                    String content, UserDTO user, Instant createdAt, Instant updatedAt,
                    Instant deletedAt) {
        this.id = id;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.subject = subject;
        this.content = content;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
