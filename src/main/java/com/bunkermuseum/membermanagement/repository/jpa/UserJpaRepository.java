package com.bunkermuseum.membermanagement.repository.jpa;

import com.bunkermuseum.membermanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for User entity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email address.
     *
     * @param email the email address to search for
     *
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds users with pagination and fuzzy search filtering.
     *
     * <p>Performs intelligent fuzzy search across name, email, and phone fields using
     * PostgreSQL's trigram similarity. The search handles typos, misspellings, and
     * partial matches. Results are ranked by relevance:</p>
     *
     * <ul>
     *   <li>Exact matches (highest priority)</li>
     *   <li>Prefix matches (starts with search term)</li>
     *   <li>Substring matches (contains search term)</li>
     *   <li>Fuzzy matches using trigram similarity (handles typos)</li>
     * </ul>
     *
     * <p>Only returns non-deleted users. Handles null values safely.
     * Uses PostgreSQL's pg_trgm extension for fuzzy matching with a similarity
     * threshold of 0.3 (configurable for precision vs. recall tradeoff).</p>
     *
     * @param searchQuery the search term to filter by (must not be null)
     * @param pageable pagination parameters (must not be null)
     *
     * @return paginated list of users matching the search criteria, sorted by relevance
     */
    @Query(value = """
        SELECT DISTINCT u.* FROM users u
        WHERE u.deleted_at IS NULL
        AND (
            LOWER(u.name) = LOWER(:searchQuery)
            OR LOWER(u.email) = LOWER(:searchQuery)
            OR (u.phone IS NOT NULL AND LOWER(u.phone) = LOWER(:searchQuery))
            OR LOWER(u.name) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT(:searchQuery, '%')))
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
            OR similarity(u.name, :searchQuery) > 0.3
            OR similarity(u.email, :searchQuery) > 0.3
            OR (u.phone IS NOT NULL AND similarity(u.phone, :searchQuery) > 0.3)
        )
        ORDER BY
            CASE
                WHEN LOWER(u.name) = LOWER(:searchQuery) THEN 1
                WHEN LOWER(u.email) = LOWER(:searchQuery) THEN 1
                WHEN LOWER(u.name) LIKE LOWER(CONCAT(:searchQuery, '%')) THEN 2
                WHEN LOWER(u.email) LIKE LOWER(CONCAT(:searchQuery, '%')) THEN 2
                WHEN LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) THEN 3
                WHEN LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) THEN 3
                ELSE 4
            END,
            GREATEST(
                similarity(u.name, :searchQuery),
                similarity(u.email, :searchQuery),
                COALESCE(similarity(u.phone, :searchQuery), 0)
            ) DESC,
            u.name ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT u.id) FROM users u
        WHERE u.deleted_at IS NULL
        AND (
            LOWER(u.name) = LOWER(:searchQuery)
            OR LOWER(u.email) = LOWER(:searchQuery)
            OR (u.phone IS NOT NULL AND LOWER(u.phone) = LOWER(:searchQuery))
            OR LOWER(u.name) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT(:searchQuery, '%')))
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
            OR similarity(u.name, :searchQuery) > 0.3
            OR similarity(u.email, :searchQuery) > 0.3
            OR (u.phone IS NOT NULL AND similarity(u.phone, :searchQuery) > 0.3)
        )
        """,
        nativeQuery = true)
    Page<User> findBySearchQuery(@Param("searchQuery") String searchQuery, Pageable pageable);

    /**
     * Finds users with pagination, fuzzy search filtering, and optional deleted user inclusion.
     *
     * <p>Performs intelligent fuzzy search across name, email, and phone fields using
     * PostgreSQL's trigram similarity. The search handles typos, misspellings, and
     * partial matches. Results are ranked by relevance:</p>
     *
     * <ul>
     *   <li>Exact matches (highest priority)</li>
     *   <li>Prefix matches (starts with search term)</li>
     *   <li>Substring matches (contains search term)</li>
     *   <li>Fuzzy matches using trigram similarity (handles typos)</li>
     * </ul>
     *
     * <p>Can return active users only, deleted users only, or all users based on the status parameter.
     * Handles null values safely. Uses PostgreSQL's pg_trgm extension for fuzzy matching with a
     * similarity threshold of 0.3 (configurable for precision vs. recall tradeoff).</p>
     *
     * @param searchQuery the search term to filter by (must not be null)
     * @param status filter status: "active" (deleted_at IS NULL), "deleted" (deleted_at IS NOT NULL), or "all" (no filter)
     * @param pageable pagination parameters (must not be null)
     *
     * @return paginated list of users matching the search criteria, sorted by relevance
     *
     * @author Philipp Borkovic
     */
    @Query(value = """
        SELECT DISTINCT u.* FROM users u
        WHERE (
            CASE
                WHEN :status = 'active' THEN u.deleted_at IS NULL
                WHEN :status = 'deleted' THEN u.deleted_at IS NOT NULL
                ELSE true
            END
        )
        AND (
            LOWER(u.name) = LOWER(:searchQuery)
            OR LOWER(u.email) = LOWER(:searchQuery)
            OR (u.phone IS NOT NULL AND LOWER(u.phone) = LOWER(:searchQuery))
            OR LOWER(u.name) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT(:searchQuery, '%')))
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
            OR similarity(u.name, :searchQuery) > 0.3
            OR similarity(u.email, :searchQuery) > 0.3
            OR (u.phone IS NOT NULL AND similarity(u.phone, :searchQuery) > 0.3)
        )
        ORDER BY
            CASE
                WHEN LOWER(u.name) = LOWER(:searchQuery) THEN 1
                WHEN LOWER(u.email) = LOWER(:searchQuery) THEN 1
                WHEN LOWER(u.name) LIKE LOWER(CONCAT(:searchQuery, '%')) THEN 2
                WHEN LOWER(u.email) LIKE LOWER(CONCAT(:searchQuery, '%')) THEN 2
                WHEN LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) THEN 3
                WHEN LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) THEN 3
                ELSE 4
            END,
            GREATEST(
                similarity(u.name, :searchQuery),
                similarity(u.email, :searchQuery),
                COALESCE(similarity(u.phone, :searchQuery), 0)
            ) DESC,
            u.name ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT u.id) FROM users u
        WHERE (
            CASE
                WHEN :status = 'active' THEN u.deleted_at IS NULL
                WHEN :status = 'deleted' THEN u.deleted_at IS NOT NULL
                ELSE true
            END
        )
        AND (
            LOWER(u.name) = LOWER(:searchQuery)
            OR LOWER(u.email) = LOWER(:searchQuery)
            OR (u.phone IS NOT NULL AND LOWER(u.phone) = LOWER(:searchQuery))
            OR LOWER(u.name) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT(:searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT(:searchQuery, '%')))
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
            OR (u.phone IS NOT NULL AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
            OR similarity(u.name, :searchQuery) > 0.3
            OR similarity(u.email, :searchQuery) > 0.3
            OR (u.phone IS NOT NULL AND similarity(u.phone, :searchQuery) > 0.3)
        )
        """,
        nativeQuery = true)
    Page<User> findBySearchQueryAndStatus(
        @Param("searchQuery") String searchQuery,
        @Param("status") String status,
        Pageable pageable
    );
}