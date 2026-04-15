package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretBackupService}.
 * Enabled when {@code spring.vault.backup.enabled=true} (default: true).
 */
@AutoConfiguration
@EnableConfigurationProperties(VaultConfigProperties.class)
@ConditionalOnProperty(
        prefix = "spring.vault.backup",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretBackupAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretBackupService vaultSecretBackupService(
            VaultTemplate vaultTemplate,
            VaultConfigProperties properties) {
        return new VaultSecretBackupService(vaultTemplate, properties);
    }
}
