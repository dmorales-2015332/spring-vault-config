package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that enforces rate limiting on Vault secret fetch operations
 * to prevent overwhelming the Vault server.
 */
@Service
public class VaultSecretRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretRateLimiterService.class);

    private final int maxRequestsPerWindow;
    private final long windowSizeSeconds;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> windowStart = new ConcurrentHashMap<>();

    public VaultSecretRateLimiterService(int maxRequestsPerWindow, long windowSizeSeconds) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSizeSeconds = windowSizeSeconds;
    }

    /**
     * Returns true if the request for the given path is allowed under the rate limit.
     */
    public boolean isAllowed(String secretPath) {
        Instant now = Instant.now();
        windowStart.putIfAbsent(secretPath, now);
        requestCounts.putIfAbsent(secretPath, new AtomicInteger(0));

        Instant windowBegin = windowStart.get(secretPath);
        if (now.isAfter(windowBegin.plusSeconds(windowSizeSeconds))) {
            windowStart.put(secretPath, now);
            requestCounts.get(secretPath).set(0);
        }

        int count = requestCounts.get(secretPath).incrementAndGet();
        if (count > maxRequestsPerWindow) {
            log.warn("Rate limit exceeded for secret path '{}': {} requests in window", secretPath, count);
            return false;
        }
        return true;
    }

    public void reset(String secretPath) {
        requestCounts.remove(secretPath);
        windowStart.remove(secretPath);
    }

    public int getCurrentCount(String secretPath) {
        AtomicInteger counter = requestCounts.get(secretPath);
        return counter == null ? 0 : counter.get();
    }
}
