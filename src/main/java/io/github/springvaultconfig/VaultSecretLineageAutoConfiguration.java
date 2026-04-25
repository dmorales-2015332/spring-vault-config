package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretLineageService}.
 *
 * <p>Enabled by default when a {@link VaultTemplate} bean is present.
 * Can be disabled via {@code spring.vault.lineage.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnBean(VaultTemplate.class)
@ConditionalOnProperty(name = "spring.vault.lineage.enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretLineageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretLineageService vaultSecretLineageService(VaultTemplate vaultTemplate) {
        return new VaultSecretLineageService(vaultTemplate);
    }
}
