package io.github.springvaultconfig;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a secret access threshold is breached.
 */
public class VaultSecretAlertEvent extends ApplicationEvent {

    private final String secretPath;
    private final int accessCount;
    private final int threshold;

    public VaultSecretAlertEvent(Object source, String secretPath, int accessCount, int threshold) {
        super(source);
        this.secretPath = secretPath;
        this.accessCount = accessCount;
        this.threshold = threshold;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return "VaultSecretAlertEvent{secretPath='" + secretPath +
                "', accessCount=" + accessCount +
                ", threshold=" + threshold + '}';
    }
}
