package com.bunkermuseum.membermanagement.dto.mapper;

import com.bunkermuseum.membermanagement.dto.RoleDTO;
import com.bunkermuseum.membermanagement.dto.UserDTO;
import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting between User entities and UserDTOs.
 *
 * <p>This mapper provides a centralized, clean way to transform User entities
 * into their DTO representations for API responses. It ensures consistent
 * data transformation and excludes sensitive information from being exposed.</p>
 *
 * <h3>Design Principles:</h3>
 * <ul>
 *   <li>Single Responsibility - dedicated to User/UserDTO mapping</li>
 *   <li>Stateless - all methods are static, no instance state</li>
 *   <li>Null-safe - handles null values gracefully</li>
 *   <li>Security-focused - explicitly excludes sensitive fields</li>
 *   <li>Performance - uses stream API for efficient collection transformations</li>
 * </ul>
 *
 * <h3>Excluded Fields (Security):</h3>
 * <ul>
 *   <li>password - password hashes are never exposed</li>
 *   <li>googleId, microsoftId - internal OAuth identifiers</li>
 *   <li>deletedAt - internal soft delete timestamp</li>
 *   <li>createdAt, updatedAt - internal audit timestamps</li>
 * </ul>
 *
 * @author Philipp Borkovic
 *
 * @see User
 * @see UserDTO
 * @see RoleDTO
 */
public final class UserMapper {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This is a utility class with only static methods and should not be instantiated.</p>
     *
     * @author Philipp Borkovic
     */
    private UserMapper() {
        throw new UnsupportedOperationException("UserMapper is a utility class and cannot be instantiated");
    }

    /**
     * Converts a User entity to a UserDTO.
     *
     * <p>This method transforms a User entity into its DTO representation,
     * excluding sensitive fields and resolving circular references by converting
     * Role entities to RoleDTOs.</p>
     *
     * <p><strong>Null Handling:</strong> Returns null if the input user is null.</p>
     *
     * @param user The User entity to convert, may be null
     * @return The UserDTO representation, or null if input is null
     *
     * @author Philipp Borkovic
     */
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        Set<RoleDTO> roleDTOs = user.getRoles() != null
            ? user.getRoles().stream()
                .map(UserMapper::roleToDTO)
                .collect(Collectors.toSet())
            : new HashSet<>();

        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getEmailVerifiedAt(),
            user.getAvatarPath(),
            user.getSalutation(),
            user.getAcademicTitle(),
            user.getRank(),
            user.getBirthday(),
            user.getPhone(),
            user.getStreet(),
            user.getCity(),
            user.getPostalCode(),
            roleDTOs
        );
    }

    /**
     * Converts a list of User entities to a list of UserDTOs.
     *
     * <p>This method provides efficient batch transformation of User entities
     * using Java Streams API. It filters out any null values from the input list.</p>
     *
     * <p><strong>Null Handling:</strong> Returns an empty list if input is null.
     * Null elements within the list are filtered out.</p>
     *
     * @param users The list of User entities to convert, may be null
     * @return A list of UserDTOs, never null but may be empty
     *
     * @author Philipp Borkovic
     */
    public static List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return List.of();
        }

        return users.stream()
            .filter(user -> user != null)
            .map(UserMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Converts a Role entity to a RoleDTO.
     *
     * <p>This is a helper method used internally to convert Role entities
     * within User entities to RoleDTOs, preventing circular references.</p>
     *
     * <p><strong>Null Handling:</strong> Returns null if the input role is null.</p>
     *
     * @param role The Role entity to convert, may be null
     * @return The RoleDTO representation, or null if input is null
     *
     * @author Philipp Borkovic
     */
    private static RoleDTO roleToDTO(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleDTO(
            role.getId(),
            role.getName()
        );
    }
}
