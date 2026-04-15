package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that periodically checks for updated secrets in Vault and publishes
 * {@link VaultSecretRotatedEvent} when changes are detected.
 */
public class VaultSecretRefreshService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretRefreshService.class);

    private final VaultSecretLoader secretLoader;
    private final ApplicationEventPublisher eventPublisher;
    private final VaultConfigProperties properties;
    private final Map<String, Map<String, String>> secretCache = new ConcurrentHashMap<>();

    public VaultSecretRefreshService(VaultSecretLoader secretLoader,
                                     ApplicationEventPublisher eventPublisher,
                                     VaultConfigProperties properties) {
        this.secretLoader = secretLoader;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${spring.vault.refresh-interval-ms:60000}")
    public void refreshSecrets() {
        Set<String> paths = properties.getPaths();
        if (paths == null || paths.isEmpty()) {
            log.debug("No Vault secret paths configured for refresh.");
            return;
        }
        for (String path : paths) {
            try {
                Map<String, String> freshSecrets = secretLoader.loadSecrets(path);
                Map<String, String> previous = secretCache.put(path, freshSecrets);
                if (previous != null && !previous.equals(freshSecrets)) {
                    log.info("Detected secret rotation at path: {}", path);
                    eventPublisher.publishEvent(new VaultSecretRotatedEvent(this, path, freshSecrets));
                } else if (previous == null) {
                    log.debug("Initialized secret cache for path: {}", path);
                }
            } catch (Exception e) {
                log.error("Failed to refresh secrets at path '{}': {}", path, e.getMessage(), e);
            }
        }
    }

    /**
     * Returns the currently cached secrets for the given path, or an empty map.
     */
    public Map<String, String> getCachedSecrets(String path) {
        return secretCache.getOrDefault(path, Map.of());
    }
}
