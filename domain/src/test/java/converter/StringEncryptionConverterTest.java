package converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.AesEncryptionUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StringEncryptionConverter.
 * Tests the JPA AttributeConverter with mocked encryption utility.
 */
class StringEncryptionConverterTest {

    @Mock
    private AesEncryptionUtil mockEncryptionUtil;

    private StringEncryptionConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new StringEncryptionConverter(mockEncryptionUtil);
    }

    @Test
    @DisplayName("Should encrypt attribute when converting to database column")
    void testConvertToDatabaseColumn() {
        // Given
        String plaintext = "shpat_test_token_12345";
        String expectedEncrypted = "encrypted_base64_string";
        when(mockEncryptionUtil.encrypt(plaintext)).thenReturn(expectedEncrypted);

        // When
        String result = converter.convertToDatabaseColumn(plaintext);

        // Then
        assertEquals(expectedEncrypted, result);
        verify(mockEncryptionUtil, times(1)).encrypt(plaintext);
    }

    @Test
    @DisplayName("Should return null when converting null attribute to database column")
    void testConvertToDatabaseColumnNull() {
        // When
        String result = converter.convertToDatabaseColumn(null);

        // Then
        assertNull(result);
        verify(mockEncryptionUtil, never()).encrypt(anyString());
    }

    @Test
    @DisplayName("Should decrypt data when converting to entity attribute")
    void testConvertToEntityAttribute() {
        // Given
        String encrypted = "encrypted_base64_string";
        String expectedPlaintext = "shpat_test_token_12345";
        when(mockEncryptionUtil.decrypt(encrypted)).thenReturn(expectedPlaintext);

        // When
        String result = converter.convertToEntityAttribute(encrypted);

        // Then
        assertEquals(expectedPlaintext, result);
        verify(mockEncryptionUtil, times(1)).decrypt(encrypted);
    }

    @Test
    @DisplayName("Should return null when converting null database data to entity attribute")
    void testConvertToEntityAttributeNull() {
        // When
        String result = converter.convertToEntityAttribute(null);

        // Then
        assertNull(result);
        verify(mockEncryptionUtil, never()).decrypt(anyString());
    }

    @Test
    @DisplayName("Should throw RuntimeException when encryption fails")
    void testEncryptionFailure() {
        // Given
        String plaintext = "test-token";
        when(mockEncryptionUtil.encrypt(plaintext))
                .thenThrow(new RuntimeException("Encryption failed"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            converter.convertToDatabaseColumn(plaintext);
        });
    }

    @Test
    @DisplayName("Should return plaintext on decryption failure (backward compatibility)")
    void testDecryptionFailureBackwardCompatibility() {
        // Given - Old plaintext token stored before encryption was enabled
        String oldPlaintextToken = "shpat_old_unencrypted_token";
        when(mockEncryptionUtil.decrypt(oldPlaintextToken))
                .thenThrow(new RuntimeException("Decryption failed - not encrypted data"));

        // When
        String result = converter.convertToEntityAttribute(oldPlaintextToken);

        // Then - Should return the original plaintext for backward compatibility
        assertEquals(oldPlaintextToken, result);
        verify(mockEncryptionUtil, times(1)).decrypt(oldPlaintextToken);
    }

    @Test
    @DisplayName("Should handle empty string encryption")
    void testEmptyStringEncryption() {
        // Given
        String empty = "";
        String encryptedEmpty = "encrypted_empty_string";
        when(mockEncryptionUtil.encrypt(empty)).thenReturn(encryptedEmpty);

        // When
        String result = converter.convertToDatabaseColumn(empty);

        // Then
        assertEquals(encryptedEmpty, result);
        verify(mockEncryptionUtil, times(1)).encrypt(empty);
    }

    @Test
    @DisplayName("Should handle empty string decryption")
    void testEmptyStringDecryption() {
        // Given
        String encryptedEmpty = "encrypted_empty_string";
        String empty = "";
        when(mockEncryptionUtil.decrypt(encryptedEmpty)).thenReturn(empty);

        // When
        String result = converter.convertToEntityAttribute(encryptedEmpty);

        // Then
        assertEquals(empty, result);
        verify(mockEncryptionUtil, times(1)).decrypt(encryptedEmpty);
    }
}
