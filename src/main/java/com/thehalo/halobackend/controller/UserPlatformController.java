package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.platform.request.AddPlatformRequest;
import com.thehalo.halobackend.dto.platform.request.UpdatePlatformRequest;
import com.thehalo.halobackend.dto.platform.response.PlatformDetailResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.service.user.UserPlatformService;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Influencer social media platform management
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "User Platform Management", description = "Endpoints for influencers to manage their social media platforms")
public class UserPlatformController {

    private final UserPlatformService platformService;

    // List all linked social platforms for current user
    @GetMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get my platforms", description = "Retrieves a list of all social media platforms linked by the current influencer.")
    @ApiResponse(responseCode = "200", description = "Platforms loaded successfully")
    public ResponseEntity<HaloApiResponse<List<PlatformSummaryResponse>>> getMyPlatforms() {
        return ResponseFactory.success(platformService.getMyPlatforms(), "Platforms loaded");
    }

    // Full detail of a single platform
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get platform detail", description = "Retrieves full details of a specific linked platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform details retrieved"),
            @ApiResponse(responseCode = "404", description = "Platform not found or does not belong to user")
    })
    public ResponseEntity<HaloApiResponse<PlatformDetailResponse>> getPlatform(@PathVariable Long id) {
        return ResponseFactory.success(platformService.getPlatform(id), "Platform loaded");
    }

    // Link a new social media channel
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Add a new platform", description = "Links a new social media platform for the influencer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Platform successfully linked"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<HaloApiResponse<PlatformSummaryResponse>> addPlatform(
            @RequestPart("request") @Valid AddPlatformRequest request,
            @RequestPart("addressProof") org.springframework.web.multipart.MultipartFile addressProof,
            @RequestPart("incomeProof") org.springframework.web.multipart.MultipartFile incomeProof) {
        return ResponseFactory.success(platformService.addPlatform(request, addressProof, incomeProof), "Platform linked", HttpStatus.CREATED);
    }

    // Update an existing social media channel
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Update a platform", description = "Updates an existing social media platform for the influencer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Platform not found")
    })
    public ResponseEntity<HaloApiResponse<PlatformDetailResponse>> updatePlatform(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlatformRequest request) {
        return ResponseFactory.success(platformService.updatePlatform(id, request), "Platform updated");
    }

    // Unlink / delete a social platform
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Delete a platform", description = "Unlinks and deletes a specified social media platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform successfully removed"),
            @ApiResponse(responseCode = "404", description = "Platform not found or does not belong to user")
    })
    public ResponseEntity<HaloApiResponse<Void>> deletePlatform(@PathVariable Long id) {
        platformService.deletePlatform(id);
        return ResponseFactory.success("Platform removed");
    }

    // Check if user has verified platforms (for navigation)
    @GetMapping("/verification/has-verified")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Check verified platforms", description = "Checks if the current user has any verified platforms.")
    @ApiResponse(responseCode = "200", description = "Verification status retrieved")
    public ResponseEntity<Boolean> hasVerifiedPlatforms() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(platformService.hasVerifiedPlatforms(userId));
    }

    // Check if user has any platforms
    @GetMapping("/verification/has-any")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Check any platforms", description = "Checks if the current user has any platforms.")
    @ApiResponse(responseCode = "200", description = "Platform existence status retrieved")
    public ResponseEntity<Boolean> hasAnyPlatforms() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(platformService.hasAnyPlatforms(userId));
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }
}
