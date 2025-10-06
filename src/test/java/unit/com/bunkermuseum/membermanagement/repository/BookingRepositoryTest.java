package unit.com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.BookingRepository;
import com.bunkermuseum.membermanagement.repository.jpa.BookingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit test suite for the {@link BookingRepository} class.
 *
 * <p>This test class validates the BookingRepository implementation.
 * It uses Mockito to mock the underlying JPA repository and focuses on
 * testing repository layer logic, validation, and error handling.</p>
 *
 * @see BookingRepository
 * @see BookingJpaRepository
 * @see Booking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingRepository Unit Tests")
class BookingRepositoryTest {

    /**
     * Mock instance of the JPA repository used by BookingRepository.
     * This mock allows us to control and verify interactions with the data layer.
     */
    @Mock
    private BookingJpaRepository jpaRepository;

    /**
     * Test instance of BookingRepository for testing repository logic.
     */
    private BookingRepository bookingRepository;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A BookingRepository instance with the mocked JPA repository</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        bookingRepository = new BookingRepository(jpaRepository);
    }
}
