package com.thehalo.halobackend.security.service;

import com.thehalo.halobackend.exception.security.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class RateLimiterService {

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public void checkRateLimit(String key, int maxRequests, Duration window) {
        RateLimitBucket bucket = buckets.computeIfAbsent(key,
            k -> new RateLimitBucket(maxRequests, window));

        if (!bucket.tryConsume()) {
            int retryAfterSeconds = (int) bucket.getTimeUntilReset().getSeconds();
            log.warn("Rate limit exceeded for key: {}", key);
            throw new RateLimitExceededException(retryAfterSeconds);
        }
    }

    @Scheduled(fixedRate = 60000) // Clean up every minute
    public void cleanupExpiredBuckets() {
        int removed = 0;
        for (Map.Entry<String, RateLimitBucket> entry : buckets.entrySet()) {
            if (entry.getValue().isExpired()) {
                buckets.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("Cleaned up {} expired rate limit buckets", removed);
        }
    }

    private static class RateLimitBucket {
        private final int maxRequests;
        private final Duration window;
        private final Queue<Instant> requests;

        public RateLimitBucket(int maxRequests, Duration window) {
            this.maxRequests = maxRequests;
            this.window = window;
            this.requests = new ConcurrentLinkedQueue<>();
        }

        public synchronized boolean tryConsume() {
            Instant now = Instant.now();
            Instant cutoff = now.minus(window);

            // Remove expired requests
            requests.removeIf(timestamp -> timestamp.isBefore(cutoff));

            if (requests.size() < maxRequests) {
                requests.add(now);
                return true;
            }

            return false;
        }

        public Duration getTimeUntilReset() {
            Instant oldest = requests.peek();
            if (oldest == null) return Duration.ZERO;
            return Duration.between(Instant.now(), oldest.plus(window));
        }

        public boolean isExpired() {
            return requests.isEmpty() ||
                   requests.peek().plus(window.multipliedBy(2)).isBefore(Instant.now());
        }
    }
}
