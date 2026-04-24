package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretPromotionService}.
 *
 * <p>Enabled when {@code spring.vault.promotion.enabled=true} (default: true)
 * and a {@link VaultTemplate} bean is present.
 */
@AutoConfiguration
@ConditionalOnBean(VaultTemplate.class)
@ConditionalOnProperty(
        prefix = "spring.vault.promotion",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretPromotionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretPromotionService vaultSecretPromotionService(VaultTemplate vaultTemplate) {
        return new VaultSecretPromotionService(vaultTemplate);
    }
}
