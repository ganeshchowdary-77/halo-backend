package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.dashboard.InfluencerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/influencer/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INFLUENCER')")
@Tag(name = "Influencer Dashboard", description = "Dashboard overview for influencers")
public class InfluencerDashboardController {

    private final InfluencerDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Get dashboard overview", description = "Returns policy summary, payment dues, and quick stats")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getDashboardOverview() {
        return ResponseFactory.success(dashboardService.getDashboardOverview(), "Dashboard overview retrieved");
    }

    @GetMapping("/payment-dues")
    @Operation(summary = "Get upcoming payment dues", description = "Returns all upcoming payments within next 30 days")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getUpcomingPaymentDues() {
        return ResponseFactory.success(dashboardService.getUpcomingPaymentDues(), "Payment dues retrieved");
    }

    @GetMapping("/policies/active")
    @Operation(summary = "Get active policies summary", description = "Returns summary of all active policies")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getActivePolicies() {
        return ResponseFactory.success(dashboardService.getActivePoliciesSummary(), "Active policies retrieved");
    }
}
