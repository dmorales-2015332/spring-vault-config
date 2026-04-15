package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for listing and resolving secrets within a given Vault namespace.
 * Supports recursive listing of secret paths under a configured base path.
 */
public class VaultSecretNamespaceService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretNamespaceService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;

    public VaultSecretNamespaceService(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = Objects.requireNonNull(vaultTemplate, "vaultTemplate must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    /**
     * Lists all secret keys available under the given namespace path.
     *
     * @param namespacePath the relative path within the configured secret backend
     * @return list of secret keys, or empty list if none found
     */
    public List<String> listSecretKeys(String namespacePath) {
        Objects.requireNonNull(namespacePath, "namespacePath must not be null");
        String fullPath = buildFullPath(namespacePath);
        log.debug("Listing secret keys at path: {}", fullPath);
        try {
            VaultResponse response = vaultTemplate.read(fullPath);
            if (response == null || response.getData() == null) {
                log.warn("No data found at namespace path: {}", fullPath);
                return Collections.emptyList();
            }
            Object keys = response.getData().get("keys");
            if (keys instanceof List<?> keyList) {
                return keyList.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to list secret keys at path '{}': {}", fullPath, e.getMessage(), e);
            throw new VaultSecretLoadException("Failed to list secrets at namespace: " + namespacePath, e);
        }
    }

    /**
     * Resolves all secret entries (key-value pairs) under the given namespace path.
     *
     * @param namespacePath the relative path within the configured secret backend
     * @return map of secret key to value, or empty map if none found
     */
    public Map<String, Object> resolveSecrets(String namespacePath) {
        Objects.requireNonNull(namespacePath, "namespacePath must not be null");
        String fullPath = buildFullPath(namespacePath);
        log.debug("Resolving secrets at namespace path: {}", fullPath);
        try {
            VaultResponse response = vaultTemplate.read(fullPath);
            if (response == null || response.getData() == null) {
                log.warn("No secrets resolved at namespace path: {}", fullPath);
                return Collections.emptyMap();
            }
            log.info("Resolved {} secret(s) from namespace path: {}", response.getData().size(), fullPath);
            return Collections.unmodifiableMap(response.getData());
        } catch (Exception e) {
            log.error("Failed to resolve secrets at path '{}': {}", fullPath, e.getMessage(), e);
            throw new VaultSecretLoadException("Failed to resolve secrets at namespace: " + namespacePath, e);
        }
    }

    private String buildFullPath(String namespacePath) {
        String basePath = properties.getBackend();
        if (basePath == null || basePath.isBlank()) {
            return namespacePath;
        }
        return basePath.endsWith("/") ? basePath + namespacePath : basePath + "/" + namespacePath;
    }
}
