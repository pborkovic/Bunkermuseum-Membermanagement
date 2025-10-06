package com.bunkermuseum.membermanagement.repository.contract;

import com.bunkermuseum.membermanagement.model.Booking;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;

/**
 * Repository contract interface for Booking entity operations.
 *
 * <p>This interface defines the contract for Booking-specific data access operations.
 * It serves as a clean contract extending BaseRepositoryContract to inherit standard
 * CRUD operations.</p>
 *
 * @see Booking
 * @see com.bunkermuseum.membermanagement.repository.base.BaseRepository
 */
public interface BookingRepositoryContract extends BaseRepositoryContract<Booking> {
}
