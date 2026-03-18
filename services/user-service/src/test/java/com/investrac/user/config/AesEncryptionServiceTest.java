package com.investrac.user.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AesEncryptionService Tests")
class AesEncryptionServiceTest {

    private AesEncryptionService aes;

    // Valid 256-bit test key (32 bytes = 64 hex chars)
    private static final String TEST_KEY =
        "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20";

    @BeforeEach
    void setUp() {
        aes = new AesEncryptionService();
        ReflectionTestUtils.setField(aes, "encryptionKeyHex", TEST_KEY);
    }

    @Test
    @DisplayName("encrypt/decrypt round-trip recovers original plaintext")
    void roundTrip_EncryptThenDecrypt_RecoversPan() {
        String pan = "ABCDE1234F";
        String encrypted = aes.encrypt(pan);
        String decrypted = aes.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(pan);
        assertThat(encrypted).doesNotContain(pan);
    }

    @Test
    @DisplayName("encrypting same PAN twice produces different ciphertext (random IV)")
    void encrypt_SamePan_DifferentCiphertext() {
        String pan = "ABCDE1234F";
        String enc1 = aes.encrypt(pan);
        String enc2 = aes.encrypt(pan);

        // Random IV ensures different output each time
        assertThat(enc1).isNotEqualTo(enc2);
        // But both decrypt to same value
        assertThat(aes.decrypt(enc1)).isEqualTo(pan);
        assertThat(aes.decrypt(enc2)).isEqualTo(pan);
    }

    @Test
    @DisplayName("encrypt returns null for null input")
    void encrypt_NullInput_ReturnsNull() {
        assertThat(aes.encrypt(null)).isNull();
        assertThat(aes.encrypt("")).isNull();
        assertThat(aes.encrypt("  ")).isNull();
    }

    @Test
    @DisplayName("decrypt returns null for null input")
    void decrypt_NullInput_ReturnsNull() {
        assertThat(aes.decrypt(null)).isNull();
    }

    @Test
    @DisplayName("maskPan: correctly masks middle characters")
    void maskPan_CorrectMasking() {
        assertThat(AesEncryptionService.maskPan("ABCDE1234F"))
            .isEqualTo("ABCXX1234F");
    }

    @Test
    @DisplayName("maskPan: returns placeholder for invalid length")
    void maskPan_InvalidLength_ReturnsPlaceholder() {
        assertThat(AesEncryptionService.maskPan(null)).isEqualTo("XXXXXXXXXX");
        assertThat(AesEncryptionService.maskPan("SHORT")).isEqualTo("XXXXXXXXXX");
    }

    @Test
    @DisplayName("decrypt throws on tampered ciphertext (GCM auth tag failure)")
    void decrypt_TamperedData_ThrowsException() {
        String encrypted = aes.encrypt("ABCDE1234F");
        // Tamper with the ciphertext
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "XXXX";

        assertThatThrownBy(() -> aes.decrypt(tampered))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Decryption failed");
    }
}
