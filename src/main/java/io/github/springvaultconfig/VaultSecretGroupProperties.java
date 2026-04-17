package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * Configuration properties for defining secret groups.
 */
@ConfigurationProperties(prefix = "vault.secret-groups")
public class VaultSecretGroupProperties {

    /** Whether secret group loading is enabled. */
    private boolean enabled = true;

    /**
     * Map of group name -> list of Vault paths.
     * Example:
     * vault.secret-groups.groups.database=secret/db/primary,secret/db/replica
     */
    private Map<String, List<String>> groups = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;
    }
}
