package com.bunkermuseum.membermanagement.model;

import com.bunkermuseum.membermanagement.model.base.Model;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Booking entity representing financial bookings and transactions.
 *
 * <p>This entity extends the base {@link Model} class to inherit UUID primary keys,
 * automatic timestamps, and soft delete functionality. It tracks both expected
 * and actual financial transactions with comprehensive metadata.</p>
 *
 * <h3>Database Schema:</h3>
 * <p>The bookings table includes the following fields beyond the inherited base fields:</p>
 * <ul>
 *   <li><code>expected_purpose</code> (VARCHAR(255), NULLABLE) - The intended purpose of the transaction</li>
 *   <li><code>expected_amount</code> (DECIMAL(10,2), NULLABLE) - The expected transaction amount</li>
 *   <li><code>received_at</code> (TIMESTAMP, NULLABLE) - When the payment was received</li>
 *   <li><code>actual_purpose</code> (VARCHAR(255), NULLABLE) - The actual purpose as received</li>
 *   <li><code>actual_amount</code> (DECIMAL(10,2), NULLABLE) - The actual amount received</li>
 *   <li><code>ofMG</code> (VARCHAR(255), NULLABLE) - Member identifier</li>
 *   <li><code>note</code> (VARCHAR(255), NULLABLE) - Additional notes or remarks</li>
 *   <li><code>account_statement_page</code> (VARCHAR(255), NULLABLE) - Reference to account statement</li>
 *   <li><code>code</code> (VARCHAR(255), NULLABLE) - Transaction code or reference</li>
 *   <li><code>user_id</code> (UUID, NULLABLE, FK) - Foreign key to users table</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see Model
 * @see User
 * @see jakarta.persistence.Entity
 */
@Entity
@Table(name = "bookings",
    indexes = {
        @Index(name = "idx_bookings_received_at", columnList = "received_at"),
        @Index(name = "idx_bookings_user_id", columnList = "user_id"),
    }
)
public class Booking extends Model {

    /**
     * Protected default constructor for JPA.
     *
     * <p>This constructor is required by JPA specification for entity instantiation
     * during database operations. It should not be called directly by application
     * code. Use the public constructors for creating new booking instances.</p>
     *
     * @author Philipp Borkovic
     */
    protected Booking() {
        // Default constructor for JPA
    }

    /**
     * The expected purpose of the transaction.
     *
     * <p>This field stores what the transaction was intended for.
     * It can be used to track the planned or budgeted purpose.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'expected_purpose' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "expected_purpose", length = 255)
    private @Nullable String expectedPurpose;

    /**
     * The expected amount of the transaction.
     *
     * <p>This field stores the anticipated or planned amount for the transaction.
     * Stored with precision of 10 digits and 2 decimal places.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'expected_amount' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, DECIMAL(10,2)</p>
     */
    @Column(name = "expected_amount", precision = 10, scale = 2)
    private @Nullable BigDecimal expectedAmount;

    /**
     * The timestamp when the payment was received.
     *
     * <p>This field tracks when the actual payment or transaction was received,
     * which may differ from the expected or planned date.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'received_at' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, TIMESTAMP</p>
     */
    @Column(name = "received_at")
    private @Nullable Instant receivedAt;

    /**
     * The actual purpose of the transaction as received.
     *
     * <p>This field stores the actual purpose stated in the received transaction,
     * which may differ from the expected purpose.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'actual_purpose' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "actual_purpose", length = 255)
    private @Nullable String actualPurpose;

    /**
     * The actual amount of the transaction received.
     *
     * <p>This field stores the actual amount received in the transaction,
     * which may differ from the expected amount.
     * Stored with precision of 10 digits and 2 decimal places.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'actual_amount' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, DECIMAL(10,2)</p>
     */
    @Column(name = "actual_amount", precision = 10, scale = 2)
    private @Nullable BigDecimal actualAmount;

    /**
     * Member identifier or reference.
     *
     * <p>This field stores the identifier of the member associated with this booking.
     * It can reference a member ID, name, or other identifying information.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'ofMG' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "ofMG", length = 255)
    private @Nullable String ofMG;

    /**
     * The user associated with this booking.
     *
     * <p>This field establishes a many-to-one relationship between bookings and users.
     * Each booking can be associated with one user, representing who created or
     * is responsible for the booking.</p>
     *
     * <p><strong>Relationship Details:</strong></p>
     * <ul>
     *   <li>Many bookings can belong to one user</li>
     *   <li>The relationship is optional (nullable)</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'user_id' foreign key column</p>
     * <p><strong>Constraints:</strong> NULLABLE, UUID, FOREIGN KEY to users(id)</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private @Nullable User user;

    /**
     * Additional notes or remarks about the booking.
     *
     * <p>This field stores any additional comments, observations, or
     * important information related to this booking.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'note' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "note", length = 255)
    private @Nullable String note;

    /**
     * Reference to the account statement page.
     *
     * <p>This field stores a reference to the specific page or section
     * of the account statement where this booking appears.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'account_statement_page' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "account_statement_page", length = 255)
    private @Nullable String accountStatementPage;

    /**
     * Transaction code or reference.
     *
     * <p>This field stores a unique code, reference number, or identifier
     * for this booking transaction.</p>
     *
     * <p><strong>Database Mapping:</strong> Maps to 'code' column</p>
     * <p><strong>Constraints:</strong> NULLABLE, VARCHAR(255)</p>
     */
    @Column(name = "code", length = 255)
    private @Nullable String code;


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
     * Sets the expected purpose of the transaction.
     *
     * @param expectedPurpose The expected purpose
     *
     * @author Philipp Borkovic
     */
    public void setExpectedPurpose(@Nullable String expectedPurpose) {
        this.expectedPurpose = expectedPurpose;
    }

    /**
     * Gets the expected amount.
     *
     * @return The expected amount, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    /**
     * Sets the expected amount.
     *
     * @param expectedAmount The expected amount
     *
     * @author Philipp Borkovic
     */
    public void setExpectedAmount(@Nullable BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    /**
     * Gets the timestamp when payment was received.
     *
     * @return The received timestamp, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant getReceivedAt() {
        return receivedAt;
    }

    /**
     * Sets the timestamp when payment was received.
     *
     * @param receivedAt The received timestamp
     *
     * @author Philipp Borkovic
     */
    public void setReceivedAt(@Nullable Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    /**
     * Gets the actual purpose of the transaction.
     *
     * @return The actual purpose, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getActualPurpose() {
        return actualPurpose;
    }

    /**
     * Sets the actual purpose of the transaction.
     *
     * @param actualPurpose The actual purpose
     *
     * @author Philipp Borkovic
     */
    public void setActualPurpose(@Nullable String actualPurpose) {
        this.actualPurpose = actualPurpose;
    }

    /**
     * Gets the actual amount received.
     *
     * @return The actual amount, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getActualAmount() {
        return actualAmount;
    }

    /**
     * Sets the actual amount received.
     *
     * @param actualAmount The actual amount
     *
     * @author Philipp Borkovic
     */
    public void setActualAmount(@Nullable BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    /**
     * Gets the member identifier.
     *
     * @return The member identifier, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getOfMG() {
        return ofMG;
    }

    /**
     * Sets the member identifier.
     *
     * @param ofMG The member identifier
     *
     * @author Philipp Borkovic
     */
    public void setOfMG(@Nullable String ofMG) {
        this.ofMG = ofMG;
    }

    /**
     * Gets the user associated with this booking.
     *
     * @return The user, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this booking.
     *
     * @param user The user
     *
     * @author Philipp Borkovic
     */
    public void setUser(@Nullable User user) {
        this.user = user;
    }

    /**
     * Gets the note or remark.
     *
     * @return The note, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getNote() {
        return note;
    }

    /**
     * Sets the note or remark.
     *
     * @param note The note
     *
     * @author Philipp Borkovic
     */
    public void setNote(@Nullable String note) {
        this.note = note;
    }

    /**
     * Gets the account statement page reference.
     *
     * @return The account statement page reference, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getAccountStatementPage() {
        return accountStatementPage;
    }

    /**
     * Sets the account statement page reference.
     *
     * @param accountStatementPage The account statement page reference
     *
     * @author Philipp Borkovic
     */
    public void setAccountStatementPage(@Nullable String accountStatementPage) {
        this.accountStatementPage = accountStatementPage;
    }

    /**
     * Gets the transaction code.
     *
     * @return The transaction code, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getCode() {
        return code;
    }

    /**
     * Sets the transaction code.
     *
     * @param code The transaction code
     *
     * @author Philipp Borkovic
     */
    public void setCode(@Nullable String code) {
        this.code = code;
    }

    /**
     * Returns a string representation of the Booking entity.
     *
     * <p>Provides a human-readable representation including the class name,
     * ID, actual amount, and received date for debugging and logging purposes.</p>
     *
     * @return A string representation of this booking
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("Booking{id=%s, actualAmount=%s, receivedAt=%s}",
            getId(), actualAmount, receivedAt);
    }
}
