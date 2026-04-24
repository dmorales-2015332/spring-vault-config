package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretDependencyService}.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "vault",
        name = "secret-dependency.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretDependencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretDependencyService vaultSecretDependencyService() {
        return new VaultSecretDependencyService();
    }
}
