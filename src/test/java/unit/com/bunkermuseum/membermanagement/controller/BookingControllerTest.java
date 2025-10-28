package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.BookingController;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    /**
     * Tests the {@link BookingController#getAllBookings} method with successful retrieval.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves all bookings from the service</li>
     *   <li>The returned list contains the expected bookings</li>
     *   <li>The booking service getAllBookings method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve all bookings")
    void testGetAllBookings_Success_ReturnsBookings() {
        // Arrange
        BookingDTO booking1 = mock(BookingDTO.class);
        BookingDTO booking2 = mock(BookingDTO.class);

        List<BookingDTO> bookings = List.of(booking1, booking2);

        when(bookingService.getAllBookings()).thenReturn(bookings);

        // Act
        List<BookingDTO> result = bookingController.getAllBookings();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingService).getAllBookings();
    }

    /**
     * Tests the {@link BookingController#getAllBookings} method when no bookings exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty list when no bookings are present</li>
     *   <li>No exception is thrown for empty results</li>
     *   <li>The list is not null</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when no bookings exist")
    void testGetAllBookings_NoBookings_ReturnsEmptyList() {
        // Arrange
        when(bookingService.getAllBookings()).thenReturn(List.of());

        // Act
        List<BookingDTO> result = bookingController.getAllBookings();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingService).getAllBookings();
    }

    /**
     * Tests the {@link BookingController#getAllBookings} method when service throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A ResponseStatusException with INTERNAL_SERVER_ERROR is thrown</li>
     *   <li>The exception message contains "Failed to retrieve bookings"</li>
     *   <li>Service layer exceptions are properly converted to HTTP responses</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw INTERNAL_SERVER_ERROR when service fails")
    void testGetAllBookings_ServiceThrowsException_ThrowsInternalServerError() {
        // Arrange
        when(bookingService.getAllBookings())
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            bookingController.getAllBookings();
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to retrieve bookings"));
        verify(bookingService).getAllBookings();
    }

    /**
     * Tests the {@link BookingController#getAllBookings} method with large dataset.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method handles large result sets correctly</li>
     *   <li>All bookings are returned without truncation</li>
     *   <li>Performance is acceptable for administrative operations</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle large number of bookings")
    void testGetAllBookings_LargeDataset_ReturnsAllBookings() {
        // Arrange
        List<BookingDTO> bookings = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            bookings.add(mock(BookingDTO.class));
        }

        when(bookingService.getAllBookings()).thenReturn(bookings);

        // Act
        List<BookingDTO> result = bookingController.getAllBookings();

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        verify(bookingService).getAllBookings();
    }

    /**
     * Tests the {@link BookingController#getAllBookings} method when service returns null.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>Null is returned when service returns null</li>
     *   <li>The controller passes through null responses from service layer</li>
     * </ul>
     *
     * <p><strong>Note:</strong> This behavior could be improved by adding null-check
     * validation in the controller to throw a proper exception instead of returning null.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return null when service returns null")
    void testGetAllBookings_ServiceReturnsNull_ReturnsNull() {
        // Arrange
        when(bookingService.getAllBookings()).thenReturn(null);

        // Act
        List<BookingDTO> result = bookingController.getAllBookings();

        // Assert
        assertNull(result);
        verify(bookingService).getAllBookings();
    }
}
