package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service that validates Vault secrets against configurable compliance rules,
 * such as minimum length, required keys, and value pattern constraints.
 */
public class VaultSecretComplianceService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretComplianceService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultSecretComplianceProperties properties;

    public VaultSecretComplianceService(VaultTemplate vaultTemplate,
                                        VaultSecretComplianceProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Validates all secrets at the given path against the configured compliance rules.
     *
     * @param secretPath the Vault secret path to validate
     * @return list of compliance violation messages; empty if compliant
     */
    public List<String> validate(String secretPath) {
        List<String> violations = new ArrayList<>();
        VaultResponse response = vaultTemplate.read(secretPath);
        if (response == null || response.getData() == null) {
            violations.add("Secret path not found or empty: " + secretPath);
            return violations;
        }
        Map<String, Object> data = response.getData();

        for (String requiredKey : properties.getRequiredKeys()) {
            if (!data.containsKey(requiredKey)) {
                violations.add("Missing required key '" + requiredKey + "' at path: " + secretPath);
            }
        }

        for (Map.Entry<String, String> entry : properties.getKeyPatterns().entrySet()) {
            String key = entry.getKey();
            String patternStr = entry.getValue();
            if (data.containsKey(key)) {
                String value = String.valueOf(data.get(key));
                if (!Pattern.matches(patternStr, value)) {
                    violations.add("Key '" + key + "' value does not match pattern '" + patternStr + "'");
                }
            }
        }

        int minLength = properties.getMinValueLength();
        if (minLength > 0) {
            data.forEach((key, val) -> {
                if (val != null && String.valueOf(val).length() < minLength) {
                    violations.add("Key '" + key + "' value is shorter than minimum length " + minLength);
                }
            });
        }

        if (violations.isEmpty()) {
            log.info("Compliance check passed for path: {}", secretPath);
        } else {
            log.warn("Compliance violations found for path {}: {}", secretPath, violations);
        }
        return violations;
    }
}
