package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.dashboard.OfficerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/officer/dashboard")
@Tag(name = "Officer Dashboard", description = "Performance metrics and analytics for Claims Officers")
@RequiredArgsConstructor
public class OfficerDashboardController {

    private final OfficerDashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get officer overview", description = "Returns personal performance metrics for the current officer")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = dashboardService.getOverview();
        return ResponseFactory.success(overview, "Officer overview retrieved");
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get claims analytics", description = "Returns system-wide claims analytics")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getAnalytics() {
        Map<String, Object> analytics = dashboardService.getClaimsAnalytics();
        return ResponseFactory.success(analytics, "Claims analytics retrieved");
    }
}
