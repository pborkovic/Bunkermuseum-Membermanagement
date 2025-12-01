package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.service.ExportService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * REST controller responsible for handling data export operations for users, bookings, and emails.
 *
 * <p>
 * The {@code ExportDownloadController} exposes HTTP GET endpoints under the base path
 * <code>/api/export</code> to allow clients to download exported data in various formats such as
 * CSV, JSON, PDF, XML, and XLSX. Each endpoint delegates the actual export logic to the
 * {@link ExportService} and returns the data as a downloadable HTTP response with properly
 * constructed filenames and content types.
 * </p>
 *
 * @see ExportService
 * @see #downloadUsers(String, String)
 * @see #downloadBookings(String, String, LocalDate, LocalDate)
 * @see #downloadEmails(String, String)
 * @see #generateFilename(String, String, String)
 * @see #buildDownloadResponse(byte[], String, String)
 * @see #getContentType(String)
 */
@RestController
@RequestMapping("/api/export")
@PermitAll
public class ExportDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(ExportDownloadController.class);

    private final ExportService exportService;

    /**
     * Constructs an instance of {@code ExportDownloadController} with the specified {@link ExportService}.
     * <p>
     * The provided {@code ExportService} is used to perform all export operations
     * (users, bookings, emails) that this controller exposes via HTTP endpoints.
     * </p>
     *
     * @param exportService the {@link ExportService} instance responsible for handling export logic;
     *                      must not be {@code null}.
     *
     * @throws NullPointerException if {@code exportService} is {@code null}.
     *
     * @see ExportService
     *
     * @author Philipp Borkovic
     */
    public ExportDownloadController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Handles HTTP GET requests for downloading exported user data in the specified format.
     *
     * <p>
     * This endpoint triggers the export of user records for a specified user type
     * (e.g., administrators, customers, or guests), generates a filename following
     * the standard convention, and returns the data as a downloadable HTTP response.
     * </p>
     *
     * @param userType the type of users to export; must not be {@code null}.
     *                 Typical values include {@code "admin"}, {@code "customer"}, or {@code "guest"}.
     * @param format   the desired output file format (e.g., {@code "csv"}, {@code "json"}, {@code "xlsx"}, or {@code "pdf"});
     *                 must not be {@code null}.
     *
     * @return a {@link ResponseEntity} representing a downloadable file attachment
     *         containing the exported user data, with appropriate headers.
     *
     * @throws IllegalArgumentException if {@code userType} or {@code format} is invalid
     *                                  or unsupported by {@link ExportService}.
     * @author Philipp Borkovic
     */
    @GetMapping("/users")
    public ResponseEntity<byte[]> downloadUsers(
            @RequestParam String userType,
            @RequestParam String format) {
        logger.info("Download users: userType={}, format={}", userType, format);

        byte[] exportData = exportService.exportUsers(userType, format);
        String filename = generateFilename("users", userType, format);

        return buildDownloadResponse(exportData, filename, format);
    }

    /**
     * Handles HTTP GET requests for downloading booking data exports in the specified format,
     * optionally filtered by a date range.
     *
     * <p>
     * This endpoint initiates an export of booking records (e.g., confirmed, pending, or cancelled),
     * delegates the export process to {@link ExportService#exportBookings(String, String, LocalDate, LocalDate)},
     * and returns the generated data as a downloadable file.
     * </p>
     *
     * @param bookingType the category of bookings to export; must not be {@code null}.
     *                    Common values may include {@code "confirmed"}, {@code "pending"}, or {@code "cancelled"}.
     * @param format      the desired output file format (e.g., {@code "csv"}, {@code "pdf"}, {@code "xlsx"}, {@code "json"});
     *                    must not be {@code null}.
     * @param startDate   the optional start date to filter bookings (inclusive); may be {@code null} to include all dates.
     * @param endDate     the optional end date to filter bookings (inclusive); may be {@code null} to include all dates.
     *
     * @return a {@link ResponseEntity} representing a downloadable file attachment containing the exported data,
     *         with appropriate content type and filename headers applied.
     *
     * @throws IllegalArgumentException if {@code bookingType} or {@code format} is invalid or unsupported.
     *
     * author Philipp Borkovic
     */
    @GetMapping("/bookings")
    public ResponseEntity<byte[]> downloadBookings(
            @RequestParam String bookingType,
            @RequestParam String format,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        logger.info("Download bookings: bookingType={}, format={}, startDate={}, endDate={}",
                bookingType, format, startDate, endDate);

        byte[] exportData = exportService.exportBookings(bookingType, format, startDate, endDate);
        String filename = generateFilename("bookings", bookingType, format);

        return buildDownloadResponse(exportData, filename, format);
    }

    /**
     * Handles HTTP GET requests for downloading exported email data in the specified format.
     *
     * <p>
     * This endpoint triggers the export of email records of a given type (for example,
     * notifications, newsletters, or transactional messages), generates a properly named
     * file, and returns it as a downloadable HTTP response.
     * </p>
     *
     * @param emailType the category of email data to export; must not be {@code null}.
     *                  Typical values may include {@code "notification"}, {@code "newsletter"},
     *                  or {@code "transactional"}.
     * @param format    the desired output file format (e.g., {@code "csv"}, {@code "pdf"},
     *                  {@code "xlsx"}, or {@code "json"}); must not be {@code null}.
     *
     * @return a {@link ResponseEntity} containing the exported file data as a downloadable attachment,
     *         with the appropriate content type and filename headers applied.
     *
     * @throws IllegalArgumentException if {@code emailType} or {@code format} is invalid
     *                                  or unsupported by the {@link ExportService}.
     *
     * @author Philipp Borkovic
     */
    @GetMapping("/emails")
    public ResponseEntity<byte[]> downloadEmails(
            @RequestParam String emailType,
            @RequestParam String format) {
        logger.info("Download emails: emailType={}, format={}", emailType, format);

        byte[] exportData = exportService.exportEmails(emailType, format);
        String filename = generateFilename("emails", emailType, format);

        return buildDownloadResponse(exportData, filename, format);
    }

    /**
     * Handles HTTP GET requests for downloading a single user's data in the specified format.
     *
     * <p>
     * This endpoint exports all data associated with a specific user identified by their UUID.
     * The export includes all user profile information, settings, and related data in the
     * requested format (e.g., XLSX, PDF, XML, JSON).
     * </p>
     *
     * @param userId the UUID of the user to export; must not be {@code null}.
     * @param format the desired output file format (e.g., {@code "xlsx"}, {@code "pdf"},
     *               {@code "xml"}, {@code "json"}); must not be {@code null}.
     *
     * @return a {@link ResponseEntity} containing the exported user data as a downloadable attachment,
     *         with the appropriate content type and filename headers applied.
     *
     * @throws IllegalArgumentException if the provided {@code userId} is invalid or {@code format} is not supported.
     *
     * @author Philipp Borkovic
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<byte[]> downloadUser(
            @PathVariable UUID userId,
            @RequestParam String format
    ) {
        logger.info("Download user: userId={}, format={}", userId, format);

        byte[] exportData = exportService.exportUser(userId, format);

        String filename = String.format("user_%s_%s.%s",
                userId.toString().substring(0, 8),
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                format);

        return buildDownloadResponse(exportData, filename, format);
    }

    /**
     * Handles HTTP GET requests for downloading a single booking's data in the specified format.
     *
     * <p>
     * This endpoint exports all data associated with a specific booking identified by its UUID.
     * The export includes booking details, amounts, dates, and associated user information in the
     * requested format (e.g., XLSX, PDF, XML, JSON).
     * </p>
     *
     * @param bookingId the UUID of the booking to export; must not be {@code null}.
     * @param format    the desired output file format (e.g., {@code "xlsx"}, {@code "pdf"},
     *                  {@code "xml"}, {@code "json"}); must not be {@code null}.
     *
     * @return a {@link ResponseEntity} containing the exported booking data as a downloadable attachment,
     *         with the appropriate content type and filename headers applied.
     *
     * @throws IllegalArgumentException if the provided {@code bookingId} is invalid or {@code format} is not supported.
     *
     * @author Philipp Borkovic
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<byte[]> downloadBooking(
            @PathVariable UUID bookingId,
            @RequestParam String format
    ) {
        logger.info("Download booking: bookingId={}, format={}", bookingId, format);

        byte[] exportData = exportService.exportBooking(bookingId, format);

        String filename = String.format("booking_%s_%s.%s",
                bookingId.toString().substring(0, 8),
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                format);

        return buildDownloadResponse(exportData, filename, format);
    }

    /**
     * Builds a standardized HTTP download response containing the exported file data.
     *
     * <p>
     * This method constructs a {@link ResponseEntity} configured for file download by setting the
     * appropriate <i>Content-Disposition</i> and <i>Content-Type</i> headers, and embedding the
     * binary export content as the response body.
     * </p>
     *
     * @param data     the binary content of the exported file; must not be {@code null}.
     * @param filename the filename to assign to the downloaded file (including extension);
     *                 must not be {@code null}.
     * @param format   the file format or extension (e.g., {@code "csv"}, {@code "pdf"}, {@code "xlsx"});
     *                 must not be {@code null}.
     *
     * @return a {@link ResponseEntity} configured to prompt a file download in the client browser,
     *         containing the specified export data and headers.
     *
     * @throws NullPointerException if {@code data}, {@code filename}, or {@code format} is {@code null}.
     *
     * @author Philipp Borkovic
     */
    private ResponseEntity<byte[]> buildDownloadResponse(byte[] data, String filename, String format) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(getContentType(format))
                .body(data);
    }

    /**
     * Generates a standardized filename for an exported file based on the provided category, type, and format.
     *
     * <p>
     * The filename follows the convention:
     * <pre>{@code
     *   <category>_<type>_<yyyy-MM-dd>.<format>
     * }</pre>
     * </p>
     *
     * <p>
     * The current system date (in ISO-8601 format) is automatically appended to ensure
     * file uniqueness and traceability of export timestamps. All components are concatenated
     * using underscores for consistency and filesystem safety.
     * </p>
     *
     * @param category a high-level identifier for the export domain (e.g., {@code "users"}, {@code "bookings"}, {@code "emails"});
     *                 must not be {@code null}.
     * @param type     a specific sub-category or record type within the domain (e.g., {@code "admin"}, {@code "confirmed"});
     *                 must not be {@code null}.
     * @param format   the output file format or extension (e.g., {@code "csv"}, {@code "json"}, {@code "pdf"});
     *                 must not be {@code null}.
     *
     * @return a formatted filename string representing the export, never {@code null}.
     *         Example: {@code "bookings_confirmed_2025-03-05.xlsx"}.
     *
     * @throws NullPointerException if any argument is {@code null}.
     *
     * @author Philipp Borkovic
     */
    private String generateFilename(String category, String type, String format) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        return String.format("%s_%s_%s.%s", category, type, date, format);
    }

    /**
     * Resolves the appropriate {@link MediaType} for a given export format string.
     * <p>
     *
     * This method maps a provided format identifier (such as {@code "pdf"}, {@code "json"}, or {@code "xlsx"})
     * to its corresponding {@link MediaType}. The result is used to set the HTTP
     * <i>Content-Type</i> header for export responses, ensuring that clients interpret
     * the exported file correctly.
     * </p>
     *
     * <table border="1" cellpadding="4" cellspacing="0">
     *   <caption>Supported format mappings</caption>
     *   <tr><th>Format</th><th>Returned MediaType</th></tr>
     *   <tr><td>{@code xlsx}</td><td>{@code application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}</td></tr>
     *   <tr><td>{@code pdf}</td><td>{@code application/pdf}</td></tr>
     *   <tr><td>{@code xml}</td><td>{@code application/xml}</td></tr>
     *   <tr><td>{@code json}</td><td>{@code application/json}</td></tr>
     *   <tr><td>other / unknown</td><td>{@code application/octet-stream}</td></tr>
     * </table>
     *
     * @param format the desired export format, such as {@code "pdf"}, {@code "xlsx"}, or {@code "json"};
     *               must not be {@code null}.
     * @return the corresponding {@link MediaType} constant for the specified format;
     *         {@link MediaType#APPLICATION_OCTET_STREAM} if the format is unrecognized.
     *
     * @throws NullPointerException if {@code format} is {@code null}.
     *
     * @see MediaType
     *
     * @author Philipp Borkovic
     */
    private MediaType getContentType(String format) {
        return switch (format.toLowerCase()) {
            case "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "xml" -> MediaType.APPLICATION_XML;
            case "json" -> MediaType.APPLICATION_JSON;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
