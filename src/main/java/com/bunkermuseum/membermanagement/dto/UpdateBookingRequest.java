package com.bunkermuseum.membermanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for editing the entries of a single existing booking.
 *
 * <p>The request carries the complete set of editable booking fields. Every field
 * replaces the stored value, so a {@code null} or blank value clears that entry.
 * The member the booking belongs to is not editable through this request.</p>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><strong>Amounts:</strong> Optional, must not be negative</li>
 *   <li><strong>Text fields:</strong> Optional, max 255 characters (database column length)</li>
 *   <li><strong>receivedAt:</strong> Optional calendar date the payment was received</li>
 * </ul>
 *
 * @see BookingDTO
 */
public class UpdateBookingRequest {

    /**
     * The expected purpose of the transaction ("Soll" Verwendungszweck).
     */
    @Size(max = 255, message = "Erwarteter Verwendungszweck darf maximal 255 Zeichen lang sein")
    private @Nullable String expectedPurpose;

    /**
     * The expected amount of the transaction ("Soll" Betrag).
     */
    @DecimalMin(value = "0.00", message = "Erwarteter Betrag darf nicht negativ sein")
    private @Nullable BigDecimal expectedAmount;

    /**
     * The date the payment was received.
     */
    private @Nullable LocalDate receivedAt;

    /**
     * The actual purpose of the transaction as received ("Ist" Verwendungszweck).
     */
    @Size(max = 255, message = "Tatsächlicher Verwendungszweck darf maximal 255 Zeichen lang sein")
    private @Nullable String actualPurpose;

    /**
     * The actual amount received ("Ist" Betrag).
     */
    @DecimalMin(value = "0.00", message = "Tatsächlicher Betrag darf nicht negativ sein")
    private @Nullable BigDecimal actualAmount;

    /**
     * The member name as recorded on the booking.
     */
    @Size(max = 255, message = "Mitglied darf maximal 255 Zeichen lang sein")
    private @Nullable String ofMG;

    /**
     * Free-text note.
     */
    @Size(max = 255, message = "Notiz darf maximal 255 Zeichen lang sein")
    private @Nullable String note;

    /**
     * Page of the account statement the payment appears on.
     */
    @Size(max = 255, message = "Kontoauszug Seite darf maximal 255 Zeichen lang sein")
    private @Nullable String accountStatementPage;

    /**
     * Booking code.
     */
    @Size(max = 255, message = "Code darf maximal 255 Zeichen lang sein")
    private @Nullable String code;

    /**
     * Default constructor for framework usage (Jackson, Spring, etc.).
     *
     * @author Philipp Borkovic
     */
    public UpdateBookingRequest() {
    }

    public @Nullable String getExpectedPurpose() {
        return expectedPurpose;
    }

    public void setExpectedPurpose(@Nullable String expectedPurpose) {
        this.expectedPurpose = expectedPurpose;
    }

    public @Nullable BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(@Nullable BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public @Nullable LocalDate getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(@Nullable LocalDate receivedAt) {
        this.receivedAt = receivedAt;
    }

    public @Nullable String getActualPurpose() {
        return actualPurpose;
    }

    public void setActualPurpose(@Nullable String actualPurpose) {
        this.actualPurpose = actualPurpose;
    }

    public @Nullable BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(@Nullable BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public @Nullable String getOfMG() {
        return ofMG;
    }

    public void setOfMG(@Nullable String ofMG) {
        this.ofMG = ofMG;
    }

    public @Nullable String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public @Nullable String getAccountStatementPage() {
        return accountStatementPage;
    }

    public void setAccountStatementPage(@Nullable String accountStatementPage) {
        this.accountStatementPage = accountStatementPage;
    }

    public @Nullable String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return String.format(
            "UpdateBookingRequest{expectedAmount=%s, actualAmount=%s, receivedAt=%s}",
                expectedAmount, actualAmount, receivedAt
        );
    }
}
