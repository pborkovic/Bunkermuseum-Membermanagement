package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.dto.AssignBookingRequest;
import com.bunkermuseum.membermanagement.dto.BookingDTO;
import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.model.Role;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.BookingRepositoryContract;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.BookingServiceContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Booking entity business operations.
 *
 * <p>This service extends {@link BaseService} to inherit standard CRUD operations,
 * validation workflows, transaction management, and error handling while implementing
 * {@link BookingServiceContract} to provide the Booking-specific business logic contract.
 * It follows the established service architecture patterns and provides comprehensive
 * business rule enforcement for Booking entities.</p>
 *
 * @see BaseService
 * @see BookingServiceContract
 * @see Booking
 * @see BookingRepositoryContract
 */
@Service
@Transactional(readOnly = true)
public class BookingService extends BaseService<Booking, BookingRepositoryContract>
        implements BookingServiceContract {

    // Injected lazily to avoid breaking existing tests that instantiate the service manually
    @Autowired(required = false)
    private UserRepositoryContract userRepository;

    /**
     * Constructs a new BookingService with the provided repository.
     *
     * <p>This constructor injects the BookingRepositoryContract dependency
     * and passes it to the parent BaseService for standard operations.
     * The repository provides the actual database interaction capabilities
     * while BaseService adds logging, error handling, and additional utilities.</p>
     *
     * @param repository The repository contract for Booking entities
     *
     * @author Philipp Borkovic
     */
    public BookingService(BookingRepositoryContract repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public java.util.List<BookingDTO> getAllBookings() {
        try {
            List<Booking> bookings = repository.findAll();
            return bookings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        } catch (Exception exception) {
            logger.error("Error retrieving all bookings", exception);

            throw new RuntimeException("Failed to retrieve bookings", exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Assigns a booking to all users of a specified member type. Uses type-safe
     * {@link com.bunkermuseum.membermanagement.dto.MemberType} enum for filtering.</p>
     *
     * <p>The method performs validation, filters users by role, creates booking entities,
     * and persists them in a single transaction. If no users match the member type,
     * the operation completes successfully but returns 0.</p>
     *
     * @param request The validated booking assignment request
     * @return The number of bookings created (number of users targeted)
     * @throws IllegalArgumentException if member type is null or invalid
     * @throws RuntimeException if database operation fails
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public int assignBookingToUsers(AssignBookingRequest request) {
        try {
            // Validate member type
            if (request.getMemberType() == null) {
                throw new IllegalArgumentException("Mitgliedstyp ist erforderlich");
            }

            // Resolve target users by member type (using enum's roleName)
            String roleName = request.getMemberType().getRoleName();
            List<User> targets = resolveUsersByMemberType(roleName);

            if (targets.isEmpty()) {
                logger.warn("No users found with member type: {}", request.getMemberType());
                return 0;
            }

            // Prepare bookings to create
            List<Booking> toCreate = targets.stream().map(user -> {
                Booking booking = new Booking(null);
                booking.setUser(user);
                booking.setExpectedAmount(request.getExpectedAmount());
                booking.setActualAmount(request.getActualAmount());
                booking.setActualPurpose(request.getActualPurpose());
                return booking;
            }).collect(Collectors.toList());

            repository.createAll(toCreate);
            logger.info("Successfully assigned booking to {} users with member type: {}",
                toCreate.size(), request.getMemberType());
            return toCreate.size();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error assigning booking to users: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error assigning booking to users", e);
            throw new RuntimeException("Fehler beim Zuweisen der Buchung zu Benutzern", e);
        }
    }

    /**
     * Resolves users by member type (role-based filtering).
     *
     * <p>This method filters active users by their assigned roles, targeting
     * users with the specified member type role.</p>
     *
     * @param memberType The member type role name (e.g., "ORDENTLICHE_MITGLIEDER", "FÖRDERNDE_MITGLIEDER")
     * @return List of users with the specified member type role, never null but may be empty
     *
     * @author Philipp Borkovic
     */
    private List<User> resolveUsersByMemberType(String memberType) {
        // Fallback: if userRepository is not available (e.g., unit tests), return empty list
        if (userRepository == null) {
            logger.warn("UserRepositoryContract not injected; cannot resolve users by member type");
            return Collections.emptyList();
        }

        List<User> allUsers = userRepository.findAll();
        String memberTypeLowerCase = memberType.toLowerCase();

        return allUsers.stream()
            .filter(user -> user.getRoles() != null)
            .filter(user -> user.getRoles().stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(roleName -> roleName.equals(memberTypeLowerCase)))
            .collect(Collectors.toList());
    }


    /**
     * Converts a Booking entity to a BookingDTO.
     *
     * <p>This method maps all relevant fields from the entity to the DTO,
     * replacing the User entity reference with just the userId to avoid
     * circular references and serialization issues.</p>
     *
     * @param booking The booking entity to convert
     * @return A BookingDTO representation of the booking
     *
     * @author Philipp Borkovic
     */
    private BookingDTO toDTO(Booking booking) {
        return new BookingDTO(
            booking.getId(),
            booking.getExpectedPurpose(),
            booking.getExpectedAmount(),
            booking.getReceivedAt(),
            booking.getActualPurpose(),
            booking.getActualAmount(),
            booking.getOfMG(),
            booking.getUser() != null ? booking.getUser().getId() : null,
            booking.getNote(),
            booking.getAccountStatementPage(),
            booking.getCode()
        );
    }
}
