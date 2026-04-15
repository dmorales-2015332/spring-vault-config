package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretTaggingService}.
 * Activated when Vault tagging is enabled and a {@link VaultOperations} bean is present.
 */
@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(
        prefix = "spring.vault",
        name = "tagging.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class VaultSecretTaggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretTaggingService vaultSecretTaggingService(
            VaultOperations vaultOperations,
            VaultConfigProperties vaultConfigProperties) {
        return new VaultSecretTaggingService(vaultOperations, vaultConfigProperties);
    }
}
