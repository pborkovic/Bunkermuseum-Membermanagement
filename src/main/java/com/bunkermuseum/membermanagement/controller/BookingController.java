package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import com.vaadin.hilla.Endpoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for Booking management operations.
 *
 * <p>This controller provides RESTful endpoints for booking management following
 * REST conventions and providing proper HTTP status codes and error handling.</p>
 *
 * @see BookingServiceContract
 */
@Endpoint
public class BookingController {

    private final BookingServiceContract bookingService;

    /**
     * Constructs a new BookingController with the provided service.
     *
     * @param bookingService The booking service for business operations
     *
     * @author Philipp Borkovic
     */
    public BookingController(BookingServiceContract bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Retrieves all bookings from the system.
     *
     * <p>This method fetches all bookings. It's intended for administrative
     * purposes and should be protected by appropriate authorization checks.</p>
     *
     * @return List of all bookings in the system
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during retrieval.
     *
     * @author Philipp Borkovic
     */
    public java.util.List<Booking> getAllBookings() {
        try {
            return bookingService.getAllBookings();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve bookings", e);
        }
    }
}
