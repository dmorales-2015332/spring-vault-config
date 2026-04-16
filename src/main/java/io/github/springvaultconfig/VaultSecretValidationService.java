package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates that required secrets exist and are non-empty in Vault.
 */
public class VaultSecretValidationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretValidationService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;

    public VaultSecretValidationService(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Validates that all required secret keys exist at the configured path.
     *
     * @param requiredKeys list of keys that must be present and non-blank
     * @throws VaultSecretValidationException if any required key is missing or blank
     */
    public void validateRequiredSecrets(List<String> requiredKeys) {
        if (requiredKeys == null || requiredKeys.isEmpty()) {
            log.debug("No required secret keys configured for validation");
            return;
        }

        String path = properties.getPath();
        log.info("Validating {} required secret(s) at path: {}", requiredKeys.size(), path);

        VaultResponse response = vaultTemplate.read(path);
        Map<String, Object> data = (response != null && response.getData() != null)
                ? response.getData() : Map.of();

        List<String> missing = new ArrayList<>();
        for (String key : requiredKeys) {
            Object value = data.get(key);
            if (value == null || value.toString().isBlank()) {
                missing.add(key);
            }
        }

        if (!missing.isEmpty()) {
            log.error("Vault secret validation failed. Missing or blank keys: {}", missing);
            throw new VaultSecretValidationException(
                    "Required Vault secrets are missing or blank: " + missing);
        }

        log.info("All required Vault secrets validated successfully");
    }

    /**
     * Returns true if the secret at the given path contains the specified key with a non-blank value.
     */
    public boolean secretExists(String path, String key) {
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            return false;
        }
        Object value = response.getData().get(key);
        return value != null && !value.toString().isBlank();
    }
}
