package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault.obfuscation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretObfuscationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretObfuscationService vaultSecretObfuscationService(VaultConfigProperties properties) {
        return new VaultSecretObfuscationService(properties);
    }
}
