package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(VaultSecretLoader.class)
public class VaultSecretCompositeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretCompositeService vaultSecretCompositeService(VaultSecretLoader secretLoader) {
        return new VaultSecretCompositeService(secretLoader);
    }
}
