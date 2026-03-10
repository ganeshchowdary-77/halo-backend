package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.request.ReviewClaimRequest;
import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.dto.common.ApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.claim.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Claim filing (influencer) and review (officer) endpoints
@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Tag(name = "Claim Management", description = "Endpoints for influencers to file claims and officers to review them")
public class ClaimController {

    private final ClaimService claimService;

    // Influencer: view their own claims
    @GetMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get my claims", description = "Retrieves all claims filed by the current influencer.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Claims loaded successfully")
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getMyClaims() {
        return ResponseFactory.success(claimService.getMyClaims(), "Claims loaded");
    }

    // Anyone with access: full claim detail
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INFLUENCER','CLAIMS_OFFICER','POLICY_ADMIN')")
    @Operation(summary = "Get claim detail", description = "Retrieves full details of a specific claim.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Claim details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<ApiResponse<ClaimDetailResponse>> getDetail(@PathVariable Long id) {
        return ResponseFactory.success(claimService.getDetail(id), "Claim loaded");
    }

    // Influencer: file a new defamation/incident claim
    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "File a claim", description = "Files a new incident claim against an active policy.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Claim successfully filed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or policy is not active"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<ApiResponse<ClaimDetailResponse>> file(
            @Valid @RequestBody FileClaimRequest request) {
        return ResponseFactory.success(claimService.file(request), "Claim filed successfully", HttpStatus.CREATED);
    }

    // Officer: review queue — all submitted claims
    @GetMapping("/queue")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get review queue", description = "Retrieves a queue of SUBMITTED or UNDER_REVIEW claims for officers.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Claim queue retrieved successfully")
    public ResponseEntity<ApiResponse<List<ClaimSummaryResponse>>> getQueue() {
        return ResponseFactory.success(claimService.getClaimQueue(), "Claim queue loaded");
    }

    // Officer: approve a claim and set the payout amount
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Approve a claim", description = "Approves a claim and sets the final payout amount.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Claim approved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or claim is already processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<ApiResponse<ClaimDetailResponse>> approve(
            @PathVariable Long id, @Valid @RequestBody ReviewClaimRequest request) {
        return ResponseFactory.success(claimService.approve(id, request), "Claim approved");
    }

    // Officer: deny a claim with written justification
    @PostMapping("/{id}/deny")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Deny a claim", description = "Denies a claim with a written justification (officer comments).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Claim denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or claim is already processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<ApiResponse<ClaimDetailResponse>> deny(
            @PathVariable Long id, @Valid @RequestBody ReviewClaimRequest request) {
        return ResponseFactory.success(claimService.deny(id, request), "Claim denied");
    }
}
