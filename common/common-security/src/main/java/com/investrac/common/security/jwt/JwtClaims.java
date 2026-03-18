package com.investrac.common.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Date;

/**
 * Typed wrapper around raw JJWT Claims.
 *
 * Every service works with JwtClaims instead of raw Claims objects.
 * This isolates JJWT dependency from service-specific code.
 *
 * Token contents (set by auth-service on login):
 *   sub   = userId (Long as String)
 *   email = user email
 *   roles = comma-separated roles ("ROLE_USER,ROLE_ADMIN")
 *   type  = "ACCESS"
 *   iat   = issued-at timestamp
 *   exp   = expiry timestamp
 *   jti   = unique token ID (UUID)
 */
@Getter
@Builder
public class JwtClaims {

    private final Long    userId;
    private final String  email;
    private final String  roles;
    private final String  tokenType;
    private final String  tokenId;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public static JwtClaims fromClaims(Claims claims) {
        return JwtClaims.builder()
            .userId(Long.parseLong(claims.getSubject()))
            .email(claims.get("email", String.class))
            .roles(claims.get("roles", String.class))
            .tokenType(claims.get("type", String.class))
            .tokenId(claims.getId())
            .issuedAt(toInstant(claims.getIssuedAt()))
            .expiresAt(toInstant(claims.getExpiration()))
            .build();
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAccessToken() {
        return "ACCESS".equals(tokenType);
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    private static Instant toInstant(Date date) {
        return date != null ? date.toInstant() : null;
    }
}
