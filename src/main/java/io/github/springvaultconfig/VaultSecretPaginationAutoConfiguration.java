package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(prefix = "vault", name = "pagination.enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretPaginationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretPaginationService vaultSecretPaginationService(VaultOperations vaultOperations) {
        return new VaultSecretPaginationService(vaultOperations);
    }
}
