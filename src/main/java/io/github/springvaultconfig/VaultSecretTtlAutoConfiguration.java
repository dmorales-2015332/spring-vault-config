package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretTtlService}.
 * Activated when a {@link VaultOperations} bean is present and the feature is not disabled.
 */
@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(name = "vault.ttl.enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretTtlAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretTtlService vaultSecretTtlService(VaultOperations vaultOperations) {
        return new VaultSecretTtlService(vaultOperations);
    }
}
