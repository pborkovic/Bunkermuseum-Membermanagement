package com.bunkermuseum.membermanagement.service.contract;

import com.bunkermuseum.membermanagement.model.Booking;

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
     * <p>This method fetches all bookings from the database.
     * It's intended for administrative purposes and should be protected
     * by appropriate authorization checks.</p>
     *
     * @return List of all bookings in the system
     *
     * @throws RuntimeException if retrieval fails
     *
     * @author Philipp Borkovic
     */
    java.util.List<Booking> getAllBookings();
}
