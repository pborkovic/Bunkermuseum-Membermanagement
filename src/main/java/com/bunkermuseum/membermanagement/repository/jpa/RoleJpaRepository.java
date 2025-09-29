package com.bunkermuseum.membermanagement.repository.jpa;

import com.bunkermuseum.membermanagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for Role entity.
 *
 * <p>This interface provides the underlying Spring Data JPA repository for Role entities.
 * Spring Data JPA will automatically create the implementation of this interface
 * at runtime with all the standard CRUD operations plus custom query methods.
 * This repository is used internally by the RoleRepository implementation.</p>
 *
 * @author Philipp Borkovic
 * @see Role
 * @see JpaRepository
 * @see com.bunkermuseum.membermanagement.repository.RoleRepository
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<Role, UUID> {

}