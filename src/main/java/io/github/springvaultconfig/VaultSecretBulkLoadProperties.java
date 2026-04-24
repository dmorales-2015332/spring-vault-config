package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the Vault secret bulk load feature.
 */
@ConfigurationProperties(prefix = "vault.bulk-load")
public class VaultSecretBulkLoadProperties {

    /** Whether bulk loading is enabled. */
    private boolean enabled = true;

    /** List of Vault paths to load in bulk at startup. */
    private List<String> paths = new ArrayList<>();

    /** Whether to fail fast if any path fails to load. */
    private boolean failFast = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }
}
