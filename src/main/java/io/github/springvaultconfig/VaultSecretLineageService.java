package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the lineage (origin and transformation history) of secrets loaded from Vault.
 * Useful for auditing and compliance — knowing where a secret came from and how it changed.
 */
@Service
public class VaultSecretLineageService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretLineageService.class);

    private final VaultTemplate vaultTemplate;

    /** Map of secretKey -> ordered list of lineage entries */
    private final Map<String, List<LineageEntry>> lineageMap = new ConcurrentHashMap<>();

    public VaultSecretLineageService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Records a lineage entry for a secret.
     *
     * @param secretKey  the logical key of the secret (e.g. "db.password")
     * @param vaultPath  the Vault path from which it was read
     * @param version    the version of the secret (may be null for KV v1)
     * @param source     a label describing the origin (e.g. "startup", "refresh", "rotation")
     */
    public void record(String secretKey, String vaultPath, Integer version, String source) {
        LineageEntry entry = new LineageEntry(secretKey, vaultPath, version, source, Instant.now());
        lineageMap.computeIfAbsent(secretKey, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);
        log.debug("Lineage recorded: key={} path={} version={} source={}", secretKey, vaultPath, version, source);
    }

    /**
     * Returns the full lineage history for a given secret key.
     */
    public List<LineageEntry> getLineage(String secretKey) {
        return Collections.unmodifiableList(lineageMap.getOrDefault(secretKey, Collections.emptyList()));
    }

    /**
     * Returns the most recent lineage entry for a secret key, or empty if none.
     */
    public Optional<LineageEntry> getLatest(String secretKey) {
        List<LineageEntry> entries = lineageMap.get(secretKey);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entries.size() - 1));
    }

    /**
     * Clears lineage history for a specific secret key.
     */
    public void clearLineage(String secretKey) {
        lineageMap.remove(secretKey);
        log.debug("Lineage cleared for key={}", secretKey);
    }

    /**
     * Returns all tracked secret keys.
     */
    public Set<String> trackedKeys() {
        return Collections.unmodifiableSet(lineageMap.keySet());
    }

    public record LineageEntry(
            String secretKey,
            String vaultPath,
            Integer version,
            String source,
            Instant recordedAt
    ) {}
}
