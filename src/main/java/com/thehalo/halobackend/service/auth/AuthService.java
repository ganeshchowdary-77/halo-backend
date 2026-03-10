package com.thehalo.halobackend.service.auth;

import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import com.thehalo.halobackend.dto.auth.request.RefreshTokenRequest;
import com.thehalo.halobackend.dto.auth.request.RegisterRequest;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String authHeader);
}
