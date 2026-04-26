package io.github.springvaultconfig;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Event published when a Vault secret at a given path has changed.
 */
public class VaultSecretNotificationEvent {

    private final String path;
    private final Map<String, String> oldValues;
    private final Map<String, String> newValues;
    private final Instant occurredAt;

    public VaultSecretNotificationEvent(String path,
                                        Map<String, String> oldValues,
                                        Map<String, String> newValues) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be null or blank");
        }
        this.path = path;
        this.oldValues = oldValues != null ? Collections.unmodifiableMap(oldValues) : Collections.emptyMap();
        this.newValues = newValues != null ? Collections.unmodifiableMap(newValues) : Collections.emptyMap();
        this.occurredAt = Instant.now();
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getOldValues() {
        return oldValues;
    }

    public Map<String, String> getNewValues() {
        return newValues;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "VaultSecretNotificationEvent{" +
                "path='" + path + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
