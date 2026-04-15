package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that monitors secret leases and publishes notification events
 * when secrets are approaching their expiry time.
 */
public class VaultSecretExpiryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretExpiryNotificationService.class);

    private final VaultOperations vaultOperations;
    private final ApplicationEventPublisher eventPublisher;
    private final VaultConfigProperties properties;
    private final Map<String, Instant> secretExpiryMap = new ConcurrentHashMap<>();

    public VaultSecretExpiryNotificationService(VaultOperations vaultOperations,
                                                ApplicationEventPublisher eventPublisher,
                                                VaultConfigProperties properties) {
        this.vaultOperations = vaultOperations;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    /**
     * Registers a secret path with its expected expiry time for monitoring.
     *
     * @param secretPath the Vault secret path
     * @param expiry     the instant at which the secret expires
     */
    public void registerSecretExpiry(String secretPath, Instant expiry) {
        secretExpiryMap.put(secretPath, expiry);
        log.debug("Registered secret expiry for path '{}': {}", secretPath, expiry);
    }

    /**
     * Removes a secret path from expiry monitoring.
     *
     * @param secretPath the Vault secret path to deregister
     */
    public void deregisterSecret(String secretPath) {
        secretExpiryMap.remove(secretPath);
        log.debug("Deregistered secret expiry monitoring for path '{}'", secretPath);
    }

    /**
     * Scheduled check that evaluates all monitored secrets and publishes
     * {@link VaultSecretExpiryEvent} for any that are within the warning threshold.
     */
    @Scheduled(fixedDelayString = "${spring.vault.expiry-check-interval-ms:60000}")
    public void checkSecretExpiry() {
        Duration warningThreshold = properties.getExpiryWarningThreshold();
        Instant now = Instant.now();

        secretExpiryMap.forEach((path, expiry) -> {
            Duration timeUntilExpiry = Duration.between(now, expiry);
            if (!timeUntilExpiry.isNegative() && timeUntilExpiry.compareTo(warningThreshold) <= 0) {
                log.warn("Secret at path '{}' expires in {} seconds", path, timeUntilExpiry.getSeconds());
                eventPublisher.publishEvent(new VaultSecretExpiryEvent(this, path, expiry, timeUntilExpiry));
            } else if (timeUntilExpiry.isNegative()) {
                log.error("Secret at path '{}' has already expired at {}", path, expiry);
                eventPublisher.publishEvent(new VaultSecretExpiryEvent(this, path, expiry, Duration.ZERO));
            }
        });
    }

    public Map<String, Instant> getMonitoredSecrets() {
        return Map.copyOf(secretExpiryMap);
    }
}
