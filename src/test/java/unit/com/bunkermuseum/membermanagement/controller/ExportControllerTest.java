package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.ExportController;
import com.bunkermuseum.membermanagement.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit test suite for the {@link ExportController} class.
 *
 * <p>This test class validates all export endpoints exposed by the ExportController
 * for Vaadin Hilla frontend integration. It uses Mockito to mock the ExportService dependency,
 * focusing on testing controller logic, parameter handling, and response formatting.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *   <li>Exporting users by type and format</li>
 *   <li>Exporting bookings by type, format, and date range</li>
 *   <li>Exporting emails by type and format</li>
 *   <li>Exporting single user by ID and format</li>
 *   <li>Exporting single booking by ID and format</li>
 *   <li>Response message formatting</li>
 * </ul>
 *
 * @see ExportController
 * @see ExportService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExportController Unit Tests")
class ExportControllerTest {

    @Mock
    private ExportService exportService;

    private ExportController exportController;

    @BeforeEach
    void setUp() {
        exportController = new ExportController(exportService);
    }

    @Test
    @DisplayName("Should successfully export users with valid parameters")
    void testExportUsers_ValidParameters_Success() {
        // Arrange
        String userType = "admin";
        String format = "xlsx";
        byte[] exportData = "export data".getBytes();

        when(exportService.exportUsers(userType, format)).thenReturn(exportData);

        // Act
        String result = exportController.exportUsers(userType, format);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Export successful"));
        assertTrue(result.contains(String.valueOf(exportData.length)));
        verify(exportService).exportUsers(userType, format);
    }

    @Test
    @DisplayName("Should handle different user types correctly")
    void testExportUsers_DifferentUserTypes_Success() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(exportData);

        // Act
        String resultAdmin = exportController.exportUsers("admin", "csv");
        String resultCustomer = exportController.exportUsers("customer", "json");

        // Assert
        assertNotNull(resultAdmin);
        assertNotNull(resultCustomer);
        verify(exportService).exportUsers("admin", "csv");
        verify(exportService).exportUsers("customer", "json");
    }

    @Test
    @DisplayName("Should handle different formats correctly")
    void testExportUsers_DifferentFormats_Success() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(exportData);

        // Act
        exportController.exportUsers("admin", "xlsx");
        exportController.exportUsers("admin", "csv");
        exportController.exportUsers("admin", "json");

        // Assert
        verify(exportService).exportUsers("admin", "xlsx");
        verify(exportService).exportUsers("admin", "csv");
        verify(exportService).exportUsers("admin", "json");
    }

    @Test
    @DisplayName("Should successfully export bookings with date range")
    void testExportBookings_WithDateRange_Success() {
        // Arrange
        String bookingType = "confirmed";
        String format = "xlsx";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        byte[] exportData = "booking data".getBytes();

        when(exportService.exportBookings(bookingType, format, startDate, endDate)).thenReturn(exportData);

        // Act
        String result = exportController.exportBookings(bookingType, format, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Export successful"));
        assertTrue(result.contains(String.valueOf(exportData.length)));
        verify(exportService).exportBookings(bookingType, format, startDate, endDate);
    }

    @Test
    @DisplayName("Should export bookings without date range")
    void testExportBookings_WithoutDateRange_Success() {
        // Arrange
        String bookingType = "all";
        String format = "csv";
        byte[] exportData = "all bookings".getBytes();

        when(exportService.exportBookings(bookingType, format, null, null)).thenReturn(exportData);

        // Act
        String result = exportController.exportBookings(bookingType, format, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Export successful"));
        verify(exportService).exportBookings(bookingType, format, null, null);
    }

    @Test
    @DisplayName("Should handle different booking types")
    void testExportBookings_DifferentBookingTypes_Success() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportBookings(anyString(), anyString(), any(), any())).thenReturn(exportData);

        // Act
        exportController.exportBookings("confirmed", "xlsx", null, null);
        exportController.exportBookings("pending", "csv", null, null);
        exportController.exportBookings("cancelled", "json", null, null);

        // Assert
        verify(exportService).exportBookings("confirmed", "xlsx", null, null);
        verify(exportService).exportBookings("pending", "csv", null, null);
        verify(exportService).exportBookings("cancelled", "json", null, null);
    }

    @Test
    @DisplayName("Should successfully export emails with valid parameters")
    void testExportEmails_ValidParameters_Success() {
        // Arrange
        String emailType = "notification";
        String format = "xlsx";
        byte[] exportData = "email data".getBytes();

        when(exportService.exportEmails(emailType, format)).thenReturn(exportData);

        // Act
        String result = exportController.exportEmails(emailType, format);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Export successful"));
        assertTrue(result.contains(String.valueOf(exportData.length)));
        verify(exportService).exportEmails(emailType, format);
    }

    @Test
    @DisplayName("Should handle different email types")
    void testExportEmails_DifferentEmailTypes_Success() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportEmails(anyString(), anyString())).thenReturn(exportData);

        // Act
        exportController.exportEmails("notification", "xlsx");
        exportController.exportEmails("newsletter", "csv");
        exportController.exportEmails("transactional", "json");

        // Assert
        verify(exportService).exportEmails("notification", "xlsx");
        verify(exportService).exportEmails("newsletter", "csv");
        verify(exportService).exportEmails("transactional", "json");
    }

    @Test
    @DisplayName("Should successfully export single user by ID")
    void testExportUser_ValidUserId_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String format = "xlsx";
        byte[] exportData = "user data".getBytes();

        when(exportService.exportUser(userId, format)).thenReturn(exportData);

        // Act
        String result = exportController.exportUser(userId, format);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Export successful"));
        assertTrue(result.contains(String.valueOf(exportData.length)));
        verify(exportService).exportUser(userId, format);
    }

    @Test
    @DisplayName("Should handle different formats for single user export")
    void testExportUser_DifferentFormats_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        byte[] exportData = "data".getBytes();
        when(exportService.exportUser(eq(userId), anyString())).thenReturn(exportData);

        // Act
        exportController.exportUser(userId, "xlsx");
        exportController.exportUser(userId, "csv");
        exportController.exportUser(userId, "json");
        exportController.exportUser(userId, "xml");

        // Assert
        verify(exportService).exportUser(userId, "xlsx");
        verify(exportService).exportUser(userId, "csv");
        verify(exportService).exportUser(userId, "json");
        verify(exportService).exportUser(userId, "xml");
    }

    @Test
    @DisplayName("Should successfully export single booking by ID")
    void testExportBooking_ValidBookingId_Success() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        String format = "xlsx";
        byte[] exportData = "booking data".getBytes();

        when(exportService.exportBooking(bookingId, format)).thenReturn(exportData);

        // Act
        String result = exportController.exportBooking(bookingId, format);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Export successful"));
        assertTrue(result.contains(String.valueOf(exportData.length)));
        verify(exportService).exportBooking(bookingId, format);
    }

    @Test
    @DisplayName("Should handle different formats for single booking export")
    void testExportBooking_DifferentFormats_Success() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        byte[] exportData = "data".getBytes();
        when(exportService.exportBooking(eq(bookingId), anyString())).thenReturn(exportData);

        // Act
        exportController.exportBooking(bookingId, "xlsx");
        exportController.exportBooking(bookingId, "csv");
        exportController.exportBooking(bookingId, "json");
        exportController.exportBooking(bookingId, "xml");

        // Assert
        verify(exportService).exportBooking(bookingId, "xlsx");
        verify(exportService).exportBooking(bookingId, "csv");
        verify(exportService).exportBooking(bookingId, "json");
        verify(exportService).exportBooking(bookingId, "xml");
    }

    @Test
    @DisplayName("Should format response message correctly with byte count")
    void testExportMethods_ResponseFormat_ContainsCorrectInformation() {
        // Arrange
        byte[] smallData = "small".getBytes();
        byte[] largeData = new byte[1024 * 1024]; // 1MB

        when(exportService.exportUsers(anyString(), anyString())).thenReturn(smallData);
        when(exportService.exportBookings(anyString(), anyString(), any(), any())).thenReturn(largeData);

        // Act
        String smallResult = exportController.exportUsers("admin", "xlsx");
        String largeResult = exportController.exportBookings("all", "xlsx", null, null);

        // Assert
        assertTrue(smallResult.contains(String.valueOf(smallData.length)));
        assertTrue(largeResult.contains(String.valueOf(largeData.length)));
    }

    @Test
    @DisplayName("Should handle empty export data")
    void testExportMethods_EmptyData_ReturnsZeroBytes() {
        // Arrange
        byte[] emptyData = new byte[0];
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(emptyData);

        // Act
        String result = exportController.exportUsers("admin", "xlsx");

        // Assert
        assertTrue(result.contains("0 bytes"));
    }
}
