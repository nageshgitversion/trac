package com.investrac.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption for sensitive PII fields (PAN).
 *
 * Uses AES-256-GCM (Galois/Counter Mode):
 *  - Authenticated encryption — detects tampering
 *  - Random IV per encryption — same plaintext produces different ciphertext
 *  - 256-bit key from environment variable
 *
 * Output format: Base64(IV + AuthTag + Ciphertext)
 *
 * SECURITY:
 *  - Encryption key loaded from env var ENCRYPTION_KEY (32-byte hex)
 *  - Never log plaintext PAN
 *  - Never return decrypted PAN in API responses (only masked)
 */
@Component
@Slf4j
public class AesEncryptionService {

    private static final String ALGORITHM      = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LENGTH  = 12;   // 96 bits
    private static final int    GCM_TAG_LENGTH = 128;  // bits

    @Value("${security.encryption.key:0000000000000000000000000000000000000000000000000000000000000000}")
    private String encryptionKeyHex;   // 64 hex chars = 32 bytes = 256 bits

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return null;
        try {
            byte[] keyBytes = hexToBytes(encryptionKeyHex);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec paramSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Prepend IV to ciphertext
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            log.error("Encryption failed — PAN not stored");
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) return null;
        try {
            byte[] keyBytes = hexToBytes(encryptionKeyHex);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer buf = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);

            GCMParameterSpec paramSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return new String(cipher.doFinal(ciphertext), "UTF-8");
        } catch (Exception e) {
            log.error("Decryption failed for encrypted field");
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /** Mask PAN for display: ABCDE1234F → ABCXX1234F */
    public static String maskPan(String pan) {
        if (pan == null || pan.length() != 10) return "XXXXXXXXXX";
        return pan.substring(0, 3) + "XX" + pan.substring(5);
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
