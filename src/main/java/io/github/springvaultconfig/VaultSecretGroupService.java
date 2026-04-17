package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.*;

/**
 * Service for loading and managing groups of secrets from Vault under a common prefix.
 */
public class VaultSecretGroupService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretGroupService.class);

    private final VaultTemplate vaultTemplate;
    private final Map<String, Map<String, Object>> groupCache = new LinkedHashMap<>();

    public VaultSecretGroupService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Load all secrets under the given paths and group them by a logical group name.
     *
     * @param groupName the logical group name
     * @param paths     one or more Vault paths to load
     * @return merged map of all secrets in the group
     */
    public Map<String, Object> loadGroup(String groupName, List<String> paths) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String path : paths) {
            try {
                VaultResponse response = vaultTemplate.read(path);
                if (response != null && response.getData() != null) {
                    merged.putAll(response.getData());
                    log.debug("Loaded secrets from path '{}' into group '{}'", path, groupName);
                } else {
                    log.warn("No data found at Vault path '{}' for group '{}'", path, groupName);
                }
            } catch (Exception e) {
                log.error("Failed to load secrets from path '{}' for group '{}': {}", path, groupName, e.getMessage());
            }
        }
        groupCache.put(groupName, Collections.unmodifiableMap(merged));
        return groupCache.get(groupName);
    }

    /**
     * Retrieve a previously loaded group from cache.
     *
     * @param groupName the logical group name
     * @return optional map of secrets
     */
    public Optional<Map<String, Object>> getGroup(String groupName) {
        return Optional.ofNullable(groupCache.get(groupName));
    }

    /**
     * List all known group names.
     */
    public Set<String> listGroups() {
        return Collections.unmodifiableSet(groupCache.keySet());
    }

    /**
     * Evict a group from the cache.
     */
    public void evictGroup(String groupName) {
        groupCache.remove(groupName);
        log.info("Evicted secret group '{}' from cache", groupName);
    }
}
