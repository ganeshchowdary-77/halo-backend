package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.profile.request.AddProfileRequest;
import com.thehalo.halobackend.dto.profile.response.ProfileDetailResponse;
import com.thehalo.halobackend.dto.profile.response.ProfileSummaryResponse;
import com.thehalo.halobackend.service.profile.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Influencer social media profile management
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "Endpoints for influencers to manage their social media profiles")
public class UserProfileController {

    private final UserProfileService profileService;

    // List all linked social profiles for current user
    @GetMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get my profiles", description = "Retrieves a list of all social media profiles linked by the current influencer.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profiles loaded successfully")
    public ResponseEntity<ApiResponse<List<ProfileSummaryResponse>>> getMyProfiles() {
        return ResponseFactory.success(profileService.getMyProfiles(), "Profiles loaded");
    }

    // Full detail of a single profile
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get profile detail", description = "Retrieves full details of a specific linked profile.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile details retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found or does not belong to user")
    })
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> getProfile(@PathVariable Long id) {
        return ResponseFactory.success(profileService.getProfile(id), "Profile loaded");
    }

    // Link a new social media channel
    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Add a new profile", description = "Links a new social media platform profile for the influencer.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Profile successfully linked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<ProfileSummaryResponse>> addProfile(
            @Valid @RequestBody AddProfileRequest request) {
        return ResponseFactory.success(profileService.addProfile(request), "Profile linked", HttpStatus.CREATED);
    }

    // Unlink / delete a social profile
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Delete a profile", description = "Unlinks and deletes a specified social media profile.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile successfully removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found or does not belong to user")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseFactory.success("Profile removed");
    }
}
