package io.github.springvaultconfig;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultHealth;

/**
 * Spring Boot Actuator {@link HealthIndicator} that reports the status of the
 * HashiCorp Vault connection used by spring-vault-config.
 */
public class VaultHealthIndicator implements HealthIndicator {

    private final VaultOperations vaultOperations;

    public VaultHealthIndicator(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    @Override
    public Health health() {
        try {
            VaultHealth vaultHealth = vaultOperations.opsForSys().health();
            if (vaultHealth.isInitialized() && !vaultHealth.isSealed() && !vaultHealth.isStandby()) {
                return Health.up()
                        .withDetail("initialized", vaultHealth.isInitialized())
                        .withDetail("sealed", vaultHealth.isSealed())
                        .withDetail("standby", vaultHealth.isStandby())
                        .withDetail("version", vaultHealth.getVersion())
                        .build();
            }
            return Health.down()
                    .withDetail("initialized", vaultHealth.isInitialized())
                    .withDetail("sealed", vaultHealth.isSealed())
                    .withDetail("standby", vaultHealth.isStandby())
                    .withDetail("version", vaultHealth.getVersion())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
