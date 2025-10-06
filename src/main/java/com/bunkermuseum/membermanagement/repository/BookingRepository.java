package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.BookingRepositoryContract;
import com.bunkermuseum.membermanagement.repository.jpa.BookingJpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository implementation for Booking entity operations.
 *
 * <p>This class provides the concrete implementation for Booking-specific data access operations.
 * It extends {@link BaseRepository} to inherit standard CRUD operations, validation workflows,
 * transaction management, and error handling while implementing {@link BookingRepositoryContract}
 * to provide the Booking-specific repository contract.</p>
 *
 * @see BaseRepository
 * @see BookingRepositoryContract
 * @see Booking
 */
@Repository
public class BookingRepository extends BaseRepository<Booking, BookingJpaRepository>
        implements BookingRepositoryContract {

    /**
     * Constructs a new BookingRepository with the provided JPA repository.
     *
     * <p>This constructor injects the Spring Data JpaRepository dependency
     * and passes it to the parent BaseRepository for standard CRUD operations.
     * The JpaRepository provides the actual database interaction capabilities
     * while BaseRepository adds logging, error handling, and additional utilities.</p>
     *
     * @param repository The Spring Data JpaRepository for Booking entities
     *
     * @author Philipp Borkovic
     */
    public BookingRepository(BookingJpaRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    protected String getEntityName() {
        return "Booking";
    }
}
