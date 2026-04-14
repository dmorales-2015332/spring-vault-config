package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for Spring Vault Config starter.
 * All properties are prefixed with {@code vault}.
 */
@Validated
@ConfigurationProperties(prefix = "vault")
public class VaultConfigProperties {

    /** Vault server URI, e.g. https://vault.example.com:8200 */
    @NotBlank
    private String uri = "http://localhost:8200";

    /** Vault token used for authentication. */
    private String token;

    /** KV secrets engine mount path. */
    @NotBlank
    private String mountPath = "secret";

    /** Path within the mount to read secrets from. */
    @NotBlank
    private String secretPath = "application";

    /** Whether to enable automatic lease renewal. */
    private boolean leaseRenewalEnabled = true;

    /** Lease renewal interval in seconds (must be positive). */
    @Positive
    private long leaseRenewalIntervalSeconds = 60;

    /** Namespace to use (Vault Enterprise only; leave blank for OSS). */
    private String namespace;

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMountPath() { return mountPath; }
    public void setMountPath(String mountPath) { this.mountPath = mountPath; }

    public String getSecretPath() { return secretPath; }
    public void setSecretPath(String secretPath) { this.secretPath = secretPath; }

    public boolean isLeaseRenewalEnabled() { return leaseRenewalEnabled; }
    public void setLeaseRenewalEnabled(boolean leaseRenewalEnabled) { this.leaseRenewalEnabled = leaseRenewalEnabled; }

    public long getLeaseRenewalIntervalSeconds() { return leaseRenewalIntervalSeconds; }
    public void setLeaseRenewalIntervalSeconds(long leaseRenewalIntervalSeconds) { this.leaseRenewalIntervalSeconds = leaseRenewalIntervalSeconds; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
}
