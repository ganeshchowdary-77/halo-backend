package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.service.policy.PolicyService;
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

// Policy purchase and management for influencers
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "Endpoints for influencers to purchase and manage their active policies")
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get my policies", description = "Retrieves all policies owned by the current influencer.")
    @ApiResponse(responseCode = "200", description = "List of policies retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<PolicySummaryResponse>>> getMyPolicies() {
        return ResponseFactory.success(policyService.getMyPolicies(), "Policies loaded");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INFLUENCER','POLICY_ADMIN','UNDERWRITER')")
    @Operation(summary = "Get policy detail", description = "Retrieves full details of a specific policy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy details retrieved"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<HaloApiResponse<PolicyDetailResponse>> getDetail(@PathVariable Long id) {
        return ResponseFactory.success(policyService.getDetail(id), "Policy loaded");
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Direct purchase", description = "Directly purchases a policy for an approved product without a custom quote.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy successfully purchased"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid product")
    })
    public ResponseEntity<HaloApiResponse<PolicyDetailResponse>> purchase(
            @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseFactory.success(policyService.purchase(request), "Policy purchased", HttpStatus.CREATED);
    }

    @PostMapping("/purchase/quote/{quoteId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Purchase from quote", description = "Converts an approved custom quote into an active policy.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy successfully purchased from quote"),
            @ApiResponse(responseCode = "400", description = "Quote is not APPROVED or already accepted"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    public ResponseEntity<HaloApiResponse<PolicyDetailResponse>> purchaseFromQuote(@PathVariable Long quoteId) {
        return ResponseFactory.success(policyService.purchaseFromQuote(quoteId), "Policy purchased from quote",
                HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Cancel policy", description = "Cancels an active policy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy successfully cancelled"),
            @ApiResponse(responseCode = "400", description = "Policy is not active"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<HaloApiResponse<PolicySummaryResponse>> cancel(@PathVariable Long id) {
        return ResponseFactory.success(policyService.cancel(id), "Policy cancelled");
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Pay premium", description = "Pays the premium to activate a pending policy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Premium paid successfully"),
            @ApiResponse(responseCode = "400", description = "Policy is not in PENDING_PAYMENT state"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<HaloApiResponse<PolicyDetailResponse>> payPremium(@PathVariable Long id) {
        return ResponseFactory.success(policyService.payPremium(id), "Premium paid successfully");
    }

    // Admin Endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Get all policies", description = "Retrieves all policies for administration.")
    public ResponseEntity<HaloApiResponse<List<PolicySummaryResponse>>> getAllPolicies() {
        return ResponseFactory.success(policyService.getAllPolicies(), "All policies loaded");
    }

    @GetMapping("/admin/applications")
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Get policy applications", description = "Retrieves all submitted policy applications in the queue.")
    public ResponseEntity<HaloApiResponse<List<com.thehalo.halobackend.dto.policy.response.PolicyApplicationResponse>>> getAdminApplications() {
        return ResponseFactory.success(policyService.getAdminApplications(), "Applications loaded");
    }

    @PostMapping("/admin/applications/{id}/approve")
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Approve application", description = "Approves a policy application.")
    public ResponseEntity<HaloApiResponse<Void>> approveApplication(@PathVariable Long id) {
        policyService.approvePolicyApplication(id);
        return ResponseFactory.success(null, "Application approved");
    }

    @PostMapping("/admin/applications/{id}/reject")
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Reject application")
    public ResponseEntity<HaloApiResponse<Void>> rejectApplication(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        policyService.rejectPolicyApplication(id, body.get("reason"));
        return ResponseFactory.success(null, "Application rejected");
    }


    // Helper endpoints for navigation visibility
    @GetMapping("/status/has-active")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Check active policies", description = "Checks if the current user has any active policies.")
    public ResponseEntity<Boolean> hasActivePolicies() {
        return ResponseEntity.ok(policyService.hasActivePolicies());
    }

    @GetMapping("/status/has-paid-premium")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Check paid premium", description = "Checks if the current user has paid premium for any policy.")
    public ResponseEntity<Boolean> hasPaidPremium() {
        return ResponseEntity.ok(policyService.hasPaidPremium());
    }

}
