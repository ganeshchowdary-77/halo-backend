package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.risk.request.CreateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.request.UpdateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.response.RiskParameterResponse;
import com.thehalo.halobackend.service.risk.RiskParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk-parameters")
@RequiredArgsConstructor
@Tag(name = "Risk Parameters", description = "Risk parameter management for underwriters")
@SecurityRequirement(name = "bearerAuth")
public class RiskParameterController {

    private final RiskParameterService riskParameterService;

    @PostMapping
    @Operation(summary = "Create new risk parameter", description = "Create a new risk parameter with audit trail")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> createParameter(
            @Valid @RequestBody CreateRiskParameterRequest request,
            Authentication authentication) {
        
        String createdByEmail = authentication.getName();
        RiskParameterResponse createdParameter = riskParameterService.createParameter(request, createdByEmail);
        
        return ResponseFactory.success(createdParameter, "Risk parameter created successfully");
    }

    @GetMapping
    @Operation(summary = "Get all risk parameters", description = "Retrieve all active risk parameters with pagination")
    @PreAuthorize("hasRole('UNDERWRITER') or hasRole('IAM_ADMIN')")
    public ResponseEntity<?> getAllParameters(Pageable pageable) {
        Page<RiskParameterResponse> parameters = riskParameterService.getAllParameters(pageable);
        return ResponseFactory.success(parameters, "Risk parameters retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get risk parameter by ID", description = "Retrieve a specific risk parameter")
    @PreAuthorize("hasRole('UNDERWRITER') or hasRole('IAM_ADMIN')")
    public ResponseEntity<?> getParameterById(@PathVariable Long id) {
        RiskParameterResponse parameter = riskParameterService.getParameterById(id);
        return ResponseFactory.success(parameter, "Risk parameter retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update risk parameter", description = "Update a risk parameter multiplier with audit trail")
    @PreAuthorize("hasRole('UNDERWRITER')")
    public ResponseEntity<?> updateParameter(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRiskParameterRequest request,
            Authentication authentication) {
        
        String updatedByEmail = authentication.getName();
        RiskParameterResponse updatedParameter = riskParameterService.updateParameter(id, request, updatedByEmail);
        
        return ResponseFactory.success(updatedParameter, "Risk parameter updated successfully");
    }
}