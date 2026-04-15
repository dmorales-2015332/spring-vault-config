package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for tracking and renewing Vault leases before they expire.
 */
public class VaultLeaseRenewalService {

    private static final Logger log = LoggerFactory.getLogger(VaultLeaseRenewalService.class);

    private final VaultOperations vaultOperations;
    private final VaultConfigProperties properties;
    private final Map<String, Long> leaseRegistry = new ConcurrentHashMap<>();

    public VaultLeaseRenewalService(VaultOperations vaultOperations, VaultConfigProperties properties) {
        this.vaultOperations = vaultOperations;
        this.properties = properties;
    }

    /**
     * Registers a lease ID with its expiry time (epoch millis).
     */
    public void registerLease(String leaseId, long ttlSeconds) {
        if (leaseId == null || leaseId.isBlank()) {
            return;
        }
        long expiryMs = System.currentTimeMillis() + (ttlSeconds * 1000L);
        leaseRegistry.put(leaseId, expiryMs);
        log.debug("Registered lease '{}' expiring in {}s", leaseId, ttlSeconds);
    }

    /**
     * Scheduled task that renews leases approaching expiry.
     * Runs at a fixed rate defined by vault.lease-renewal-interval-ms (default 60s).
     */
    @Scheduled(fixedRateString = "${vault.lease-renewal-interval-ms:60000}")
    public void renewExpiringLeases() {
        long renewThresholdMs = properties.getLeaseRenewalThresholdSeconds() * 1000L;
        long now = System.currentTimeMillis();

        leaseRegistry.entrySet().removeIf(entry -> {
            String leaseId = entry.getKey();
            long expiryMs = entry.getValue();

            if (now >= expiryMs) {
                log.warn("Lease '{}' has already expired, removing from registry", leaseId);
                return true;
            }

            if ((expiryMs - now) <= renewThresholdMs) {
                try {
                    VaultResponse response = vaultOperations.write(
                            "sys/leases/renew",
                            Map.of("lease_id", leaseId, "increment", properties.getLeaseDurationSeconds())
                    );
                    if (response != null && response.getData() != null) {
                        Object newTtl = response.getData().get("lease_duration");
                        long newTtlSeconds = newTtl instanceof Number ? ((Number) newTtl).longValue()
                                : properties.getLeaseDurationSeconds();
                        entry.setValue(System.currentTimeMillis() + newTtlSeconds * 1000L);
                        log.info("Renewed lease '{}' for {}s", leaseId, newTtlSeconds);
                    }
                } catch (Exception e) {
                    log.error("Failed to renew lease '{}': {}", leaseId, e.getMessage());
                }
            }
            return false;
        });
    }

    /**
     * Returns an unmodifiable snapshot of the current lease registry.
     */
    public Map<String, Long> getLeaseRegistry() {
        return Map.copyOf(leaseRegistry);
    }

    /**
     * Revokes a lease explicitly and removes it from the registry.
     */
    public void revokeLease(String leaseId) {
        try {
            vaultOperations.write("sys/leases/revoke", Map.of("lease_id", leaseId));
            leaseRegistry.remove(leaseId);
            log.info("Revoked lease '{}'", leaseId);
        } catch (Exception e) {
            log.error("Failed to revoke lease '{}': {}", leaseId, e.getMessage());
        }
    }
}
