package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultToken;
import org.springframework.vault.support.VaultTokenRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service responsible for renewing the Vault authentication token before it expires.
 * Periodically checks token TTL and renews when below the configured threshold.
 */
public class VaultTokenRenewalService {

    private static final Logger log = LoggerFactory.getLogger(VaultTokenRenewalService.class);

    private final VaultOperations vaultOperations;
    private final VaultConfigProperties properties;
    private final AtomicReference<Instant> lastRenewalTime = new AtomicReference<>(Instant.now());

    public VaultTokenRenewalService(VaultOperations vaultOperations, VaultConfigProperties properties) {
        this.vaultOperations = vaultOperations;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${spring.vault.token-renewal.check-interval-ms:60000}")
    public void renewTokenIfNeeded() {
        try {
            log.debug("Checking Vault token validity...");
            var tokenInfo = vaultOperations.opsForToken().lookupSelf();
            if (tokenInfo == null || tokenInfo.getData() == null) {
                log.warn("Unable to look up Vault token info; skipping renewal.");
                return;
            }

            Number ttl = (Number) tokenInfo.getData().get("ttl");
            boolean renewable = Boolean.TRUE.equals(tokenInfo.getData().get("renewable"));

            if (ttl == null) {
                log.debug("Token has no TTL (likely a root token); skipping renewal.");
                return;
            }

            long ttlSeconds = ttl.longValue();
            long thresholdSeconds = properties.getTokenRenewalThresholdSeconds();

            if (renewable && ttlSeconds < thresholdSeconds) {
                log.info("Vault token TTL {}s is below threshold {}s — renewing token.", ttlSeconds, thresholdSeconds);
                vaultOperations.opsForToken().renewSelf(Duration.ofSeconds(properties.getTokenRenewalIncrementSeconds()));
                lastRenewalTime.set(Instant.now());
                log.info("Vault token successfully renewed.");
            } else {
                log.debug("Vault token TTL {}s is sufficient (threshold {}s); no renewal needed.", ttlSeconds, thresholdSeconds);
            }
        } catch (Exception ex) {
            log.error("Failed to renew Vault token: {}", ex.getMessage(), ex);
        }
    }

    public Instant getLastRenewalTime() {
        return lastRenewalTime.get();
    }
}
