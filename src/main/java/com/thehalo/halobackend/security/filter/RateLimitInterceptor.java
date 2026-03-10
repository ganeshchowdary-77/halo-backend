package com.thehalo.halobackend.security.filter;

import com.thehalo.halobackend.security.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                            Object handler) throws Exception {
        String path = request.getRequestURI();
        String ipAddress = getClientIpAddress(request);

        if (path.equals("/api/v1/auth/login")) {
            rateLimiterService.checkRateLimit("login:" + ipAddress, 5, Duration.ofMinutes(1));
        } else if (path.equals("/api/v1/auth/register")) {
            rateLimiterService.checkRateLimit("register:" + ipAddress, 3, Duration.ofMinutes(1));
        }

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
