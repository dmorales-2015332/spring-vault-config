package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking and enforcing secret read quotas per path.
 */
public class VaultSecretQuotaService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretQuotaService.class);

    private final VaultOperations vaultOperations;
    private final int maxReadsPerPath;
    private final ConcurrentHashMap<String, Integer> readCounts = new ConcurrentHashMap<>();

    public VaultSecretQuotaService(VaultOperations vaultOperations, int maxReadsPerPath) {
        this.vaultOperations = vaultOperations;
        this.maxReadsPerPath = maxReadsPerPath;
    }

    public Map<String, Object> readSecret(String path) {
        int count = readCounts.merge(path, 1, Integer::sum);
        if (count > maxReadsPerPath) {
            log.warn("Quota exceeded for vault path '{}': {} reads (max {})", path, count, maxReadsPerPath);
            throw new VaultSecretLoadException("Read quota exceeded for path: " + path);
        }
        log.debug("Reading secret at '{}' (read #{} of {})", path, count, maxReadsPerPath);
        VaultResponse response = vaultOperations.read(path);
        if (response == null || response.getData() == null) {
            return new HashMap<>();
        }
        return response.getData();
    }

    public int getReadCount(String path) {
        return readCounts.getOrDefault(path, 0);
    }

    public void resetQuota(String path) {
        readCounts.remove(path);
        log.info("Quota reset for vault path '{}'", path);
    }

    public void resetAllQuotas() {
        readCounts.clear();
        log.info("All vault path quotas reset");
    }

    public Map<String, Integer> getAllReadCounts() {
        return new HashMap<>(readCounts);
    }
}
