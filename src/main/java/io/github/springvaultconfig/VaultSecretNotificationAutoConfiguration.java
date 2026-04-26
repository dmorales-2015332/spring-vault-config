package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link VaultSecretNotificationService}.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "vault.notification",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VaultSecretNotificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretNotificationService vaultSecretNotificationService(
            ApplicationEventPublisher eventPublisher) {
        return new VaultSecretNotificationService(eventPublisher);
    }
}
