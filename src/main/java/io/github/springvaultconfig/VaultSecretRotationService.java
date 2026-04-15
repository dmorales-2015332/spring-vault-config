package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for detecting secret rotation in Vault and publishing
 * {@link VaultSecretRotatedEvent} when a secret value has changed.
 */
@Service
public class VaultSecretRotationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretRotationService.class);

    private final VaultSecretLoader secretLoader;
    private final VaultConfigProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    private final Map<String, String> secretSnapshot = new ConcurrentHashMap<>();

    public VaultSecretRotationService(VaultSecretLoader secretLoader,
                                      VaultConfigProperties properties,
                                      ApplicationEventPublisher eventPublisher) {
        this.secretLoader = secretLoader;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Captures an initial snapshot of secrets so future polls can detect changes.
     */
    public void captureSnapshot(Map<String, String> secrets) {
        secretSnapshot.clear();
        secretSnapshot.putAll(secrets);
        log.debug("Captured secret snapshot with {} entries", secrets.size());
    }

    /**
     * Polls Vault for the configured secret path and compares against the last
     * known snapshot. Fires a {@link VaultSecretRotatedEvent} for each changed key.
     */
    @Scheduled(fixedDelayString = "${spring.vault.rotation-check-interval-ms:60000}")
    public void checkForRotation() {
        String path = properties.getSecretPath();
        log.debug("Checking for secret rotation at path: {}", path);
        try {
            Map<String, String> current = secretLoader.loadSecrets(path);
            current.forEach((key, value) -> {
                String previous = secretSnapshot.get(key);
                if (previous != null && !previous.equals(value)) {
                    log.info("Secret rotation detected for key '{}' at path '{}'", key, path);
                    eventPublisher.publishEvent(
                            new VaultSecretRotatedEvent(this, key, path, Instant.now()));
                }
            });
            secretSnapshot.putAll(current);
        } catch (VaultSecretLoadException e) {
            log.warn("Failed to check secret rotation for path '{}': {}", path, e.getMessage());
        }
    }

    public Map<String, String> getSecretSnapshot() {
        return Map.copyOf(secretSnapshot);
    }
}
