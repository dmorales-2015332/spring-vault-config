package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretFallbackService}.
 * Enabled by default; can be disabled via {@code spring.vault.fallback.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "spring.vault.fallback",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretFallbackAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretFallbackService vaultSecretFallbackService(VaultConfigProperties properties) {
        return new VaultSecretFallbackService(properties);
    }
}
