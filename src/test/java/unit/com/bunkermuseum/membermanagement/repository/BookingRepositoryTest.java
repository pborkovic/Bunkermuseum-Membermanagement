package unit.com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.BookingRepository;
import com.bunkermuseum.membermanagement.repository.jpa.BookingJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    /**
     * Tests the {@link BookingRepository#findAll} method with successful retrieval.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves all bookings from the repository</li>
     *   <li>The returned list contains the expected bookings</li>
     *   <li>The JPA repository findAll method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully retrieve all bookings")
    void testFindAll_Success_ReturnsBookings() {
        // Arrange
        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);
        List<Booking> bookings = List.of(booking1, booking2);

        when(jpaRepository.findAll()).thenReturn(bookings);

        // Act
        List<Booking> result = bookingRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link BookingRepository#findAll} method when no bookings exist.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty list when no bookings are present</li>
     *   <li>No exception is thrown for empty results</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty list when no bookings exist")
    void testFindAll_NoBookings_ReturnsEmptyList() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(List.of());

        // Act
        List<Booking> result = bookingRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link BookingRepository#findById} method with valid ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves a booking by its ID</li>
     *   <li>Returns an Optional containing the booking</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find booking by valid ID")
    void testFindById_ValidId_ReturnsBooking() {
        // Arrange
        UUID id = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(booking));

        // Act
        Optional<Booking> result = bookingRepository.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(booking, result.get());
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link BookingRepository#findById} method when booking not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty Optional when booking doesn't exist</li>
     *   <li>No exception is thrown for non-existent IDs</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty optional when booking not found")
    void testFindById_NonExistentId_ReturnsEmpty() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Booking> result = bookingRepository.findById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link BookingRepository#findByIdOrFail} method with valid ID.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully retrieves a booking by its ID</li>
     *   <li>Returns the booking directly (not wrapped in Optional)</li>
     *   <li>The JPA repository is called with the correct ID</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find booking by ID or fail with valid ID")
    void testFindByIdOrFail_ValidId_ReturnsBooking() {
        // Arrange
        UUID id = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(booking));

        // Act
        Booking result = bookingRepository.findByIdOrFail(id);

        // Assert
        assertNotNull(result);
        assertEquals(booking, result);
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link BookingRepository#findByIdOrFail} method when booking not found.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when booking doesn't exist</li>
     *   <li>The exception is caused by an EntityNotFoundException</li>
     *   <li>The exception message contains "Booking" and the ID</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when booking not found")
    void testFindByIdOrFail_NonExistentId_ThrowsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRepository.findByIdOrFail(id);
        });

        assertTrue(exception.getCause() instanceof EntityNotFoundException);
        assertTrue(exception.getCause().getMessage().contains("Booking"));
        assertTrue(exception.getCause().getMessage().contains(id.toString()));
        verify(jpaRepository).findById(id);
    }

    /**
     * Tests the {@link BookingRepository#create} method with valid booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully creates a new booking</li>
     *   <li>Returns the saved booking</li>
     *   <li>The JPA repository save method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully create a new booking")
    void testCreate_ValidBooking_ReturnsCreatedBooking() {
        // Arrange
        Booking booking = mock(Booking.class);
        when(jpaRepository.save(booking)).thenReturn(booking);

        // Act
        Booking result = bookingRepository.create(booking);

        // Assert
        assertNotNull(result);
        assertEquals(booking, result);
        verify(jpaRepository).save(booking);
    }

    /**
     * Tests the {@link BookingRepository#update} method with valid booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully updates an existing booking</li>
     *   <li>Returns the updated booking</li>
     *   <li>The JPA repository checks existence and saves the booking</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully update an existing booking")
    void testUpdate_ExistingBooking_ReturnsUpdatedBooking() {
        // Arrange
        UUID id = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        when(jpaRepository.existsById(id)).thenReturn(true);
        when(jpaRepository.save(booking)).thenReturn(booking);

        // Act
        Booking result = bookingRepository.update(id, booking);

        // Assert
        assertNotNull(result);
        assertEquals(booking, result);
        verify(jpaRepository).existsById(id);
        verify(jpaRepository).save(booking);
    }

    /**
     * Tests the {@link BookingRepository#update} method with non-existent booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when booking doesn't exist</li>
     *   <li>The exception is caused by an EntityNotFoundException</li>
     *   <li>The exception message contains "Booking" and the ID</li>
     *   <li>The save method is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when updating non-existent booking")
    void testUpdate_NonExistentBooking_ThrowsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        when(jpaRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRepository.update(id, booking);
        });

        assertTrue(exception.getCause() instanceof EntityNotFoundException);
        assertTrue(exception.getCause().getMessage().contains("Booking"));
        assertTrue(exception.getCause().getMessage().contains(id.toString()));
        verify(jpaRepository).existsById(id);
        verify(jpaRepository, never()).save(any());
    }

    /**
     * Tests the {@link BookingRepository#deleteById} method with existing booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully soft deletes an existing booking</li>
     *   <li>Returns true to indicate successful deletion</li>
     *   <li>The entity is fetched, marked as deleted, and saved</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should successfully delete an existing booking")
    void testDeleteById_ExistingBooking_ReturnsTrue() {
        // Arrange
        UUID id = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(booking));
        when(jpaRepository.save(booking)).thenReturn(booking);

        // Act
        boolean result = bookingRepository.deleteById(id);

        // Assert
        assertTrue(result);
        verify(jpaRepository).findById(id);
        verify(booking).delete();
        verify(jpaRepository).save(booking);
    }

    /**
     * Tests the {@link BookingRepository#deleteById} method with non-existent booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns false when booking doesn't exist</li>
     *   <li>No exception is thrown for non-existent bookings</li>
     *   <li>The save method is never called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return false when deleting non-existent booking")
    void testDeleteById_NonExistentBooking_ReturnsFalse() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = bookingRepository.deleteById(id);

        // Assert
        assertFalse(result);
        verify(jpaRepository).findById(id);
        verify(jpaRepository, never()).save(any());
    }

    /**
     * Tests the {@link BookingRepository#count} method.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns the correct count of bookings</li>
     *   <li>The JPA repository count method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return correct count of bookings")
    void testCount_ReturnsCorrectCount() {
        // Arrange
        when(jpaRepository.count()).thenReturn(5L);

        // Act
        long result = bookingRepository.count();

        // Assert
        assertEquals(5L, result);
        verify(jpaRepository).count();
    }

    /**
     * Tests the {@link BookingRepository#existsById} method with existing booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns true for existing bookings</li>
     *   <li>The JPA repository existsById method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return true when booking exists")
    void testExistsById_ExistingBooking_ReturnsTrue() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = bookingRepository.existsById(id);

        // Assert
        assertTrue(result);
        verify(jpaRepository).existsById(id);
    }

    /**
     * Tests the {@link BookingRepository#existsById} method with non-existent booking.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns false for non-existent bookings</li>
     *   <li>The JPA repository existsById method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return false when booking does not exist")
    void testExistsById_NonExistentBooking_ReturnsFalse() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(jpaRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = bookingRepository.existsById(id);

        // Assert
        assertFalse(result);
        verify(jpaRepository).existsById(id);
    }

    /**
     * Tests the {@link BookingRepository#findFirst} method with existing bookings.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns the first booking from the repository</li>
     *   <li>Returns an Optional containing the first booking</li>
     *   <li>The JPA repository findAll with pagination is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return first booking when bookings exist")
    void testFindFirst_WithBookings_ReturnsFirstBooking() {
        // Arrange
        Booking firstBooking = mock(Booking.class);
        Page<Booking> page = new PageImpl<>(List.of(firstBooking));
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        Optional<Booking> result = bookingRepository.findFirst();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(firstBooking, result.get());
        verify(jpaRepository).findAll(any(PageRequest.class));
    }

    /**
     * Tests the {@link BookingRepository#findFirst} method with no bookings.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns an empty Optional when no bookings exist</li>
     *   <li>No exception is thrown for empty results</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return empty optional when no bookings exist")
    void testFindFirst_NoBookings_ReturnsEmpty() {
        // Arrange
        Page<Booking> emptyPage = new PageImpl<>(List.of());
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        Optional<Booking> result = bookingRepository.findFirst();

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository).findAll(any(PageRequest.class));
    }

    /**
     * Tests the {@link BookingRepository#findAllById} method with multiple IDs.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns bookings matching the provided IDs</li>
     *   <li>The JPA repository findAllById method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should find all bookings by IDs")
    void testFindAllById_MultipleIds_ReturnsMatchingBookings() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = List.of(id1, id2);
        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);
        List<Booking> bookings = List.of(booking1, booking2);

        when(jpaRepository.findAllById(ids)).thenReturn(bookings);

        // Act
        List<Booking> result = bookingRepository.findAllById(ids);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository).findAllById(ids);
    }

    /**
     * Tests the {@link BookingRepository#createAll} method with multiple bookings.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method successfully creates multiple bookings</li>
     *   <li>Returns all saved bookings</li>
     *   <li>The JPA repository saveAll method is called</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should create all bookings")
    void testCreateAll_MultipleBookings_ReturnsCreatedBookings() {
        // Arrange
        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);
        List<Booking> bookings = List.of(booking1, booking2);

        when(jpaRepository.saveAll(bookings)).thenReturn(bookings);

        // Act
        List<Booking> result = bookingRepository.createAll(bookings);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository).saveAll(bookings);
    }

    /**
     * Tests the {@link BookingRepository#findActive} method.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns only active (non-deleted) bookings</li>
     *   <li>Bookings with deletedAt == null are included</li>
     *   <li>Bookings with deletedAt != null are excluded</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return only active bookings")
    void testFindActive_ReturnsOnlyActiveBookings() {
        // Arrange
        Booking activeBooking = mock(Booking.class);
        Booking deletedBooking = mock(Booking.class);
        when(activeBooking.deletedAt()).thenReturn(null);
        when(deletedBooking.deletedAt()).thenReturn(java.time.Instant.now());

        List<Booking> allBookings = List.of(activeBooking, deletedBooking);
        when(jpaRepository.findAll()).thenReturn(allBookings);

        // Act
        List<Booking> result = bookingRepository.findActive();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(activeBooking));
        assertFalse(result.contains(deletedBooking));
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link BookingRepository#findDeleted} method.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns only soft-deleted bookings</li>
     *   <li>Bookings with deletedAt != null are included</li>
     *   <li>Bookings with deletedAt == null are excluded</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return only deleted bookings")
    void testFindDeleted_ReturnsOnlyDeletedBookings() {
        // Arrange
        Booking activeBooking = mock(Booking.class);
        Booking deletedBooking = mock(Booking.class);
        when(activeBooking.deletedAt()).thenReturn(null);
        when(deletedBooking.deletedAt()).thenReturn(java.time.Instant.now());

        List<Booking> allBookings = List.of(activeBooking, deletedBooking);
        when(jpaRepository.findAll()).thenReturn(allBookings);

        // Act
        List<Booking> result = bookingRepository.findDeleted();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(deletedBooking));
        assertFalse(result.contains(activeBooking));
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link BookingRepository#findWithDeleted} method.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>The method returns all bookings including soft-deleted ones</li>
     *   <li>Both active and deleted bookings are included</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should return all bookings including deleted")
    void testFindWithDeleted_ReturnsAllBookings() {
        // Arrange
        Booking activeBooking = mock(Booking.class);
        Booking deletedBooking = mock(Booking.class);

        List<Booking> allBookings = List.of(activeBooking, deletedBooking);
        when(jpaRepository.findAll()).thenReturn(allBookings);

        // Act
        List<Booking> result = bookingRepository.findWithDeleted();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(activeBooking));
        assertTrue(result.contains(deletedBooking));
        verify(jpaRepository).findAll();
    }

    /**
     * Tests the {@link BookingRepository#findAll} method when JPA repository throws exception.
     *
     * <p>This test verifies that:</p>
     * <ul>
     *   <li>A RuntimeException is thrown when JPA operation fails</li>
     *   <li>The exception message contains "Failed to execute operation"</li>
     *   <li>The JPA repository is called before exception is thrown</li>
     * </ul>
     *
     * @author Philipp Borkovic
     */
    @Test
    @DisplayName("Should throw RuntimeException when JPA repository fails")
    void testFindAll_JpaRepositoryThrowsException_ThrowsRuntimeException() {
        // Arrange
        when(jpaRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingRepository.findAll();
        });

        assertTrue(exception.getMessage().contains("Failed to execute operation"));
        verify(jpaRepository).findAll();
    }
}
