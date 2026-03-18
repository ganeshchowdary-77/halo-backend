package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.enums.PlatformVerificationStatus;
import com.thehalo.halobackend.model.user.UserPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlatformRepository extends JpaRepository<UserPlatform, Long> {

    // All platforms for a given user
    List<UserPlatform> findByUserId(Long userId);

    // Duplicate handle check across same platform
    Optional<UserPlatform> findByPlatformIdAndHandle(Long platformId, String handle);

    // Validate ownership before delete
    Optional<UserPlatform> findByIdAndUserId(Long id, Long userId);

    // Find all unverified platforms for IAM admin review
    List<UserPlatform> findByVerifiedFalse();

    long countByVerifiedFalse();

    // Check if user has verified platforms (for navigation)
    boolean existsByUserIdAndVerificationStatus(Long userId, PlatformVerificationStatus status);
    
    // Check if user has verified platforms (simplified)
    boolean existsByUserIdAndVerifiedTrue(Long userId);

    // Check if user has any platforms
    boolean existsByUserId(Long userId);

    // Find platforms by verification status
    List<UserPlatform> findByVerificationStatus(PlatformVerificationStatus status);
}
