package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.service.ExportService;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST endpoint controller responsible for handling data export operations across various domains.
 *
 * <p>
 * The {@code ExportController} provides endpoints for exporting different categories of data —
 * including users, bookings, and emails — into a variety of output formats such as CSV, JSON, or XML.
 * Each export operation delegates its core logic to the {@link ExportService}, which handles
 * data retrieval, transformation, and serialization.
 * </p>
 *
 * @see ExportService
 * @see org.slf4j.Logger
 * @see jakarta.annotation.security.PermitAll
 */
@Endpoint
@PermitAll
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Exports user data of the specified type into the given format.
     * <p>
     * This method triggers the export process for a particular category of users (e.g.,
     * administrators, customers, guests) and returns a confirmation message indicating
     * the success of the operation, along with the size of the generated export data.
     * </p>
     *
     * @param userType the type of users to export; must not be {@code null}.
     *                 Typical values might include {@code "admin"}, {@code "customer"}, or {@code "guest"}.
     * @param format   the desired export format; must not be {@code null}.
     *                 Supported formats may include {@code "csv"}, {@code "json"}, or {@code "xml"}.
     *
     * @return a human-readable message confirming the export operation and reporting
     *         the total number of bytes generated, e.g. {@code "Export successful - 1024 bytes generated"}.
     *
     * @throws IllegalArgumentException if the provided {@code userType} or {@code format} is invalid
     *                                  or not supported by the export service.
     *
     * @see ExportService#exportUsers(String, String)
     *
     * @author Philipp Borkovic
     */
    @Nonnull
    public String exportUsers(@Nonnull String userType, @Nonnull String format) {
        logger.info("Export users request: userType={}, format={}", userType, format);

        byte[] exportData = exportService.exportUsers(userType, format);

        return "Export successful - " + exportData.length + " bytes generated";
    }

    /**
     * Exports booking data of the specified type and format, optionally filtered by a date range.
     *
     * <p>
     * This method initiates an export operation for bookings (e.g., confirmed, pending, or cancelled)
     * and returns a confirmation message indicating successful completion and the size of the
     * generated export data. The export process is delegated to
     * {@code exportService.exportBookings(String, String, LocalDate, LocalDate)}.
     * </p>
     *
     * @param bookingType the category of bookings to export; must not be {@code null}.
     *                    Typical values might include {@code "confirmed"}, {@code "pending"}, or {@code "cancelled"}.
     * @param format      the desired export format; must not be {@code null}.
     *                    Common values include {@code "csv"}, {@code "json"}, or {@code "xml"}.
     * @param startDate   the start date of the booking filter range; may be {@code null} to include all dates.
     * @param endDate     the end date of the booking filter range; may be {@code null} to include all dates.
     *
     * @return a human-readable message confirming that the export operation completed successfully
     *         and reporting the total number of bytes generated, e.g.
     *         {@code "Export successful - 4096 bytes generated"}.
     *
     * @throws IllegalArgumentException if {@code bookingType} or {@code format} is invalid or unsupported.
     *
     * @see ExportService#exportBookings(String, String, LocalDate, LocalDate)
     *
     * @author Philipp Borkovic
     */
    @Nonnull
    public String exportBookings(
            @Nonnull String bookingType,
            @Nonnull String format,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate
    ) {
        logger.info("Export bookings request: bookingType={}, format={}, startDate={}, endDate={}",
                bookingType, format, startDate, endDate);

        byte[] exportData = exportService.exportBookings(bookingType, format, startDate, endDate);

        return "Export successful - " + exportData.length + " bytes generated";
    }

    /**
     * Exports email records of the specified type into the desired format.
     * <p>
     * This method initiates an export process for a particular category of email data (such as
     * notifications, newsletters, or transactional messages) and returns a confirmation message
     * indicating successful completion along with the total size of the generated export data.
     * </p>i
     *
     * @param emailType the category of email data to export; must not be {@code null}.
     *                  Common values may include {@code "notification"}, {@code "newsletter"},
     *                  or {@code "transactional"}.
     * @param format    the target output format; must not be {@code null}.
     *                  Supported formats usually include {@code "csv"}, {@code "json"}, or {@code "xml"}.
     *
     * @return a human-readable confirmation message indicating that the export operation
     *         completed successfully and showing the total number of bytes generated,
     *         for example: {@code "Export successful - 512 bytes generated"}.
     *
     * @throws IllegalArgumentException if {@code emailType} or {@code format} is invalid
     *                                  or unsupported by the export service.
     *
     * @see ExportService#exportEmails(String, String)
     *
     * @author Philipp Borkovic
     */
    @Nonnull
    public String exportEmails(@Nonnull String emailType, @Nonnull String format) {
        logger.info("Export emails request: emailType={}, format={}", emailType, format);

        byte[] exportData = exportService.exportEmails(emailType, format);

        return "Export successful - " + exportData.length + " bytes generated";
    }

    /**
     * Exports a single user's data in the specified format.
     *
     * <p>
     * This method exports all data associated with a specific user identified by their UUID.
     * The export includes all user profile information, settings, and related data.
     * </p>
     *
     * @param userId the UUID of the user to export; must not be {@code null}.
     * @param format the desired export format; must not be {@code null}.
     *               Supported formats include {@code "xlsx"}, {@code "csv"}, {@code "json"}, or {@code "xml"}.
     *
     * @return a human-readable message confirming the export operation and reporting
     *         the total number of bytes generated, e.g. {@code "Export successful - 2048 bytes generated"}.
     *
     * @throws IllegalArgumentException if the provided {@code userId} is invalid or {@code format} is not supported.
     *
     * @see ExportService#exportUser(UUID, String)
     *
     * @author Philipp Borkovic
     */
    @Nonnull
    public String exportUser(@Nonnull UUID userId, @Nonnull String format) {
        logger.info("Export user request: userId={}, format={}", userId, format);

        byte[] exportData = exportService.exportUser(userId, format);

        return "Export successful - " + exportData.length + " bytes generated";
    }

    /**
     * Exports a single booking's data in the specified format.
     *
     * <p>
     * This method exports all data associated with a specific booking identified by its UUID.
     * The export includes booking details, amounts, dates, and associated user information.
     * </p>
     *
     * @param bookingId the UUID of the booking to export; must not be {@code null}.
     * @param format    the desired export format; must not be {@code null}.
     *                  Supported formats include {@code "xlsx"}, {@code "csv"}, {@code "json"}, or {@code "xml"}.
     *
     * @return a human-readable message confirming the export operation and reporting
     *         the total number of bytes generated, e.g. {@code "Export successful - 1536 bytes generated"}.
     *
     * @throws IllegalArgumentException if the provided {@code bookingId} is invalid or {@code format} is not supported.
     *
     * @see ExportService#exportBooking(UUID, String)
     *
     * @author Philipp Borkovic
     */
    @Nonnull
    public String exportBooking(@Nonnull UUID bookingId, @Nonnull String format) {
        logger.info("Export booking request: bookingId={}, format={}", bookingId, format);

        byte[] exportData = exportService.exportBooking(bookingId, format);

        return "Export successful - " + exportData.length + " bytes generated";
    }
}
