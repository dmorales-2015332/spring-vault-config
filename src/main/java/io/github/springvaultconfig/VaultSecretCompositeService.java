package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates secrets from multiple Vault paths into a single merged map.
 * Later paths in the list take precedence over earlier ones.
 */
@Service
public class VaultSecretCompositeService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretCompositeService.class);

    private final VaultSecretLoader secretLoader;

    public VaultSecretCompositeService(VaultSecretLoader secretLoader) {
        this.secretLoader = secretLoader;
    }

    /**
     * Load and merge secrets from all provided paths.
     *
     * @param paths ordered list of Vault secret paths
     * @return merged map of secrets
     */
    public Map<String, String> loadComposite(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return Map.of();
        }
        Map<String, String> merged = new HashMap<>();
        for (String path : paths) {
            try {
                Map<String, String> secrets = secretLoader.loadSecrets(path);
                merged.putAll(secrets);
                log.debug("Merged {} secrets from path '{}'", secrets.size(), path);
            } catch (VaultSecretLoadException e) {
                log.warn("Failed to load secrets from path '{}': {}", path, e.getMessage());
            }
        }
        log.info("Composite load complete: {} total keys from {} paths", merged.size(), paths.size());
        return Map.copyOf(merged);
    }
}
