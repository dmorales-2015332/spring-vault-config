package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration that registers {@link VaultLeaseRenewalService} when
 * lease renewal is enabled and a {@link VaultOperations} bean is present.
 */
@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(prefix = "vault", name = "lease-renewal-enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(VaultOperations.class)
public class VaultLeaseRenewalAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultLeaseRenewalService vaultLeaseRenewalService(
            VaultOperations vaultOperations,
            VaultConfigProperties properties) {
        return new VaultLeaseRenewalService(vaultOperations, properties);
    }
}
