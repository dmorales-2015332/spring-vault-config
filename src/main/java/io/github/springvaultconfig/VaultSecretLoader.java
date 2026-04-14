package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads secrets from HashiCorp Vault for configured secret paths.
 */
public class VaultSecretLoader {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretLoader.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;

    public VaultSecretLoader(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Loads all secrets from the configured Vault paths.
     *
     * @return a flat map of property key to secret value
     */
    public Map<String, Object> loadSecrets() {
        Map<String, Object> secrets = new HashMap<>();

        for (String secretPath : properties.getPaths()) {
            String fullPath = buildFullPath(secretPath);
            log.debug("Loading secrets from Vault path: {}", fullPath);

            try {
                VaultResponse response = vaultTemplate.read(fullPath);
                if (response != null && response.getData() != null) {
                    secrets.putAll(response.getData());
                    log.info("Loaded {} secret(s) from Vault path: {}", response.getData().size(), fullPath);
                } else {
                    log.warn("No data found at Vault path: {}", fullPath);
                }
            } catch (Exception e) {
                if (properties.isFailFast()) {
                    throw new VaultSecretLoadException("Failed to load secrets from path: " + fullPath, e);
                }
                log.error("Failed to load secrets from Vault path: {}. Skipping.", fullPath, e);
            }
        }

        return Collections.unmodifiableMap(secrets);
    }

    private String buildFullPath(String secretPath) {
        String backend = properties.getBackend();
        if (backend != null && !backend.isBlank()) {
            return backend + "/" + secretPath;
        }
        return secretPath;
    }
}
