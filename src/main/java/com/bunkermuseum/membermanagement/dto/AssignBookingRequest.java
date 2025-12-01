package com.bunkermuseum.membermanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Request DTO for assigning bookings to users by member type.
 *
 * <p>This DTO provides a clean, validated request structure for creating bookings
 * for multiple users based on their member type. It uses Jakarta Bean Validation
 * to ensure data integrity and follows the immutable request pattern.</p>
 *
 * <h3>Design Principles:</h3>
 * <ul>
 *   <li>Type-safe member type filtering using {@link MemberType} enum</li>
 *   <li>Comprehensive Jakarta validation annotations</li>
 *   <li>Simple, focused API with only essential fields</li>
 *   <li>Clear validation error messages in German</li>
 * </ul>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><strong>memberType:</strong> Required, must be valid enum value</li>
 *   <li><strong>expectedAmount:</strong> Required, must be > 0</li>
 *   <li><strong>actualAmount:</strong> Required, must be > 0</li>
 *   <li><strong>actualPurpose:</strong> Required, non-blank, max 500 characters</li>
 * </ul>
 *
 * @see MemberType
 */
public class AssignBookingRequest {

    /**
     * The member type to target for booking assignment.
     * Determines which users receive the booking based on their role.
     */
    @NotNull(message = "Mitgliedstyp ist erforderlich")
    private MemberType memberType;

    /**
     * The expected amount of the transaction.
     * Must be a positive value greater than zero.
     */
    @NotNull(message = "Erwarteter Betrag ist erforderlich")
    @DecimalMin(value = "0.01", message = "Erwarteter Betrag muss größer als 0 sein")
    private BigDecimal expectedAmount;

    /**
     * The actual amount of the transaction received.
     * Must be a positive value greater than zero.
     */
    @NotNull(message = "Tatsächlicher Betrag ist erforderlich")
    @DecimalMin(value = "0.01", message = "Tatsächlicher Betrag muss größer als 0 sein")
    private BigDecimal actualAmount;

    /**
     * The actual purpose of the transaction as received.
     * Optional field with maximum length of 200 characters.
     * If not provided, defaults to "Mitgliedsbeitrag".
     * Note: The system will automatically append the year and member name to this purpose.
     */
    @Size(max = 200, message = "Verwendungszweck darf maximal 200 Zeichen lang sein")
    private String actualPurpose = "Mitgliedsbeitrag";

    /**
     * Default constructor for framework usage (Jackson, Spring, etc.).
     *
     * @author Philipp Borkovic
     */
    public AssignBookingRequest() {
    }

    /**
     * Constructs a new AssignBookingRequest with all required fields.
     *
     * @param memberType The member type to target
     * @param expectedAmount The expected transaction amount
     * @param actualAmount The actual transaction amount
     * @param actualPurpose The actual transaction purpose
     *
     * @author Philipp Borkovic
     */
    public AssignBookingRequest(
        MemberType memberType,
        BigDecimal expectedAmount,
        BigDecimal actualAmount,
        String actualPurpose
    ) {
        this.memberType = memberType;
        this.expectedAmount = expectedAmount;
        this.actualAmount = actualAmount;
        this.actualPurpose = actualPurpose;
    }

    /**
     * Gets the member type for targeting.
     *
     * @return The member type, never null after validation
     *
     * @author Philipp Borkovic
     */
    public @Nullable MemberType getMemberType() {
        return memberType;
    }

    /**
     * Sets the member type for targeting.
     *
     * @param memberType The member type to set
     *
     * @author Philipp Borkovic
     */
    public void setMemberType(@Nullable MemberType memberType) {
        this.memberType = memberType;
    }

    /**
     * Gets the expected transaction amount.
     *
     * @return The expected amount, never null after validation
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    /**
     * Sets the expected transaction amount.
     *
     * @param expectedAmount The expected amount to set
     *
     * @author Philipp Borkovic
     */
    public void setExpectedAmount(@Nullable BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    /**
     * Gets the actual transaction amount.
     *
     * @return The actual amount, never null after validation
     *
     * @author Philipp Borkovic
     */
    public @Nullable BigDecimal getActualAmount() {
        return actualAmount;
    }

    /**
     * Sets the actual transaction amount.
     *
     * @param actualAmount The actual amount to set
     *
     * @author Philipp Borkovic
     */
    public void setActualAmount(@Nullable BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    /**
     * Gets the actual transaction purpose.
     *
     * @return The actual purpose, never null or blank after validation
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getActualPurpose() {
        return actualPurpose;
    }

    /**
     * Sets the actual transaction purpose.
     *
     * @param actualPurpose The actual purpose to set
     *
     * @author Philipp Borkovic
     */
    public void setActualPurpose(@Nullable String actualPurpose) {
        this.actualPurpose = actualPurpose;
    }

    /**
     * Returns a string representation of this request.
     *
     * @return A string representation including key fields
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format(
            "AssignBookingRequest{memberType=%s, expectedAmount=%s, actualAmount=%s}",
                memberType, expectedAmount, actualAmount
        );
    }
}
