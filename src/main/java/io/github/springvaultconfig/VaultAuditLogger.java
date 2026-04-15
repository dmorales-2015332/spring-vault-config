package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Logs audit events for Vault secret access and rotation.
 * Tracks secret load counts and rotation events for observability.
 */
@Component
public class VaultAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(VaultAuditLogger.class);

    private final AtomicLong secretLoadCount = new AtomicLong(0);
    private final AtomicLong rotationCount = new AtomicLong(0);
    private volatile Instant lastRotationTime;

    /**
     * Records a secret load event for the given path.
     *
     * @param path the Vault secret path that was loaded
     */
    public void recordSecretLoad(String path) {
        long count = secretLoadCount.incrementAndGet();
        log.info("[VAULT AUDIT] Secret loaded from path='{}' totalLoads={} at={}",
                path, count, Instant.now());
    }

    /**
     * Records a secret access denial or error.
     *
     * @param path   the Vault secret path
     * @param reason the reason for the failure
     */
    public void recordSecretAccessFailure(String path, String reason) {
        log.warn("[VAULT AUDIT] Secret access failed path='{}' reason='{}' at={}",
                path, reason, Instant.now());
    }

    /**
     * Listens for VaultSecretRotatedEvent and records rotation audit entry.
     *
     * @param event the rotation event
     */
    @EventListener
    public void onSecretRotated(VaultSecretRotatedEvent event) {
        long count = rotationCount.incrementAndGet();
        lastRotationTime = Instant.now();
        log.info("[VAULT AUDIT] Secret rotated path='{}' totalRotations={} at={}",
                event.getSecretPath(), count, lastRotationTime);
    }

    public long getSecretLoadCount() {
        return secretLoadCount.get();
    }

    public long getRotationCount() {
        return rotationCount.get();
    }

    public Instant getLastRotationTime() {
        return lastRotationTime;
    }
}
