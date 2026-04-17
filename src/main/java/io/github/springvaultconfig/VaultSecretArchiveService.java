package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for archiving Vault secrets with timestamps for audit and recovery purposes.
 */
@Service
public class VaultSecretArchiveService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretArchiveService.class);

    private final VaultTemplate vaultTemplate;
    private final ConcurrentHashMap<String, List<ArchivedSecret>> archive = new ConcurrentHashMap<>();

    public VaultSecretArchiveService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public void archive(String path, Map<String, Object> secrets) {
        if (path == null || secrets == null || secrets.isEmpty()) {
            log.warn("Skipping archive for null or empty secrets at path: {}", path);
            return;
        }
        ArchivedSecret entry = new ArchivedSecret(path, Collections.unmodifiableMap(secrets), Instant.now());
        archive.computeIfAbsent(path, k -> new ArrayList<>()).add(entry);
        log.info("Archived {} secret(s) at path '{}' on {}", secrets.size(), path, entry.archivedAt());
    }

    public List<ArchivedSecret> getArchive(String path) {
        return Collections.unmodifiableList(archive.getOrDefault(path, Collections.emptyList()));
    }

    public Map<String, List<ArchivedSecret>> getAllArchives() {
        return Collections.unmodifiableMap(archive);
    }

    public void clearArchive(String path) {
        archive.remove(path);
        log.info("Cleared archive for path '{}'", path);
    }

    public int archiveSize(String path) {
        return archive.getOrDefault(path, Collections.emptyList()).size();
    }

    public record ArchivedSecret(String path, Map<String, Object> secrets, Instant archivedAt) {}
}
