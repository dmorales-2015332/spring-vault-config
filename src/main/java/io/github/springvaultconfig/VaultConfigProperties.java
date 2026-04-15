package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the spring-vault-config starter.
 */
@ConfigurationProperties(prefix = "vault.config")
public class VaultConfigProperties {

    /** Vault server URI, e.g. http://localhost:8200 */
    private String uri = "http://localhost:8200";

    /** Vault token used for authentication */
    private String token;

    /** Comma-separated list of secret paths to load at startup */
    private List<String> paths = new ArrayList<>();

    /** Namespace (Vault Enterprise) */
    private String namespace;

    /** Whether to fail fast if Vault is unreachable at startup */
    private boolean failFast = true;

    /** Lease renewal interval in seconds */
    private long renewalIntervalSeconds = 60;

    /** Token renewal interval in seconds */
    private long tokenRenewalIntervalSeconds = 300;

    /** TTL for the in-memory secret cache in seconds (0 = no caching) */
    private long cacheTtlSeconds = 300;

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public List<String> getPaths() { return paths; }
    public void setPaths(List<String> paths) { this.paths = paths; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public boolean isFailFast() { return failFast; }
    public void setFailFast(boolean failFast) { this.failFast = failFast; }

    public long getRenewalIntervalSeconds() { return renewalIntervalSeconds; }
    public void setRenewalIntervalSeconds(long renewalIntervalSeconds) { this.renewalIntervalSeconds = renewalIntervalSeconds; }

    public long getTokenRenewalIntervalSeconds() { return tokenRenewalIntervalSeconds; }
    public void setTokenRenewalIntervalSeconds(long tokenRenewalIntervalSeconds) { this.tokenRenewalIntervalSeconds = tokenRenewalIntervalSeconds; }

    public long getCacheTtlSeconds() { return cacheTtlSeconds; }
    public void setCacheTtlSeconds(long cacheTtlSeconds) { this.cacheTtlSeconds = cacheTtlSeconds; }
}
