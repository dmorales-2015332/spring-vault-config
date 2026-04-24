package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for rolling back secrets to a previous version in HashiCorp Vault.
 * Supports KV v2 secrets engine rollback by restoring a specific version's data.
 */
public class VaultSecretRollbackService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretRollbackService.class);

    private final VaultOperations vaultOperations;
    private final VaultSecretVersioningService versioningService;

    public VaultSecretRollbackService(VaultOperations vaultOperations,
                                       VaultSecretVersioningService versioningService) {
        this.vaultOperations = vaultOperations;
        this.versioningService = versioningService;
    }

    /**
     * Rolls back the secret at the given path to the specified version.
     *
     * @param mountPath  the KV v2 mount path (e.g. "secret")
     * @param secretPath the path of the secret within the mount
     * @param version    the version number to roll back to
     * @return true if rollback succeeded, false otherwise
     */
    public boolean rollback(String mountPath, String secretPath, int version) {
        log.info("Rolling back secret '{}' at mount '{}' to version {}", secretPath, mountPath, version);

        Optional<Map<String, Object>> versionData = versioningService.readVersion(mountPath, secretPath, version);
        if (versionData.isEmpty()) {
            log.warn("Version {} not found for secret '{}' — rollback aborted", version, secretPath);
            return false;
        }

        String writePath = mountPath + "/data/" + secretPath;
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", versionData.get());

        try {
            vaultOperations.write(writePath, payload);
            log.info("Successfully rolled back secret '{}' to version {}", secretPath, version);
            return true;
        } catch (Exception ex) {
            log.error("Failed to roll back secret '{}' to version {}: {}", secretPath, version, ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Rolls back to the immediately previous version of the secret.
     *
     * @param mountPath  the KV v2 mount path
     * @param secretPath the path of the secret
     * @return true if rollback succeeded, false otherwise
     */
    public boolean rollbackToPrevious(String mountPath, String secretPath) {
        int currentVersion = versioningService.getCurrentVersion(mountPath, secretPath);
        if (currentVersion <= 1) {
            log.warn("Secret '{}' is already at version 1 — cannot roll back further", secretPath);
            return false;
        }
        return rollback(mountPath, secretPath, currentVersion - 1);
    }
}
