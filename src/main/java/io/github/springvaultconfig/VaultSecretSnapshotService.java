package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for capturing and restoring point-in-time snapshots of Vault secrets.
 */
public class VaultSecretSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretSnapshotService.class);

    private final VaultTemplate vaultTemplate;
    private final Map<String, VaultSecretSnapshot> snapshots = new ConcurrentHashMap<>();

    public VaultSecretSnapshotService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public VaultSecretSnapshot takeSnapshot(String path) {
        var response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            throw new VaultSecretLoadException("No data found at path: " + path);
        }
        Map<String, Object> data = new LinkedHashMap<>(response.getData());
        VaultSecretSnapshot snapshot = new VaultSecretSnapshot(path, data, Instant.now());
        snapshots.put(path, snapshot);
        log.info("Snapshot taken for path '{}' at {}", path, snapshot.takenAt());
        return snapshot;
    }

    public VaultSecretSnapshot getSnapshot(String path) {
        return snapshots.get(path);
    }

    public Map<String, VaultSecretSnapshot> getAllSnapshots() {
        return Collections.unmodifiableMap(snapshots);
    }

    public void clearSnapshot(String path) {
        snapshots.remove(path);
        log.info("Snapshot cleared for path '{}'", path);
    }

    public boolean hasSnapshot(String path) {
        return snapshots.containsKey(path);
    }
}
