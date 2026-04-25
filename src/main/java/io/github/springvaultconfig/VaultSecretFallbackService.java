package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that provides fallback secret values when Vault is unavailable.
 * Fallback values can be registered programmatically or loaded from a static source.
 */
@Service
public class VaultSecretFallbackService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretFallbackService.class);

    private final Map<String, String> fallbackSecrets = new ConcurrentHashMap<>();
    private final VaultConfigProperties properties;

    public VaultSecretFallbackService(VaultConfigProperties properties) {
        this.properties = properties;
    }

    /**
     * Register a fallback value for a given secret key.
     *
     * @param key   the secret key
     * @param value the fallback value
     */
    public void registerFallback(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Fallback key must not be null or blank");
        }
        fallbackSecrets.put(key, value);
        log.debug("Registered fallback for secret key: {}", key);
    }

    /**
     * Retrieve a fallback value for the given secret key.
     *
     * @param key the secret key
     * @return an Optional containing the fallback value, or empty if none registered
     */
    public Optional<String> getFallback(String key) {
        String value = fallbackSecrets.get(key);
        if (value != null) {
            log.warn("Using fallback value for secret key: {}", key);
        }
        return Optional.ofNullable(value);
    }

    /**
     * Remove a registered fallback value.
     *
     * @param key the secret key to deregister
     */
    public void removeFallback(String key) {
        fallbackSecrets.remove(key);
        log.debug("Removed fallback for secret key: {}", key);
    }

    /**
     * Check whether a fallback is registered for the given key.
     *
     * @param key the secret key
     * @return true if a fallback exists
     */
    public boolean hasFallback(String key) {
        return fallbackSecrets.containsKey(key);
    }

    /**
     * Return all currently registered fallback keys.
     *
     * @return an unmodifiable view of registered fallback keys
     */
    public Map<String, String> getAllFallbacks() {
        return Map.copyOf(fallbackSecrets);
    }

    /**
     * Clear all registered fallbacks.
     */
    public void clearFallbacks() {
        fallbackSecrets.clear();
        log.info("All fallback secrets cleared");
    }
}
