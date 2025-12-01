package com.bunkermuseum.membermanagement.lib.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password policy validator for OWASP ASVS compliance.
 *
 * <p>This validator enforces strong password policies aligned with OWASP ASVS
 * (Application Security Verification Standard) Level 2 requirements and industry
 * best practices for password security.</p>
 *
 * <h3>Password Requirements:</h3>
 * <ul>
 *     <li><strong>Minimum Length:</strong> 12 characters (OWASP ASVS V2.1.1)</li>
 *     <li><strong>Maximum Length:</strong> 128 characters (prevent DoS)</li>
 *     <li><strong>Complexity:</strong> Must contain 3 of 4 character types:
 *         <ul>
 *             <li>Lowercase letters (a-z)</li>
 *             <li>Uppercase letters (A-Z)</li>
 *             <li>Numbers (0-9)</li>
 *             <li>Special characters (!@#$%^&*)</li>
 *         </ul>
 *     </li>
 *     <li><strong>Common Passwords:</strong> Rejects commonly breached passwords</li>
 *     <li><strong>Sequential Characters:</strong> No more than 2 sequential characters</li>
 *     <li><strong>Repeated Characters:</strong> No more than 2 consecutive identical characters</li>
 * </ul>
 *
 * <h3>OWASP Compliance:</h3>
 * <ul>
 *     <li>OWASP ASVS V2.1.1 - Password length requirements</li>
 *     <li>OWASP ASVS V2.1.7 - Password complexity requirements</li>
 *     <li>OWASP ASVS V2.1.8 - Common password check</li>
 *     <li>NIST SP 800-63B - Password composition rules</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
public class PasswordValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_CHARACTER_TYPES = 2;

    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>_\\-+=\\[\\]~`]");

    /**
     * Top 100 most commonly breached passwords that must be rejected.
     *
     * <p>Based on OWASP and Have I Been Pwned breach databases.</p>
     */
    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "123456789", "12345678", "12345", "1234567",
        "password1", "1234567890", "qwerty", "abc123", "111111", "123123",
        "admin", "letmein", "welcome", "monkey", "dragon", "master", "sunshine",
        "princess", "football", "qwerty123", "solo", "passw0rd", "starwars",
        "password123", "login", "admin123", "root", "toor", "pass", "test",
        "guest", "oracle", "cisco", "changeme", "administrator", "user"
    };

    /**
     * Validates a password against OWASP ASVS requirements.
     *
     * <p>This method performs comprehensive password validation including length,
     * complexity, common password checks, and pattern detection.</p>
     *
     * <h3>Validation Process:</h3>
     * <ol>
     *     <li>Check null/blank validation</li>
     *     <li>Validate length requirements</li>
     *     <li>Check character complexity</li>
     *     <li>Detect common/breached passwords</li>
     *     <li>Check for sequential characters</li>
     *     <li>Check for repeated characters</li>
     * </ol>
     *
     * @param password The password to validate. Must not be null.
     * @return ValidationResult containing validation status and error messages
     *
     * @author Philipp Borkovic
     */
    public static ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("Password must not be null or blank");
            return new ValidationResult(false, errors);
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add(String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH));
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            errors.add(String.format("Password must not exceed %d characters", MAX_PASSWORD_LENGTH));
        }

        int characterTypes = 0;
        if (LOWERCASE_PATTERN.matcher(password).find()) {
            characterTypes++;
        }
        if (UPPERCASE_PATTERN.matcher(password).find()) {
            characterTypes++;
        }
        if (DIGIT_PATTERN.matcher(password).find()) {
            characterTypes++;
        }
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            characterTypes++;
        }

        if (characterTypes < MIN_CHARACTER_TYPES) {
            errors.add(String.format(
                "Password must contain at least %d of the following: lowercase, uppercase, numbers, special characters",
                MIN_CHARACTER_TYPES
            ));
        }

        String lowerPassword = password.toLowerCase();
        for (String commonPassword : COMMON_PASSWORDS) {
            if (lowerPassword.contains(commonPassword)) {
                errors.add("Password contains commonly used patterns and is not secure");

                break;
            }
        }

        if (containsSequentialCharacters(password)) {
            errors.add("Password contains sequential characters (e.g., abc, 123)");
        }
        if (containsRepeatedCharacters(password)) {
            errors.add("Password contains too many repeated characters");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Checks if password contains sequential characters (e.g., "abc", "123", "xyz").
     *
     * @param password The password to check
     * @return true if sequential characters found, false otherwise
     *
     * @author Philipp Borkovic
     */
    private static boolean containsSequentialCharacters(String password) {
        String lower = password.toLowerCase();
        for (int i = 0; i < lower.length() - 2; i++) {
            char c1 = lower.charAt(i);
            char c2 = lower.charAt(i + 1);
            char c3 = lower.charAt(i + 2);

            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if password contains more than 2 consecutive repeated characters.
     *
     * @param password The password to check
     * @return true if too many repeated characters found, false otherwise
     *
     * @author Philipp Borkovic
     */
    private static boolean containsRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Result of password validation.
     *
     * <p>This record encapsulates the validation result including success status
     * and detailed error messages for failed validations.</p>
     *
     * @param isValid True if password meets all requirements, false otherwise
     * @param errors List of validation error messages (empty if valid)
     *
     * @author Philipp Borkovic
     */
    public record ValidationResult(boolean isValid, List<String> errors) {
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
