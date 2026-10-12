package unit.com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.dto.AssignBookingRequest;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.dto.MemberType;
import com.bunkermuseum.membermanagement.dto.UpdateBookingRequest;
import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.BookingRepositoryContract;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * Mock instance of the user repository contract, field-injected into BookingService.
     */
    @Mock
    private UserRepositoryContract userRepository;

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
        ReflectionTestUtils.setField(bookingService, "userRepository", userRepository);
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
        List<BookingDTO> result = bookingService.getAllBookings();

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
        List<BookingDTO> result = bookingService.getAllBookings();

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
        List<BookingDTO> result = bookingService.getAllBookings();

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
    @DisplayName("Should throw exception when repository returns null")
    void testGetAllBookings_RepositoryReturnsNull_ThrowsException() {
        // Arrange
        when(bookingRepository.findAll()).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookingService.getAllBookings();
        });
        verify(bookingRepository).findAll();
    }

    /**
     * Builds an assignment request targeting a single user.
     *
     * @param userId The target user ID
     * @return The request
     */
    private AssignBookingRequest singleUserRequest(UUID userId) {
        AssignBookingRequest request = new AssignBookingRequest(
            null, new BigDecimal("50.00"), new BigDecimal("50.00"), "Spende");
        request.setUserId(userId);
        return request;
    }

    @Test
    @DisplayName("Should create exactly one booking when a single user is targeted")
    @SuppressWarnings("unchecked")
    void testAssignBookingToUsers_SingleUser_CreatesOneBooking() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.isActive()).thenReturn(true);
        when(user.getName()).thenReturn("Max Mustermann");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        int result = bookingService.assignBookingToUsers(singleUserRequest(userId));

        // Assert
        assertEquals(1, result);
        org.mockito.ArgumentCaptor<List<Booking>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(bookingRepository).createAll(captor.capture());
        List<Booking> created = captor.getValue();
        assertEquals(1, created.size());
        assertSame(user, created.getFirst().getUser());
        assertEquals("Spende " + Year.now().getValue() + ", Max Mustermann", created.getFirst().getActualPurpose());
        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should reject a single-user booking for an unknown user")
    void testAssignBookingToUsers_SingleUserNotFound_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> bookingService.assignBookingToUsers(singleUserRequest(userId)));
        verify(bookingRepository, never()).createAll(any());
    }

    @Test
    @DisplayName("Should reject a single-user booking for a deleted user")
    void testAssignBookingToUsers_SingleUserDeleted_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.isActive()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> bookingService.assignBookingToUsers(singleUserRequest(userId)));
        verify(bookingRepository, never()).createAll(any());
    }

    @Test
    @DisplayName("Should reject a request with both member type and user set")
    void testAssignBookingToUsers_BothTargetsSet_ThrowsException() {
        // Arrange
        AssignBookingRequest request = singleUserRequest(UUID.randomUUID());
        request.setMemberType(MemberType.REGULAR_MEMBERS);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.assignBookingToUsers(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should reject a request with neither member type nor user set")
    void testAssignBookingToUsers_NoTargetSet_ThrowsException() {
        // Arrange
        AssignBookingRequest request = singleUserRequest(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.assignBookingToUsers(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should update all entries of a single booking")
    void testUpdateBooking_Success_ReplacesEntries() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking(null);
        booking.setActualAmount(new BigDecimal("10.00"));
        booking.setNote("alt");
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.update(eq(bookingId), any(Booking.class))).thenAnswer(inv -> inv.getArgument(1));

        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setExpectedAmount(new BigDecimal("50.00"));
        request.setExpectedPurpose("Mitgliedsbeitrag 2026");
        request.setActualAmount(new BigDecimal("45.50"));
        request.setActualPurpose("  Mitgliedsbeitrag 2026, Max  ");
        request.setReceivedAt(LocalDate.of(2026, 3, 15));
        request.setOfMG("Max Mustermann");
        request.setNote("   ");
        request.setAccountStatementPage("12");
        request.setCode("MB");

        // Act
        BookingDTO result = bookingService.updateBooking(bookingId, request);

        // Assert
        assertEquals(new BigDecimal("50.00"), result.getExpectedAmount());
        assertEquals("Mitgliedsbeitrag 2026", result.getExpectedPurpose());
        assertEquals(new BigDecimal("45.50"), result.getActualAmount());
        assertEquals("Mitgliedsbeitrag 2026, Max", result.getActualPurpose());
        assertEquals(Instant.parse("2026-03-14T23:00:00Z"), result.getReceivedAt());
        assertEquals("Max Mustermann", result.getOfMG());
        assertNull(result.getNote());
        assertEquals("12", result.getAccountStatementPage());
        assertEquals("MB", result.getCode());
        verify(bookingRepository).update(bookingId, booking);
    }

    @Test
    @DisplayName("Should throw when updating a booking that does not exist")
    void testUpdateBooking_NotFound_ThrowsException() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
            () -> bookingService.updateBooking(bookingId, new UpdateBookingRequest()));
        verify(bookingRepository, never()).update(any(), any());
    }

    @Test
    @DisplayName("Should throw when updating a deleted booking")
    void testUpdateBooking_DeletedBooking_ThrowsException() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking(null);
        booking.delete();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
            () -> bookingService.updateBooking(bookingId, new UpdateBookingRequest()));
        verify(bookingRepository, never()).update(any(), any());
    }

    @Test
    @DisplayName("Should return only the current user's active bookings")
    void testGetCurrentUserBookings_ReturnsOwnActiveBookings() {
        // Arrange
        UUID currentUserId = UUID.randomUUID();
        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(currentUserId);
        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(UUID.randomUUID());

        Booking own = new Booking(null);
        own.setUser(currentUser);
        own.setCode("OWN");
        Booking ownDeleted = new Booking(null);
        ownDeleted.setUser(currentUser);
        ownDeleted.delete();
        Booking foreign = new Booking(null);
        foreign.setUser(otherUser);
        Booking unassigned = new Booking(null);
        when(bookingRepository.findAll()).thenReturn(List.of(own, ownDeleted, foreign, unassigned));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(currentUser, null, List.of()));

        try {
            // Act
            List<BookingDTO> result = bookingService.getCurrentUserBookings();

            // Assert
            assertEquals(1, result.size());
            assertEquals("OWN", result.getFirst().getCode());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Should create a booking without actual amount so it can be entered later")
    @SuppressWarnings("unchecked")
    void testAssignBookingToUsers_WithoutActualAmount_LeavesItEmpty() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.isActive()).thenReturn(true);
        when(user.getName()).thenReturn("Max Mustermann");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AssignBookingRequest request = new AssignBookingRequest(
            null, new BigDecimal("50.00"), null, "Mitgliedsbeitrag");
        request.setUserId(userId);

        // Act
        int result = bookingService.assignBookingToUsers(request);

        // Assert
        assertEquals(1, result);
        org.mockito.ArgumentCaptor<List<Booking>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(bookingRepository).createAll(captor.capture());
        Booking created = captor.getValue().getFirst();
        assertEquals(new BigDecimal("50.00"), created.getExpectedAmount());
        assertNull(created.getActualAmount());
    }
}
