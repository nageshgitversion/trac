package com.investrac.common.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed rate limiter for per-user and per-IP request throttling.
 *
 * Used by auth-service for login attempt limiting.
 * Can be used by any service that needs rate limiting beyond
 * what the API Gateway provides.
 *
 * Algorithm: Sliding window counter
 *   - Key: "rate:{type}:{identifier}" (e.g., "rate:login:user@email.com")
 *   - Value: request count in the window
 *   - TTL: window duration
 *
 * Example:
 *   rateLimitingService.isAllowed("login", userEmail, 5, Duration.ofMinutes(15))
 *   → true if fewer than 5 login attempts in the last 15 minutes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitingService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "rate:";

    /**
     * Check if the action is allowed within the rate limit.
     * Increments the counter and returns true if under the limit.
     *
     * @param action      Type of action (e.g., "login", "otp", "api")
     * @param identifier  User email, IP, or userId
     * @param maxRequests Maximum allowed requests in the window
     * @param window      Time window duration
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String action, String identifier,
                              int maxRequests, Duration window) {
        String key = KEY_PREFIX + action + ":" + identifier;

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count == null) {
                log.warn("Redis increment returned null for key={}", key);
                return true; // Fail open — don't block on Redis error
            }

            if (count == 1) {
                // First request — set the expiry window
                redisTemplate.expire(key, window);
            }

            boolean allowed = count <= maxRequests;
            if (!allowed) {
                log.warn("Rate limit exceeded: action={} identifier_prefix={} count={}/{}",
                    action,
                    identifier.length() > 5 ? identifier.substring(0, 5) + "..." : "***",
                    count, maxRequests);
            }
            return allowed;

        } catch (Exception e) {
            log.error("Redis rate limit check failed for action={}: {} — failing open",
                action, e.getMessage());
            return true; // Fail open on Redis error
        }
    }

    /**
     * Get remaining allowed requests in the current window.
     */
    public long getRemainingRequests(String action, String identifier, int maxRequests) {
        String key = KEY_PREFIX + action + ":" + identifier;
        try {
            String val = redisTemplate.opsForValue().get(key);
            if (val == null) return maxRequests;
            long current = Long.parseLong(val);
            return Math.max(0, maxRequests - current);
        } catch (Exception e) {
            return maxRequests;
        }
    }

    /**
     * Reset rate limit for a user — call after successful auth.
     */
    public void reset(String action, String identifier) {
        String key = KEY_PREFIX + action + ":" + identifier;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to reset rate limit key={}: {}", key, e.getMessage());
        }
    }
}
