package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that registers {@link VaultAuditLogger} when audit logging
 * is enabled via {@code spring.vault.audit.enabled=true} (default: true).
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "spring.vault.audit",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultAuditLogger vaultAuditLogger() {
        return new VaultAuditLogger();
    }
}
