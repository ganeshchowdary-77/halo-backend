package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.request.ReviewClaimRequest;
import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.claim.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(responseCode = "200", description = "Claims loaded successfully")
    public ResponseEntity<HaloApiResponse<List<ClaimSummaryResponse>>> getMyClaims() {
        return ResponseFactory.success(claimService.getMyClaims(), "Claims loaded");
    }

    // Anyone with access: full claim detail
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INFLUENCER','CLAIMS_OFFICER','POLICY_ADMIN')")
    @Operation(summary = "Get claim detail", description = "Retrieves full details of a specific claim.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim details retrieved"),
            @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<HaloApiResponse<ClaimDetailResponse>> getDetail(@PathVariable Long id) {
        return ResponseFactory.success(claimService.getDetail(id), "Claim loaded");
    }

    // Influencer: file a new defamation/incident claim
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "File a claim", description = "Files a new incident claim against an active policy.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claim successfully filed"),
            @ApiResponse(responseCode = "400", description = "Validation error or policy is not active"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<HaloApiResponse<ClaimDetailResponse>> file(
            @RequestPart("request") @Valid FileClaimRequest request,
            @RequestPart("documents") List<org.springframework.web.multipart.MultipartFile> documents) {
        return ResponseFactory.success(claimService.file(request, documents), "Claim filed successfully", HttpStatus.CREATED);
    }

    // Officer: review queue — all submitted claims
    @GetMapping("/queue")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get review queue", description = "Retrieves a SEARCHABLE and PAGINATED queue of SUBMITTED or UNDER_REVIEW claims for officers.")
    @ApiResponse(responseCode = "200", description = "Claim queue retrieved successfully")
    public ResponseEntity<HaloApiResponse<org.springframework.data.domain.Page<ClaimSummaryResponse>>> getQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.thehalo.halobackend.enums.ClaimStatus status) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseFactory.success(claimService.getClaimQueuePaginated(search, status, pageable), "Claim queue loaded");
    }

    // Public: get all approved claims for settlement logs
    @GetMapping("/approved")
    @Operation(summary = "Get approved claims", description = "Retrieves all approved claims for settlement logs display.")
    @ApiResponse(responseCode = "200", description = "Approved claims retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<ClaimSummaryResponse>>> getApprovedClaims() {
        return ResponseFactory.success(claimService.getApprovedClaims(), "Approved claims loaded");
    }

    // Officer: assign claim to self
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Assign claim to officer", description = "Assigns a claim to the current claims officer for review.")
    @ApiResponse(responseCode = "200", description = "Claim assigned successfully")
    public ResponseEntity<HaloApiResponse<ClaimDetailResponse>> assignClaim(@PathVariable Long id) {
        return ResponseFactory.success(claimService.assignClaim(id), "Claim assigned successfully");
    }

    // Officer: release claim back to queue
    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Release claim", description = "Releases a claim back to the unassigned queue.")
    @ApiResponse(responseCode = "200", description = "Claim released successfully")
    public ResponseEntity<HaloApiResponse<ClaimDetailResponse>> releaseClaim(@PathVariable Long id) {
        return ResponseFactory.success(claimService.releaseClaim(id), "Claim released successfully");
    }

    // Officer: get assigned claims
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get assigned claims", description = "Retrieves claims assigned to the current officer.")
    @ApiResponse(responseCode = "200", description = "Assigned claims retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<ClaimSummaryResponse>>> getAssignedClaims() {
        return ResponseFactory.success(claimService.getAssignedClaims(), "Assigned claims loaded");
    }

    // Officer: get assignment logs
    // TODO: Implement getAssignmentLogs method in ClaimService
    /*
    @GetMapping("/logs")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get assignment logs", description = "Retrieves assignment and approval logs for audit trail.")
    @ApiResponse(responseCode = "200", description = "Assignment logs retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<Object>>> getAssignmentLogs() {
        return ResponseFactory.success(claimService.getAssignmentLogs(), "Assignment logs loaded");
    }
    */

    // Officer: approve a claim and set the payout amount
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Approve a claim", description = "Approves a claim and sets the final payout amount.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim approved"),
            @ApiResponse(responseCode = "400", description = "Validation error or claim is already processed"),
            @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<HaloApiResponse<ClaimDetailResponse>> approve(
            @PathVariable Long id, @Valid @RequestBody ReviewClaimRequest request) {
        return ResponseFactory.success(claimService.approve(id, request), "Claim approved");
    }

    // Officer: deny a claim with written justification
    @PostMapping("/{id}/deny")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Deny a claim", description = "Denies a claim with a written justification (officer comments).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim denied"),
            @ApiResponse(responseCode = "400", description = "Validation error or claim is already processed"),
            @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<HaloApiResponse<ClaimDetailResponse>> deny(
            @PathVariable Long id, @Valid @RequestBody ReviewClaimRequest request) {
        return ResponseFactory.success(claimService.deny(id, request), "Claim denied");
    }
}
