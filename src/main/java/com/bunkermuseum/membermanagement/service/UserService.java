package com.bunkermuseum.membermanagement.service;

import com.bunkermuseum.membermanagement.model.User;
import com.bunkermuseum.membermanagement.repository.contract.UserRepositoryContract;
import com.bunkermuseum.membermanagement.service.base.BaseService;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for User entity business operations.
 *
 * <p>This service extends {@link BaseService} to inherit standard CRUD operations,
 * validation workflows, transaction management, and error handling while implementing
 * {@link UserServiceContract} to provide the User-specific business logic contract.
 * It follows the established service architecture patterns and provides comprehensive
 * business rule enforcement for User entities.</p>
 *
 * <h3>Architecture Integration:</h3>
 * <ul>
 *   <li><strong>Base Service:</strong> Inherits comprehensive business operations and utilities</li>
 *   <li><strong>Contract Implementation:</strong> Implements UserServiceContract interface</li>
 *   <li><strong>Repository Integration:</strong> Uses UserRepositoryContract for data access</li>
 *   <li><strong>Transaction Management:</strong> Supports transactional business operations</li>
 *   <li><strong>Validation Framework:</strong> Includes validation hooks and business rule enforcement</li>
 * </ul>
 *
 * <h3>Transaction Configuration:</h3>
 * <ul>
 *   <li>Read operations use {@code @Transactional(readOnly = true)} for optimization</li>
 *   <li>Write operations use full transactions with rollback on failure</li>
 *   <li>Complex operations maintain ACID properties across multiple steps</li>
 * </ul>
 *
 *
 * @author Philipp Borkovic
 *
 * @see BaseService
 * @see UserServiceContract
 * @see User
 * @see UserRepositoryContract
 */
@Service
@Transactional(readOnly = true)
public class UserService extends BaseService<User, UserRepositoryContract>
        implements UserServiceContract {

    public UserService(UserRepositoryContract repository) {
        super(repository);
    }
}