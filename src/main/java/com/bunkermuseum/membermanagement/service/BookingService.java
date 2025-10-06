package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.contract.BookingRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Booking entity business operations.
 *
 * <p>This service extends {@link BaseService} to inherit standard CRUD operations,
 * validation workflows, transaction management, and error handling while implementing
 * {@link BookingServiceContract} to provide the Booking-specific business logic contract.
 * It follows the established service architecture patterns and provides comprehensive
 * business rule enforcement for Booking entities.</p>
 *
 * @see BaseService
 * @see BookingServiceContract
 * @see Booking
 * @see BookingRepositoryContract
 */
@Service
@Transactional(readOnly = true)
public class BookingService extends BaseService<Booking, BookingRepositoryContract>
        implements BookingServiceContract {

    /**
     * Constructs a new BookingService with the provided repository.
     *
     * <p>This constructor injects the BookingRepositoryContract dependency
     * and passes it to the parent BaseService for standard operations.
     * The repository provides the actual database interaction capabilities
     * while BaseService adds logging, error handling, and additional utilities.</p>
     *
     * @param repository The repository contract for Booking entities
     *
     * @author Philipp Borkovic
     */
    public BookingService(BookingRepositoryContract repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public java.util.List<Booking> getAllBookings() {
        try {
            return repository.findAll();
        } catch (Exception exception) {
            logger.error("Error retrieving all bookings", exception);

            throw new RuntimeException("Failed to retrieve bookings", exception);
        }
    }
}
