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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public int assignBookingToUsers(AssignBookingRequest request) {
        try {
            if (request.getMemberType() == null) {
                throw new IllegalArgumentException("Member type is required");
            }

            String roleName = request.getMemberType().getRoleName();
            List<User> targets = resolveUsersByMemberType(roleName);

            if (targets.isEmpty()) {
                logger.warn("No users found with member type: {}", request.getMemberType());

                return 0;
            }

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
        } catch (IllegalArgumentException exception) {
            logger.error("Validation error assigning booking to users: {}", exception.getMessage());

            throw exception;
        } catch (Exception exception) {
            logger.error("Error assigning booking to users", exception);

            throw new RuntimeException("Failed to assign booking to users", exception);
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
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public List<BookingDTO> getCurrentUserBookings() {
        try {
            UUID currentUserId = getCurrentAuthenticatedUserId();

            if (currentUserId == null) {
                logger.warn("Cannot retrieve bookings: User is not authenticated or has no valid ID");

                return Collections.emptyList();
            }

            List<Booking> userBookings = filterBookingsByUserId(currentUserId);

            return userBookings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        } catch (Exception exception) {
            logger.error("Unexpected error retrieving bookings for current user", exception);
            throw new RuntimeException("Failed to retrieve user bookings", exception);
        }
    }

    /**
     * Extracts the authenticated user's ID from Spring Security context with validation.
     *
     * @return The user's UUID, or {@code null} if not authenticated or principal is invalid
     *
     * @author Philipp Borkovic
     */
    private UUID getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.debug("No authentication found in security context");

            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            logger.debug("Authentication principal is null");

            return null;
        }

        if (!(principal instanceof User currentUser)) {
            logger.warn("Authentication principal is not a User instance. Found: {}",
                principal.getClass().getName());

            return null;
        }

        UUID userId = currentUser.getId();
        if (userId == null) {
            logger.warn("Authenticated user has no ID. User: {}", currentUser.getEmail());

            return null;
        }

        return userId;
    }

    /**
     * Filters bookings by user ID using in-memory stream filtering.
     *
     * @param userId The user's UUID
     * @return List of bookings for the user, empty if none exist
     *
     * @throws IllegalArgumentException if userId is null
     *
     * @author Philipp Borkovic
     */
    private List<Booking> filterBookingsByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<Booking> allBookings = repository.findAll();

        return allBookings.stream()
            .filter(booking -> booking.getUser() != null)
            .filter(booking -> userId.equals(booking.getUser().getId()))
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
