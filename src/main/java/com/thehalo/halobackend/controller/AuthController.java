package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import com.thehalo.halobackend.dto.auth.request.RegisterRequest;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — fully public (no JWT required).
 * ResponseFactory.success() already returns ResponseEntity<ApiResponse<T>>,
 * so we return those directly to avoid double-wrapping.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

        private final AuthService authService;

        /** POST /api/v1/auth/register — creates INFLUENCER account, returns JWT */
        @PostMapping("/register")
        @Operation(summary = "Register a new influencer", description = "Creates a new influencer account and returns a JWT token.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account successfully created"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or email already exists")
        })
        public ResponseEntity<com.thehalo.halobackend.dto.common.ApiResponse<AuthResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {

                return ResponseFactory.success(
                                authService.register(request),
                                "Account created successfully",
                                HttpStatus.CREATED);
        }

        /** POST /api/v1/auth/login — accepts email, returns JWT */
        @PostMapping("/login")
        @Operation(summary = "Login to the system", description = "Authenticates a user by email and returns a JWT token.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, token generated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<com.thehalo.halobackend.dto.common.ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request) {

                return ResponseFactory.success(
                                authService.login(request),
                                "Login successful");
        }

        /** Public health-check */
        @GetMapping("/ping")
        @Operation(summary = "Health check ping", description = "Public health check endpoint.")
        public ResponseEntity<String> ping() {
                return ResponseEntity.ok("The Halo API is running");
        }

        /** POST /api/v1/auth/refresh — refreshes access token using refresh token */
        @PostMapping("/refresh")
        @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
        })
        public ResponseEntity<com.thehalo.halobackend.dto.common.ApiResponse<AuthResponse>> refresh(
                        @Valid @RequestBody com.thehalo.halobackend.dto.auth.request.RefreshTokenRequest request) {

                return ResponseFactory.success(
                                authService.refreshToken(request),
                                "Token refreshed successfully");
        }

        /** POST /api/v1/auth/logout — revokes all user tokens */
        @PostMapping("/logout")
        @Operation(summary = "Logout user", description = "Revokes all refresh tokens for the authenticated user.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logout successful"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
                authService.logout(authHeader);
                return ResponseEntity.noContent().build();
        }
}