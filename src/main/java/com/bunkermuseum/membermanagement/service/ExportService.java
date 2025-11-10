package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.repository.contract.EmailRepositoryContract;
import com.bunkermuseum.membermanagement.service.contract.ExportServiceContract;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Service implementation for handling data export functionality.
 *
 * <p>This service provides methods to export users, bookings, and emails in various
 * formats including Excel (XLSX), PDF, XML, and JSON. The service uses Apache POI for
 * Excel generation, Jackson for JSON serialization, and Java DOM for XML generation.</p>
 *
 * <h3>Export Process:</h3>
 * <ol>
 *   <li>Filter data based on specified criteria (type, date range)</li>
 *   <li>Convert data to requested format (xlsx, pdf, xml, json)</li>
 *   <li>Return byte array for download</li>
 * </ol>
 *
 * <h3>Supported Export Formats:</h3>
 * <ul>
 *   <li><code>xlsx</code> - Excel 2007+ format (.xlsx)</li>
 *   <li><code>pdf</code> - Portable Document Format (.pdf)</li>
 *   <li><code>xml</code> - Extensible Markup Language (.xml)</li>
 *   <li><code>json</code> - JavaScript Object Notation (.json)</li>
 * </ul>
 *
 */
@Service
@Transactional(readOnly = true)
public class ExportService implements ExportServiceContract {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final UserRepositoryContract userRepository;
    private final EmailRepositoryContract emailRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new ExportService.
     *
     * <p>Initializes the Jackson ObjectMapper with Java Time support and pretty printing
     * for stable, human-readable JSON output. The service is stateless and thread-safe
     * regarding read-only operations; it relies on repository contracts for data access.</p>
     *
     * @param userRepository repository used to read users for exports; must not be null
     * @param emailRepository repository used to read emails for exports; must not be null
     *
     */
    public ExportService(
            UserRepositoryContract userRepository,
            EmailRepositoryContract emailRepository
    ) {
        this.userRepository = userRepository;
        this.emailRepository = emailRepository;

        // Configure ObjectMapper for JSON exports
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public byte @NonNull [] exportUsers(@NonNull String userType, @NonNull String format) {
        log.info("Exporting users: type={}, format={}", userType, format);

        List<User> users = filterUsersByType(userType);

        return switch (format.toLowerCase()) {
            case "xlsx" -> exportUsersToExcel(users);
            case "pdf" -> exportUsersToPdf(users);
            case "xml" -> exportUsersToXml(users);
            case "json" -> exportUsersToJson(users);
            default -> throw new IllegalArgumentException("Unsupported export format: " + format);
        };
    }

    /**
     * {@inheritDoc}}
     *
     * @author Philipp Borkovic
     */
    @Override
    public byte @NonNull [] exportBookings(
            @NonNull String bookingType,
            @NonNull String format,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate
    ) {
        log.info("Exporting bookings: type={}, format={}, startDate={}, endDate={}",
                bookingType, format, startDate, endDate);

        List<BookingDTO> bookings = filterBookingsByType(bookingType, startDate, endDate);

        return switch (format.toLowerCase()) {
            case "xlsx" -> exportBookingsToExcel(bookings);
            case "pdf" -> exportBookingsToPdf(bookings);
            case "xml" -> exportBookingsToXml(bookings);
            case "json" -> exportBookingsToJson(bookings);
            default -> throw new IllegalArgumentException("Unsupported export format: " + format);
        };
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public byte @NonNull [] exportEmails(@NonNull String emailType, @NonNull String format) {
        log.info("Exporting emails: type={}, format={}", emailType, format);

        List<Email> emails = filterEmailsByType(emailType);

        return switch (format.toLowerCase()) {
            case "xlsx" -> exportEmailsToExcel(emails);
            case "pdf" -> exportEmailsToPdf(emails);
            case "xml" -> exportEmailsToXml(emails);
            case "json" -> exportEmailsToJson(emails);
            default -> throw new IllegalArgumentException("Unsupported export format: " + format);
        };
    }


    /**
     * Filters users by type.
     *
     * <p>Accepted values for {@code userType} (case-insensitive): "all", "ordentlich",
     * "foerdernd", "ausgetreten". The filtering currently uses the {@code deletedAt}
     * timestamp only; supporting members are treated like active users until member type
     * information is available.</p>
     *
     * @param userType the category of users to include
     * @return the filtered list of users (never null)
     * @throws IllegalArgumentException if {@code userType} is unsupported
     */
    private List<User> filterUsersByType(String userType) {
        List<User> allUsers = userRepository.findAll();

        return switch (userType.toLowerCase()) {
            case "all" -> allUsers;
            case "ordentlich" -> allUsers.stream()
                    .filter(user -> user.deletedAt() == null)
                    .toList(); // Regular members (active users)
            case "foerdernd" -> allUsers.stream()
                    .filter(user -> user.deletedAt() == null)
                    .toList(); // Supporting members (TODO: add proper filtering by member type)
            case "ausgetreten" -> allUsers.stream()
                    .filter(user -> user.deletedAt() != null)
                    .toList(); // Former members (deleted users)
            default -> throw new IllegalArgumentException("Unsupported user type: " + userType);
        };
    }

    /**
     * Generates an Excel workbook representing a list of {@link User} objects and returns it as a byte array.
     *
     * <p>
     * The resulting Excel file contains a sheet named {@code "Users"} with the following columns:
     * <ul>
     *     <li>ID</li>
     *     <li>Name</li>
     *     <li>Email</li>
     *     <li>Email Verified</li>
     *     <li>Phone</li>
     *     <li>City</li>
     *     <li>Country</li>
     *     <li>Created At</li>
     * </ul>
     * Boolean values, such as email verification status, are rendered as {@code "Yes"} or {@code "No"}.
     * Null values in the {@link User} objects are replaced with empty strings to ensure valid Excel content.
     * </p>
     *
     * @param users the list of {@link User} objects to include in the Excel sheet; must not be {@code null}.
     *
     * @return a byte array containing the generated Excel workbook; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during Excel workbook creation or writing.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportUsersToExcel(List<User> users) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Name", "Email", "Email Verified", "Phone", "City", "Country", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId() != null ? user.getId().toString() : "");
                row.createCell(1).setCellValue(user.getName() != null ? user.getName() : "");
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(user.getEmailVerifiedAt() != null ? "Yes" : "No");
                row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(5).setCellValue(user.getCity() != null ? user.getCity() : "");
                row.createCell(6).setCellValue(user.getCountry() != null ? user.getCountry() : "");
                row.createCell(7).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return out.toByteArray();
        } catch (Exception exception) {
            log.error("Error exporting users to Excel", exception);

            throw new RuntimeException("Failed to export users to Excel", exception);
        }
    }

    /**
     * Generates a PDF document representing a list of {@link User} objects and returns it as a byte array.
     *
     * <p>
     * The generated PDF contains a title ("User Export") and a table with the following columns:
     * <ul>
     *     <li>Name</li>
     *     <li>Email</li>
     *     <li>Phone</li>
     *     <li>City</li>
     * </ul>
     * Long text values are truncated to ensure they fit within their respective columns.
     * The table automatically spans multiple pages if the number of users exceeds the space available on a single page.
     * </p>
     *
     * @param users the list of {@link User} objects to include in the PDF; must not be {@code null}.
     *
     * @return a byte array containing the generated PDF; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during PDF generation or writing.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportUsersToPdf(List<User> users) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20f;

            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("User Export");
            contentStream.endText();
            yPosition -= 30;

            String[] headers = {"Name", "Email", "Phone", "City"};
            float[] columnWidths = {tableWidth * 0.25f, tableWidth * 0.35f, tableWidth * 0.20f, tableWidth * 0.20f};

            contentStream.setFont(headerFont, 10);
            float xPosition = margin;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
                xPosition += columnWidths[i];
            }
            yPosition -= rowHeight;

            contentStream.setFont(bodyFont, 9);
            int rowCount = 0;
            for (User user : users) {
                if (yPosition < margin + rowHeight) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(bodyFont, 9);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                xPosition = margin;
                String[] rowData = {
                    truncate(user.getName(), 20),
                    truncate(user.getEmail(), 30),
                    truncate(user.getPhone(), 15),
                    truncate(user.getCity(), 15)
                };

                for (int i = 0; i < rowData.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    contentStream.showText(rowData[i] != null ? rowData[i] : "");
                    contentStream.endText();
                    xPosition += columnWidths[i];
                }
                yPosition -= rowHeight;
                rowCount++;
            }

            contentStream.close();
            document.save(out);

            return out.toByteArray();
        } catch (IOException exception) {
            log.error("Error exporting users to PDF", exception);

            throw new RuntimeException("Failed to export users to PDF", exception);
        }
    }

    /**
     * Serializes a list of {@link User} objects into an XML byte array.
     *
     * <p>
     * This method constructs an XML document in memory with a root element named {@code <users>},
     * containing individual {@code <user>} elements for each user in the provided list. Each user
     * element includes the following child elements:
     * <ul>
     *     <li>id</li>
     *     <li>name</li>
     *     <li>email</li>
     *     <li>emailVerified</li>
     *     <li>phone</li>
     *     <li>city</li>
     *     <li>country</li>
     *     <li>createdAt</li>
     * </ul>
     * Null values are safely converted to empty strings to maintain XML validity.
     * </p>
     *
     * @param users the list of {@link User} objects to serialize; must not be {@code null}.
     *
     * @return a byte array containing the XML representation of the provided users; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during XML document creation or serialization.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportUsersToXml(List<User> users) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("users");
            doc.appendChild(rootElement);

            for (User user : users) {
                Element userElement = doc.createElement("user");
                rootElement.appendChild(userElement);

                appendElement(doc, userElement, "id", user.getId() != null ? user.getId().toString() : "");
                appendElement(doc, userElement, "name", user.getName());
                appendElement(doc, userElement, "email", user.getEmail());
                appendElement(doc, userElement, "emailVerified", user.getEmailVerifiedAt() != null ? "true" : "false");
                appendElement(doc, userElement, "phone", user.getPhone());
                appendElement(doc, userElement, "city", user.getCity());
                appendElement(doc, userElement, "country", user.getCountry());
                appendElement(doc, userElement, "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            }

            return documentToByteArray(doc);
        } catch (Exception exception) {
            log.error("Error exporting users to XML", exception);

            throw new RuntimeException("Failed to export users to XML", exception);
        }
    }

    /**
     * Serializes the given users to JSON using Jackson.
     *
     * <p>Dates are written in ISO-8601 format and output is pretty-printed.</p>
     *
     * @param users the users to serialize
     *
     * @return JSON document as a byte array (UTF-8)
     *
     * @throws RuntimeException if JSON serialization fails
     *
     * @author Philipp Borkovic
     */
    private byte[] exportUsersToJson(List<User> users) {
        try {
            return objectMapper.writeValueAsBytes(users);
        } catch (Exception exception) {
            log.error("Error exporting users to JSON", exception);

            throw new RuntimeException("Failed to export users to JSON", exception);
        }
    }


    /**
     * Filters bookings by type and optional date range.
     *
     * <p>This is a placeholder implementation. Expected behavior:
     * <ul>
     *   <li>"all" — return all bookings</li>
     *   <li>"today", "yesterday" — filter by creation date</li>
     *   <li>"this_week", "last_week" — filter by ISO week</li>
     *   <li>"custom" — filter inclusively between {@code startDate} and {@code endDate}</li>
     * </ul>
     * If {@code bookingType} is "custom", both dates should be provided.</p>
     *
     * @param bookingType the booking category selector
     * @param startDate start of range, required for "custom"
     * @param endDate end of range, required for "custom"
     * @return the filtered list of bookings
     */
    private List<BookingDTO> filterBookingsByType(String bookingType, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement proper filtering based on booking type and date range
        return List.of();
    }

    /**
     * Generates an Excel (XLSX) spreadsheet representing a list of {@link BookingDTO} objects and returns it as a byte array.
     *
     * <p>
     * This method creates a workbook with a single sheet named "Bookings" containing the following columns:
     * <ul>
     *     <li>ID</li>
     *     <li>Code</li>
     *     <li>MG</li>
     *     <li>Purpose</li>
     *     <li>Amount</li>
     *     <li>Received At</li>
     *     <li>Status (Assigned or Open)</li>
     * </ul>
     * Column headers are bolded for clarity. Each booking in the provided list is added as a row with its respective values.
     * Columns are auto-sized for readability.
     * </p>
     *
     * @param bookings the list of {@link BookingDTO} objects to include in the Excel spreadsheet; must not be {@code null}.
     *
     * @return a byte array representing the generated XLSX file; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during Excel workbook creation or writing.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportBookingsToExcel(List<BookingDTO> bookings) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Code", "MG", "Purpose", "Amount", "Received At", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (BookingDTO booking : bookings) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(booking.getId() != null ? booking.getId().toString() : "");
                row.createCell(1).setCellValue(booking.getCode() != null ? booking.getCode() : "");
                row.createCell(2).setCellValue(booking.getOfMG() != null ? booking.getOfMG() : "");
                row.createCell(3).setCellValue(booking.getExpectedPurpose() != null ? booking.getExpectedPurpose() : "");
                row.createCell(4).setCellValue(booking.getExpectedAmount() != null ? booking.getExpectedAmount().toString() : "");
                row.createCell(5).setCellValue(booking.getReceivedAt() != null ? booking.getReceivedAt().toString() : "");
                row.createCell(6).setCellValue(booking.getUserId() != null ? "Assigned" : "Open");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return out.toByteArray();
        } catch (Exception exception) {
            log.error("Error exporting bookings to Excel", exception);

            throw new RuntimeException("Failed to export bookings to Excel", exception);
        }
    }

    /**
     * Generates a PDF document representing a list of {@link BookingDTO} objects and returns it as a byte array.
     *
     * <p>
     * The PDF includes a title and a table summarizing key booking details:
     * <ul>
     *     <li>Code</li>
     *     <li>MG</li>
     *     <li>Purpose</li>
     *     <li>Amount</li>
     * </ul>
     * The table is formatted to fit standard A4 pages, and automatic page breaks are applied if
     * the content exceeds the available page height.
     * </p>
     *
     * @param bookings the list of {@link BookingDTO} objects to include in the PDF; must not be {@code null}.
     *
     * @return a byte array representing the generated PDF document; never {@code null}.
     *
     * @throws RuntimeException if an {@link IOException} occurs during PDF generation or saving.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportBookingsToPdf(List<BookingDTO> bookings) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20f;

            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Booking Export");
            contentStream.endText();
            yPosition -= 30;

            String[] headers = {"Code", "MG", "Purpose", "Amount"};
            float[] columnWidths = {tableWidth * 0.20f, tableWidth * 0.20f, tableWidth * 0.40f, tableWidth * 0.20f};

            contentStream.setFont(headerFont, 10);
            float xPosition = margin;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
                xPosition += columnWidths[i];
            }
            yPosition -= rowHeight;

            contentStream.setFont(bodyFont, 9);
            for (BookingDTO booking : bookings) {
                if (yPosition < margin + rowHeight) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(bodyFont, 9);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                xPosition = margin;
                String[] rowData = {
                    truncate(booking.getCode(), 15),
                    truncate(booking.getOfMG(), 15),
                    truncate(booking.getExpectedPurpose(), 35),
                    booking.getExpectedAmount() != null ? booking.getExpectedAmount().toString() : ""
                };

                for (int i = 0; i < rowData.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    contentStream.showText(rowData[i] != null ? rowData[i] : "");
                    contentStream.endText();
                    xPosition += columnWidths[i];
                }
                yPosition -= rowHeight;
            }

            contentStream.close();
            document.save(out);
            return out.toByteArray();
        } catch (IOException exception) {
            log.error("Error exporting bookings to PDF", exception);

            throw new RuntimeException("Failed to export bookings to PDF", exception);
        }
    }

    /**
     * Serializes a list of {@link BookingDTO} objects into an XML byte array.
     *
     * <p>
     * This method uses a configured {@link com.fasterxml.jackson.databind.ObjectMapper}
     * (or a specialized XML mapper) to convert the provided list of bookings into XML format
     * suitable for export or download.
     * </p>
     *
     * @param bookings the list of {@link BookingDTO} objects to serialize; must not be {@code null}.
     *
     * @return a byte array containing the XML representation of the provided bookings; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during XML serialization.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportBookingsToXml(List<BookingDTO> bookings) {
        try {
            return objectMapper.writeValueAsBytes(bookings);
        } catch (Exception exception) {
            log.error("Error exporting bookings to XML", exception);

            throw new RuntimeException("Failed to export bookings to XML", exception);
        }
    }

    /**
     * Serializes a list of {@link BookingDTO} objects into a JSON byte array.
     * <p>
     * This method uses a configured {@link com.fasterxml.jackson.databind.ObjectMapper} instance
     * to convert the provided list of bookings into JSON format suitable for export or download.
     * </p>
     *
     * @param bookings the list of {@link BookingDTO} objects to serialize; must not be {@code null}.
     *
     * @return a byte array containing the JSON representation of the provided bookings; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during JSON serialization.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportBookingsToJson(List<BookingDTO> bookings) {
        try {
            return objectMapper.writeValueAsBytes(bookings);
        } catch (Exception exception) {
            log.error("Error exporting bookings to JSON", exception);

            throw new RuntimeException("Failed to export bookings to JSON", exception);
        }
    }

    /**
     * Filters emails by type.
     *
     * <p>Accepted values for {@code emailType} (case-insensitive):
     * <ul>
     *   <li>"system" — emails generated by the system (no user association)</li>
     *   <li>"user" — emails authored/sent by users (has a user association)</li>
     * </ul>
     *
     * @param emailType the category of emails to include
     *
     * @return the filtered list of emails (never null)
     *
     * @throws IllegalArgumentException if {@code emailType} is unsupported
     *
     * @author Philipp Borkovic
     */
    private List<Email> filterEmailsByType(String emailType) {
        List<Email> allEmails = emailRepository.findAll();

        return switch (emailType.toLowerCase()) {
            case "system" -> allEmails.stream()
                    .filter(Email::isSystemEmail)
                    .toList(); // System-generated emails (no user association)
            case "user" -> allEmails.stream()
                    .filter(Email::isUserEmail)
                    .toList(); // User-sent emails (has user association)
            default -> throw new IllegalArgumentException("Unsupported email type: " + emailType);
        };
    }

    /**
     * Generates an Excel (XLSX) spreadsheet representing a list of {@link Email} objects and returns it as a byte array.
     *
     * <p>
     * This method creates a workbook with a single sheet named "Emails" containing the following columns:
     * <ul>
     *     <li>ID</li>
     *     <li>Subject</li>
     *     <li>Recipient</li>
     *     <li>Sent At</li>
     *     <li>Status (Sent or Deleted)</li>
     * </ul>
     * Column headers are bolded for clarity. Each email in the provided list is added as a row with its respective values.
     * Columns are auto-sized for readability.
     * </p>
     *
     * @param emails the list of {@link Email} objects to include in the Excel spreadsheet; must not be {@code null}.
     *
     * @return a byte array representing the generated XLSX file; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during Excel workbook creation or writing.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportEmailsToExcel(List<Email> emails) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Emails");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Subject", "Recipient", "Sent At", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Email email : emails) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(email.getId() != null ? email.getId().toString() : "");
                row.createCell(1).setCellValue(email.getSubject() != null ? email.getSubject() : "");
                row.createCell(2).setCellValue(email.getToAddress() != null ? email.getToAddress() : "");
                row.createCell(3).setCellValue(email.getCreatedAt() != null ? email.getCreatedAt().toString() : "");
                row.createCell(4).setCellValue(email.deletedAt() == null ? "Sent" : "Deleted");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception exception) {
            log.error("Error exporting emails to Excel", exception);

            throw new RuntimeException("Failed to export emails to Excel", exception);
        }
    }

    /**
     * Generates a PDF document representing a list of {@link Email} objects and returns it as a byte array.
     *
     * <p>
     * This method creates a PDF file with a title and a table summarizing email details, including:
     * <ul>
     *     <li>Subject</li>
     *     <li>Recipient</li>
     *     <li>Sent Date</li>
     *     <li>Status (Sent or Deleted)</li>
     * </ul>
     * The PDF is formatted to fit standard A4 pages, with automatic page breaks if the content exceeds
     * the page height.
     * </p>
     *
     * @param emails the list of {@link Email} objects to include in the PDF; must not be {@code null}.
     *
     * @return a byte array representing the generated PDF document; never {@code null}.
     *
     * @throws RuntimeException if an {@link IOException} occurs during PDF generation or saving.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportEmailsToPdf(List<Email> emails) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20f;

            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Email Export");
            contentStream.endText();
            yPosition -= 30;

            String[] headers = {"Subject", "Recipient", "Sent At", "Status"};
            float[] columnWidths = {tableWidth * 0.40f, tableWidth * 0.30f, tableWidth * 0.20f, tableWidth * 0.10f};

            contentStream.setFont(headerFont, 10);
            float xPosition = margin;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(headers[i]);
                contentStream.endText();
                xPosition += columnWidths[i];
            }
            yPosition -= rowHeight;

            contentStream.setFont(bodyFont, 9);
            for (Email email : emails) {
                if (yPosition < margin + rowHeight) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(bodyFont, 9);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                xPosition = margin;
                String[] rowData = {
                    truncate(email.getSubject(), 35),
                    truncate(email.getToAddress(), 25),
                    email.getCreatedAt() != null ? email.getCreatedAt().toString().substring(0, 10) : "",
                    email.deletedAt() == null ? "Sent" : "Deleted"
                };

                for (int i = 0; i < rowData.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    contentStream.showText(rowData[i] != null ? rowData[i] : "");
                    contentStream.endText();
                    xPosition += columnWidths[i];
                }
                yPosition -= rowHeight;
            }

            contentStream.close();
            document.save(out);
            return out.toByteArray();
        } catch (IOException exception) {
            log.error("Error exporting emails to PDF", exception);

            throw new RuntimeException("Failed to export emails to PDF", exception);
        }
    }

    /**
     * Serializes emails to XML and returns bytes.
     *
     * <p>Currently implemented via Jackson {@code ObjectMapper}. Note: without the Jackson XML module,
     * this will produce JSON bytes instead of XML. Consider replacing with a DOM-based XML builder or
     * configuring the XML module if true XML is required.</p>
     *
     * @param emails the emails to serialize
     * @return XML (or JSON, see note) document as a byte array
     *
     * @throws RuntimeException if serialization fails
     *
     * @author Philipp Borkovic
     */
    private byte[] exportEmailsToXml(List<Email> emails) {
        try {
            return objectMapper.writeValueAsBytes(emails);
        } catch (Exception e) {
            log.error("Error exporting emails to XML", e);
            throw new RuntimeException("Failed to export emails to XML", e);
        }
    }

    /**
     * Serializes a list of {@link Email} objects into a JSON byte array.
     *
     * <p>
     * This method uses a configured {@link com.fasterxml.jackson.databind.ObjectMapper} instance
     * to convert the provided list of emails into JSON format suitable for export or download.
     * </p>
     *
     * @param emails the list of {@link Email} objects to serialize; must not be {@code null}.
     *
     * @return a byte array containing the JSON representation of the provided emails; never {@code null}.
     *
     * @throws RuntimeException if an error occurs during JSON serialization.
     *
     * @author Philipp Borkovic
     */
    private byte[] exportEmailsToJson(List<Email> emails) {
        try {
            return objectMapper.writeValueAsBytes(emails);
        } catch (Exception exception) {
            log.error("Error exporting emails to JSON", exception);

            throw new RuntimeException("Failed to export emails to JSON", exception);
        }
    }

    /**
     * Appends a child element with a text value to a specified parent element in an XML document.
     *
     * <p>
     * This method creates a new {@link Element} with the given {@code name}, sets its text content
     * to the provided {@code value} (or an empty string if {@code value} is {@code null}),
     * and appends it as a child to the specified {@code parent} element.
     * </p>
     *
     * @param doc    the XML {@link Document} in which the element will be created; must not be {@code null}.
     * @param parent the parent {@link Element} to which the new element will be appended; must not be {@code null}.
     * @param name   the tag name of the element to create; must not be {@code null}.
     * @param value  the text content of the element; if {@code null}, an empty string is used.
     *
     * @throws NullPointerException if {@code doc}, {@code parent}, or {@code name} is {@code null}.
     *
     * @author Philipp Borkovic
     */
    private void appendElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);

        element.appendChild(doc.createTextNode(value != null ? value : ""));
        parent.appendChild(element);
    }

    /**
     * Transforms a DOM {@link Document} into a UTF-8 encoded, indented byte array.
     *
     * <p>Uses the platform default {@link TransformerFactory}. The output is human-readable
     * and attempts to indent elements for clarity.</p>
     *
     * @param doc the DOM document to serialize
     *
     * @return UTF-8 encoded XML bytes
     *
     * @throws Exception if transformation fails
     *
     * @author Philipp Borkovic
     */
    private byte[] documentToByteArray(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);

        return out.toByteArray();
    }


    /**
     * Returns a string truncated to {@code maxLength}, appending "..." if truncation occurs.
     *
     * <p>If {@code str} is null, an empty string is returned. If {@code maxLength} is less
     * than 4, the behavior will still return at most {@code maxLength} characters and may
     * not include an ellipsis.</p>
     *
     * @param str the original string; may be null
     * @param maxLength the maximum length of the resulting string
     *
     * @return a possibly truncated string, never null
     *
     * @author Philipp Borkovic
     */
    private String truncate(String str, int maxLength) {
        if (str == null){
            return "";
        }

        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
