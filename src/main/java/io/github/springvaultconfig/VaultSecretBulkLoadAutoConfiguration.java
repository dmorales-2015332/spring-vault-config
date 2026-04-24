package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretBulkLoadService}.
 */
@Configuration
@EnableConfigurationProperties(VaultSecretBulkLoadProperties.class)
@ConditionalOnProperty(prefix = "vault.bulk-load", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretBulkLoadAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretBulkLoadService vaultSecretBulkLoadService(
            VaultTemplate vaultTemplate,
            VaultConfigProperties vaultConfigProperties) {
        return new VaultSecretBulkLoadService(vaultTemplate, vaultConfigProperties);
    }
}
