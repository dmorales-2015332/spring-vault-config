package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for loading multiple Vault secrets in a single bulk operation,
 * reducing round-trips and improving startup performance.
 */
public class VaultSecretBulkLoadService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretBulkLoadService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;
    private final Map<String, Map<String, Object>> bulkCache = new ConcurrentHashMap<>();

    public VaultSecretBulkLoadService(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Loads secrets for all given paths in one pass and returns a merged map.
     *
     * @param paths list of Vault secret paths to load
     * @return merged map of all key-value pairs across all paths
     */
    public Map<String, Object> bulkLoad(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            log.warn("No paths provided for bulk load.");
            return Collections.emptyMap();
        }
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String path : paths) {
            try {
                Map<String, Object> secrets = loadPath(path);
                bulkCache.put(path, secrets);
                merged.putAll(secrets);
                log.debug("Bulk loaded {} keys from path: {}", secrets.size(), path);
            } catch (Exception e) {
                log.error("Failed to bulk load secrets from path '{}': {}", path, e.getMessage(), e);
            }
        }
        log.info("Bulk load complete: {} paths loaded, {} total keys.", paths.size(), merged.size());
        return merged;
    }

    /**
     * Returns the cached result for a specific path, or empty map if not loaded.
     */
    public Map<String, Object> getCached(String path) {
        return bulkCache.getOrDefault(path, Collections.emptyMap());
    }

    /**
     * Clears the internal bulk cache.
     */
    public void clearCache() {
        bulkCache.clear();
        log.debug("Bulk load cache cleared.");
    }

    private Map<String, Object> loadPath(String path) {
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            log.warn("No data returned from Vault path: {}", path);
            return Collections.emptyMap();
        }
        return response.getData();
    }
}
