package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretHashService}.
 * Enabled by default; can be disabled via {@code spring.vault.hash.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.vault.hash", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretHashAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretHashService vaultSecretHashService(VaultConfigProperties properties) {
        String algorithm = properties.getHashAlgorithm() != null
                ? properties.getHashAlgorithm()
                : "SHA-256";
        return new VaultSecretHashService(algorithm);
    }
}
