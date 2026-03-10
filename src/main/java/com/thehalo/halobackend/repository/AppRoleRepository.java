package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.model.profile.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByName(RoleName name);
}
