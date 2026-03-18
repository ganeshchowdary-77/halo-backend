package com.thehalo.halobackend.security.service;

import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.system.RefreshToken;
import com.thehalo.halobackend.repository.RefreshTokenRepository;
import com.thehalo.halobackend.security.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostConstruct
    public void validateConfiguration() {
        String secret = jwtProperties.getSecret();
        
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 256 bits (32 characters). " +
                "Set app.jwt.secret environment variable or property."
            );
        }
        
        if (secret.equals("dev-secret-key-should-be-at-least-32-characters-long")) {
        }
    }

    public String generateAccessToken(AppUser user) {
        String role = user.getRole().getName().name();
            
        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getId())
            .claim("role", role)
            .claim("name", user.getFullName())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(AppUser user) {
        String token = Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getId())
            .claim("type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
        
        // Store refresh token in database for revocation capability
        RefreshToken refreshTokenEntity = RefreshToken.builder()
            .token(token)
            .user(user)
            .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
            .revoked(false)
            .build();
            
        refreshTokenRepository.save(refreshTokenEntity);
        
        return token;
    }

    public String getOrCreateRefreshToken(AppUser user) {
        return refreshTokenRepository
            .findFirstByUserIdAndRevokedFalseAndExpiresAtAfterOrderByExpiresAtDesc(user.getId(), LocalDateTime.now())
            .map(RefreshToken::getToken)
            .orElseGet(() -> generateRefreshToken(user));
    }

    public TokenValidationResult validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return TokenValidationResult.valid(claims);
        } catch (ExpiredJwtException e) {
            return TokenValidationResult.expired();
        } catch (JwtException e) {
            return TokenValidationResult.invalid();
        }
    }

    public boolean isRefreshTokenRevoked(String token) {
        return !refreshTokenRepository.existsByTokenAndRevokedFalse(token);
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token)
            .ifPresent(refreshToken -> {
                refreshToken.setRevoked(true);
                refreshTokenRepository.save(refreshToken);
            });
    }

    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Inner class for validation result
    public static class TokenValidationResult {
        private final boolean valid;
        private final boolean expired;
        private final Claims claims;

        private TokenValidationResult(boolean valid, boolean expired, Claims claims) {
            this.valid = valid;
            this.expired = expired;
            this.claims = claims;
        }

        public static TokenValidationResult valid(Claims claims) {
            return new TokenValidationResult(true, false, claims);
        }

        public static TokenValidationResult expired() {
            return new TokenValidationResult(false, true, null);
        }

        public static TokenValidationResult invalid() {
            return new TokenValidationResult(false, false, null);
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isExpired() {
            return expired;
        }

        public Claims getClaims() {
            return claims;
        }
    }
}
