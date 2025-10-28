package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.dto.AssignBookingRequest;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.model.Booking;

import java.util.List;

/**
 * Service contract interface for Booking entity business operations.
 *
 * <p>This interface defines the contract for Booking-specific business logic operations.
 * It serves as a clean contract without extending any base interfaces, allowing
 * for maximum flexibility in implementation while maintaining clear separation
 * of concerns.</p>
 *
 * @see Booking
 * @see com.bunkermuseum.membermanagement.service.base.BaseService
 */
public interface BookingServiceContract {

    /**
     * Retrieves all bookings from the system.
     *
     * <p>This method fetches all bookings from the database and returns them
     * as DTOs to avoid serialization issues with entity relationships.
     * It's intended for administrative purposes and should be protected
     * by appropriate authorization checks.</p>
     *
     * @return List of all booking DTOs in the system
     *
     * @throws RuntimeException if retrieval fails
     */
    List<BookingDTO> getAllBookings();

    /**
     * Assigns and creates bookings for a target set of users based on member type filtering.
     *
     * <p>This method provides bulk booking creation capabilities for administrative users,
     * allowing bookings to be assigned to all users matching a specific member type (role).
     * It creates individual booking records for each targeted user with the specified
     * payment details and purpose.</p>
     *
     * <p><strong>Targeting Strategy:</strong></p>
     * <ul>
     *   <li>Uses the {@code memberType} field from the request to determine target users</li>
     *   <li>Resolves users by matching their assigned roles against the member type's role name</li>
     *   <li>Only creates bookings for active users with matching roles</li>
     *   <li>Skips users without the specified role or inactive accounts</li>
     * </ul>
     *
     * <p><strong>Validation Requirements:</strong></p>
     * <p>The implementation must validate the following before processing:</p>
     * <ul>
     *   <li>Member type is not null and is a valid {@code MemberType} enum value</li>
     *   <li>Expected amount and actual amount meet business rules (if applicable)</li>
     *   <li>Purpose text meets length and content requirements (if applicable)</li>
     * </ul>
     *
     * <p><strong>Transaction Behavior:</strong></p>
     * <p>This operation should be executed within a database transaction to ensure:</p>
     * <ul>
     *   <li>All bookings are created atomically (all or nothing)</li>
     *   <li>Rollback occurs if any single booking creation fails</li>
     *   <li>Data consistency is maintained across all created records</li>
     * </ul>
     *
     * @param request The validated booking assignment request containing:
     *                <ul>
     *                  <li>{@code memberType} - The target member category (required)</li>
     *                  <li>{@code expectedAmount} - The anticipated payment amount</li>
     *                  <li>{@code actualAmount} - The actual payment amount (may be null for pending)</li>
     *                  <li>{@code actualPurpose} - Description of the booking purpose</li>
     *                </ul>
     *                Must not be {@code null} and should be validated using Jakarta Bean Validation.
     *
     * @return The number of bookings successfully created. Returns {@code 0} if:
     *         <ul>
     *           <li>No users match the specified member type</li>
     *           <li>All matching users are inactive or invalid</li>
     *           <li>The member type resolves to an empty user set</li>
     *         </ul>
     *
     * @see AssignBookingRequest
     * @see com.bunkermuseum.membermanagement.model.Booking
     */
    int assignBookingToUsers(AssignBookingRequest request);

    /**
     * Retrieves all bookings associated with the currently authenticated user.
     *
     * <p>This method provides secure access to a user's personal booking history by:</p>
     * <ul>
     *   <li>Extracting the authenticated user from the Spring Security context</li>
     *   <li>Filtering all bookings to only include those belonging to the current user</li>
     *   <li>Converting entities to DTOs to prevent circular references and ensure safe serialization</li>
     *   <li>Returning an empty list if the user has no bookings or is not authenticated</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Member dashboard to display personal booking history</li>
     *   <li>Payment tracking and verification</li>
     *   <li>Transaction history for accounting purposes</li>
     *   <li>Self-service portals for members to view their payments</li>
     * </ul>
     *
     * @return Immutable list of {@link BookingDTO} objects representing the current user's bookings,
     *         ordered by database default (typically creation date). Returns an empty list if:
     *         <ul>
     *           <li>The user is not authenticated</li>
     *           <li>The user has no associated bookings</li>
     *           <li>The authenticated principal is not a valid User entity</li>
     *         </ul>
     *         Never returns {@code null}.
     *
     * @throws RuntimeException if an unexpected error occurs during retrieval, such as:
     *         <ul>
     *           <li>Database connection failure</li>
     *           <li>Data access errors</li>
     *           <li>DTO conversion failures</li>
     *         </ul>
     *
     * @see BookingDTO
     * @see com.bunkermuseum.membermanagement.model.User
     * @see org.springframework.security.core.context.SecurityContextHolder
     * @see org.springframework.security.core.Authentication
     */
    List<BookingDTO> getCurrentUserBookings();
}
