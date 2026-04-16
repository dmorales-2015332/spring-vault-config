package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault.access-log", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(VaultConfigProperties.class)
public class VaultSecretAccessLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretAccessLogService vaultSecretAccessLogService(VaultConfigProperties properties) {
        int maxEntries = properties.getAccessLogMaxEntries() > 0 ? properties.getAccessLogMaxEntries() : 100;
        return new VaultSecretAccessLogService(maxEntries);
    }
}
