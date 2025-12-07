package com.bunkermuseum.membermanagement.dto.mapper;

import com.bunkermuseum.membermanagement.dto.EmailDTO;
import com.bunkermuseum.membermanagement.dto.UserDTO;
import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting between Email entities and EmailDTOs.
 *
 * <p>This mapper handles the conversion of Email entities to DTOs for safe
 * serialization to the frontend, avoiding Hibernate proxy issues and circular
 * references with User entities.</p>
 *
 * @author Philipp Borkovic
 */
public class EmailMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private EmailMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts an Email entity to an EmailDTO.
     *
     * <p>This method safely handles the lazy-loaded User relationship by
     * converting it to a UserDTO, which prevents Hibernate proxy serialization
     * issues.</p>
     *
     * @param email the Email entity to convert
     * @return the EmailDTO, or null if the input is null
     */
    public static EmailDTO toDTO(Email email) {
        if (email == null) {
            return null;
        }

        UserDTO userDTO = null;
        if (email.getUser() != null) {
            User user = email.getUser();
            userDTO = UserMapper.toDTO(user);
        }

        return new EmailDTO(
            email.getId(),
            email.getFromAddress(),
            email.getToAddress(),
            email.getSubject(),
            email.getContent(),
            userDTO,
            email.getCreatedAt(),
            email.getUpdatedAt(),
            email.deletedAt()
        );
    }

    /**
     * Converts a list of Email entities to a list of EmailDTOs.
     *
     * @param emails the list of Email entities to convert
     * @return the list of EmailDTOs
     */
    public static List<EmailDTO> toDTOList(List<Email> emails) {
        if (emails == null) {
            return null;
        }

        return emails.stream()
            .map(EmailMapper::toDTO)
            .collect(Collectors.toList());
    }
}
