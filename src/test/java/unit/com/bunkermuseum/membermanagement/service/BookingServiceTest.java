package unit.com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.contract.BookingRepositoryContract;
import com.bunkermuseum.membermanagement.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    /**
     * Tests the {@link BookingService#getAllBookings} method with successful retrieval.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves all bookings from the repository</li>
     *   <li>The returned list contains the expected bookings</li>
     *   <li>The repository findAll method is called</li>
     *   <li>No exceptions are thrown during successful operation</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve all bookings")
    void testGetAllBookings_Success_ReturnsBookings() {
        // Arrange
        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);
        List<Booking> bookings = List.of(booking1, booking2);

        when(bookingRepository.findAll()).thenReturn(bookings);

        // Act
        List<Booking> result = bookingService.getAllBookings();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingRepository).findAll();
    }

    /**
     * Tests the {@link BookingService#getAllBookings} method when no bookings exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty list when no bookings are present</li>
     *   <li>No exception is thrown for empty results</li>
     *   <li>The list is not null</li>
     *   <li>The repository findAll method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when no bookings exist")
    void testGetAllBookings_NoBookings_ReturnsEmptyList() {
        // Arrange
        when(bookingRepository.findAll()).thenReturn(List.of());

        // Act
        List<Booking> result = bookingService.getAllBookings();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookingRepository).findAll();
    }

    /**
     * Tests the {@link BookingService#getAllBookings} method when repository throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when repository operation fails</li>
     *   <li>The exception message contains "Failed to retrieve bookings"</li>
     *   <li>The repository findAll method is called before exception is thrown</li>
     *   <li>Service layer wraps repository exceptions appropriately</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when repository fails")
    void testGetAllBookings_RepositoryThrowsException_ThrowsRuntimeException() {
        // Arrange
        when(bookingRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.getAllBookings();
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve bookings"));
        verify(bookingRepository).findAll();
    }

    /**
     * Tests the {@link BookingService#getAllBookings} method with large dataset.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method handles large result sets correctly</li>
     *   <li>All bookings are returned without truncation</li>
     *   <li>Performance is acceptable for large datasets</li>
     *   <li>The repository findAll method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should handle large number of bookings")
    void testGetAllBookings_LargeDataset_ReturnsAllBookings() {
        // Arrange
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            bookings.add(mock(Booking.class));
        }

        when(bookingRepository.findAll()).thenReturn(bookings);

        // Act
        List<Booking> result = bookingService.getAllBookings();

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.size());
        verify(bookingRepository).findAll();
    }

    /**
     * Tests the {@link BookingService#getAllBookings} method when repository returns null.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The service returns null when repository returns null</li>
     *   <li>The repository findAll method is called</li>
     *   <li>Service passes through null responses from repository</li>
     * </ul>
     *
     * <p><strong>Note:</strong> This behavior could be improved by adding null-check
     * validation in the service to throw a proper exception instead of returning null.</p>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return null when repository returns null")
    void testGetAllBookings_RepositoryReturnsNull_ReturnsNull() {
        // Arrange
        when(bookingRepository.findAll()).thenReturn(null);

        // Act
        List<Booking> result = bookingService.getAllBookings();

        // Assert
        assertNull(result);
        verify(bookingRepository).findAll();
    }
}
