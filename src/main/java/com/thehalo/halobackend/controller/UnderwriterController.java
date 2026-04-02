package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.service.policy.PolicyApplicationService;
import com.thehalo.halobackend.ai.AiRiskService;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UnderwriterController — Drastically simplified.
 * No queue picking, no claiming/releasing. High-risk applications are auto-assigned.
 * Underwriters just see their assigned applications and approve/reject.
 */
@RestController
@RequestMapping("/api/v1/underwriter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNDERWRITER')")
@Tag(name = "Underwriter", description = "Underwriter application review")
@SecurityRequirement(name = "bearerAuth")
public class UnderwriterController {

    private final PolicyApplicationService applicationService;
    private final AiRiskService aiRiskService;

    // ═══════════ ASSIGNED APPLICATION ENDPOINTS ═══════════

    @GetMapping("/applications/assigned")
    @Operation(summary = "Get assigned applications", description = "View high-risk applications auto-assigned to this underwriter")
    public ResponseEntity<?> getAssignedApplications(Authentication auth) {
        Long underwriterId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        List<PolicyApplicationDetailResponse> apps = applicationService.getAssignedApplications(underwriterId);
        return ResponseFactory.success(apps, "Assigned applications retrieved");
    }

    @GetMapping("/applications/{id}")
    @Operation(summary = "Get application detail", description = "Full detail of a specific application for review")
    public ResponseEntity<?> getApplicationDetail(@PathVariable Long id) {
        PolicyApplicationDetailResponse detail = applicationService.getApplicationDetail(id);
        return ResponseFactory.success(detail, "Application detail retrieved");
    }

    @PostMapping("/applications/{id}/approve")
    @Operation(summary = "Approve application", description = "Approve a high-risk application. Creates a policy with PENDING_PAYMENT status.")
    public ResponseEntity<?> approveApplication(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        PolicyApplicationDetailResponse result = applicationService.approveApplication(id, notes);
        return ResponseFactory.success(result, "Application approved. Policy created.");
    }

    @PostMapping("/applications/{id}/reject")
    @Operation(summary = "Reject application", description = "Reject a high-risk application with a reason.")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Application rejected by underwriter.");
        PolicyApplicationDetailResponse result = applicationService.rejectApplication(id, reason);
        return ResponseFactory.success(result, "Application rejected.");
    }

    // ═══════════ AI RISK NARRATIVE ═══════════

    @PostMapping("/applications/{id}/ai-narrative")
    @Operation(summary = "Generate AI risk narrative", description = "Generate an AI-powered risk assessment narrative for an application")
    public ResponseEntity<?> generateAiRiskNarrative(@PathVariable Long id) {
        var narrative = aiRiskService.generateRiskNarrative(id);
        return ResponseFactory.success(narrative, "AI risk narrative generated");
    }
}
