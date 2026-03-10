package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.request.ReviewClaimRequest;
import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;

import java.util.List;

public interface ClaimService {
    List<ClaimSummaryResponse> getMyClaims();

    ClaimDetailResponse getDetail(Long claimId);

    ClaimDetailResponse file(FileClaimRequest request);

    ClaimDetailResponse approve(Long claimId, ReviewClaimRequest request);

    ClaimDetailResponse deny(Long claimId, ReviewClaimRequest request);

    List<ClaimSummaryResponse> getClaimQueue();
}
