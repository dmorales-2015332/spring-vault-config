package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretComplianceService}.
 */
@Configuration
@EnableConfigurationProperties(VaultSecretComplianceProperties.class)
@ConditionalOnProperty(prefix = "vault.compliance", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretComplianceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretComplianceService vaultSecretComplianceService(
            VaultTemplate vaultTemplate,
            VaultSecretComplianceProperties properties) {
        return new VaultSecretComplianceService(vaultTemplate, properties);
    }
}
