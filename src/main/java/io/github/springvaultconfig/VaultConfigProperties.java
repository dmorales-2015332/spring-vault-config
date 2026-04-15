package io.github.springvaultconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the spring-vault-config starter.
 */
@ConfigurationProperties(prefix = "spring.vault")
public class VaultConfigProperties {

    /** Vault server URI. */
    private String uri = "http://localhost:8200";

    /** Vault authentication token. */
    private String token;

    /** Path prefix for secrets (e.g. "secret/myapp"). */
    private String secretPath = "secret/application";

    /** Whether lease renewal is enabled. */
    private boolean leaseRenewalEnabled = true;

    /** Lease renewal interval in seconds. */
    private int leaseRenewalIntervalSeconds = 60;

    /** Whether token renewal is enabled. */
    private boolean tokenRenewalEnabled = true;

    /** Token renewal interval in seconds. */
    private int tokenRenewalIntervalSeconds = 300;

    /** Whether health indicator is enabled. */
    private boolean healthEnabled = true;

    /** Whether audit logging is enabled. */
    private boolean auditEnabled = true;

    /** Whether secret caching is enabled. */
    private boolean cacheEnabled = true;

    /** Cache TTL in seconds. */
    private int cacheTtlSeconds = 300;

    /** Whether secret masking is enabled. */
    private boolean maskingEnabled = true;

    /** Whether secret versioning is enabled. */
    private boolean versioningEnabled = false;

    /** Whether transit encryption is enabled. */
    private boolean encryptionEnabled = false;

    /** Transit encryption key name. */
    private String encryptionKeyName = "spring-vault-config";

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getSecretPath() { return secretPath; }
    public void setSecretPath(String secretPath) { this.secretPath = secretPath; }

    public boolean isLeaseRenewalEnabled() { return leaseRenewalEnabled; }
    public void setLeaseRenewalEnabled(boolean leaseRenewalEnabled) { this.leaseRenewalEnabled = leaseRenewalEnabled; }

    public int getLeaseRenewalIntervalSeconds() { return leaseRenewalIntervalSeconds; }
    public void setLeaseRenewalIntervalSeconds(int leaseRenewalIntervalSeconds) { this.leaseRenewalIntervalSeconds = leaseRenewalIntervalSeconds; }

    public boolean isTokenRenewalEnabled() { return tokenRenewalEnabled; }
    public void setTokenRenewalEnabled(boolean tokenRenewalEnabled) { this.tokenRenewalEnabled = tokenRenewalEnabled; }

    public int getTokenRenewalIntervalSeconds() { return tokenRenewalIntervalSeconds; }
    public void setTokenRenewalIntervalSeconds(int tokenRenewalIntervalSeconds) { this.tokenRenewalIntervalSeconds = tokenRenewalIntervalSeconds; }

    public boolean isHealthEnabled() { return healthEnabled; }
    public void setHealthEnabled(boolean healthEnabled) { this.healthEnabled = healthEnabled; }

    public boolean isAuditEnabled() { return auditEnabled; }
    public void setAuditEnabled(boolean auditEnabled) { this.auditEnabled = auditEnabled; }

    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }

    public int getCacheTtlSeconds() { return cacheTtlSeconds; }
    public void setCacheTtlSeconds(int cacheTtlSeconds) { this.cacheTtlSeconds = cacheTtlSeconds; }

    public boolean isMaskingEnabled() { return maskingEnabled; }
    public void setMaskingEnabled(boolean maskingEnabled) { this.maskingEnabled = maskingEnabled; }

    public boolean isVersioningEnabled() { return versioningEnabled; }
    public void setVersioningEnabled(boolean versioningEnabled) { this.versioningEnabled = versioningEnabled; }

    public boolean isEncryptionEnabled() { return encryptionEnabled; }
    public void setEncryptionEnabled(boolean encryptionEnabled) { this.encryptionEnabled = encryptionEnabled; }

    public String getEncryptionKeyName() { return encryptionKeyName; }
    public void setEncryptionKeyName(String encryptionKeyName) { this.encryptionKeyName = encryptionKeyName; }
}
