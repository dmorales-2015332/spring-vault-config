package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultTemplate;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault", name = "import.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(VaultTemplate.class)
public class VaultSecretImportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretImportService vaultSecretImportService(VaultTemplate vaultTemplate) {
        return new VaultSecretImportService(vaultTemplate);
    }
}
