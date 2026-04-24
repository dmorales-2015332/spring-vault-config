package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service for replicating secrets from one Vault path to one or more target paths.
 * Useful for promoting secrets across environments or duplicating shared credentials.
 */
public class VaultSecretReplicationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretReplicationService.class);

    private final VaultTemplate vaultTemplate;

    public VaultSecretReplicationService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = Objects.requireNonNull(vaultTemplate, "vaultTemplate must not be null");
    }

    /**
     * Replicate all key-value data from {@code sourcePath} to each path in {@code targetPaths}.
     *
     * @param sourcePath  the Vault KV path to read from
     * @param targetPaths the list of Vault KV paths to write to
     * @return a map of target path to replication result (true = success, false = failure)
     */
    public Map<String, Boolean> replicate(String sourcePath, List<String> targetPaths) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(targetPaths, "targetPaths must not be null");

        Map<String, Object> sourceData = readSource(sourcePath);
        Map<String, Boolean> results = new HashMap<>();

        for (String target : targetPaths) {
            results.put(target, writeTarget(target, sourceData));
        }

        return results;
    }

    /**
     * Replicate a specific subset of keys from the source path to the target path.
     *
     * @param sourcePath the Vault KV path to read from
     * @param targetPath the Vault KV path to write to
     * @param keys       the keys to replicate; if empty, all keys are replicated
     * @return true if the replication succeeded
     */
    public boolean replicateKeys(String sourcePath, String targetPath, List<String> keys) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");
        Objects.requireNonNull(keys, "keys must not be null");

        Map<String, Object> sourceData = readSource(sourcePath);

        Map<String, Object> filtered = new HashMap<>();
        if (keys.isEmpty()) {
            filtered.putAll(sourceData);
        } else {
            for (String key : keys) {
                if (sourceData.containsKey(key)) {
                    filtered.put(key, sourceData.get(key));
                } else {
                    log.warn("Key '{}' not found in source path '{}'", key, sourcePath);
                }
            }
        }

        return writeTarget(targetPath, filtered);
    }

    private Map<String, Object> readSource(String sourcePath) {
        VaultResponse response = vaultTemplate.read(sourcePath);
        if (response == null || response.getData() == null) {
            log.warn("No data found at source path '{}'", sourcePath);
            return new HashMap<>();
        }
        return response.getData();
    }

    private boolean writeTarget(String targetPath, Map<String, Object> data) {
        try {
            vaultTemplate.write(targetPath, data);
            log.info("Replicated {} key(s) to '{}'", data.size(), targetPath);
            return true;
        } catch (Exception ex) {
            log.error("Failed to replicate secrets to path '{}': {}", targetPath, ex.getMessage(), ex);
            return false;
        }
    }
}
