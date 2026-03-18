package com.investrac.auth.service;

import com.investrac.auth.exception.AuthException;
import com.investrac.common.dto.ErrorCodes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Service using RS256 (asymmetric RSA keys)
 * - PRIVATE key: only in auth-service (signs tokens)
 * - PUBLIC key:  in all services + API Gateway (verifies tokens)
 *
 * Why RS256 over HS256:
 * With HS256, any service that can verify tokens can also CREATE tokens (same secret).
 * With RS256, only auth-service (has private key) can create tokens.
 * All other services can only verify — much more secure.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${security.jwt.private-key}")
    private String privateKeyBase64;

    @Value("${security.jwt.public-key}")
    private String publicKeyBase64;

    @Value("${security.jwt.access-token-expiry:900000}")     // 15 minutes
    private long accessTokenExpiry;

    /**
     * Generate JWT access token with user context.
     */
    public String generateAccessToken(Long userId, String email, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("type", "ACCESS");

        return Jwts.builder()
            .claims(claims)
            .subject(String.valueOf(userId))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
            .id(UUID.randomUUID().toString())
            .signWith(getPrivateKey())
            .compact();
    }

    /**
     * Generate a cryptographically secure refresh token (not a JWT).
     * Stored in DB, rotated on every use.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Validate and extract claims from JWT.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getPublicKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // ── Key loading ──

    private PrivateKey getPrivateKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            log.error("Failed to load JWT private key: {}", e.getMessage());
            throw new AuthException(
                ErrorCodes.INTERNAL_ERROR,
                "JWT configuration error",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private PublicKey getPublicKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            log.error("Failed to load JWT public key: {}", e.getMessage());
            throw new AuthException(
                ErrorCodes.INTERNAL_ERROR,
                "JWT configuration error",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
