package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for {@link VaultSecretCacheService}.
 * Enabled by default; disable with vault.config.cache.enabled=false.
 */
@Configuration
@EnableConfigurationProperties(VaultConfigProperties.class)
@ConditionalOnProperty(prefix = "vault.config.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretCacheService vaultSecretCacheService(VaultConfigProperties properties) {
        return new VaultSecretCacheService(properties);
    }
}
