package config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import util.AesEncryptionUtil;

/**
 * Configuration class for encryption utilities.
 * Loads encryption keys from application.properties and provides
 * AesEncryptionUtil as a Spring-managed bean.
 */
@Configuration
public class EncryptionConfig {

    @Value("${encryption.aes.key}")
    private String aesEncryptionKey;

    /**
     * Creates and configures the AES encryption utility bean.
     * The encryption key is loaded from the 'encryption.aes.key' property.
     *
     * @return Configured AesEncryptionUtil instance
     * @throws IllegalArgumentException if the encryption key is invalid
     */
    @Bean
    public AesEncryptionUtil aesEncryptionUtil() {
        return new AesEncryptionUtil(aesEncryptionKey);
    }
}
