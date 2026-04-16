package io.github.springvaultconfig;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Immutable record representing a point-in-time snapshot of secrets at a Vault path.
 */
public record VaultSecretSnapshot(
        String path,
        Map<String, Object> data,
        Instant takenAt
) {
    public VaultSecretSnapshot {
        data = Collections.unmodifiableMap(data);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public int size() {
        return data.size();
    }
}
