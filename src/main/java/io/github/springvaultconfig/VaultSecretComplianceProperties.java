package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for Vault secret compliance validation.
 */
@ConfigurationProperties(prefix = "vault.compliance")
public class VaultSecretComplianceProperties {

    /** Whether compliance checking is enabled. */
    private boolean enabled = true;

    /** List of keys that must be present in every secret. */
    private List<String> requiredKeys = new ArrayList<>();

    /**
     * Map of key name to regex pattern; the secret value for that key must match.
     * Example: username -> [a-zA-Z0-9_]{3,32}
     */
    private Map<String, String> keyPatterns = new HashMap<>();

    /** Minimum character length required for all secret values. 0 disables this check. */
    private int minValueLength = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRequiredKeys() {
        return requiredKeys;
    }

    public void setRequiredKeys(List<String> requiredKeys) {
        this.requiredKeys = requiredKeys;
    }

    public Map<String, String> getKeyPatterns() {
        return keyPatterns;
    }

    public void setKeyPatterns(Map<String, String> keyPatterns) {
        this.keyPatterns = keyPatterns;
    }

    public int getMinValueLength() {
        return minValueLength;
    }

    public void setMinValueLength(int minValueLength) {
        this.minValueLength = minValueLength;
    }
}
