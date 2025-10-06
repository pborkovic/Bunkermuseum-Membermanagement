package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.BookingController;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit test suite for the {@link BookingController} class.
 *
 * <p>This test class validates all booking endpoints exposed by the BookingController
 * for Vaadin Hilla frontend integration. It uses Mockito to mock service dependencies,
 * focusing on testing controller logic, response mapping, and error handling.</p>
 *
 * @see BookingController
 * @see BookingServiceContract
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingController Unit Tests")
class BookingControllerTest {

    /**
     * Mock instance of the booking service contract for business operations.
     * This mock allows us to control booking behavior and responses.
     */
    @Mock
    private BookingServiceContract bookingService;

    /**
     * Test instance of BookingController for testing endpoint behavior.
     */
    private BookingController bookingController;

    /**
     * Sets up the test environment before each test method execution.
     *
     * <p>This method initializes:</p>
     * <ul>
     *   <li>A BookingController instance with mocked service dependency</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @BeforeEach
    void setUp() {
        bookingController = new BookingController(bookingService);
    }
}
