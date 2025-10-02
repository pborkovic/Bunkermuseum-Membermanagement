package com.bunkermuseum.membermanagement.repository.jpa;

import com.bunkermuseum.membermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}