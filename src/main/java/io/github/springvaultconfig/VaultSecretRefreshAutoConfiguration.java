package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auto-configuration for {@link VaultSecretRefreshService}.
 * Enabled when {@code spring.vault.refresh-enabled=true} (default: true).
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(VaultConfigProperties.class)
@ConditionalOnProperty(name = "spring.vault.refresh-enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretRefreshAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretRefreshService vaultSecretRefreshService(VaultSecretLoader secretLoader,
                                                               ApplicationEventPublisher eventPublisher,
                                                               VaultConfigProperties properties) {
        return new VaultSecretRefreshService(secretLoader, eventPublisher, properties);
    }
}
