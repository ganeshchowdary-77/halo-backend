package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.enums.PlatformName;
import com.thehalo.halobackend.service.platform.SocialMediaVerificationService;
import com.thehalo.halobackend.service.platform.SocialMediaVerificationService.MockAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mock-social")
@RequiredArgsConstructor
@Tag(name = "Social Media Mock API", description = "Mock endpoints to simulate integration with external social media platforms")
public class SocialMediaMockController {

    private final SocialMediaVerificationService verificationService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Search mock social accounts", description = "Returns mock account suggestions based on platform and handle search.")
    public ResponseEntity<HaloApiResponse<List<MockAccount>>> searchAccounts(
            @RequestParam PlatformName platform,
            @RequestParam String query) {
        return ResponseFactory.success(
            verificationService.searchMockAccounts(platform, query),
            "Mock accounts retrieved"
        );
    }

    @GetMapping("/details")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Get mock account details", description = "Returns full details for a specific mock account handle.")
    public ResponseEntity<HaloApiResponse<MockAccount>> getAccountDetails(
            @RequestParam PlatformName platform,
            @RequestParam String handle) {
        MockAccount account = verificationService.getMockAccount(platform, handle);
        if (account == null) {
            return ResponseFactory.error("Mock account not found", org.springframework.http.HttpStatus.NOT_FOUND.value());
        }
        return ResponseFactory.success(account, "Mock account details retrieved");
    }
}
