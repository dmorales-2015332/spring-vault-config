package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretCleanupService}.
 * Enabled when vault is configured and cleanup is not disabled.
 */
@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(
        prefix = "vault.secret.cleanup",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretCleanupAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretCleanupService vaultSecretCleanupService(VaultOperations vaultOperations) {
        return new VaultSecretCleanupService(vaultOperations);
    }
}
