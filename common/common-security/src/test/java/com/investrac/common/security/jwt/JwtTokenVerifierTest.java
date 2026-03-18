package com.investrac.common.security.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.*;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenVerifier Tests")
class JwtTokenVerifierTest {

    @Mock JwtPublicKeyProvider publicKeyProvider;
    @InjectMocks JwtTokenVerifier verifier;

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a real RSA key pair for tests
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();

        when(publicKeyProvider.getPublicKey()).thenReturn(keyPair.getPublic());
    }

    private String buildToken(Long userId, String email, String roles, int expiryMs) {
        return Jwts.builder()
            .claims(Map.of("email", email, "roles", roles, "type", "ACCESS"))
            .subject(String.valueOf(userId))
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(keyPair.getPrivate())
            .compact();
    }

    // ── VALID TOKENS ────────────────────────────────────────────

    @Test
    @DisplayName("verify: returns claims for valid token")
    void verify_ValidToken_ReturnsClaims() {
        String token = buildToken(100L, "arjun@investrac.in", "ROLE_USER", 900_000);
        Optional<JwtClaims> result = verifier.verify(token);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(100L);
        assertThat(result.get().getEmail()).isEqualTo("arjun@investrac.in");
        assertThat(result.get().getRoles()).isEqualTo("ROLE_USER");
        assertThat(result.get().isAccessToken()).isTrue();
    }

    @Test
    @DisplayName("verify: returns claims with correct userId when subject is string number")
    void verify_SubjectParsedAsLong() {
        String token = buildToken(99999L, "user@test.in", "ROLE_USER", 900_000);
        Optional<JwtClaims> result = verifier.verify(token);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(99999L);
    }

    @Test
    @DisplayName("isValid: returns true for valid token")
    void isValid_ValidToken_ReturnsTrue() {
        String token = buildToken(1L, "test@investrac.in", "ROLE_USER", 900_000);
        assertThat(verifier.isValid(token)).isTrue();
    }

    // ── EXPIRED TOKENS ──────────────────────────────────────────

    @Test
    @DisplayName("verify: returns empty for expired token")
    void verify_ExpiredToken_ReturnsEmpty() {
        // Already expired (negative expiry)
        String token = buildToken(100L, "arjun@investrac.in", "ROLE_USER", -1000);
        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    @DisplayName("isValid: returns false for expired token")
    void isValid_ExpiredToken_ReturnsFalse() {
        String token = buildToken(100L, "arjun@investrac.in", "ROLE_USER", -1000);
        assertThat(verifier.isValid(token)).isFalse();
    }

    // ── INVALID TOKENS ──────────────────────────────────────────

    @Test
    @DisplayName("verify: returns empty for token signed with different key")
    void verify_WrongSigningKey_ReturnsEmpty() throws Exception {
        // Generate a different key pair and sign with it
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair wrongPair = gen.generateKeyPair();

        String token = Jwts.builder()
            .subject("100")
            .expiration(new Date(System.currentTimeMillis() + 900_000))
            .signWith(wrongPair.getPrivate())  // Sign with WRONG key
            .compact();

        // Verifier uses the correct public key — should reject
        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    @DisplayName("verify: returns empty for malformed token")
    void verify_MalformedToken_ReturnsEmpty() {
        assertThat(verifier.verify("this.is.not.a.jwt")).isEmpty();
        assertThat(verifier.verify("")).isEmpty();
        assertThat(verifier.verify("Bearer abc123")).isEmpty();
    }

    @Test
    @DisplayName("verify: returns empty for null token")
    void verify_NullToken_ReturnsEmpty() {
        assertThat(verifier.verify(null)).isEmpty();
    }

    // ── JwtClaims ───────────────────────────────────────────────

    @Test
    @DisplayName("JwtClaims.hasRole: correctly detects role in comma-separated list")
    void jwtClaims_hasRole_CorrectlyDetectsRole() {
        JwtClaims claims = JwtClaims.builder()
            .userId(1L).email("a@b.com")
            .roles("ROLE_USER,ROLE_ADMIN")
            .tokenType("ACCESS")
            .build();

        assertThat(claims.hasRole("ROLE_USER")).isTrue();
        assertThat(claims.hasRole("ROLE_ADMIN")).isTrue();
        assertThat(claims.hasRole("ROLE_SUPERADMIN")).isFalse();
    }

    @Test
    @DisplayName("JwtClaims.isAccessToken: true for ACCESS type")
    void jwtClaims_isAccessToken() {
        JwtClaims accessClaims = JwtClaims.builder()
            .userId(1L).tokenType("ACCESS").build();
        JwtClaims refreshClaims = JwtClaims.builder()
            .userId(1L).tokenType("REFRESH").build();

        assertThat(accessClaims.isAccessToken()).isTrue();
        assertThat(refreshClaims.isAccessToken()).isFalse();
    }
}
