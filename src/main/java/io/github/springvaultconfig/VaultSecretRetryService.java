package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Service that retries failed Vault secret fetches with exponential backoff.
 */
@Service
public class VaultSecretRetryService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretRetryService.class);

    private final VaultTemplate vaultTemplate;
    private final VaultConfigProperties properties;

    public VaultSecretRetryService(VaultTemplate vaultTemplate, VaultConfigProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    /**
     * Attempt a secret at the given path, retrying up to maxAttempts times
     * with exponential backoff starting at initialDelay.
     */
    public Map<String, Object> readWithRetry(String path, int maxAttempts, Duration initialDelay) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        Duration delay = initialDelay;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Attempt {}/{} to read secret at path: {}", attempt, maxAttempts, path);
                var response = vaultTemplate.read(path);
                if (response == null || response.getData() == null) {
                    throw new VaultSecretLoadException("No data found at path: " + path);
                }
                log.info("Successfully read secret at path: {} on attempt {}", path, attempt);
                return response.getData();
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed for path {}: {}", attempt, maxAttempts, path, e.getMessage());
                if (attempt < maxAttempts) {
                    sleep(delay);
                    delay = delay.multipliedBy(2);
                }
            }
        }
        throw new VaultSecretLoadException(
                "Failed to read secret at path '" + path + "' after " + maxAttempts + " attempts",
                lastException);
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
