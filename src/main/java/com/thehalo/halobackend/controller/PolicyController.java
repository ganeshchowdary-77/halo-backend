package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ApiResponse;
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of policies retrieved successfully")
    public ResponseEntity<ApiResponse<List<PolicySummaryResponse>>> getMyPolicies() {
        return ResponseFactory.success(policyService.getMyPolicies(), "Policies loaded");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INFLUENCER','POLICY_ADMIN','UNDERWRITER')")
    @Operation(summary = "Get policy detail", description = "Retrieves full details of a specific policy.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Policy details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> getDetail(@PathVariable Long id) {
        return ResponseFactory.success(policyService.getDetail(id), "Policy loaded");
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Direct purchase", description = "Directly purchases a policy for an approved product without a custom quote.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Policy successfully purchased"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid product")
    })
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> purchase(
            @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseFactory.success(policyService.purchase(request), "Policy purchased", HttpStatus.CREATED);
    }

    @PostMapping("/purchase/quote/{quoteId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Purchase from quote", description = "Converts an approved custom quote into an active policy.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Policy successfully purchased from quote"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Quote is not APPROVED or already accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Quote not found")
    })
    public ResponseEntity<ApiResponse<PolicyDetailResponse>> purchaseFromQuote(@PathVariable Long quoteId) {
        return ResponseFactory.success(policyService.purchaseFromQuote(quoteId), "Policy purchased from quote",
                HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Cancel policy", description = "Cancels an active policy.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Policy successfully cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Policy is not active"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<ApiResponse<PolicySummaryResponse>> cancel(@PathVariable Long id) {
        return ResponseFactory.success(policyService.cancel(id), "Policy cancelled");
    }
}
