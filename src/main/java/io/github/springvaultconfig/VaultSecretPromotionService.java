package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for promoting secrets between Vault namespaces or paths
 * (e.g., from staging to production).
 */
public class VaultSecretPromotionService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretPromotionService.class);

    private final VaultTemplate vaultTemplate;

    public VaultSecretPromotionService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = Objects.requireNonNull(vaultTemplate, "vaultTemplate must not be null");
    }

    /**
     * Promotes a secret from the source path to the target path.
     *
     * @param sourcePath the Vault path to read the secret from
     * @param targetPath the Vault path to write the secret to
     * @return the number of keys promoted
     * @throws VaultSecretLoadException if the source secret cannot be read
     */
    public int promote(String sourcePath, String targetPath) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");

        log.info("Promoting secret from '{}' to '{}'", sourcePath, targetPath);

        VaultResponse response = vaultTemplate.read(sourcePath);
        if (response == null || response.getData() == null) {
            throw new VaultSecretLoadException(
                    "No secret data found at source path: " + sourcePath);
        }

        Map<String, Object> data = new HashMap<>(response.getData());
        vaultTemplate.write(targetPath, data);

        log.info("Successfully promoted {} key(s) from '{}' to '{}'",
                data.size(), sourcePath, targetPath);
        return data.size();
    }

    /**
     * Promotes only the specified keys from the source path to the target path.
     *
     * @param sourcePath the Vault path to read from
     * @param targetPath the Vault path to write to
     * @param keys       the specific keys to promote
     * @return the number of keys promoted
     */
    public int promoteKeys(String sourcePath, String targetPath, Iterable<String> keys) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");
        Objects.requireNonNull(keys, "keys must not be null");

        VaultResponse response = vaultTemplate.read(sourcePath);
        if (response == null || response.getData() == null) {
            throw new VaultSecretLoadException(
                    "No secret data found at source path: " + sourcePath);
        }

        Map<String, Object> sourceData = response.getData();
        Map<String, Object> subset = new HashMap<>();
        for (String key : keys) {
            if (sourceData.containsKey(key)) {
                subset.put(key, sourceData.get(key));
            } else {
                log.warn("Key '{}' not found in source path '{}', skipping", key, sourcePath);
            }
        }

        if (!subset.isEmpty()) {
            vaultTemplate.write(targetPath, subset);
            log.info("Promoted {} key(s) from '{}' to '{}'", subset.size(), sourcePath, targetPath);
        }
        return subset.size();
    }
}
