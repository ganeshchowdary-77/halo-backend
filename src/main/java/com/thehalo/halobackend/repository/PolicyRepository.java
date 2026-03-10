package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.enums.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // Find all policies for a given user
    List<Policy> findByUserId(Long userId);

    // Used for duplicate check when purchasing
    Optional<Policy> findByProfileIdAndProductIdAndStatus(Long profileId, Long productId, PolicyStatus status);

    // Count active policies for a product (used in ProductDetailResponse)
    long countByProductIdAndStatus(Long productId, PolicyStatus status);

    // Find all policies by status
    List<Policy> findByStatus(PolicyStatus status);
}
