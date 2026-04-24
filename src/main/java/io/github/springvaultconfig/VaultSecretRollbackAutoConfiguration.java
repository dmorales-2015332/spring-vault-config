package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretRollbackService}.
 * Activated when vault rollback is enabled and the required beans are present.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "vault", name = "rollback.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean({VaultOperations.class, VaultSecretVersioningService.class})
public class VaultSecretRollbackAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretRollbackService vaultSecretRollbackService(
            VaultOperations vaultOperations,
            VaultSecretVersioningService vaultSecretVersioningService) {
        return new VaultSecretRollbackService(vaultOperations, vaultSecretVersioningService);
    }
}
