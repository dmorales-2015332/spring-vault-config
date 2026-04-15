package io.github.springvaultconfig;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Spring application event published when a Vault secret value has been rotated
 * (i.e., its value has changed since the last poll).
 */
public class VaultSecretRotatedEvent extends ApplicationEvent {

    private final String secretKey;
    private final String vaultPath;
    private final Instant detectedAt;

    public VaultSecretRotatedEvent(Object source, String secretKey, String vaultPath, Instant detectedAt) {
        super(source);
        this.secretKey = secretKey;
        this.vaultPath = vaultPath;
        this.detectedAt = detectedAt;
    }

    /**
     * The individual key within the Vault secret that changed.
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * The Vault path where the secret is stored.
     */
    public String getVaultPath() {
        return vaultPath;
    }

    /**
     * The timestamp at which the rotation was detected.
     */
    public Instant getDetectedAt() {
        return detectedAt;
    }

    @Override
    public String toString() {
        return "VaultSecretRotatedEvent{" +
                "secretKey='" + secretKey + '\'' +
                ", vaultPath='" + vaultPath + '\'' +
                ", detectedAt=" + detectedAt +
                '}';
    }
}
