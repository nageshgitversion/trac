package com.investrac.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * Global JWT filter — validates every request before routing.
 *
 * Public endpoints bypass JWT validation.
 * Valid JWT → extract userId, email, roles → pass as headers to downstream services.
 * Invalid/expired JWT → return 401 immediately, never reach service.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.public-key}")
    private String publicKeyBase64;

    // Endpoints that don't require JWT
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/actuator/health",
        "/actuator/info",
        "/v3/api-docs",
        "/swagger-ui"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip JWT check for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Extract Bearer token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "WLTH-1002", "Authorization header missing or invalid");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = validateToken(token);

            // Inject user context as headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Email", claims.get("email", String.class))
                .header("X-User-Roles", claims.get("roles", String.class))
                .header("X-Trace-Id", exchange.getRequest().getId())
                .build();

            log.debug("JWT validated for userId={}, path={}", claims.getSubject(), path);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for path={}", path);
            return unauthorizedResponse(exchange, "WLTH-1002", "Token has expired");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature for path={}", path);
            return unauthorizedResponse(exchange, "WLTH-1003", "Invalid token signature");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token for path={}", path);
            return unauthorizedResponse(exchange, "WLTH-1003", "Malformed token");
        } catch (Exception e) {
            log.error("JWT validation error for path={}: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "WLTH-1003", "Token validation failed");
        }
    }

    private Claims validateToken(String token) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String errorCode, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
            "{\"success\":false,\"errorCode\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
            errorCode, message, java.time.Instant.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Run before all other filters
    }
}
