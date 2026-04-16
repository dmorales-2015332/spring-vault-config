package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretFilterService}.
 * Activated when {@code vault.config.filter.enabled=true} (default: true).
 */
@AutoConfiguration
@EnableConfigurationProperties(VaultConfigProperties.class)
@ConditionalOnProperty(prefix = "vault.config.filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretFilterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretFilterService vaultSecretFilterService(VaultConfigProperties properties) {
        VaultConfigProperties.FilterProperties filter = properties.getFilter();
        return new VaultSecretFilterService(
                filter != null ? filter.getIncludePatterns() : null,
                filter != null ? filter.getExcludePatterns() : null
        );
    }
}
