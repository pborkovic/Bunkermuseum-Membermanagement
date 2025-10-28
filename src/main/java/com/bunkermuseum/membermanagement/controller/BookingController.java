package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.dto.AssignBookingRequest;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
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
     * Assigns a booking to all users of a specified member type.
     *
     * <p>This endpoint validates the request using Jakarta Bean Validation and creates
     * bookings for all users matching the specified member type. The validation ensures:</p>
     * <ul>
     *   <li>Member type is specified and valid</li>
     *   <li>Amounts are positive numbers greater than zero</li>
     *   <li>Purpose text is not blank and within size limits</li>
     * </ul>
     *
     * @param request The validated booking assignment request with member type and booking details
     * @return The number of bookings created (equals the number of targeted users)
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if validation fails
     *         or member type is invalid
     * @throws ResponseStatusException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if
     *         database operation fails
     *
     * @author Philipp Borkovic
     */
    public int assignBookingToUsers(@Valid AssignBookingRequest request) {
        try {
            return bookingService.assignBookingToUsers(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Fehler beim Zuweisen der Buchung", e);
        }
    }
}
