package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "vault.rate-limiter", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(VaultConfigProperties.class)
public class VaultSecretRateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretRateLimiterService vaultSecretRateLimiterService(VaultConfigProperties properties) {
        int maxRequests = properties.getRateLimiterMaxRequests() > 0 ? properties.getRateLimiterMaxRequests() : 100;
        long windowSeconds = properties.getRateLimiterWindowSeconds() > 0 ? properties.getRateLimiterWindowSeconds() : 60;
        return new VaultSecretRateLimiterService(maxRequests, windowSeconds);
    }
}
