package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault.alert", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(VaultConfigProperties.class)
public class VaultSecretAlertAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretAlertService vaultSecretAlertService(
            ApplicationEventPublisher eventPublisher,
            VaultConfigProperties properties) {
        int threshold = properties.getAlertAccessThreshold() > 0
                ? properties.getAlertAccessThreshold()
                : 100;
        return new VaultSecretAlertService(eventPublisher, threshold);
    }
}
