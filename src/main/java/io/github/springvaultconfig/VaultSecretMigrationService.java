package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for migrating secrets between Vault paths or mount points.
 * Supports copying, moving, and bulk migration of secrets.
 */
public class VaultSecretMigrationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretMigrationService.class);

    private final VaultTemplate vaultTemplate;

    public VaultSecretMigrationService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = Objects.requireNonNull(vaultTemplate, "vaultTemplate must not be null");
    }

    /**
     * Copies a secret from sourcePath to destinationPath without removing the source.
     *
     * @param sourcePath      the Vault path to read from
     * @param destinationPath the Vault path to write to
     * @return true if migration succeeded, false otherwise
     */
    public boolean copySecret(String sourcePath, String destinationPath) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(destinationPath, "destinationPath must not be null");

        log.info("Copying secret from '{}' to '{}'", sourcePath, destinationPath);
        VaultResponse response = vaultTemplate.read(sourcePath);
        if (response == null || response.getData() == null) {
            log.warn("No secret found at source path '{}'", sourcePath);
            return false;
        }
        vaultTemplate.write(destinationPath, response.getData());
        log.info("Successfully copied secret from '{}' to '{}'", sourcePath, destinationPath);
        return true;
    }

    /**
     * Moves a secret from sourcePath to destinationPath, deleting the source after copy.
     *
     * @param sourcePath      the Vault path to read from
     * @param destinationPath the Vault path to write to
     * @return true if migration succeeded, false otherwise
     */
    public boolean moveSecret(String sourcePath, String destinationPath) {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(destinationPath, "destinationPath must not be null");

        log.info("Moving secret from '{}' to '{}'", sourcePath, destinationPath);
        boolean copied = copySecret(sourcePath, destinationPath);
        if (copied) {
            vaultTemplate.delete(sourcePath);
            log.info("Deleted source secret at '{}' after move", sourcePath);
        }
        return copied;
    }

    /**
     * Bulk-migrates a map of source->destination path pairs.
     *
     * @param pathMappings map of sourcePath to destinationPath
     * @return map of sourcePath to migration success status
     */
    public Map<String, Boolean> bulkMigrate(Map<String, String> pathMappings) {
        Objects.requireNonNull(pathMappings, "pathMappings must not be null");
        Map<String, Boolean> results = new HashMap<>();
        pathMappings.forEach((src, dst) -> {
            try {
                results.put(src, copySecret(src, dst));
            } catch (Exception e) {
                log.error("Failed to migrate secret from '{}' to '{}': {}", src, dst, e.getMessage());
                results.put(src, false);
            }
        });
        return results;
    }
}
