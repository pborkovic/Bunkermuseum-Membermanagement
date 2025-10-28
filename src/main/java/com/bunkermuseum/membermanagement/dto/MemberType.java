package com.bunkermuseum.membermanagement.dto;

/**
 * Enumeration of member types for booking assignment.
 *
 * <p>This enum provides type-safe representation of member categories in the system.
 * It prevents invalid member type strings and provides a clear, self-documenting API
 * for member type filtering operations.</p>
 */
public enum MemberType {

    /**
     * Regular/ordinary members of the organization.
     * German: "Ordentliche Mitglieder"
     */
    REGULAR_MEMBERS("Ordentliche Mitglieder", "ordentliche_mitglieder"),

    /**
     * Supporting/sponsoring members of the organization.
     * German: "Fördernde Mitglieder"
     */
    SUPPORTING_MEMBERS("Fördernde Mitglieder", "fördernde_mitglieder");

    /**
     * Human-readable display name for UI presentation.
     */
    private final String displayName;

    /**
     * Role name used for database filtering (lowercase).
     */
    private final String roleName;

    /**
     * Constructs a MemberType with the specified display name and role name.
     *
     * @param displayName The human-readable name for UI display
     * @param roleName The lowercase role name for database queries
     *
     * @author Philipp Borkovic
     */
    MemberType(String displayName, String roleName) {
        this.displayName = displayName;
        this.roleName = roleName;
    }

    /**
     * Gets the human-readable display name.
     *
     * @return The display name for UI presentation
     *
     * @author Philipp Borkovic
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the role name for database filtering.
     *
     * @return The lowercase role name used in queries
     *
     * @author Philipp Borkovic
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Returns the enum constant name (e.g., "ORDENTLICHE_MITGLIEDER").
     *
     * @return The enum constant name
     */
    @Override
    public String toString() {
        return name();
    }
}
