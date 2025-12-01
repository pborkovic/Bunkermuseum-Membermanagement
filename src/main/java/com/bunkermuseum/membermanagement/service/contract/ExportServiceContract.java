package com.bunkermuseum.membermanagement.service.contract;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service contract for handling data export functionality across different entities.
 *
 * <p>This interface defines methods for exporting users, bookings, and emails in various
 * formats (Excel, PDF, XML, JSON). Each export method supports filtering by specific
 * criteria and returns the exported data as a byte array.</p>
 */
public interface ExportServiceContract {

    /**
     * Exports users according to the requested user type and output format.
     *
     * <p>Supported userType values (case-insensitive):
     * <ul>
     *   <li>"all" — all users regardless of status</li>
     *   <li>"ordentlich" — active members (users without a deletion timestamp)</li>
     *   <li>"foerdernd" — supporting members (currently same filter as "ordentlich")</li>
     *   <li>"ausgetreten" — former members (users with a deletion timestamp)</li>
     * </ul>
     *
     * <p>Supported format values (case-insensitive): "xlsx", "pdf", "xml", "json".</p>
     *
     * @param userType the logical group of users to include in the export
     * @param format the target file format to generate
     *
     * @return the exported document as a byte array, suitable for direct download/streaming
     *
     * @throws IllegalArgumentException if either {@code userType} or {@code format} is unsupported
     */
    byte @NonNull [] exportUsers(@NonNull String userType, @NonNull String format);

    /**
     * Exports bookings according to the requested booking type, date window, and output format.
     *
     * <p>Supported bookingType values (case-insensitive):
     * <ul>
     *   <li>"all" — all bookings</li>
     *   <li>"today" — bookings created today</li>
     *   <li>"yesterday" — bookings created yesterday</li>
     *   <li>"this_week" — bookings from the current ISO week</li>
     *   <li>"last_week" — bookings from the previous ISO week</li>
     *   <li>"custom" — bookings between {@code startDate} and {@code endDate} (inclusive)</li>
     * </ul>
     *
     * <p>Supported format values (case-insensitive): "xlsx", "pdf", "xml", "json".</p>
     *
     * @param bookingType the logical group of bookings to include
     * @param format the target file format to generate
     * @param startDate optional start of the date range (required if {@code bookingType} is "custom")
     * @param endDate optional end of the date range (required if {@code bookingType} is "custom")
     *
     * @return the exported document as a byte array
     *
     * @throws IllegalArgumentException if {@code format} is unsupported or if {@code bookingType} is unsupported
     */
    byte @NonNull [] exportBookings(
        @NonNull String bookingType,
        @NonNull String format,
        @Nullable LocalDate startDate,
        @Nullable LocalDate endDate
    );

    /**
     * Exports emails according to the requested email type and output format.
     *
     * <p>Supported emailType values (case-insensitive):
     * <ul>
     *   <li>"system" — system-generated emails (no user association)</li>
     *   <li>"user" — user-sent emails (have a user association)</li>
     * </ul>
     *
     * <p>Supported format values (case-insensitive): "xlsx", "pdf", "xml", "json".</p>
     *
     * @param emailType the logical group of emails to include
     * @param format the target file format to generate
     *
     * @return the exported document as a byte array
     *
     * @throws IllegalArgumentException if {@code format} is unsupported or if {@code emailType} is unsupported
     */
    byte @NonNull [] exportEmails(@NonNull String emailType, @NonNull String format);

    /**
     * Exports a single user's data in the specified format.
     *
     * <p>Exports all data associated with the specified user including profile information,
     * settings, and related data.</p>
     *
     * <p>Supported format values (case-insensitive): "xlsx", "pdf", "xml", "json".</p>
     *
     * @param userId the UUID of the user to export
     * @param format the target file format to generate
     *
     * @return the exported document as a byte array
     *
     * @throws IllegalArgumentException if {@code format} is unsupported or if {@code userId} is invalid
     */
    byte @NonNull [] exportUser(@NonNull UUID userId, @NonNull String format);

    /**
     * Exports a single booking's data in the specified format.
     *
     * <p>Exports all data associated with the specified booking including amounts, dates,
     * and user information.</p>
     *
     * <p>Supported format values (case-insensitive): "xlsx", "pdf", "xml", "json".</p>
     *
     * @param bookingId the UUID of the booking to export
     * @param format the target file format to generate
     *
     * @return the exported document as a byte array
     *
     * @throws IllegalArgumentException if {@code format} is unsupported or if {@code bookingId} is invalid
     */
    byte @NonNull [] exportBooking(@NonNull UUID bookingId, @NonNull String format);
}
