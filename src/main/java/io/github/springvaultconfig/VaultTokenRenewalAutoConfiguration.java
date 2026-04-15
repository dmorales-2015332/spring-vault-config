package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultTokenRenewalService}.
 * Enabled when {@code spring.vault.token-renewal.enabled=true} (default) and
 * a {@link VaultOperations} bean is present in the context.
 */
@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "spring.vault.token-renewal",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnBean(VaultOperations.class)
public class VaultTokenRenewalAutoConfiguration {

    @Bean
    public VaultTokenRenewalService vaultTokenRenewalService(
            VaultOperations vaultOperations,
            VaultConfigProperties vaultConfigProperties) {
        return new VaultTokenRenewalService(vaultOperations, vaultConfigProperties);
    }
}
