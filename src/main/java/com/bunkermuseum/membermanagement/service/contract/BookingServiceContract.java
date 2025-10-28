package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.dto.AssignBookingRequest;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import java.util.List;

/**
 * Service contract interface for Booking entity business operations.
 *
 * <p>This interface defines the contract for Booking-specific business logic operations.
 * It serves as a clean contract without extending any base interfaces, allowing
 * for maximum flexibility in implementation while maintaining clear separation
 * of concerns.</p>
 *
 * @author Philipp Borkovic
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
     *
     * @author Philipp Borkovic
     */
    List<BookingDTO> getAllBookings();

    /**
     * Assigns/creates a booking for a target set of users based on the provided request.
     *
     * <p>When {@code allUsersAssigned} is true, the booking will be created for all users.
     * Otherwise the {@code userIds} and/or {@code roleNames} are used to filter targets.</p>
     *
     * @param request The request containing targeting information and booking details
     * @return The number of bookings created/assigned
     */
    int assignBookingToUsers(AssignBookingRequest request);
}
