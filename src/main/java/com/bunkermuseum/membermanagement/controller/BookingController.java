package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.dto.AssignBookingRequest;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.PermitAll;
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
@PermitAll
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
     * Retrieves all bookings from the system as DTOs.
     *
     * <p>This method fetches all bookings and returns them as DTOs to avoid
     * serialization issues with entity relationships. It's intended for administrative
     * purposes and should be protected by appropriate authorization checks.</p>
     *
     * @return List of all booking DTOs in the system
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         any unexpected error occurs during retrieval.
     *
     * @author Philipp Borkovic
     */
    public java.util.List<BookingDTO> getAllBookings() {
        try {
            return bookingService.getAllBookings();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve bookings", e);
        }
    }

    /**
     * Assigns/creates a booking for selected users and/or roles, or all users by default.
     *
     * @param request Targeting information and booking details
     * @return Number of bookings created
     */
    public int assignBookingToUsers(AssignBookingRequest request) {
        try {
            return bookingService.assignBookingToUsers(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to assign booking to users", e);
        }
    }
}
