package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration for {@link VaultSecretLockService}.
 * Enabled when {@code vault.secret.lock.enabled=true} (default) and
 * a {@link VaultOperations} bean is present.
 */
@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(prefix = "vault.secret.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretLockService vaultSecretLockService(VaultOperations vaultOperations) {
        return new VaultSecretLockService(vaultOperations);
    }
}
