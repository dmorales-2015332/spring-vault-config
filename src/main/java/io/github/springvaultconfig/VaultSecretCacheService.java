package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches secrets fetched from Vault to reduce redundant API calls.
 * Each entry tracks its fetch time and a configurable TTL.
 */
@Service
public class VaultSecretCacheService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretCacheService.class);

    private final Duration cacheTtl;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public VaultSecretCacheService(VaultConfigProperties properties) {
        this.cacheTtl = Duration.ofSeconds(properties.getCacheTtlSeconds());
    }

    public void put(String path, Map<String, String> secrets) {
        cache.put(path, new CacheEntry(secrets, Instant.now()));
        log.debug("Cached secrets for path '{}', TTL={}s", path, cacheTtl.getSeconds());
    }

    public Optional<Map<String, String>> get(String path) {
        CacheEntry entry = cache.get(path);
        if (entry == null) {
            return Optional.empty();
        }
        if (Duration.between(entry.fetchedAt(), Instant.now()).compareTo(cacheTtl) > 0) {
            log.debug("Cache expired for path '{}'", path);
            cache.remove(path);
            return Optional.empty();
        }
        log.debug("Cache hit for path '{}'", path);
        return Optional.of(entry.secrets());
    }

    public void invalidate(String path) {
        cache.remove(path);
        log.debug("Invalidated cache for path '{}'", path);
    }

    public void invalidateAll() {
        cache.clear();
        log.info("All cached secrets invalidated");
    }

    public int size() {
        return cache.size();
    }

    private record CacheEntry(Map<String, String> secrets, Instant fetchedAt) {}
}
