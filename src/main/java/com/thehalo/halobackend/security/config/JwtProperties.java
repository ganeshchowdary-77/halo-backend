package com.thehalo.halobackend.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Secret key used for signing JWT
     * Should be 256-bit (minimum 32 characters)
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds
     */
    private long accessTokenExpiration;

    /**
     * Refresh token expiration time in milliseconds
     */
    private long refreshTokenExpiration;
}