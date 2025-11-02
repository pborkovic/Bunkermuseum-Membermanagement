package com.bunkermuseum.membermanagement.repository.contract;

import com.bunkermuseum.membermanagement.model.Email;
import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

/**
 * Repository contract interface for Email entity operations.
 *
 * <p>This interface defines the contract for Email-specific data access operations.
 * It serves as a clean contract extending BaseRepositoryContract, providing email-specific
 * query methods while maintaining clear separation of concerns.</p>
 *
 * @see Email
 * @see com.bunkermuseum.membermanagement.repository.base.BaseRepository
 */
public interface EmailRepositoryContract extends BaseRepositoryContract<Email> {

    /**
     * Finds all emails sent by a specific user.
     *
     * <p>This method retrieves all emails associated with a particular user,
     * ordered by creation date in descending order (most recent first). This is
     * useful for displaying a user's email history or audit trail.</p>
     *
     * @param user the user whose emails to find. Must not be null.
     * @param pageable pagination and sorting parameters. Must not be null.
     * @return page of emails sent by the user, never null
     * @throws IllegalArgumentException if user or pageable is null
     */
    Page<Email> findByUser(User user, Pageable pageable);

    /**
     * Finds all system emails (where user_id is null).
     *
     * <p>This method retrieves all emails that were sent by the system
     * rather than by a specific user. System emails include automated
     * notifications, welcome emails, password resets, etc.</p>
     *
     * @param pageable pagination and sorting parameters. Must not be null.
     * @return page of system-generated emails, never null
     * @throws IllegalArgumentException if pageable is null
     */
    Page<Email> findSystemEmails(Pageable pageable);

    /**
     * Finds all emails sent to a specific email address.
     *
     * <p>This method retrieves all emails sent to a particular recipient
     * email address, ordered by creation date in descending order. Useful
     * for tracking all communications sent to a specific recipient.</p>
     *
     * @param toAddress the recipient email address. Must not be null or blank.
     * @param pageable pagination and sorting parameters. Must not be null.
     * @return page of emails sent to the address, never null
     * @throws IllegalArgumentException if toAddress is null/blank or pageable is null
     */
    Page<Email> findByToAddress(String toAddress, Pageable pageable);

    /**
     * Finds all emails from a specific email address.
     *
     * <p>This method retrieves all emails sent from a particular sender
     * email address, ordered by creation date in descending order. Useful
     * for tracking all communications sent from a specific sender.</p>
     *
     * @param fromAddress the sender email address. Must not be null or blank.
     * @param pageable pagination and sorting parameters. Must not be null.
     * @return page of emails sent from the address, never null
     * @throws IllegalArgumentException if fromAddress is null/blank or pageable is null
     */
    Page<Email> findByFromAddress(String fromAddress, Pageable pageable);

    /**
     * Finds emails created within a specific date range.
     *
     * <p>This method retrieves all emails created between the specified
     * start and end timestamps, ordered by creation date in descending order.
     * Useful for reporting and analytics on email volume over time.</p>
     *
     * @param startDate the start of the date range (inclusive). Must not be null.
     * @param endDate the end of the date range (inclusive). Must not be null.
     * @param pageable pagination and sorting parameters. Must not be null.
     * @return page of emails created within the date range, never null
     * @throws IllegalArgumentException if any parameter is null or if startDate is after endDate
     */
    Page<Email> findByDateRange(Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Counts the number of emails sent by a specific user.
     *
     * <p>This method returns the total count of emails associated with
     * the given user. Useful for usage statistics and analytics.</p>
     *
     * @param user the user whose emails to count. Must not be null.
     * @return the number of emails sent by the user
     * @throws IllegalArgumentException if user is null
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
    long countSystemEmails();

    /**
     * Searches emails by user with optional filtering by subject or content.
     *
     * <p>This method provides full-text search functionality across email subject
     * and content fields for emails sent by a specific user. The search is
     * case-insensitive and matches partial strings.</p>
     *
     * @param user the user whose emails to search. Must not be null.
     * @param searchTerm the search term to match in subject or content. Must not be null or blank.
     * @param pageable pagination and sorting parameters. Must not be null.
     * @return page of emails matching the search criteria, never null
     * @throws IllegalArgumentException if any parameter is null or searchTerm is blank
     */
    Page<Email> searchByUser(User user, String searchTerm, Pageable pageable);
}
