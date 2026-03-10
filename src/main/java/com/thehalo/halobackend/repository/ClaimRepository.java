package com.thehalo.halobackend.repository;

import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Influencer's own claims
    List<Claim> findByFiledById(Long userId);

    // Officer's claim queue — all claims in a given status
    List<Claim> findByStatus(ClaimStatus status);

    // Officer's assigned queue
    List<Claim> findByAssignedOfficerId(Long officerId);

    // Validate claim belongs to requesting user
    Optional<Claim> findByIdAndFiledById(Long id, Long userId);
}
