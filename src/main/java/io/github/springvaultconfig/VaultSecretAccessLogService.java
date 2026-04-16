package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks and logs access to Vault secrets for auditing purposes.
 */
@Service
public class VaultSecretAccessLogService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretAccessLogService.class);

    private final Map<String, List<AccessEntry>> accessLog = new ConcurrentHashMap<>();
    private final int maxEntriesPerSecret;

    public VaultSecretAccessLogService(int maxEntriesPerSecret) {
        this.maxEntriesPerSecret = maxEntriesPerSecret;
    }

    public VaultSecretAccessLogService() {
        this(100);
    }

    public void recordAccess(String secretPath, String accessor) {
        if (secretPath == null || secretPath.isBlank()) {
            throw new IllegalArgumentException("Secret path must not be blank");
        }
        AccessEntry entry = new AccessEntry(secretPath, accessor, Instant.now());
        accessLog.compute(secretPath, (k, list) -> {
            if (list == null) list = new ArrayList<>();
            if (list.size() >= maxEntriesPerSecret) list.remove(0);
            list.add(entry);
            return list;
        });
        log.debug("Secret accessed: path={} accessor={} at={}", secretPath, accessor, entry.timestamp());
    }

    public List<AccessEntry> getAccessLog(String secretPath) {
        return Collections.unmodifiableList(accessLog.getOrDefault(secretPath, List.of()));
    }

    public void clearLog(String secretPath) {
        accessLog.remove(secretPath);
    }

    public record AccessEntry(String secretPath, String accessor, Instant timestamp) {}
}
