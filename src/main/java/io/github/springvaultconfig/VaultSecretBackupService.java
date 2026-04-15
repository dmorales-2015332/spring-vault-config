package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for backing up and restoring Vault secrets locally in-memory.
 * Useful for fallback scenarios when Vault is temporarily unavailable.
 */
public class VaultSecretBackupService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretBackupService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;
    private final Map<String, BackupEntry> backupStore = new ConcurrentHashMap<>();

    public VaultSecretBackupService(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Backs up secrets from the given path into the in-memory store.
     *
     * @param path the Vault secret path to back up
     */
    public void backup(String path) {
        try {
            VaultResponse response = vaultTemplate.read(path);
            if (response != null && response.getData() != null) {
                backupStore.put(path, new BackupEntry(new HashMap<>(response.getData()), Instant.now()));
                log.info("Backed up secrets from path: {}", path);
            } else {
                log.warn("No data found at path '{}' to back up", path);
            }
        } catch (Exception e) {
            log.error("Failed to back up secrets from path '{}': {}", path, e.getMessage(), e);
        }
    }

    /**
     * Restores secrets from the backup store for the given path.
     *
     * @param path the Vault secret path to restore
     * @return immutable map of secret key-value pairs, or empty map if no backup exists
     */
    public Map<String, Object> restore(String path) {
        BackupEntry entry = backupStore.get(path);
        if (entry == null) {
            log.warn("No backup found for path '{}'", path);
            return Collections.emptyMap();
        }
        log.info("Restoring secrets from backup for path '{}' (backed up at {})", path, entry.timestamp());
        return Collections.unmodifiableMap(entry.data());
    }

    /**
     * Checks whether a backup exists for the given path.
     *
     * @param path the Vault secret path
     * @return true if a backup entry exists
     */
    public boolean hasBackup(String path) {
        return backupStore.containsKey(path);
    }

    /**
     * Clears the backup entry for the given path.
     *
     * @param path the Vault secret path
     */
    public void clearBackup(String path) {
        backupStore.remove(path);
        log.info("Cleared backup for path '{}'", path);
    }

    /**
     * Returns the timestamp of the backup for the given path, or null if not present.
     */
    public Instant getBackupTimestamp(String path) {
        BackupEntry entry = backupStore.get(path);
        return entry != null ? entry.timestamp() : null;
    }

    private record BackupEntry(Map<String, Object> data, Instant timestamp) {}
}
