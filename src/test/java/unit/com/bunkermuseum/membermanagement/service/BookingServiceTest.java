package unit.com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.contract.BookingRepositoryContract;
import com.bunkermuseum.membermanagement.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit test suite for the {@link BookingService} class.
 *
 * <p>This test class validates all booking business operations provided by the
 * BookingService implementation. It uses Mockito to mock repository dependencies,
 * focusing on testing business logic, validation, and error handling.</p>
 *
 * @see BookingService
 * @see BookingRepositoryContract
 * @see Booking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    /**
     * Mock instance of the booking repository contract used by BookingService.
     * This mock allows us to control and verify data layer interactions.
     */
    @Mock
    private BookingRepositoryContract bookingRepository;

    /**
     * Test instance of BookingService for testing business logic.
     */
    private BookingService bookingService;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A BookingService instance with mocked repository</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository);
    }
}
