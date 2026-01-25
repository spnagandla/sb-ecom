package com.buyology.backend.repository;

import com.buyology.backend.model.Role;
import com.buyology.backend.model.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByRoleName(UserRoles userRoles);
}
