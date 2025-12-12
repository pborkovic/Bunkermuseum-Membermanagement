package com.bunkermuseum.membermanagement.lib.validation;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Validates file content integrity using magic bytes (file signatures).
 *
 * <p>This validator ensures that uploaded files are actually the file type they claim to be
 * by checking the file's magic bytes (binary signature) rather than relying solely on
 * MIME type or file extension, which can be easily spoofed.</p>
 *
 * <h3>Security Benefits:</h3>
 * <ul>
 *     <li><strong>OWASP A08:2021:</strong> Software and Data Integrity Failures prevention</li>
 *     <li><strong>File Upload Security:</strong> Prevents malicious file uploads disguised as images</li>
 *     <li><strong>Content Verification:</strong> Validates actual file content, not just metadata</li>
 * </ul>
 *
 * <h3>Supported File Types:</h3>
 * <ul>
 *     <li>JPEG (FF D8 FF)</li>
 *     <li>PNG (89 50 4E 47 0D 0A 1A 0A)</li>
 *     <li>WebP (52 49 46 46 ... 57 45 42 50)</li>
 * </ul>
 */
public class FileContentValidator {
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] WEBP_MAGIC_RIFF = {0x52, 0x49, 0x46, 0x46}; // "RIFF"
    private static final byte[] WEBP_MAGIC_WEBP = {0x57, 0x45, 0x42, 0x50}; // "WEBP" at offset 8

    /**
     * Validates that the file content matches the expected image type.
     *
     * <p>This method reads the file's magic bytes and compares them against known
     * image format signatures to ensure the file is actually an image.</p>
     *
     * @param file The multipart file to validate
     * @return ValidationResult containing validation status and error messages
     *
     * @author Philipp Borkovic
     */
    public static ValidationResult validateImageContent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ValidationResult(false, "File is empty or null");
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileHeader = new byte[12];
            int bytesRead = inputStream.read(fileHeader);

            if (bytesRead < 3) {
                return new ValidationResult(false, "File is too small to be a valid image");
            }

            if (startsWith(fileHeader, JPEG_MAGIC)) {
                return new ValidationResult(true, "Valid JPEG file");
            }

            if (bytesRead >= 8 && startsWith(fileHeader, PNG_MAGIC)) {
                return new ValidationResult(true, "Valid PNG file");
            }

            if (bytesRead >= 12 &&
                    startsWith(fileHeader, WEBP_MAGIC_RIFF) &&
                    Arrays.equals(Arrays.copyOfRange(fileHeader, 8, 12), WEBP_MAGIC_WEBP)) {
                return new ValidationResult(true, "Valid WebP file");
            }

            return new ValidationResult(false,
                    "File content does not match any supported image format (JPEG, PNG, WebP). " +
                    "The file may be corrupted or is not actually an image.");

        } catch (IOException e) {
            return new ValidationResult(false, "Failed to read file content: " + e.getMessage());
        }
    }

    /**
     * Checks if a byte array starts with a given magic byte sequence.
     *
     * @param data The byte array to check
     * @param magic The magic bytes to look for
     * @return true if data starts with magic, false otherwise
     *
     * @author Philipp Borkovic
     */
    private static boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) {
            return false;
        }

        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Result of file content validation.
     *
     * @param isValid True if file content is valid, false otherwise
     * @param message Validation message (error description or success message)
     *
     * @author Philipp Borkovic
     */
    public record ValidationResult(boolean isValid, String message) {}
}
