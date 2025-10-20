package com.bunkermuseum.membermanagement.repository.contract;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Repository contract interface for User entity operations.
 *
 * <p>This interface defines the contract for User-specific data access operations.
 * It serves as a clean contract without extending any base interfaces, allowing
 * for maximum flexibility in implementation while maintaining clear separation
 * of concerns.</p>
 *
 * @author Philipp Borkovic
 *
 * @see User
 * @see com.bunkermuseum.membermanagement.repository.base.BaseRepository
 */
public interface UserRepositoryContract extends BaseRepositoryContract<User> {

    /**
     * Finds a user by their email address.
     *
     * <p>This method performs a case-sensitive search for a user with the specified
     * email address. It returns an Optional that will contain the user if found,
     * or be empty if no user exists with that email.</p>
     *
     * @param email The email address to search for. Should not be null or blank.
     * @return An Optional containing the user if found, or empty if not found
     * @throws IllegalArgumentException if email is null or blank
     *
     * @author Philipp Borkovic
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds users with pagination and optional search filtering.
     *
     * <p>This method provides efficient paginated access to users with case-insensitive
     * search functionality across name, email, and phone fields.</p>
     *
     * @param searchQuery Optional search term to filter users (null for all users)
     * @param pageable Pagination parameters (page number, size, sort)
     *
     * @return Page of users matching the search criteria
     */
    Page<User> findBySearchQuery(String searchQuery, Pageable pageable);

    /**
     * Finds users with pagination, optional search filtering, and status filter.
     *
     * <p>This method provides efficient paginated access to users with case-insensitive
     * search functionality across name, email, and phone fields. The status parameter
     * allows filtering between active users, deleted users, or all users.</p>
     *
     * @param searchQuery Optional search term to filter users (null for all users)
     * @param status Filter status: "active", "deleted", or "all"
     * @param pageable Pagination parameters (page number, size, sort)
     *
     * @return Page of users matching the search criteria and status filter
     */
    Page<User> findBySearchQueryAndStatus(String searchQuery, String status, Pageable pageable);
}