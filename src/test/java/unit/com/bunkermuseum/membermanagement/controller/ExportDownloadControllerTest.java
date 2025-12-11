package unit.com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.controller.ExportDownloadController;
import com.bunkermuseum.membermanagement.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit test suite for the {@link ExportDownloadController} class.
 *
 * <p>This test class validates all export download endpoints exposed by the ExportDownloadController
 * for REST API integration. It uses Mockito to mock the ExportService dependency,
 * focusing on testing controller logic, HTTP response formatting, content types, and file downloads.</p>
 *
 * <h3>Test Coverage:</h3>
 * <ul>
 *   <li>Downloading user exports with correct content types</li>
 *   <li>Downloading booking exports with date filtering</li>
 *   <li>Downloading email exports</li>
 *   <li>Downloading single user/booking exports</li>
 *   <li>HTTP headers (Content-Disposition, Content-Type)</li>
 *   <li>Filename generation with timestamps</li>
 * </ul>
 *
 * @see ExportDownloadController
 * @see ExportService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExportDownloadController Unit Tests")
class ExportDownloadControllerTest {

    @Mock
    private ExportService exportService;

    private ExportDownloadController exportDownloadController;

    @BeforeEach
    void setUp() {
        exportDownloadController = new ExportDownloadController(exportService);
    }


    @Test
    @DisplayName("Should successfully download users export with correct headers")
    void testDownloadUsers_ValidParameters_ReturnsFileWithHeaders() {
        // Arrange
        String userType = "admin";
        String format = "xlsx";
        byte[] exportData = "user export data".getBytes();

        when(exportService.exportUsers(userType, format)).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers(userType, format);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(exportData, response.getBody());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("users_"));
        assertTrue(contentDisposition.contains(".xlsx"));

        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                response.getHeaders().getContentType());

        verify(exportService).exportUsers(userType, format);
    }

    @Test
    @DisplayName("Should handle CSV format for users download")
    void testDownloadUsers_CsvFormat_ReturnsCorrectContentType() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportUsers(anyString(), eq("csv"))).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers("admin", "csv");

        // Assert
        assertNotNull(response);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains(".csv"));
    }

    @Test
    @DisplayName("Should successfully download bookings with date range")
    void testDownloadBookings_WithDateRange_ReturnsFileWithHeaders() {
        // Arrange
        String bookingType = "confirmed";
        String format = "xlsx";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        byte[] exportData = "booking data".getBytes();

        when(exportService.exportBookings(bookingType, format, startDate, endDate)).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadBookings(
                bookingType, format, startDate, endDate);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(exportData, response.getBody());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisposition.contains("bookings_"));
        assertTrue(contentDisposition.contains(".xlsx"));

        verify(exportService).exportBookings(bookingType, format, startDate, endDate);
    }

    @Test
    @DisplayName("Should download bookings without date range")
    void testDownloadBookings_WithoutDateRange_Success() {
        // Arrange
        byte[] exportData = "all bookings".getBytes();
        when(exportService.exportBookings(anyString(), anyString(), isNull(), isNull())).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadBookings("all", "xlsx", null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(exportService).exportBookings("all", "xlsx", null, null);
    }

    @Test
    @DisplayName("Should successfully download emails export")
    void testDownloadEmails_ValidParameters_ReturnsFileWithHeaders() {
        // Arrange
        String emailType = "notification";
        String format = "json";
        byte[] exportData = "email data".getBytes();

        when(exportService.exportEmails(emailType, format)).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadEmails(emailType, format);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(exportData, response.getBody());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisposition.contains("emails_"));
        assertTrue(contentDisposition.contains(".json"));

        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        verify(exportService).exportEmails(emailType, format);
    }

    @Test
    @DisplayName("Should successfully download single user export")
    void testDownloadUser_ValidUserId_ReturnsFileWithHeaders() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String format = "xlsx";
        byte[] exportData = "user data".getBytes();

        when(exportService.exportUser(userId, format)).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUser(userId, format);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(exportData, response.getBody());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisposition.contains("user_"));
        assertTrue(contentDisposition.contains(userId.toString().substring(0, 8)));
        assertTrue(contentDisposition.contains(".xlsx"));

        verify(exportService).exportUser(userId, format);
    }

    @Test
    @DisplayName("Should handle different formats for single user download")
    void testDownloadUser_DifferentFormats_ReturnsCorrectContentTypes() {
        // Arrange
        UUID userId = UUID.randomUUID();
        byte[] exportData = "data".getBytes();
        when(exportService.exportUser(eq(userId), anyString())).thenReturn(exportData);

        // Act & Assert - XLSX
        ResponseEntity<byte[]> xlsxResponse = exportDownloadController.downloadUser(userId, "xlsx");
        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                xlsxResponse.getHeaders().getContentType());

        // Act & Assert - PDF
        ResponseEntity<byte[]> pdfResponse = exportDownloadController.downloadUser(userId, "pdf");
        assertEquals(MediaType.APPLICATION_PDF, pdfResponse.getHeaders().getContentType());

        // Act & Assert - JSON
        ResponseEntity<byte[]> jsonResponse = exportDownloadController.downloadUser(userId, "json");
        assertEquals(MediaType.APPLICATION_JSON, jsonResponse.getHeaders().getContentType());

        // Act & Assert - XML
        ResponseEntity<byte[]> xmlResponse = exportDownloadController.downloadUser(userId, "xml");
        assertEquals(MediaType.APPLICATION_XML, xmlResponse.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Should successfully download single booking export")
    void testDownloadBooking_ValidBookingId_ReturnsFileWithHeaders() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        String format = "xlsx";
        byte[] exportData = "booking data".getBytes();

        when(exportService.exportBooking(bookingId, format)).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadBooking(bookingId, format);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(exportData, response.getBody());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisposition.contains("booking_"));
        assertTrue(contentDisposition.contains(bookingId.toString().substring(0, 8)));
        assertTrue(contentDisposition.contains(".xlsx"));

        verify(exportService).exportBooking(bookingId, format);
    }

    @Test
    @DisplayName("Should handle different formats for single booking download")
    void testDownloadBooking_DifferentFormats_ReturnsCorrectContentTypes() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        byte[] exportData = "data".getBytes();
        when(exportService.exportBooking(eq(bookingId), anyString())).thenReturn(exportData);

        // Act & Assert - XLSX
        ResponseEntity<byte[]> xlsxResponse = exportDownloadController.downloadBooking(bookingId, "xlsx");
        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                xlsxResponse.getHeaders().getContentType());

        // Act & Assert - PDF
        ResponseEntity<byte[]> pdfResponse = exportDownloadController.downloadBooking(bookingId, "pdf");
        assertEquals(MediaType.APPLICATION_PDF, pdfResponse.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Should generate filename with current date")
    void testDownloadMethods_FilenameFormat_ContainsCurrentDate() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(exportData);
        when(exportService.exportBookings(anyString(), anyString(), any(), any())).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> usersResponse = exportDownloadController.downloadUsers("admin", "xlsx");
        ResponseEntity<byte[]> bookingsResponse = exportDownloadController.downloadBookings("all", "xlsx", null, null);

        // Assert
        String usersFilename = usersResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        String bookingsFilename = bookingsResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertNotNull(usersFilename);
        assertNotNull(bookingsFilename);

        // Filenames should contain ISO date format (yyyy-MM-dd)
        String todayIso = LocalDate.now().toString();
        assertTrue(usersFilename.contains(todayIso) || usersFilename.contains(todayIso.substring(0, 7)));
        assertTrue(bookingsFilename.contains(todayIso) || bookingsFilename.contains(todayIso.substring(0, 7)));
    }

    @Test
    @DisplayName("Should generate unique filenames for single entity exports")
    void testDownloadSingleEntity_FilenameFormat_ContainsIdAndDate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        byte[] exportData = "data".getBytes();

        when(exportService.exportUser(userId, "xlsx")).thenReturn(exportData);
        when(exportService.exportBooking(bookingId, "xlsx")).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> userResponse = exportDownloadController.downloadUser(userId, "xlsx");
        ResponseEntity<byte[]> bookingResponse = exportDownloadController.downloadBooking(bookingId, "xlsx");

        // Assert
        String userFilename = userResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        String bookingFilename = bookingResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertTrue(userFilename.contains(userId.toString().substring(0, 8)));
        assertTrue(bookingFilename.contains(bookingId.toString().substring(0, 8)));
    }

    @Test
    @DisplayName("Should return correct content type for PDF format")
    void testDownloadMethods_PdfFormat_ReturnsCorrectContentType() {
        // Arrange
        byte[] exportData = "pdf data".getBytes();
        when(exportService.exportUsers(anyString(), eq("pdf"))).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers("admin", "pdf");

        // Assert
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Should return correct content type for XML format")
    void testDownloadMethods_XmlFormat_ReturnsCorrectContentType() {
        // Arrange
        byte[] exportData = "xml data".getBytes();
        when(exportService.exportEmails(anyString(), eq("xml"))).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadEmails("all", "xml");

        // Assert
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Should return octet-stream for unknown format")
    void testDownloadMethods_UnknownFormat_ReturnsOctetStream() {
        // Arrange
        byte[] exportData = "data".getBytes();
        when(exportService.exportUsers(anyString(), eq("unknown"))).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers("admin", "unknown");

        // Assert
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Should return export data in response body")
    void testDownloadMethods_ResponseBody_ContainsExportData() {
        // Arrange
        byte[] exportData = "test export data".getBytes();
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(exportData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers("admin", "xlsx");

        // Assert
        assertNotNull(response.getBody());
        assertArrayEquals(exportData, response.getBody());
    }

    @Test
    @DisplayName("Should handle empty export data")
    void testDownloadMethods_EmptyData_ReturnsEmptyResponse() {
        // Arrange
        byte[] emptyData = new byte[0];
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(emptyData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers("admin", "xlsx");

        // Assert
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    @DisplayName("Should handle large export data")
    void testDownloadMethods_LargeData_ReturnsCompleteData() {
        // Arrange
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        when(exportService.exportUsers(anyString(), anyString())).thenReturn(largeData);

        // Act
        ResponseEntity<byte[]> response = exportDownloadController.downloadUsers("admin", "xlsx");

        // Assert
        assertNotNull(response.getBody());
        assertEquals(largeData.length, response.getBody().length);
    }
}
