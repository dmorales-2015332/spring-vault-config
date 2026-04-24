package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking and querying the time-to-live (TTL) of Vault secrets.
 * Maintains an in-memory registry of secret paths with their expiry timestamps.
 */
public class VaultSecretTtlService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretTtlService.class);

    private final VaultOperations vaultOperations;
    private final Map<String, Instant> ttlRegistry = new ConcurrentHashMap<>();

    public VaultSecretTtlService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     * Registers a secret path with an explicit TTL duration from now.
     *
     * @param secretPath the Vault secret path
     * @param ttl        the duration until the secret expires
     */
    public void registerTtl(String secretPath, Duration ttl) {
        Instant expiry = Instant.now().plus(ttl);
        ttlRegistry.put(secretPath, expiry);
        log.debug("Registered TTL for secret '{}': expires at {}", secretPath, expiry);
    }

    /**
     * Returns the remaining TTL for a registered secret path.
     *
     * @param secretPath the Vault secret path
     * @return an Optional containing the remaining Duration, or empty if not registered or already expired
     */
    public Optional<Duration> getRemainingTtl(String secretPath) {
        Instant expiry = ttlRegistry.get(secretPath);
        if (expiry == null) {
            return Optional.empty();
        }
        Duration remaining = Duration.between(Instant.now(), expiry);
        if (remaining.isNegative()) {
            log.debug("Secret '{}' TTL has expired", secretPath);
            return Optional.empty();
        }
        return Optional.of(remaining);
    }

    /**
     * Checks whether a registered secret has expired.
     *
     * @param secretPath the Vault secret path
     * @return true if expired or not registered, false if still valid
     */
    public boolean isExpired(String secretPath) {
        return getRemainingTtl(secretPath).isEmpty();
    }

    /**
     * Removes a secret path from the TTL registry.
     *
     * @param secretPath the Vault secret path
     */
    public void deregister(String secretPath) {
        ttlRegistry.remove(secretPath);
        log.debug("Deregistered TTL for secret '{}'", secretPath);
    }

    /**
     * Returns a snapshot of all registered secret paths and their expiry instants.
     */
    public Map<String, Instant> getAllRegistrations() {
        return Map.copyOf(ttlRegistry);
    }
}
