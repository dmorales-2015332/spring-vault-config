package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault", name = "export.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean({VaultSecretLoader.class, VaultSecretMaskingService.class})
public class VaultSecretExportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretExportService vaultSecretExportService(VaultSecretLoader secretLoader,
                                                              VaultSecretMaskingService maskingService) {
        return new VaultSecretExportService(secretLoader, maskingService);
    }
}
