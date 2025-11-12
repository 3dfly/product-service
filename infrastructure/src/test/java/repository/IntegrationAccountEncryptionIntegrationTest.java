package repository;

import converter.StringEncryptionConverter;
import entity.IntegrationAccount;
import entity.ShopType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import util.AesEncryptionUtil;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for IntegrationAccount entity with encryption.
 * Tests the converter behavior with real encryption utility.
 */
class IntegrationAccountEncryptionIntegrationTest {

    private StringEncryptionConverter converter;
    private AesEncryptionUtil encryptionUtil;

    private IntegrationAccount testAccount;
    private static final String TEST_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @BeforeEach
    void setUp() {
        encryptionUtil = new AesEncryptionUtil(TEST_KEY);
        converter = new StringEncryptionConverter(encryptionUtil);

        testAccount = IntegrationAccount.builder()
                .shopId(1L)
                .provider(ShopType.SHOPIFY)
                .externalShopId("test-store.myshopify.com")
                .accessToken("shpat_test_token_1234567890abcdefghijklmnop")
                .scopes("read_products,write_products")
                .installedAt(Instant.now())
                .updatedAt(Instant.now())
                .metadataJson("{\"version\": \"2025-07\"}")
                .build();
    }

    @Test
    @DisplayName("Should encrypt and decrypt access token correctly")
    void testEncryptionDecryptionRoundTrip() {
        // Given
        String originalToken = testAccount.getAccessToken();

        // When - Simulate JPA converting to database column
        String encryptedForDb = converter.convertToDatabaseColumn(originalToken);

        // Then - Simulate JPA converting back to entity attribute
        String decryptedFromDb = converter.convertToEntityAttribute(encryptedForDb);

        assertEquals(originalToken, decryptedFromDb);
    }

    @Test
    @DisplayName("Should store encrypted data (not plaintext)")
    void testTokenIsEncrypted() {
        // Given
        String originalToken = testAccount.getAccessToken();

        // When - Convert to database format
        String encryptedForDb = converter.convertToDatabaseColumn(originalToken);

        // Then
        assertNotEquals(originalToken, encryptedForDb);
        assertTrue(encryptedForDb.matches("^[A-Za-z0-9+/=]+$")); // Base64 format
    }

    @Test
    @DisplayName("Should handle null access token")
    void testNullAccessToken() {
        // When/Then
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("Should handle empty string access token")
    void testEmptyAccessToken() {
        // Given
        String empty = "";

        // When
        String encrypted = converter.convertToDatabaseColumn(empty);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertEquals(empty, decrypted);
    }

    @Test
    @DisplayName("Should handle very long access tokens")
    void testLongAccessToken() {
        // Given
        String longToken = "shpat_" + "x".repeat(1000);

        // When
        String encrypted = converter.convertToDatabaseColumn(longToken);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertEquals(longToken, decrypted);
    }

    @Test
    @DisplayName("Should handle special characters in access token")
    void testSpecialCharactersInToken() {
        // Given
        String tokenWithSpecialChars = "shpat_!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

        // When
        String encrypted = converter.convertToDatabaseColumn(tokenWithSpecialChars);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertEquals(tokenWithSpecialChars, decrypted);
    }

    @Test
    @DisplayName("Should encrypt same token differently each time (unique IVs)")
    void testUniqueEncryption() {
        // Given
        String token = "shpat_same_token";

        // When - Encrypt the same token twice
        String encrypted1 = converter.convertToDatabaseColumn(token);
        String encrypted2 = converter.convertToDatabaseColumn(token);

        // Then - Different encrypted values due to unique IVs
        assertNotEquals(encrypted1, encrypted2);

        // But both decrypt to same plaintext
        assertEquals(token, converter.convertToEntityAttribute(encrypted1));
        assertEquals(token, converter.convertToEntityAttribute(encrypted2));
    }

    @Test
    @DisplayName("Should handle backward compatibility with plaintext tokens")
    void testBackwardCompatibilityWithPlaintextTokens() {
        // Given - Simulate old plaintext token stored before encryption
        String oldPlaintextToken = "shpat_old_unencrypted_token_12345";

        // When - Try to decrypt (will fail and return plaintext)
        String result = converter.convertToEntityAttribute(oldPlaintextToken);

        // Then - Should return original plaintext for backward compatibility
        assertEquals(oldPlaintextToken, result);
    }

    @Test
    @DisplayName("Should handle realistic Shopify access token format")
    void testRealisticShopifyToken() {
        // Given - Realistic Shopify access token format
        String shopifyToken = "shpat_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0";

        // When
        String encrypted = converter.convertToDatabaseColumn(shopifyToken);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertEquals(shopifyToken, decrypted);
        assertNotEquals(shopifyToken, encrypted);
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple encrypt/decrypt cycles")
    void testMultipleCycles() {
        // Given
        String originalToken = testAccount.getAccessToken();

        // When - Multiple encrypt/decrypt cycles
        String encrypted1 = converter.convertToDatabaseColumn(originalToken);
        String decrypted1 = converter.convertToEntityAttribute(encrypted1);

        String encrypted2 = converter.convertToDatabaseColumn(decrypted1);
        String decrypted2 = converter.convertToEntityAttribute(encrypted2);

        String encrypted3 = converter.convertToDatabaseColumn(decrypted2);
        String decrypted3 = converter.convertToEntityAttribute(encrypted3);

        // Then - All decrypted values should match original
        assertEquals(originalToken, decrypted1);
        assertEquals(originalToken, decrypted2);
        assertEquals(originalToken, decrypted3);

        // And encrypted values should all be different (unique IVs)
        assertNotEquals(encrypted1, encrypted2);
        assertNotEquals(encrypted2, encrypted3);
        assertNotEquals(encrypted1, encrypted3);
    }
}
