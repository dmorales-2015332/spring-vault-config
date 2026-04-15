package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for reading specific versions of secrets from HashiCorp Vault KV v2.
 * Supports fetching a secret at a particular version and listing available versions.
 */
public class VaultSecretVersioningService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretVersioningService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;

    public VaultSecretVersioningService(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Reads a secret at the specified version from KV v2.
     *
     * @param secretPath relative path within the KV mount
     * @param version    the version number to retrieve
     * @return map of secret data, or empty map if not found
     */
    public Map<String, Object> readSecretAtVersion(String secretPath, int version) {
        String fullPath = buildVersionedPath(secretPath, version);
        log.debug("Reading secret at versioned path: {}", fullPath);
        try {
            VaultResponse response = vaultTemplate.read(fullPath);
            if (response == null || response.getData() == null) {
                log.warn("No data found at path '{}' version {}", secretPath, version);
                return Collections.emptyMap();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getData().get("data");
            return data != null ? data : Collections.emptyMap();
        } catch (Exception ex) {
            log.error("Failed to read secret '{}' at version {}: {}", secretPath, version, ex.getMessage());
            throw new VaultSecretLoadException(
                    "Failed to read versioned secret at path: " + secretPath, ex);
        }
    }

    /**
     * Retrieves metadata for a secret, including available versions.
     *
     * @param secretPath relative path within the KV mount
     * @return map of metadata, or empty map if not found
     */
    public Map<String, Object> readSecretMetadata(String secretPath) {
        String metadataPath = properties.getBackend() + "/metadata/" + secretPath;
        log.debug("Reading secret metadata at path: {}", metadataPath);
        try {
            VaultResponse response = vaultTemplate.read(metadataPath);
            if (response == null || response.getData() == null) {
                log.warn("No metadata found for secret path '{}'", secretPath);
                return Collections.emptyMap();
            }
            return new HashMap<>(response.getData());
        } catch (Exception ex) {
            log.error("Failed to read metadata for secret '{}': {}", secretPath, ex.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Returns the current (latest) version number of a secret, if determinable.
     *
     * @param secretPath relative path within the KV mount
     * @return Optional containing the current version, or empty if unavailable
     */
    public Optional<Integer> getCurrentVersion(String secretPath) {
        Map<String, Object> metadata = readSecretMetadata(secretPath);
        Object currentVersion = metadata.get("current_version");
        if (currentVersion instanceof Number) {
            return Optional.of(((Number) currentVersion).intValue());
        }
        return Optional.empty();
    }

    private String buildVersionedPath(String secretPath, int version) {
        return properties.getBackend() + "/data/" + secretPath + "?version=" + version;
    }
}
