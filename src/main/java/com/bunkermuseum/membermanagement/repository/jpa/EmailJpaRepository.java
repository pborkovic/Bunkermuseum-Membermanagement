package com.bunkermuseum.membermanagement.repository.jpa;

import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for Email entity.
 *
 * <p>This interface extends JpaRepository to provide standard CRUD operations
 * for Email entities, along with custom query methods for email-specific
 * data access patterns.</p>
 *
 * @see Email
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface EmailJpaRepository extends JpaRepository<Email, UUID> {

    /**
     * Finds all emails sent by a specific user.
     *
     * <p>This method retrieves all emails associated with a particular user,
     * ordered by creation date in descending order (most recent first).</p>
     *
     * @param user the user whose emails to find
     * @param pageable pagination parameters
     * @return page of emails sent by the user
     */
    Page<Email> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Finds all system emails (where user_id is null).
     *
     * <p>This method retrieves all emails that were sent by the system
     * rather than by a specific user, ordered by creation date in
     * descending order.</p>
     *
     * @param pageable pagination parameters
     * @return page of system-generated emails
     */
    Page<Email> findByUserIsNullOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Finds all emails sent to a specific email address.
     *
     * <p>This method retrieves all emails sent to a particular recipient
     * email address, ordered by creation date in descending order.</p>
     *
     * @param toAddress the recipient email address
     * @param pageable pagination parameters
     * @return page of emails sent to the address
     */
    Page<Email> findByToAddressOrderByCreatedAtDesc(String toAddress, Pageable pageable);

    /**
     * Finds all emails from a specific email address.
     *
     * <p>This method retrieves all emails sent from a particular sender
     * email address, ordered by creation date in descending order.</p>
     *
     * @param fromAddress the sender email address
     * @param pageable pagination parameters
     * @return page of emails sent from the address
     */
    Page<Email> findByFromAddressOrderByCreatedAtDesc(String fromAddress, Pageable pageable);

    /**
     * Finds emails created within a specific date range.
     *
     * <p>This method retrieves all emails created between the specified
     * start and end timestamps, ordered by creation date in descending order.</p>
     *
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @param pageable pagination parameters
     * @return page of emails created within the date range
     */
    Page<Email> findByCreatedAtBetweenOrderByCreatedAtDesc(
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );

    /**
     * Counts the number of emails sent by a specific user.
     *
     * <p>This method returns the total count of emails associated with
     * the given user.</p>
     *
     * @param user the user whose emails to count
     * @return the number of emails sent by the user
     */
    long countByUser(User user);

    /**
     * Counts the number of system emails (where user_id is null).
     *
     * <p>This method returns the total count of emails that were sent
     * by the system rather than by a specific user.</p>
     *
     * @return the number of system-generated emails
     */
    long countByUserIsNull();

    /**
     * Finds emails by user with optional filtering by subject or content.
     *
     * <p>This method provides search functionality across email subject
     * and content fields for emails sent by a specific user.</p>
     *
     * @param user the user whose emails to search
     * @param searchTerm the search term to match in subject or content
     * @param pageable pagination parameters
     * @return page of emails matching the search criteria
     */
    @Query("SELECT e FROM Email e WHERE e.user = :user AND " +
           "(LOWER(e.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Email> searchByUser(
        @Param("user") User user,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
}
