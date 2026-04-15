package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretVersioningService}.
 * Activated when vault is enabled and a VaultTemplate bean is present.
 */
@Configuration
@ConditionalOnProperty(prefix = "vault", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(VaultTemplate.class)
public class VaultSecretVersioningAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretVersioningService vaultSecretVersioningService(
            VaultTemplate vaultTemplate,
            VaultConfigProperties vaultConfigProperties) {
        return new VaultSecretVersioningService(vaultTemplate, vaultConfigProperties);
    }
}
