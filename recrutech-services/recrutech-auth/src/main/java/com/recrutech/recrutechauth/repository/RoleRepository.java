package com.recrutech.recrutechauth.repository;

import com.recrutech.recrutechauth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 * Provides CRUD operations for the Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /**
     * Find a role by name.
     *
     * @param name the name of the role to search for
     * @return an Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if a role with the given name exists.
     *
     * @param name the name to check
     * @return true if a role with the name exists, false otherwise
     */
    boolean existsByName(String name);
}