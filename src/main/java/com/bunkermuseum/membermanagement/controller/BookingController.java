package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import com.vaadin.hilla.Endpoint;

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
}
