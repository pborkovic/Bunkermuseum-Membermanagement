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
     * <p>Retrieves the intended or planned purpose that was originally anticipated
     * for this booking transaction. This value can be compared with the actual
     * purpose to identify discrepancies in transaction intentions.</p>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Budget planning and tracking</li>
     *   <li>Variance analysis between expected and actual purposes</li>
     *   <li>Audit trail for transaction expectations</li>
     * </ul>
     *
     * @return The expected purpose string, or null if no expectation was set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getExpectedPurpose() {
        return expectedPurpose;
    }

    /**
     * Sets the expected purpose of the transaction.
     *
     * <p>Assigns the intended or planned purpose for this booking transaction.
     * This should typically be set during booking creation or when planning
     * expected transactions.</p>
     *
     * @param expectedPurpose The intended purpose description, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setExpectedPurpose(@Nullable String expectedPurpose) {
        this.expectedPurpose = expectedPurpose;
    }

    /**
     * Gets the expected amount for the transaction.
     *
     * <p>Retrieves the anticipated monetary value that was planned for this
     * booking. This amount uses {@link BigDecimal} for precise financial
     * calculations and is stored with 2 decimal places precision.</p>
     *
     * @return The expected amount as BigDecimal, or null if not planned
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    /**
     * Sets the expected amount for the transaction.
     *
     * <p>Assigns the anticipated monetary value for this booking transaction.
     * Use {@link BigDecimal} to ensure precise financial calculations without
     * floating-point rounding errors.</p>
     *
     * @param expectedAmount The planned amount as BigDecimal, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setExpectedAmount(@Nullable BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    /**
     * Gets the timestamp when the payment was received.
     *
     * <p>Retrieves the exact date and time when the actual payment or transaction
     * was received and processed. Uses {@link Instant} for timezone-independent
     * timestamp storage.</p>
     *
     * @return The received timestamp as Instant, or null if not yet received
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant getReceivedAt() {
        return receivedAt;
    }

    /**
     * Sets the timestamp when the payment was received.
     *
     * <p>Records the exact date and time when the actual payment or transaction
     * was received. Should be set when payment is confirmed and processed.</p>
     *
     * @param receivedAt The received timestamp as Instant, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setReceivedAt(@Nullable Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    /**
     * Gets the actual purpose of the transaction as received.
     *
     * <p>Retrieves the actual purpose or description that was stated in the
     * received transaction. This may differ from the expected purpose and is
     * useful for reconciliation and variance analysis.</p>
     *
     * @return The actual purpose as stated in the transaction, or null if not recorded
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getActualPurpose() {
        return actualPurpose;
    }

    /**
     * Sets the actual purpose of the transaction as received.
     *
     * <p>Records the actual purpose or payment reference that was stated in the
     * received transaction. This should reflect exactly what was provided by
     * the payer, even if it differs from expectations.</p>
     *
     * @param actualPurpose The actual purpose as received, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setActualPurpose(@Nullable String actualPurpose) {
        this.actualPurpose = actualPurpose;
    }

    /**
     * Gets the actual amount received in the transaction.
     *
     * <p>Retrieves the real monetary value that was actually received, which
     * may differ from the expected amount. Uses {@link BigDecimal} for precise
     * financial calculations.</p>
     *
     * @return The actual received amount as BigDecimal, or null if not received
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getActualAmount() {
        return actualAmount;
    }

    /**
     * Sets the actual amount received in the transaction.
     *
     * <p>Records the real monetary value that was received in this transaction.
     * This value should reflect the exact amount credited, including any
     * fees, adjustments, or currency conversions.</p>
     *
     * @param actualAmount The received amount as BigDecimal, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setActualAmount(@Nullable BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    /**
     * Gets the member identifier (ofMG).
     *
     * <p>Retrieves the member reference or identifier associated with this booking.
     * This field maintains the original German abbreviation "ofMG" (of Member/Mitglied)
     * for compatibility with legacy systems.</p>
     *
     * @return The member identifier string, or null if not associated
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getOfMG() {
        return ofMG;
    }

    /**
     * Sets the member identifier (ofMG).
     *
     * <p>Assigns a member reference or identifier to this booking. This field
     * maintains backward compatibility with legacy systems using the "ofMG"
     * (of Member/Mitglied) designation.</p>
     *
     * <p><strong>Relationship with User:</strong> This text field can coexist
     * with the {@link #user} foreign key relationship, allowing both legacy
     * references and modern user associations.</p>
     *
     * @param ofMG The member identifier string, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setOfMG(@Nullable String ofMG) {
        this.ofMG = ofMG;
    }

    /**
     * Gets the user associated with this booking.
     *
     * <p>Retrieves the {@link User} entity that owns or is responsible for this
     * booking. This establishes a many-to-one relationship where multiple bookings
     * can belong to a single user.</p>
     *
     * <p><strong>Lazy Loading:</strong> This relationship uses LAZY fetch strategy,
     * meaning the User entity is not loaded from the database until this method
     * is called. This optimizes performance when user details are not needed.</p>
     *
     * @return The associated User entity, or null if no user is linked
     *
     * @author Philipp Borkovic
     */
    public @Nullable User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this booking.
     *
     * <p>Assigns a {@link User} entity as the owner or responsible party for
     * this booking. This creates a many-to-one relationship linking the booking
     * to a specific user account.</p>
     *
     * <p><strong>Relationship Management:</strong></p>
     * <ul>
     *   <li>Establishes foreign key reference to users table</li>
     *   <li>User entity must exist before assignment</li>
     *   <li>Setting to null removes the association</li>
     *   <li>Does not cascade delete (booking persists if user deleted)</li>
     * </ul>
     *
     * @param user The User entity to associate, or null to remove association
     *
     * @author Philipp Borkovic
     */
    public void setUser(@Nullable User user) {
        this.user = user;
    }

    /**
     * Gets the note or remark for this booking.
     *
     * <p>Retrieves additional comments, observations, or important information
     * related to this booking transaction. This field is useful for recording
     * contextual details not captured in other structured fields.</p>
     *
     * @return The note text, or null if no notes recorded
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getNote() {
        return note;
    }

    /**
     * Sets the note or remark for this booking.
     *
     * <p>Records additional comments, observations, or important information
     * about this booking. Use this field to document context, issues, or
     * special circumstances not captured elsewhere.</p>
     *
     * @param note The note text, or null to clear existing notes
     *
     * @author Philipp Borkovic
     */
    public void setNote(@Nullable String note) {
        this.note = note;
    }

    /**
     * Gets the account statement page reference.
     *
     * <p>Retrieves the reference to the specific page, sheet, or section of the
     * account statement (Kontoauszug) where this booking appears. This aids in
     * cross-referencing bookings with physical or digital statements.</p>
     *
     * @return The statement page reference, or null if not documented
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getAccountStatementPage() {
        return accountStatementPage;
    }

    /**
     * Sets the account statement page reference.
     *
     * <p>Records a reference to the specific location in the account statement
     * where this booking appears. This facilitates cross-referencing and
     * verification against official bank documents.</p>
     *
     * @param accountStatementPage The statement reference, or null to clear
     *
     * @author Philipp Borkovic
     */
    public void setAccountStatementPage(@Nullable String accountStatementPage) {
        this.accountStatementPage = accountStatementPage;
    }

    /**
     * Gets the transaction code or reference.
     *
     * <p>Retrieves a unique code, reference number, or identifier for this
     * booking transaction. This field can store various types of transaction
     * identifiers used for tracking and reconciliation.</p>
     *
     * @return The transaction code, or null if no code assigned
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getCode() {
        return code;
    }

    /**
     * Sets the transaction code or reference.
     *
     * <p>Assigns a unique code, reference number, or identifier to this booking.
     * This code should uniquely identify the transaction and facilitate tracking
     * across systems.</p>
     *
     * @param code The transaction code, or null to clear
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
