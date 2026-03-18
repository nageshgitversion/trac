package com.investrac.common.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Singleton provider for the RSA public key used to verify JWT tokens.
 *
 * Key loaded once at startup via @PostConstruct — not on every request.
 * This avoids repeated Base64 decode + KeyFactory calls per HTTP request.
 *
 * Configuration:
 *   security.jwt.public-key = Base64-encoded DER format RSA public key
 *
 * Used by: all services except auth-service (which also has the private key)
 * Also used by: API Gateway (reactive WebFlux context)
 */
@Component
@Slf4j
public class JwtPublicKeyProvider {

    @Value("${security.jwt.public-key}")
    private String publicKeyBase64;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            log.info("JWT public key loaded successfully");
        } catch (Exception e) {
            log.error("FATAL: Failed to load JWT public key — service cannot verify tokens: {}",
                e.getMessage());
            throw new IllegalStateException("JWT public key loading failed", e);
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
