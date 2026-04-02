package com.thehalo.halobackend.ai.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple token-bucket rate limiter for AI endpoint protection.
 * <p>
 * Limits: 10 requests/minute per authenticated user.
 * Uses a sliding window approach backed by an in-memory {@link ConcurrentHashMap}.
 */
@Component
@Slf4j
public class AiRateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    private record RateBucket(AtomicInteger count, long windowStart) {}

    private final ConcurrentHashMap<Long, RateBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Check if the user is within their rate limit.
     *
     * @param userId the authenticated user's ID
     * @return {@code true} if the request is allowed, {@code false} if throttled
     */
    public boolean isAllowed(Long userId) {
        long now = System.currentTimeMillis();

        RateBucket bucket = buckets.compute(userId, (id, existing) -> {
            if (existing == null || (now - existing.windowStart()) > WINDOW_MS) {
                return new RateBucket(new AtomicInteger(1), now);
            }
            return existing;
        });

        int current = bucket.count().incrementAndGet();

        if (current > MAX_REQUESTS_PER_MINUTE) {
            log.warn("AiRateLimiter: Rate limit exceeded for userId={} ({} requests/min)", userId, current);
            return false;
        }
        return true;
    }
}
