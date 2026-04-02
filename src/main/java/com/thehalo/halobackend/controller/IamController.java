package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.dto.iam.request.UpdateStaffRequest;
import com.thehalo.halobackend.dto.iam.response.SessionSummaryResponse;
import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.repository.RefreshTokenRepository;
import com.thehalo.halobackend.service.iam.IamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/iam")
@RequiredArgsConstructor
@Tag(name = "IAM Administration", description = "Identity & Access Management endpoints for creating and managing internal staff")
public class IamController {

    private final IamService iamService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Create staff account", description = "Creates a new internal staff account (e.g., POLICY_ADMIN, UNDERWRITER). Requires IAM_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff account created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or email taken"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires IAM_ADMIN role")
    })
    public ResponseEntity<HaloApiResponse<AuthResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request) {
        return ResponseFactory.success(
                iamService.createStaff(request),
                "Staff account successfully created",
                HttpStatus.CREATED);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "List all staff", description = "Retrieves all internal staff accounts (excludes INFLUENCER role). Requires IAM_ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Staff list retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<StaffSummaryResponse>>> getAllStaff() {
        return ResponseFactory.success(iamService.getAllStaff(), "Staff list retrieved");
    }

    @GetMapping("/staff/{id}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Get staff details", description = "Retrieves details of a specific staff member. Requires IAM_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff details retrieved"),
            @ApiResponse(responseCode = "404", description = "Staff member not found")
    })
    public ResponseEntity<HaloApiResponse<StaffSummaryResponse>> getStaffById(@PathVariable Long id) {
        return ResponseFactory.success(iamService.getStaffById(id), "Staff details retrieved");
    }

    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Update staff account", description = "Updates an existing staff account. Requires IAM_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Staff member not found"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<HaloApiResponse<StaffSummaryResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request) {
        return ResponseFactory.success(iamService.updateStaff(id, request), "Staff account updated");
    }

    @DeleteMapping("/staff/{id}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Deactivate staff account", description = "Soft deletes/deactivates a staff account. Requires IAM_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff account deactivated"),
            @ApiResponse(responseCode = "404", description = "Staff member not found")
    })
    public ResponseEntity<HaloApiResponse<Void>> deactivateStaff(@PathVariable Long id) {
        iamService.deactivateStaff(id);
        return ResponseFactory.success("Staff account deactivated");
    }

    @GetMapping("/staff/by-role/{role}")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Get staff by role", description = "Retrieves all staff members with a specific role. Requires IAM_ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Staff list by role retrieved")
    public ResponseEntity<HaloApiResponse<List<StaffSummaryResponse>>> getStaffByRole(@PathVariable String role) {
        return ResponseFactory.success(iamService.getStaffByRole(role), "Staff list by role retrieved");
    }

    @GetMapping("/verification/pending")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Get unverified profiles", description = "Retrieves all influencer profiles pending verification. Requires IAM_ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Unverified profiles retrieved")
    public ResponseEntity<HaloApiResponse<List<com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse>>> getUnverifiedProfiles() {
        return ResponseFactory.success(iamService.getUnverifiedProfiles(), "Unverified profiles retrieved");
    }

    @PostMapping("/verification/{profileId}/verify")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Verify influencer profile", description = "Verifies an influencer profile, allowing them to apply for policies. Requires IAM_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile verified successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<HaloApiResponse<com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse>> verifyProfile(@PathVariable Long profileId) {
        return ResponseFactory.success(iamService.verifyProfile(profileId), "Profile verified successfully");
    }

    @PostMapping("/verification/{profileId}/verify-aspect")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(summary = "Verify specific aspect of profile", description = "Verifies or rejects a specific aspect (niche, address, income) of an influencer profile. Requires IAM_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aspect verification updated successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "400", description = "Invalid verification type")
    })
    public ResponseEntity<HaloApiResponse<com.thehalo.halobackend.dto.platform.response.PlatformVerificationResponse>> verifyAspect(
            @PathVariable Long profileId,
            @Valid @RequestBody com.thehalo.halobackend.dto.platform.request.GranularVerificationRequest request) {
        return ResponseFactory.success(
                iamService.verifyAspect(profileId, request.getVerificationType(), request.getApproved(), request.getRejectionReason()),
                "Aspect verification updated successfully");
    }

    /**
     * GET /api/v1/iam/sessions
     *
     * Returns all currently active login sessions from the refresh_tokens table.
     * A session is considered active when its refresh token is neither revoked nor expired.
     * This gives the IAM Admin a real-time view of who is logged in across the platform.
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('IAM_ADMIN')")
    @Operation(
        summary = "Get all active sessions",
        description = "Returns all non-revoked, non-expired login sessions fetched from the token repository. Requires IAM_ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Active sessions retrieved")
    public ResponseEntity<HaloApiResponse<List<SessionSummaryResponse>>> getActiveSessions() {
        List<SessionSummaryResponse> sessions = refreshTokenRepository
                .findAllByRevokedFalseAndExpiresAtAfter(LocalDateTime.now())
                .stream()
                .map(rt -> SessionSummaryResponse.builder()
                        .tokenId(rt.getId())
                        .email(rt.getUser().getEmail())
                        .fullName(rt.getUser().getFullName())
                        .role(rt.getUser().getRole().getName().name())
                        .issuedAt(rt.getCreatedAt())
                        .expiresAt(rt.getExpiresAt())
                        .revoked(rt.isRevoked())
                        .build())
                .collect(Collectors.toList());

        return ResponseFactory.success(sessions, "Active sessions retrieved");
    }
}
