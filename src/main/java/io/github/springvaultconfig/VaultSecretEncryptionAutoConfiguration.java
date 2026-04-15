package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretEncryptionService}.
 * Enabled when {@code spring.vault.encryption.enabled=true}.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.vault.encryption", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(VaultConfigProperties.class)
public class VaultSecretEncryptionAutoConfiguration {

    private final VaultConfigProperties properties;

    public VaultSecretEncryptionAutoConfiguration(VaultConfigProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretEncryptionService vaultSecretEncryptionService(VaultOperations vaultOperations) {
        String keyName = properties.getEncryptionKeyName() != null
                ? properties.getEncryptionKeyName()
                : "spring-vault-config";
        return new VaultSecretEncryptionService(vaultOperations, keyName);
    }
}
