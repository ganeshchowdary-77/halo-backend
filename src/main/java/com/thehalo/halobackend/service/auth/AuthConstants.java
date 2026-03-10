package com.thehalo.halobackend.service.auth;

public final class AuthConstants {
    
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    public static final long MILLISECONDS_TO_SECONDS = 1000L;
    
    private AuthConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
