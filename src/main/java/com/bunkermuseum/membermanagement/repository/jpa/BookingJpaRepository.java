package com.bunkermuseum.membermanagement.repository.jpa;

import com.bunkermuseum.membermanagement.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository interface for Booking entity.
 *
 * @author Philipp Borkovic
 */
@Repository
public interface BookingJpaRepository extends JpaRepository<Booking, UUID> {
}
