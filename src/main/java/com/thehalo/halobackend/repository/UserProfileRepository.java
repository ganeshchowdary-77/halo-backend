package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.profile.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // All profiles for a given user
    List<UserProfile> findByUserId(Long userId);

    // Duplicate handle check across same platform
    Optional<UserProfile> findByPlatformIdAndHandle(Long platformId, String handle);

    // Validate ownership before delete
    Optional<UserProfile> findByIdAndUserId(Long id, Long userId);
}
