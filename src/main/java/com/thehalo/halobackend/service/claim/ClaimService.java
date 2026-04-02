package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.request.ReviewClaimRequest;
import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;

import java.util.List;

public interface ClaimService {
    List<ClaimSummaryResponse> getMyClaims();

    ClaimDetailResponse getDetail(Long claimId);

    ClaimDetailResponse file(FileClaimRequest request, List<org.springframework.web.multipart.MultipartFile> documents);

    ClaimDetailResponse approve(Long claimId, ReviewClaimRequest request);

    ClaimDetailResponse deny(Long claimId, ReviewClaimRequest request);

    ClaimDetailResponse requestDocuments(Long claimId, ReviewClaimRequest request);

    ClaimDetailResponse uploadAdditionalDocuments(Long claimId, List<org.springframework.web.multipart.MultipartFile> documents);

    List<ClaimSummaryResponse> getClaimQueue();

    org.springframework.data.domain.Page<ClaimSummaryResponse> getClaimQueuePaginated(String search, com.thehalo.halobackend.enums.ClaimStatus status, org.springframework.data.domain.Pageable pageable);

    List<ClaimSummaryResponse> getApprovedClaims();
    
    // Assignment methods
    ClaimDetailResponse assignClaim(Long claimId);

    ClaimDetailResponse releaseClaim(Long claimId);
    
    List<ClaimSummaryResponse> getAssignedClaims();
    
    // Search and analytics methods for Claims Officers
    List<ClaimSummaryResponse> searchClaimsByUserName(String userName);
    
    List<ClaimSummaryResponse> getUserClaimHistory(Long userId);
}
