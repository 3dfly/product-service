package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AesEncryptionUtil.
 * Tests encryption, decryption, and edge cases.
 */
class AesEncryptionUtilTest {

    private AesEncryptionUtil encryptionUtil;
    private static final String TEST_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="; // 32-byte key

    @BeforeEach
    void setUp() {
        encryptionUtil = new AesEncryptionUtil(TEST_KEY);
    }

    @Test
    @DisplayName("Should encrypt and decrypt text successfully")
    void testEncryptDecrypt() {
        // Given
        String plaintext = "shpat_1234567890abcdefghijklmnopqrstuvwxyz";

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted); // Encrypted should be different
        assertEquals(plaintext, decrypted); // Decrypted should match original
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        // When/Then
        assertNull(encryptionUtil.encrypt(null));
        assertNull(encryptionUtil.decrypt(null));
    }

    @Test
    @DisplayName("Should handle empty string")
    void testEmptyString() {
        // Given
        String plaintext = "";

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should produce different ciphertext for same plaintext due to unique IV")
    void testUniqueIVs() {
        // Given
        String plaintext = "test-token-123";

        // When
        String encrypted1 = encryptionUtil.encrypt(plaintext);
        String encrypted2 = encryptionUtil.encrypt(plaintext);

        // Then
        assertNotEquals(encrypted1, encrypted2); // Different IVs = different ciphertext
        assertEquals(plaintext, encryptionUtil.decrypt(encrypted1));
        assertEquals(plaintext, encryptionUtil.decrypt(encrypted2));
    }

    @Test
    @DisplayName("Should handle long text (e.g., access tokens)")
    void testLongText() {
        // Given - Simulating a long Shopify access token
        String plaintext = "shpat_" + "a".repeat(500);

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should handle special characters")
    void testSpecialCharacters() {
        // Given
        String plaintext = "token!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should handle Unicode characters")
    void testUnicodeCharacters() {
        // Given
        String plaintext = "token-with-emoji-🔒🔐🗝️";

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should throw exception for invalid encryption key")
    void testInvalidKey() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            new AesEncryptionUtil("invalid-key");
        });
    }

    @Test
    @DisplayName("Should throw exception for null encryption key")
    void testNullKey() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            new AesEncryptionUtil(null);
        });
    }

    @Test
    @DisplayName("Should throw exception for empty encryption key")
    void testEmptyKey() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            new AesEncryptionUtil("");
        });
    }

    @Test
    @DisplayName("Should throw exception for wrong key length")
    void testWrongKeyLength() {
        // Given - 16 bytes (128 bits) instead of 32 bytes (256 bits)
        String shortKey = Base64.getEncoder().encodeToString("0123456789abcdef".getBytes());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            new AesEncryptionUtil(shortKey);
        });
    }

    @Test
    @DisplayName("Should throw exception for tampered ciphertext")
    void testTamperedCiphertext() {
        // Given
        String plaintext = "secret-token";
        String encrypted = encryptionUtil.encrypt(plaintext);

        // Tamper with the encrypted data
        byte[] tamperedBytes = Base64.getDecoder().decode(encrypted);
        tamperedBytes[tamperedBytes.length - 1] ^= 0xFF; // Flip bits in last byte
        String tampered = Base64.getEncoder().encodeToString(tamperedBytes);

        // When/Then - Should fail authentication tag verification
        assertThrows(RuntimeException.class, () -> {
            encryptionUtil.decrypt(tampered);
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid Base64 ciphertext")
    void testInvalidBase64() {
        // Given
        String invalidBase64 = "not-valid-base64!@#$";

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            encryptionUtil.decrypt(invalidBase64);
        });
    }

    @Test
    @DisplayName("Should generate valid 256-bit keys")
    void testGenerateKey() {
        // When
        String generatedKey = AesEncryptionUtil.generateKey();

        // Then
        assertNotNull(generatedKey);
        byte[] keyBytes = Base64.getDecoder().decode(generatedKey);
        assertEquals(32, keyBytes.length); // 256 bits = 32 bytes

        // Verify the generated key works
        AesEncryptionUtil util = new AesEncryptionUtil(generatedKey);
        String plaintext = "test";
        String encrypted = util.encrypt(plaintext);
        String decrypted = util.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should produce Base64-encoded output")
    void testBase64Output() {
        // Given
        String plaintext = "test-token";

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);

        // Then
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encrypted));
    }

    @Test
    @DisplayName("Encrypted output should be longer than input (due to IV and auth tag)")
    void testEncryptedLength() {
        // Given
        String plaintext = "short";

        // When
        String encrypted = encryptionUtil.encrypt(plaintext);
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

        // Then
        // 12 bytes IV + encrypted data + 16 bytes auth tag
        assertTrue(encryptedBytes.length > plaintext.length());
        assertTrue(encryptedBytes.length >= 12 + plaintext.length() + 16);
    }
}
