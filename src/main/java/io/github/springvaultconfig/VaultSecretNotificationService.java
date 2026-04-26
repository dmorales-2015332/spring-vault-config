package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service that manages notifications when Vault secrets change.
 * Allows components to subscribe to secret change events for specific paths.
 */
@Service
public class VaultSecretNotificationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretNotificationService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, List<Consumer<VaultSecretNotificationEvent>>> listeners = new ConcurrentHashMap<>();

    public VaultSecretNotificationService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Subscribe to notifications for a specific secret path.
     *
     * @param path     the Vault secret path to watch
     * @param listener the callback to invoke when the secret changes
     */
    public void subscribe(String path, Consumer<VaultSecretNotificationEvent> listener) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be null or blank");
        }
        listeners.computeIfAbsent(path, k -> new ArrayList<>()).add(listener);
        log.debug("Registered notification listener for secret path: {}", path);
    }

    /**
     * Unsubscribe all listeners for a specific secret path.
     *
     * @param path the Vault secret path
     */
    public void unsubscribeAll(String path) {
        listeners.remove(path);
        log.debug("Removed all notification listeners for secret path: {}", path);
    }

    /**
     * Notify all subscribers for the given path that a secret has changed.
     *
     * @param path      the Vault secret path that changed
     * @param oldValues the previous secret values (may be null)
     * @param newValues the updated secret values
     */
    public void notify(String path, Map<String, String> oldValues, Map<String, String> newValues) {
        VaultSecretNotificationEvent event = new VaultSecretNotificationEvent(path, oldValues, newValues);
        eventPublisher.publishEvent(event);
        List<Consumer<VaultSecretNotificationEvent>> pathListeners = listeners.getOrDefault(path, List.of());
        if (pathListeners.isEmpty()) {
            log.debug("No listeners registered for secret path: {}", path);
            return;
        }
        log.info("Notifying {} listener(s) of secret change at path: {}", pathListeners.size(), path);
        for (Consumer<VaultSecretNotificationEvent> listener : pathListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.warn("Notification listener threw exception for path {}: {}", path, e.getMessage(), e);
            }
        }
    }

    /**
     * Returns the number of listeners registered for a given path.
     */
    public int listenerCount(String path) {
        return listeners.getOrDefault(path, List.of()).size();
    }
}
