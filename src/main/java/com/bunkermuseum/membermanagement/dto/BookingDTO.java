package com.bunkermuseum.membermanagement.dto;

import org.jspecify.annotations.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for Booking entity.
 *
 * <p>This DTO provides a secure, lightweight representation of bookings for API responses.
 * It excludes the User entity relationship to avoid circular references and serialization
 * issues. The DTO is immutable to ensure thread safety and prevent accidental modifications.</p>
 *
 * @see com.bunkermuseum.membermanagement.model.Booking
 */
public class BookingDTO {

    /**
     * The unique identifier for this booking.
     */
    private final UUID id;

    /**
     * The expected purpose of the transaction.
     */
    private final @Nullable String expectedPurpose;

    /**
     * The expected amount of the transaction.
     */
    private final @Nullable BigDecimal expectedAmount;

    /**
     * The timestamp when the payment was received.
     */
    private final @Nullable Instant receivedAt;

    /**
     * The actual purpose of the transaction as received.
     */
    private final @Nullable String actualPurpose;

    /**
     * The actual amount of the transaction received.
     */
    private final @Nullable BigDecimal actualAmount;

    /**
     * Member identifier or reference (legacy field).
     */
    private final @Nullable String ofMG;

    /**
     * The ID of the user associated with this booking.
     */
    private final @Nullable UUID userId;

    /**
     * Additional notes or remarks about the booking.
     */
    private final @Nullable String note;

    /**
     * Reference to the account statement page.
     */
    private final @Nullable String accountStatementPage;

    /**
     * Transaction code or reference.
     */
    private final @Nullable String code;

    /**
     * Constructs a new BookingDTO with all fields.
     *
     * @param id The unique identifier for this booking
     * @param expectedPurpose The expected purpose of the transaction
     * @param expectedAmount The expected amount of the transaction
     * @param receivedAt The timestamp when the payment was received
     * @param actualPurpose The actual purpose of the transaction as received
     * @param actualAmount The actual amount of the transaction received
     * @param ofMG Member identifier or reference
     * @param userId The ID of the user associated with this booking
     * @param note Additional notes or remarks about the booking
     * @param accountStatementPage Reference to the account statement page
     * @param code Transaction code or reference
     *
     * @author Philipp Borkovic
     */
    public BookingDTO(
        UUID id,
        @Nullable String expectedPurpose,
        @Nullable BigDecimal expectedAmount,
        @Nullable Instant receivedAt,
        @Nullable String actualPurpose,
        @Nullable BigDecimal actualAmount,
        @Nullable String ofMG,
        @Nullable UUID userId,
        @Nullable String note,
        @Nullable String accountStatementPage,
        @Nullable String code
    ) {
        this.id = id;
        this.expectedPurpose = expectedPurpose;
        this.expectedAmount = expectedAmount;
        this.receivedAt = receivedAt;
        this.actualPurpose = actualPurpose;
        this.actualAmount = actualAmount;
        this.ofMG = ofMG;
        this.userId = userId;
        this.note = note;
        this.accountStatementPage = accountStatementPage;
        this.code = code;
    }

    /**
     * Gets the unique identifier for this booking.
     *
     * @return The booking's unique identifier
     *
     * @author Philipp Borkovic
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the expected purpose of the transaction.
     *
     * @return The expected purpose, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getExpectedPurpose() {
        return expectedPurpose;
    }

    /**
     * Gets the expected amount of the transaction.
     *
     * @return The expected amount, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    /**
     * Gets the timestamp when the payment was received.
     *
     * @return The received timestamp, or null if not received
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant getReceivedAt() {
        return receivedAt;
    }

    /**
     * Gets the actual purpose of the transaction as received.
     *
     * @return The actual purpose, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getActualPurpose() {
        return actualPurpose;
    }

    /**
     * Gets the actual amount of the transaction received.
     *
     * @return The actual amount, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getActualAmount() {
        return actualAmount;
    }

    /**
     * Gets the member identifier (ofMG).
     *
     * @return The member identifier, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getOfMG() {
        return ofMG;
    }

    /**
     * Gets the ID of the user associated with this booking.
     *
     * @return The user ID, or null if not associated with a user
     *
     * @author Philipp Borkovic
     */
    public @Nullable UUID getUserId() {
        return userId;
    }

    /**
     * Gets the note or remark for this booking.
     *
     * @return The note, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getNote() {
        return note;
    }

    /**
     * Gets the account statement page reference.
     *
     * @return The statement page reference, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getAccountStatementPage() {
        return accountStatementPage;
    }

    /**
     * Gets the transaction code or reference.
     *
     * @return The transaction code, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getCode() {
        return code;
    }

    /**
     * Returns a string representation of the BookingDTO.
     *
     * @return A string representation of this booking DTO
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("BookingDTO{id=%s, actualAmount=%s, receivedAt=%s}",
            id, actualAmount, receivedAt);
    }
}