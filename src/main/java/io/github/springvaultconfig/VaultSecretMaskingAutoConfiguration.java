package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretMaskingService}.
 * Enabled by default unless {@code spring.vault.masking.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "spring.vault.masking",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretMaskingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretMaskingService vaultSecretMaskingService() {
        return new VaultSecretMaskingService();
    }
}
