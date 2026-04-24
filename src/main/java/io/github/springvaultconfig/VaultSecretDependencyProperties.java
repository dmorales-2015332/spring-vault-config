package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for secret dependency tracking.
 */
@ConfigurationProperties(prefix = "vault.secret-dependency")
public class VaultSecretDependencyProperties {

    /** Whether the dependency service is enabled. */
    private boolean enabled = true;

    /**
     * Predefined dependency pairs loaded from configuration.
     * Each entry has the form "dependent:dependency".
     */
    private List<String> mappings = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getMappings() {
        return mappings;
    }

    public void setMappings(List<String> mappings) {
        this.mappings = mappings;
    }
}
