package com.bunkermuseum.membermanagement.repository;

import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.BaseRepository;
import com.bunkermuseum.membermanagement.repository.contract.EmailRepositoryContract;
import com.bunkermuseum.membermanagement.repository.jpa.EmailJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Repository implementation for Email entity operations.
 *
 * <p>This class provides the concrete implementation for Email-specific data access operations.
 * It extends {@link BaseRepository} to inherit standard CRUD operations, validation workflows,
 * transaction management, and error handling while implementing {@link EmailRepositoryContract}
 * to provide the Email-specific repository contract.</p>
 *
 * @see BaseRepository
 * @see EmailRepositoryContract
 * @see Email
 */
@Repository
public class EmailRepository extends BaseRepository<Email, EmailJpaRepository>
        implements EmailRepositoryContract {

    /**
     * Constructs a new EmailRepository with the provided JPA repository.
     *
     * <p>This constructor injects the Spring Data JpaRepository dependency
     * and passes it to the parent BaseRepository for standard CRUD operations.
     * The JpaRepository provides the actual database interaction capabilities
     * while BaseRepository adds logging, error handling, and additional utilities.</p>
     *
     * @param repository The Spring Data JpaRepository for Email entities
     */
    public EmailRepository(EmailJpaRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getEntityName() {
        return "Email";
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<Email> findByUser(User user, Pageable pageable) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Finding emails for user: {}", user.getId());
            return repository.findByUserOrderByCreatedAtDesc(user, pageable);
        } catch (Exception e) {
            logger.error("Error finding emails for user: {}", user.getId(), e);
            throw new RuntimeException("Error occurred while finding emails for user", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<Email> findSystemEmails(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Finding system emails");
            return repository.findByUserIsNullOrderByCreatedAtDesc(pageable);
        } catch (Exception e) {
            logger.error("Error finding system emails", e);
            throw new RuntimeException("Error occurred while finding system emails", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<Email> findByToAddress(String toAddress, Pageable pageable) {
        if (toAddress == null || toAddress.isBlank()) {
            throw new IllegalArgumentException("To address must not be null or blank");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Finding emails sent to: {}", toAddress);
            return repository.findByToAddressOrderByCreatedAtDesc(toAddress, pageable);
        } catch (Exception e) {
            logger.error("Error finding emails sent to: {}", toAddress, e);
            throw new RuntimeException("Error occurred while finding emails by recipient", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<Email> findByFromAddress(String fromAddress, Pageable pageable) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalArgumentException("From address must not be null or blank");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Finding emails sent from: {}", fromAddress);
            return repository.findByFromAddressOrderByCreatedAtDesc(fromAddress, pageable);
        } catch (Exception e) {
            logger.error("Error finding emails sent from: {}", fromAddress, e);
            throw new RuntimeException("Error occurred while finding emails by sender", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<Email> findByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date must not be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Finding emails between {} and {}", startDate, endDate);
            return repository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        } catch (Exception e) {
            logger.error("Error finding emails by date range", e);
            throw new RuntimeException("Error occurred while finding emails by date range", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public long countByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        try {
            logger.debug("Counting emails for user: {}", user.getId());
            return repository.countByUser(user);
        } catch (Exception e) {
            logger.error("Error counting emails for user: {}", user.getId(), e);
            throw new RuntimeException("Error occurred while counting emails for user", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public long countSystemEmails() {
        try {
            logger.debug("Counting system emails");
            return repository.countByUserIsNull();
        } catch (Exception e) {
            logger.error("Error counting system emails", e);
            throw new RuntimeException("Error occurred while counting system emails", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @author Philipp Borkovic
     */
    @Override
    public Page<Email> searchByUser(User user, String searchTerm, Pageable pageable) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (searchTerm == null || searchTerm.isBlank()) {
            throw new IllegalArgumentException("Search term must not be null or blank");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        try {
            logger.debug("Searching emails for user: {} with term: {}", user.getId(), searchTerm);
            return repository.searchByUser(user, searchTerm, pageable);
        } catch (Exception e) {
            logger.error("Error searching emails for user: {}", user.getId(), e);
            throw new RuntimeException("Error occurred while searching emails", e);
        }
    }
}
