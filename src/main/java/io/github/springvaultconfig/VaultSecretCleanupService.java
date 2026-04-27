package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for cleaning up expired or orphaned secrets from Vault.
 * Tracks known secret paths and removes those that are no longer needed.
 */
public class VaultSecretCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretCleanupService.class);

    private final VaultOperations vaultOperations;
    private final Map<String, Long> trackedPaths = new ConcurrentHashMap<>();

    public VaultSecretCleanupService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     * Register a secret path for cleanup tracking with a TTL in milliseconds.
     */
    public void track(String path, long ttlMillis) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be blank");
        }
        long expiryTime = System.currentTimeMillis() + ttlMillis;
        trackedPaths.put(path, expiryTime);
        logger.debug("Tracking secret path '{}' for cleanup, expires at {}", path, expiryTime);
    }

    /**
     * Untrack a secret path, preventing it from being cleaned up.
     */
    public void untrack(String path) {
        trackedPaths.remove(path);
        logger.debug("Untracked secret path '{}'", path);
    }

    /**
     * Delete all tracked paths that have exceeded their TTL.
     *
     * @return list of paths that were cleaned up
     */
    public List<String> cleanupExpired() {
        long now = System.currentTimeMillis();
        List<String> cleaned = new ArrayList<>();

        trackedPaths.entrySet().removeIf(entry -> {
            if (entry.getValue() <= now) {
                String path = entry.getKey();
                try {
                    vaultOperations.delete(path);
                    cleaned.add(path);
                    logger.info("Cleaned up expired secret at path '{}'", path);
                } catch (Exception e) {
                    logger.warn("Failed to clean up secret at path '{}': {}", path, e.getMessage());
                }
                return true;
            }
            return false;
        });

        return cleaned;
    }

    /**
     * Returns the number of currently tracked paths.
     */
    public int trackedCount() {
        return trackedPaths.size();
    }

    /**
     * Returns whether a given path is currently being tracked.
     */
    public boolean isTracked(String path) {
        return trackedPaths.containsKey(path);
    }
}
