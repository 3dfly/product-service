package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for AES-GCM encryption and decryption of sensitive data.
 * Uses AES-256 with Galois/Counter Mode for authenticated encryption.
 *
 * <p>This utility is designed for encrypting data at rest, particularly
 * sensitive tokens and credentials stored in the database.</p>
 *
 * <p>Storage format: Base64(IV || EncryptedData || AuthTag)</p>
 */
public class AesEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits authentication tag

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * Creates an AES encryption utility with the provided encryption key.
     *
     * @param base64Key Base64-encoded 256-bit (32 bytes) encryption key
     * @throws IllegalArgumentException if the key is invalid
     */
    public AesEncryptionUtil(String base64Key) {
        if (base64Key == null || base64Key.trim().isEmpty()) {
            throw new IllegalArgumentException("Encryption key cannot be null or empty");
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            if (decodedKey.length != 32) {
                throw new IllegalArgumentException(
                    "Invalid key length: " + decodedKey.length + " bytes. Expected 32 bytes (256 bits)"
                );
            }
            this.secretKey = new SecretKeySpec(decodedKey, "AES");
            this.secureRandom = new SecureRandom();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to decode encryption key: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a random 256-bit AES key encoded as Base64.
     * Use this method to generate a new encryption key for configuration.
     *
     * @return Base64-encoded 256-bit AES key
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, new SecureRandom());
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    /**
     * Encrypts the given plaintext string using AES-GCM.
     *
     * @param plaintext The data to encrypt (can be null)
     * @return Base64-encoded encrypted data, or null if input is null
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Combine IV + encrypted data + auth tag into single byte array
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            // Encode as Base64 for database storage
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts the given Base64-encoded ciphertext using AES-GCM.
     *
     * @param ciphertext Base64-encoded encrypted data (can be null)
     * @return Decrypted plaintext string, or null if input is null
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }

        try {
            // Decode from Base64
            byte[] decodedData = Base64.getDecoder().decode(ciphertext);

            // Extract IV and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
}
