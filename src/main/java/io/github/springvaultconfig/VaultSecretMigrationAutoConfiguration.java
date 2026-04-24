package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretMigrationService}.
 * Activated when a {@link VaultTemplate} bean is present and migration is enabled.
 */
@AutoConfiguration
@ConditionalOnBean(VaultTemplate.class)
@ConditionalOnProperty(
        prefix = "spring.vault",
        name = "migration.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class VaultSecretMigrationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretMigrationService vaultSecretMigrationService(VaultTemplate vaultTemplate) {
        return new VaultSecretMigrationService(vaultTemplate);
    }
}
