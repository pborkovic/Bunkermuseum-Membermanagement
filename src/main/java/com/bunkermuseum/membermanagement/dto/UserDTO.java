package com.bunkermuseum.membermanagement.dto;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for User entity.
 *
 * <p>This DTO provides a secure, lightweight representation of users for API responses.
 * It excludes sensitive information and resolves circular reference issues between
 * User and Role entities. The DTO is immutable to ensure thread safety and
 * prevent accidental modifications.</p>
 *
 * <h3>Excluded Sensitive Fields:</h3>
 * <ul>
 *   <li>password - never expose password hashes</li>
 *   <li>googleId, microsoftId - internal OAuth identifiers</li>
 *   <li>deletedAt - internal soft delete mechanism</li>
 *   <li>createdAt, updatedAt - audit timestamps (can be added if needed)</li>
 * </ul>
 *
 * @see com.bunkermuseum.membermanagement.model.User
 * @see RoleDTO
 */
public class UserDTO {

    /**
     * The unique identifier for this user.
     */
    private final UUID id;

    /**
     * The user's display name.
     */
    private final String name;

    /**
     * The user's email address.
     */
    private final String email;

    /**
     * The timestamp when the user's email was verified.
     * Null if email has not been verified yet.
     */
    private final @Nullable Instant emailVerifiedAt;

    /**
     * The path to the user's avatar image.
     * Null if no avatar is set.
     */
    private final @Nullable String avatarPath;

    /**
     * The user's salutation/gender (e.g., "männlich", "weiblich", "divers").
     */
    private final @Nullable String salutation;

    /**
     * The user's academic title (e.g., "Dr.", "Prof.").
     */
    private final @Nullable String academicTitle;

    /**
     * The user's military or professional rank.
     */
    private final @Nullable String rank;

    /**
     * The user's date of birth.
     */
    private final @Nullable LocalDate birthday;

    /**
     * The user's phone number.
     */
    private final @Nullable String phone;

    /**
     * The user's street address including house number.
     */
    private final @Nullable String street;

    /**
     * The user's city.
     */
    private final @Nullable String city;

    /**
     * The user's postal code.
     */
    private final @Nullable String postalCode;

    /**
     * The user's country.
     */
    private final @Nullable String country;

    /**
     * The roles assigned to this user.
     * Uses RoleDTO to avoid circular references.
     */
    private final Set<RoleDTO> roles;

    /**
     * Constructs a new UserDTO with all fields.
     *
     * <p>This constructor is designed to be called by mapper utilities that
     * convert User entities to UserDTOs. All sensitive information should be
     * excluded during the mapping process.</p>
     *
     * @param id The unique identifier for this user
     * @param name The user's display name
     * @param email The user's email address
     * @param emailVerifiedAt The email verification timestamp
     * @param avatarPath The path to the user's avatar image
     * @param salutation The user's salutation/gender
     * @param academicTitle The user's academic title
     * @param rank The user's rank
     * @param birthday The user's date of birth
     * @param phone The user's phone number
     * @param street The user's street address
     * @param city The user's city
     * @param postalCode The user's postal code
     * @param country The user's country
     * @param roles The set of roles assigned to this user
     *
     * @author Philipp Borkovic
     */
    public UserDTO(
        UUID id,
        String name,
        String email,
        @Nullable Instant emailVerifiedAt,
        @Nullable String avatarPath,
        @Nullable String salutation,
        @Nullable String academicTitle,
        @Nullable String rank,
        @Nullable LocalDate birthday,
        @Nullable String phone,
        @Nullable String street,
        @Nullable String city,
        @Nullable String postalCode,
        @Nullable String country,
        Set<RoleDTO> roles
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.emailVerifiedAt = emailVerifiedAt;
        this.avatarPath = avatarPath;
        this.salutation = salutation;
        this.academicTitle = academicTitle;
        this.rank = rank;
        this.birthday = birthday;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.roles = roles;
    }

    /**
     * Gets the unique identifier for this user.
     *
     * @return The user's unique identifier
     *
     * @author Philipp Borkovic
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the user's display name.
     *
     * @return The user's name
     *
     * @author Philipp Borkovic
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user's email address.
     *
     * @return The user's email
     *
     * @author Philipp Borkovic
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the email verification timestamp.
     *
     * @return The timestamp when email was verified, or null if not verified
     *
     * @author Philipp Borkovic
     */
    public @Nullable Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    /**
     * Gets the avatar image path.
     *
     * @return The path to the user's avatar, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getAvatarPath() {
        return avatarPath;
    }

    /**
     * Gets the user's salutation.
     *
     * @return The user's salutation/gender, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getSalutation() {
        return salutation;
    }

    /**
     * Gets the user's academic title.
     *
     * @return The user's academic title, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getAcademicTitle() {
        return academicTitle;
    }

    /**
     * Gets the user's rank.
     *
     * @return The user's rank, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getRank() {
        return rank;
    }

    /**
     * Gets the user's date of birth.
     *
     * @return The user's birthday, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable LocalDate getBirthday() {
        return birthday;
    }

    /**
     * Gets the user's phone number.
     *
     * @return The user's phone number, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getPhone() {
        return phone;
    }

    /**
     * Gets the user's street address.
     *
     * @return The user's street address, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getStreet() {
        return street;
    }

    /**
     * Gets the user's city.
     *
     * @return The user's city, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getCity() {
        return city;
    }

    /**
     * Gets the user's postal code.
     *
     * @return The user's postal code, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getPostalCode() {
        return postalCode;
    }

    /**
     * Gets the user's country.
     *
     * @return The user's country, or null if not set
     *
     * @author Philipp Borkovic
     */
    public @Nullable String getCountry() {
        return country;
    }

    /**
     * Gets the roles assigned to this user.
     *
     * @return An immutable set of role DTOs
     *
     * @author Philipp Borkovic
     */
    public Set<RoleDTO> getRoles() {
        return roles;
    }

    /**
     * Returns a string representation of the UserDTO.
     *
     * @return A string representation of this user DTO
     *
     * @author Philipp Borkovic
     */
    @Override
    public String toString() {
        return String.format("UserDTO{id=%s, name='%s', email='%s', roles=%s}",
            id, name, email, roles.size());
    }
}
