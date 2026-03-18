package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.dashboard.UnderwriterDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/underwriter/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNDERWRITER')")
@Tag(name = "Underwriter Dashboard", description = "Dashboard for underwriters to view premium calculations")
public class UnderwriterDashboardController {

    private final UnderwriterDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Get underwriter dashboard overview", description = "Returns statistics on quotes and policies")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getOverview() {
        return ResponseFactory.success(dashboardService.getOverview(), "Dashboard overview retrieved");
    }

    @GetMapping("/premium-calculations")
    @Operation(summary = "Get all premium calculations", description = "Returns all quotes with premium calculation details")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getPremiumCalculations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseFactory.success(
            dashboardService.getPremiumCalculations(page, size), 
            "Premium calculations retrieved"
        );
    }

    @GetMapping("/premium-calculations/{quoteId}")
    @Operation(summary = "Get detailed premium calculation", description = "Returns detailed breakdown of premium calculation for a specific quote")
    public ResponseEntity<HaloApiResponse<Map<String, Object>>> getPremiumCalculationDetail(@PathVariable Long quoteId) {
        return ResponseFactory.success(
            dashboardService.getPremiumCalculationDetail(quoteId), 
            "Premium calculation detail retrieved"
        );
    }
}
