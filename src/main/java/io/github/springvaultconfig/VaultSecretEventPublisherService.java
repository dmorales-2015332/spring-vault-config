package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service responsible for publishing Vault secret lifecycle events to the Spring
 * application event bus, allowing other components to react to secret changes.
 */
@Service
public class VaultSecretEventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretEventPublisherService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final AtomicLong publishedEventCount = new AtomicLong(0);

    public VaultSecretEventPublisherService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a secret-loaded event for the given path and secrets.
     *
     * @param path    the Vault secret path
     * @param secrets the loaded key-value pairs
     */
    public void publishSecretsLoaded(String path, Map<String, String> secrets) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be null or blank");
        }
        VaultSecretEvent event = new VaultSecretEvent(
                this, VaultSecretEvent.Type.LOADED, path, secrets, Instant.now());
        eventPublisher.publishEvent(event);
        long count = publishedEventCount.incrementAndGet();
        log.debug("Published LOADED event for path '{}' (total events published: {})", path, count);
    }

    /**
     * Publishes a secret-rotated event for the given path.
     *
     * @param path    the Vault secret path
     * @param secrets the new key-value pairs after rotation
     */
    public void publishSecretsRotated(String path, Map<String, String> secrets) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be null or blank");
        }
        VaultSecretEvent event = new VaultSecretEvent(
                this, VaultSecretEvent.Type.ROTATED, path, secrets, Instant.now());
        eventPublisher.publishEvent(event);
        long count = publishedEventCount.incrementAndGet();
        log.info("Published ROTATED event for path '{}' (total events published: {})", path, count);
    }

    /**
     * Publishes a secret-expired event for the given path.
     *
     * @param path the Vault secret path that has expired
     */
    public void publishSecretsExpired(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be null or blank");
        }
        VaultSecretEvent event = new VaultSecretEvent(
                this, VaultSecretEvent.Type.EXPIRED, path, Map.of(), Instant.now());
        eventPublisher.publishEvent(event);
        long count = publishedEventCount.incrementAndGet();
        log.warn("Published EXPIRED event for path '{}' (total events published: {})", path, count);
    }

    /**
     * Returns the total number of events published since startup.
     */
    public long getPublishedEventCount() {
        return publishedEventCount.get();
    }
}
