package com.investrac.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Rate limiter key resolvers for Spring Cloud Gateway.
 *
 * ipKeyResolver   — rate limit by client IP address (used on auth endpoints)
 * userKeyResolver — rate limit by authenticated user ID from X-User-Id header
 *                   falls back to IP if header not present
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit by client IP address.
     * Used for auth endpoints to prevent brute force attacks.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return (ServerWebExchange exchange) -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * Rate limit by authenticated user ID.
     * Falls back to IP address for unauthenticated requests.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return (ServerWebExchange exchange) -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just(userId);
            }
            // Fall back to IP
            String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
            return Mono.just(ip);
        };
    }
}
