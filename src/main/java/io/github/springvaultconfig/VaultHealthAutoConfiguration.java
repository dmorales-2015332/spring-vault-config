package io.github.springvaultconfig;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

/**
 * Auto-configuration that registers a {@link VaultHealthIndicator} when:
 * <ul>
 *   <li>Spring Boot Actuator is on the classpath</li>
 *   <li>A {@link VaultOperations} bean is available</li>
 *   <li>The health indicator is not explicitly disabled via
 *       {@code management.health.vault.enabled=false}</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnEnabledHealthIndicator("vault")
public class VaultHealthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "vaultHealthIndicator")
    public VaultHealthIndicator vaultHealthIndicator(VaultOperations vaultOperations) {
        return new VaultHealthIndicator(vaultOperations);
    }
}
