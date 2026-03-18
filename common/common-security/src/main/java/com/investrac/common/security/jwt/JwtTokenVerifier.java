package com.investrac.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * JWT token verifier using RSA public key (RS256).
 *
 * This component is for VERIFICATION ONLY — no token generation.
 * Token generation is exclusively in auth-service (holds private key).
 *
 * Thread-safe — stateless, can be @Autowired as singleton.
 *
 * Usage in services:
 *   @Autowired JwtTokenVerifier jwtVerifier;
 *   JwtClaims claims = jwtVerifier.verify(token)
 *       .orElseThrow(() -> new UnauthorizedException());
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenVerifier {

    private final JwtPublicKeyProvider publicKeyProvider;

    /**
     * Verify and parse a JWT token.
     *
     * @param token Raw JWT string (without "Bearer " prefix)
     * @return Optional containing claims if valid, empty if invalid/expired/malformed
     */
    public Optional<JwtClaims> verify(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(publicKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return Optional.of(JwtClaims.fromClaims(claims));

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: subject={}", e.getClaims().getSubject());
            return Optional.empty();
        } catch (SignatureException e) {
            log.warn("JWT signature invalid — possible token tampering");
            return Optional.empty();
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed");
            return Optional.empty();
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract claims without throwing — returns empty on any error.
     * Use this when you want to attempt verification without error handling.
     */
    public Optional<Claims> extractClaims(String token) {
        try {
            return Optional.of(Jwts.parser()
                .verifyWith(publicKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Quick validity check — returns true if token is valid and not expired.
     */
    public boolean isValid(String token) {
        return verify(token).isPresent();
    }

    /**
     * Extract userId without full verification (for logging purposes only).
     * SECURITY: Do not use for access control — use verify() instead.
     */
    public Optional<Long> extractUserIdUnsafe(String token) {
        try {
            // Parse without verification — for logging/debugging only
            String[] parts = token.split("\\.");
            if (parts.length != 3) return Optional.empty();
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Simple subject extraction
            int subStart = payload.indexOf("\"sub\":\"") + 7;
            int subEnd   = payload.indexOf("\"", subStart);
            if (subStart < 7 || subEnd < 0) return Optional.empty();
            return Optional.of(Long.parseLong(payload.substring(subStart, subEnd)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
