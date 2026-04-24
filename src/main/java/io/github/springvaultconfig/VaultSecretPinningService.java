package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for pinning specific secret versions so they are not auto-rotated
 * or overwritten during refresh cycles.
 */
public class VaultSecretPinningService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretPinningService.class);

    private final VaultOperations vaultOperations;
    private final Map<String, Integer> pinnedVersions = new ConcurrentHashMap<>();

    public VaultSecretPinningService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     * Pin a secret path to a specific version.
     *
     * @param path    the secret path
     * @param version the version to pin
     */
    public void pin(String path, int version) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be blank");
        }
        if (version < 1) {
            throw new IllegalArgumentException("Version must be >= 1");
        }
        pinnedVersions.put(path, version);
        log.info("Pinned secret '{}' to version {}", path, version);
    }

    /**
     * Unpin a secret path, allowing normal rotation behaviour.
     *
     * @param path the secret path
     */
    public void unpin(String path) {
        Integer removed = pinnedVersions.remove(path);
        if (removed != null) {
            log.info("Unpinned secret '{}' (was version {})", path, removed);
        }
    }

    /**
     * Returns the pinned version for a path, if any.
     *
     * @param path the secret path
     * @return an Optional containing the pinned version, or empty
     */
    public Optional<Integer> getPinnedVersion(String path) {
        return Optional.ofNullable(pinnedVersions.get(path));
    }

    /**
     * Read the secret at the pinned version from Vault KV v2.
     *
     * @param mountPath the KV v2 mount (e.g. "secret")
     * @param path      the secret path
     * @return secret data map, or empty map if not found / not pinned
     */
    public Map<String, Object> readPinned(String mountPath, String path) {
        Optional<Integer> version = getPinnedVersion(path);
        if (version.isEmpty()) {
            log.debug("No pinned version for '{}'; skipping pinned read", path);
            return Collections.emptyMap();
        }
        String versionedPath = mountPath + "/data/" + path + "?version=" + version.get();
        VaultResponse response = vaultOperations.read(versionedPath);
        if (response == null || response.getData() == null) {
            log.warn("No data returned for pinned secret '{}' at version {}", path, version.get());
            return Collections.emptyMap();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData().getOrDefault("data", new HashMap<>());
        log.debug("Read pinned secret '{}' at version {}", path, version.get());
        return Collections.unmodifiableMap(data);
    }

    /**
     * Returns an unmodifiable snapshot of all current pins.
     */
    public Map<String, Integer> getAllPins() {
        return Collections.unmodifiableMap(pinnedVersions);
    }
}
