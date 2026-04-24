package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretPinningService}.
 *
 * <p>Activated when {@code spring.vault.pinning.enabled=true} (default: true)
 * and a {@link VaultOperations} bean is present.
 */
@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(
        prefix = "spring.vault.pinning",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretPinningAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretPinningService vaultSecretPinningService(VaultOperations vaultOperations) {
        return new VaultSecretPinningService(vaultOperations);
    }
}
