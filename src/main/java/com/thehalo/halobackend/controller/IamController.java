package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.dto.iam.request.UpdateStaffRequest;
import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.service.iam.IamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam")
@RequiredArgsConstructor
@Tag(name = "IAM Administration", description = "Identity & Access Management endpoints for creating and managing internal staff")
public class IamController {

    private final IamService iamService;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Create staff account", description = "Creates a new internal staff account (e.g., POLICY_ADMIN, UNDERWRITER). Requires IAM_ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Staff account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or email taken"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires IAM_ADMIN role")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request) {
        return ResponseFactory.success(
                iamService.createStaff(request),
                "Staff account successfully created",
                HttpStatus.CREATED);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "List all staff", description = "Retrieves all internal staff accounts (excludes INFLUENCER role). Requires IAM_ADMIN role.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff list retrieved successfully")
    public ResponseEntity<ApiResponse<List<StaffSummaryResponse>>> getAllStaff() {
        return ResponseFactory.success(iamService.getAllStaff(), "Staff list retrieved");
    }

    @GetMapping("/staff/{id}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Get staff details", description = "Retrieves details of a specific staff member. Requires IAM_ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff member not found")
    })
    public ResponseEntity<ApiResponse<StaffSummaryResponse>> getStaffById(@PathVariable Long id) {
        return ResponseFactory.success(iamService.getStaffById(id), "Staff details retrieved");
    }

    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Update staff account", description = "Updates an existing staff account. Requires IAM_ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff account updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff member not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<StaffSummaryResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request) {
        return ResponseFactory.success(iamService.updateStaff(id, request), "Staff account updated");
    }

    @DeleteMapping("/staff/{id}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Deactivate staff account", description = "Soft deletes/deactivates a staff account. Requires IAM_ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff account deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Staff member not found")
    })
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(@PathVariable Long id) {
        iamService.deactivateStaff(id);
        return ResponseFactory.success("Staff account deactivated");
    }

    @GetMapping("/staff/by-role/{role}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Get staff by role", description = "Retrieves all staff members with a specific role. Requires IAM_ADMIN role.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Staff list by role retrieved")
    public ResponseEntity<ApiResponse<List<StaffSummaryResponse>>> getStaffByRole(@PathVariable String role) {
        return ResponseFactory.success(iamService.getStaffByRole(role), "Staff list by role retrieved");
    }
}
