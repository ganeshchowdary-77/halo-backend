package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.request.SubmitPolicyApplicationRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.service.policy.PolicyApplicationService;
import com.thehalo.halobackend.service.policy.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Policy management and application endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyApplicationService applicationService;

    // ═══════════ POLICY APPLICATION ENDPOINTS ═══════════

    @PostMapping("/apply")
    @Operation(summary = "Apply for a policy", description = "Submit a policy application with security assessment. Auto-calculates risk and premium.")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> applyForPolicy(@Valid @RequestBody SubmitPolicyApplicationRequest request) {
        PolicyApplicationDetailResponse result = applicationService.submitApplication(request);
        String message = result.getPolicyId() != null
                ? "Application approved! Policy created with premium $" + result.getCalculatedPremium() + "/month."
                : "Application submitted for underwriter review.";
        return ResponseFactory.success(result, message, org.springframework.http.HttpStatus.CREATED);
    }

    @GetMapping("/applications")
    @Operation(summary = "Get my applications", description = "List all policy applications for the current user")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> getMyApplications() {
        List<PolicyApplicationDetailResponse> applications = applicationService.getMyApplications();
        return ResponseFactory.success(applications, "Applications retrieved successfully");
    }

    @GetMapping("/applications/{id}")
    @Operation(summary = "Get application detail", description = "Get a specific policy application detail")
    @PreAuthorize("hasRole('INFLUENCER') or hasRole('UNDERWRITER') or hasRole('POLICY_ADMIN')")
    public ResponseEntity<?> getApplicationDetail(@PathVariable Long id) {
        PolicyApplicationDetailResponse detail = applicationService.getApplicationDetail(id);
        return ResponseFactory.success(detail, "Application detail retrieved");
    }

    // ═══════════ POLICY LIFECYCLE ENDPOINTS ═══════════

    @GetMapping
    @Operation(summary = "Get my policies", description = "List all policies for the current user")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> getMyPolicies() {
        List<PolicySummaryResponse> policies = policyService.getMyPolicies();
        return ResponseFactory.success(policies, "Policies retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy detail", description = "Get full detail of a specific policy")
    @PreAuthorize("hasRole('INFLUENCER') or hasRole('UNDERWRITER') or hasRole('POLICY_ADMIN')")
    public ResponseEntity<?> getPolicyDetail(@PathVariable Long id) {
        PolicyDetailResponse detail = policyService.getDetail(id);
        return ResponseFactory.success(detail, "Policy detail retrieved");
    }

    @PostMapping("/purchase")
    @Operation(summary = "Purchase policy directly", description = "Direct policy purchase (deprecated — use /apply instead)")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> purchasePolicy(@Valid @RequestBody PurchasePolicyRequest request) {
        PolicyDetailResponse policy = policyService.purchase(request);
        return ResponseFactory.success(policy, "Policy purchased successfully", org.springframework.http.HttpStatus.CREATED);
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pay premium", description = "Pay premium to activate a pending policy")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> payPremium(@PathVariable Long id) {
        PolicyDetailResponse policy = policyService.payPremium(id);
        return ResponseFactory.success(policy, "Premium paid. Policy is now ACTIVE.");
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel policy", description = "Cancel an active policy")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<?> cancelPolicy(@PathVariable Long id) {
        PolicySummaryResponse policy = policyService.cancel(id);
        return ResponseFactory.success(policy, "Policy cancelled successfully");
    }

    // ═══════════ NAVIGATION HELPERS ═══════════

    @GetMapping("/status/has-active")
    @Operation(summary = "Check active policies", description = "Check if user has any active policies")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Boolean> hasActivePolicies() {
        return ResponseEntity.ok(policyService.hasActivePolicies());
    }

    @GetMapping("/status/has-paid-premium")
    @Operation(summary = "Check paid premium", description = "Check if user has paid any premium")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<Boolean> hasPaidPremium() {
        return ResponseEntity.ok(policyService.hasPaidPremium());
    }

    // ═══════════ ADMIN ENDPOINTS ═══════════

    @GetMapping("/admin/all")
    @Operation(summary = "Get all policies (Admin)", description = "List all policies across all users")
    @PreAuthorize("hasRole('POLICY_ADMIN') or hasRole('IAM_ADMIN')")
    public ResponseEntity<?> getAllPolicies() {
        List<PolicySummaryResponse> policies = policyService.getAllPolicies();
        return ResponseFactory.success(policies, "All policies retrieved successfully");
    }

    @GetMapping("/admin/applications")
    @Operation(summary = "Get all applications (Admin)", description = "List all policy applications for admin monitoring")
    @PreAuthorize("hasRole('POLICY_ADMIN') or hasRole('IAM_ADMIN')")
    public ResponseEntity<?> getAdminApplications() {
        return ResponseFactory.success(policyService.getAdminApplications(), "Applications retrieved for admin");
    }
}
