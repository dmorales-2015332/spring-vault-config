package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretScopeService}.
 */
@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(prefix = "vault", name = "scope.enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretScopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretScopeService vaultSecretScopeService(VaultOperations vaultOperations) {
        return new VaultSecretScopeService(vaultOperations);
    }
}
