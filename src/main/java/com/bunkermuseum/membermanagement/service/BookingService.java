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
     * <p>Simplified version that filters users by member type (role).
     * Supports "ORDENTLICHE_MITGLIEDER" and "FÖRDERNDE_MITGLIEDER" member types.</p>
     *
     * @author Philipp Borkovic
     */
    @Override
    @Transactional
    public int assignBookingToUsers(AssignBookingRequest request) {
        try {
            // Validate member type
            if (request.getMemberType() == null || request.getMemberType().trim().isEmpty()) {
                throw new IllegalArgumentException("Mitgliedstyp ist erforderlich");
            }

            String memberType = request.getMemberType().trim();
            if (!memberType.equals("ORDENTLICHE_MITGLIEDER") && !memberType.equals("FÖRDERNDE_MITGLIEDER")) {
                throw new IllegalArgumentException("Ungültiger Mitgliedstyp: " + memberType);
            }

            // Resolve target users by member type
            List<User> targets = resolveUsersByMemberType(memberType);
            if (targets.isEmpty()) {
                logger.warn("No users found with member type: {}", memberType);
                return 0;
            }

            // Prepare bookings to create (simplified - only 3 fields)
            List<Booking> toCreate = targets.stream().map(user -> {
                Booking b = new Booking(null);
                b.setUser(user);
                b.setExpectedAmount(request.getExpectedAmount());
                b.setActualAmount(request.getActualAmount());
                b.setActualPurpose(request.getActualPurpose());
                return b;
            }).collect(Collectors.toList());

            repository.createAll(toCreate);
            logger.info("Successfully assigned booking to {} users with member type: {}",
                toCreate.size(), memberType);
            return toCreate.size();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error assigning booking to users", e);
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
     * @deprecated Use {@link #resolveUsersByMemberType(String)} instead.
     */
    @Deprecated(forRemoval = true)
    private List<User> resolveTargetUsers(AssignBookingRequest request) {
        // Fallback: if userRepository is not available (e.g., unit tests), return empty list
        if (userRepository == null) {
            logger.warn("UserRepositoryContract not injected; cannot resolve target users");
            return Collections.emptyList();
        }

        Set<User> result = new LinkedHashSet<>();

        if (request.isAllUsersAssigned() != null && request.isAllUsersAssigned()) {
            result.addAll(userRepository.findAll());
        } else {
            if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
                // Use repository batch method to minimize queries
                result.addAll(userRepository.findAllById(new ArrayList<>(request.getUserIds())));
            }
            if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
                // Filter users by roles in-memory as minimal viable implementation
                List<User> all = userRepository.findAll();
                Set<String> roleNamesLc = request.getRoleNames().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

                for (User u : all) {
                    if (u.getRoles() == null) continue;
                    boolean match = u.getRoles().stream()
                        .map(Role::getName)
                        .filter(Objects::nonNull)
                        .map(String::toLowerCase)
                        .anyMatch(roleNamesLc::contains);
                    if (match) {
                        result.add(u);
                    }
                }
            }
        }

        return new ArrayList<>(result);
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
