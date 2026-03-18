package com.thehalo.halobackend.service.auth;

import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import com.thehalo.halobackend.dto.auth.request.RefreshTokenRequest;
import com.thehalo.halobackend.dto.auth.request.RegisterRequest;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.exception.domain.auth.EmailAlreadyRegisteredException;
import com.thehalo.halobackend.exception.domain.auth.RoleNotFoundException;
import com.thehalo.halobackend.exception.domain.auth.UserNotFoundException;
import com.thehalo.halobackend.exception.security.InvalidCredentialsException;
import com.thehalo.halobackend.exception.security.InvalidRefreshTokenException;
import com.thehalo.halobackend.exception.security.RefreshTokenExpiredException;

import static com.thehalo.halobackend.service.auth.AuthConstants.MILLISECONDS_TO_SECONDS;
import static com.thehalo.halobackend.service.auth.AuthConstants.TOKEN_TYPE_BEARER;
import com.thehalo.halobackend.model.user.AppRole;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.AppRoleRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.security.config.JwtProperties;
import com.thehalo.halobackend.security.service.JwtService;
import com.thehalo.halobackend.security.service.JwtService.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }

        AppRole role = roleRepository.findByName(RoleName.INFLUENCER)
                .orElseThrow(() -> new RoleNotFoundException(RoleName.INFLUENCER.name()));

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        AppUser saved = userRepository.save(user);
        return buildResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()));

            AppUser user = userRepository
                    .findByEmailWithRole(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

            return buildResponse(user);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new InvalidCredentialsException(e);
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Validate refresh token
        TokenValidationResult validation = jwtService.validateToken(refreshToken);
        if (!validation.isValid()) {
            if (validation.isExpired()) {
                throw new RefreshTokenExpiredException("Refresh token has expired");
            }
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        
        // Check if token is revoked
        if (jwtService.isRefreshTokenRevoked(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        
        // Get user from token
        String email = jwtService.extractEmail(refreshToken);
        AppUser user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        
        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        // Revoke old refresh token
        jwtService.revokeRefreshToken(refreshToken);
        
        return buildResponseWithTokens(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            jwtService.revokeAllUserTokens(userId);
        }
    }

    private AuthResponse buildResponse(AppUser user) {
        if (user.getRole() == null) {
            throw new RoleNotFoundException("User has no assigned role");
        }
        
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.getOrCreateRefreshToken(user);
        
        return buildResponseWithTokens(user, accessToken, refreshToken);
    }

    private AuthResponse buildResponseWithTokens(AppUser user, String accessToken, String refreshToken) {
        String roleName = user.getRole().getName().name();
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(jwtProperties.getAccessTokenExpiration() / MILLISECONDS_TO_SECONDS)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .build();
    }
}
