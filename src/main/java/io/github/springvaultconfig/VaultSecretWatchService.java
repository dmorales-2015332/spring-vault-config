package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service that watches Vault secret paths for changes and publishes
 * {@link VaultSecretChangedEvent} when a secret value is modified.
 */
public class VaultSecretWatchService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretWatchService.class);

    private final VaultOperations vaultOperations;
    private final ApplicationEventPublisher eventPublisher;
    private final long pollIntervalSeconds;

    private final Map<String, Map<String, Object>> lastKnownSecrets = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> watchHandles = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r, "vault-secret-watcher");
                t.setDaemon(true);
                return t;
            });

    public VaultSecretWatchService(VaultOperations vaultOperations,
                                   ApplicationEventPublisher eventPublisher,
                                   long pollIntervalSeconds) {
        this.vaultOperations = vaultOperations;
        this.eventPublisher = eventPublisher;
        this.pollIntervalSeconds = pollIntervalSeconds;
    }

    public void watch(String secretPath) {
        if (watchHandles.containsKey(secretPath)) {
            log.debug("Already watching secret path: {}", secretPath);
            return;
        }
        log.info("Starting watch on Vault secret path: {}", secretPath);
        seedSnapshot(secretPath);
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(
                () -> checkForChanges(secretPath),
                pollIntervalSeconds,
                pollIntervalSeconds,
                TimeUnit.SECONDS);
        watchHandles.put(secretPath, handle);
    }

    public void unwatch(String secretPath) {
        ScheduledFuture<?> handle = watchHandles.remove(secretPath);
        if (handle != null) {
            handle.cancel(false);
            lastKnownSecrets.remove(secretPath);
            log.info("Stopped watching Vault secret path: {}", secretPath);
        }
    }

    public boolean isWatching(String secretPath) {
        return watchHandles.containsKey(secretPath);
    }

    private void seedSnapshot(String secretPath) {
        try {
            VaultResponse response = vaultOperations.read(secretPath);
            if (response != null && response.getData() != null) {
                lastKnownSecrets.put(secretPath, Map.copyOf(response.getData()));
            }
        } catch (Exception e) {
            log.warn("Could not seed initial snapshot for path '{}': {}", secretPath, e.getMessage());
        }
    }

    private void checkForChanges(String secretPath) {
        try {
            VaultResponse response = vaultOperations.read(secretPath);
            Map<String, Object> current = response != null && response.getData() != null
                    ? response.getData() : Map.of();
            Map<String, Object> previous = lastKnownSecrets.getOrDefault(secretPath, Map.of());
            if (!current.equals(previous)) {
                log.info("Change detected at Vault path: {}", secretPath);
                lastKnownSecrets.put(secretPath, Map.copyOf(current));
                eventPublisher.publishEvent(new VaultSecretChangedEvent(this, secretPath, previous, current));
            }
        } catch (Exception e) {
            log.error("Error polling Vault path '{}': {}", secretPath, e.getMessage());
        }
    }
}
