package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretResolverService}.
 * Activated when a {@link VaultSecretLoader} bean is present and the feature is not disabled.
 */
@AutoConfiguration
@ConditionalOnBean(VaultSecretLoader.class)
@ConditionalOnProperty(name = "vault.secret.resolver.enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretResolverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretResolverService vaultSecretResolverService(VaultSecretLoader vaultSecretLoader) {
        return new VaultSecretResolverService(vaultSecretLoader);
    }
}
