package converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.AesEncryptionUtil;

/**
 * JPA AttributeConverter for automatic encryption and decryption of String fields.
 *
 * <p>This converter uses AES-GCM encryption to protect sensitive data stored in the database.
 * It automatically encrypts data before writing to the database and decrypts when reading.</p>
 *
 * <p>Usage: Add {@code @Convert(converter = StringEncryptionConverter.class)} to entity fields
 * that should be encrypted.</p>
 *
 * <p>Note: This converter handles backward compatibility with existing unencrypted data.
 * If decryption fails, it assumes the data is in plaintext and returns it as-is.</p>
 */
@Component
@Converter
public class StringEncryptionConverter implements AttributeConverter<String, String> {

    private final AesEncryptionUtil encryptionUtil;

    @Autowired
    public StringEncryptionConverter(AesEncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    /**
     * Converts the entity attribute to database column representation.
     * Encrypts the plaintext string before storing in the database.
     *
     * @param attribute The plaintext string from the entity (can be null)
     * @return Base64-encoded encrypted string for database storage, or null if input is null
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return encryptionUtil.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt attribute for database storage", e);
        }
    }

    /**
     * Converts the database column value to entity attribute representation.
     * Decrypts the encrypted string from the database.
     *
     * <p>For backward compatibility: If decryption fails (e.g., data was stored before
     * encryption was enabled), this method returns the value as-is and logs a warning.</p>
     *
     * @param dbData The encrypted string from the database (can be null)
     * @return Decrypted plaintext string for the entity, or null if input is null
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            return encryptionUtil.decrypt(dbData);
        } catch (Exception e) {
            // Backward compatibility: If decryption fails, assume it's plaintext
            // This allows reading existing unencrypted data
            System.err.println("⚠️  Failed to decrypt data - assuming plaintext format for backward compatibility: "
                + e.getMessage());
            return dbData;
        }
    }
}
