package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for importing secrets into Vault from an external map or source.
 */
public class VaultSecretImportService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretImportService.class);

    private final VaultTemplate vaultTemplate;

    public VaultSecretImportService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Imports a map of secrets into the given Vault path.
     *
     * @param path    the Vault path to write to
     * @param secrets the key-value pairs to import
     */
    public void importSecrets(String path, Map<String, Object> secrets) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Vault path must not be null or blank");
        }
        if (secrets == null || secrets.isEmpty()) {
            log.warn("No secrets provided for import into path: {}", path);
            return;
        }
        log.info("Importing {} secret(s) into Vault path: {}", secrets.size(), path);
        vaultTemplate.write(path, secrets);
        log.info("Successfully imported secrets into Vault path: {}", path);
    }

    /**
     * Imports secrets with a namespace prefix prepended to the path.
     *
     * @param namespace the namespace prefix
     * @param path      the relative path
     * @param secrets   the key-value pairs to import
     */
    public void importSecretsWithNamespace(String namespace, String path, Map<String, Object> secrets) {
        String fullPath = namespace + "/" + path;
        importSecrets(fullPath, secrets);
    }

    /**
     * Reads existing secrets at a path and merges new secrets on top, then writes back.
     *
     * @param path      the Vault path
     * @param newSecrets the new secrets to merge in
     */
    public void mergeSecrets(String path, Map<String, Object> newSecrets) {
        Map<String, Object> merged = new HashMap<>();
        VaultResponse existing = vaultTemplate.read(path);
        if (existing != null && existing.getData() != null) {
            merged.putAll(existing.getData());
        }
        merged.putAll(newSecrets);
        log.info("Merging {} secret(s) into existing secrets at path: {}", newSecrets.size(), path);
        vaultTemplate.write(path, merged);
    }
}
