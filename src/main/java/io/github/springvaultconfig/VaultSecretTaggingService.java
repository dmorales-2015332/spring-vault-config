package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.*;

/**
 * Service for tagging Vault secrets with metadata labels.
 * Allows associating arbitrary key-value tags with secret paths for
 * classification, ownership tracking, and filtering.
 */
public class VaultSecretTaggingService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretTaggingService.class);
    private static final String TAGS_METADATA_KEY = "custom_metadata";

    private final VaultOperations vaultOperations;
    private final VaultConfigProperties properties;

    public VaultSecretTaggingService(VaultOperations vaultOperations, VaultConfigProperties properties) {
        this.vaultOperations = Objects.requireNonNull(vaultOperations, "vaultOperations must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    /**
     * Applies tags to a secret at the given path using KV v2 metadata.
     *
     * @param secretPath relative path of the secret
     * @param tags       map of tag key-value pairs
     */
    public void tagSecret(String secretPath, Map<String, String> tags) {
        Objects.requireNonNull(secretPath, "secretPath must not be null");
        Objects.requireNonNull(tags, "tags must not be null");

        String metadataPath = buildMetadataPath(secretPath);
        Map<String, Object> body = new HashMap<>();
        body.put(TAGS_METADATA_KEY, tags);

        vaultOperations.write(metadataPath, body);
        log.info("Tagged secret '{}' with {} tag(s)", secretPath, tags.size());
    }

    /**
     * Retrieves tags associated with a secret.
     *
     * @param secretPath relative path of the secret
     * @return map of tag key-value pairs, or empty map if none found
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getTagsForSecret(String secretPath) {
        Objects.requireNonNull(secretPath, "secretPath must not be null");

        String metadataPath = buildMetadataPath(secretPath);
        VaultResponse response = vaultOperations.read(metadataPath);

        if (response == null || response.getData() == null) {
            log.debug("No metadata found for secret '{}'", secretPath);
            return Collections.emptyMap();
        }

        Object customMetadata = response.getData().get(TAGS_METADATA_KEY);
        if (customMetadata instanceof Map) {
            return (Map<String, String>) customMetadata;
        }
        return Collections.emptyMap();
    }

    /**
     * Removes all tags from a secret by writing an empty metadata map.
     *
     * @param secretPath relative path of the secret
     */
    public void clearTags(String secretPath) {
        Objects.requireNonNull(secretPath, "secretPath must not be null");

        String metadataPath = buildMetadataPath(secretPath);
        Map<String, Object> body = new HashMap<>();
        body.put(TAGS_METADATA_KEY, Collections.emptyMap());

        vaultOperations.write(metadataPath, body);
        log.info("Cleared all tags from secret '{}'", secretPath);
    }

    private String buildMetadataPath(String secretPath) {
        String mount = properties.getBackend();
        return mount + "/metadata/" + secretPath;
    }
}
