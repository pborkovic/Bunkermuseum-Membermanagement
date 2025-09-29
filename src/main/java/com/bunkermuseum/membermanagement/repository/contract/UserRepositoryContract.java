package com.bunkermuseum.membermanagement.repository.contract;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.base.contract.BaseRepositoryContract;

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
}