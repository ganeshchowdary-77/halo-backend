package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.repository.ClaimRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * IAM Dashboard Controller
 * Provides system monitoring and health metrics for IAM administrators
 */
@RestController
@RequestMapping("/api/v1/iam/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('IAM_ADMIN')")
@Tag(name = "IAM Dashboard", description = "System monitoring and metrics for IAM administrators")
public class IamDashboardController {

    private final PolicyRepository policyRepository;
    private final QuoteRequestRepository quoteRepository;
    private final ClaimRepository claimRepository;
    private final AppUserRepository userRepository;
    private final UserPlatformRepository profileRepository;

    @GetMapping("/metrics")
    @Operation(summary = "Get system metrics", description = "Returns overall system health and business metrics")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Policy metrics
        Map<String, Long> policyMetrics = new HashMap<>();
        policyMetrics.put("active", policyRepository.countByStatus(PolicyStatus.ACTIVE));
        policyMetrics.put("pendingPayment", policyRepository.countByStatus(PolicyStatus.PENDING_PAYMENT));
        policyMetrics.put("cancelled", policyRepository.countByStatus(PolicyStatus.CANCELLED));
        policyMetrics.put("total", policyRepository.count());
        metrics.put("policies", policyMetrics);
        
        // Quote metrics
        Map<String, Long> quoteMetrics = new HashMap<>();
        quoteMetrics.put("pending", quoteRepository.countByStatus(QuoteStatus.PENDING));
        quoteMetrics.put("approved", quoteRepository.countByStatus(QuoteStatus.APPROVED));
        quoteMetrics.put("rejected", quoteRepository.countByStatus(QuoteStatus.REJECTED));
        quoteMetrics.put("total", quoteRepository.count());
        metrics.put("quotes", quoteMetrics);
        
        // Claim metrics
        Map<String, Long> claimMetrics = new HashMap<>();
        claimMetrics.put("submitted", claimRepository.countByStatus(ClaimStatus.SUBMITTED));
        claimMetrics.put("approved", claimRepository.countByStatus(ClaimStatus.APPROVED));
        claimMetrics.put("denied", claimRepository.countByStatus(ClaimStatus.DENIED));
        claimMetrics.put("total", claimRepository.count());
        metrics.put("claims", claimMetrics);
        
        // User metrics
        Map<String, Long> userMetrics = new HashMap<>();
        userMetrics.put("total", userRepository.count());
        metrics.put("users", userMetrics);

        // Platform metrics
        Map<String, Long> platformMetrics = new HashMap<>();
        platformMetrics.put("unverified", profileRepository.countByVerifiedFalse());
        platformMetrics.put("total", profileRepository.count());
        metrics.put("platforms", platformMetrics);
        
        return ResponseFactory.success(metrics, "System metrics retrieved successfully");
    }

    @GetMapping("/health")
    @Operation(summary = "Get system health", description = "Returns system health status")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connectivity
            userRepository.count();
            health.put("database", "UP");
            health.put("status", "HEALTHY");
            health.put("message", "All systems operational");
            
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("status", "UNHEALTHY");
            health.put("message", "Database connection failed");
            health.put("error", e.getMessage());
        }
        
        return ResponseFactory.success(health, "System health check completed");
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get system alerts", description = "Returns pending items requiring attention")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getSystemAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        
        // Pending quotes requiring review
        long pendingQuotes = quoteRepository.countByStatus(QuoteStatus.PENDING);
        if (pendingQuotes > 0) {
            alerts.put("pendingQuotes", pendingQuotes);
        }
        
        // Submitted claims requiring review
        long submittedClaims = claimRepository.countByStatus(ClaimStatus.SUBMITTED);
        if (submittedClaims > 0) {
            alerts.put("submittedClaims", submittedClaims);
        }
        
        // Policies pending payment
        long pendingPayment = policyRepository.countByStatus(PolicyStatus.PENDING_PAYMENT);
        if (pendingPayment > 0) {
            alerts.put("policiesPendingPayment", pendingPayment);
        }

        // Unverified platforms requiring review
        long pendingVerification = profileRepository.countByVerifiedFalse();
        if (pendingVerification > 0) {
            alerts.put("pendingVerification", pendingVerification);
        }
        
        alerts.put("totalAlerts", alerts.size());
        
        return ResponseFactory.success(alerts, "System alerts retrieved successfully");
    }
}
