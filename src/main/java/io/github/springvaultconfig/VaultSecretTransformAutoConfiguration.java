package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretTransformService}.
 * Enabled by default unless {@code vault.config.transform.enabled=false}.
 */
@AutoConfiguration
@EnableConfigurationProperties(VaultConfigProperties.class)
@ConditionalOnProperty(
        prefix = "vault.config.transform",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretTransformAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretTransformService vaultSecretTransformService(VaultConfigProperties properties) {
        return new VaultSecretTransformService(properties);
    }
}
