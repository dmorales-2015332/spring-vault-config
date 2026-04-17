package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for exporting Vault secrets to various formats (env, properties, json).
 */
@Service
public class VaultSecretExportService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretExportService.class);

    private final VaultSecretLoader secretLoader;
    private final VaultSecretMaskingService maskingService;

    public VaultSecretExportService(VaultSecretLoader secretLoader, VaultSecretMaskingService maskingService) {
        this.secretLoader = secretLoader;
        this.maskingService = maskingService;
    }

    public Map<String, String> exportAsMap(String path) {
        logger.debug("Exporting secrets from path: {}", path);
        Map<String, Object> secrets = secretLoader.loadSecrets(path);
        Map<String, String> result = new HashMap<>();
        secrets.forEach((k, v) -> result.put(k, v != null ? v.toString() : ""));
        return result;
    }

    public String exportAsProperties(String path) {
        Map<String, String> secrets = exportAsMap(path);
        return secrets.entrySet().stream()
                .map(e -> e.getKey() + "=" + maskingService.mask(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
    }

    public String exportAsEnv(String path) {
        Map<String, String> secrets = exportAsMap(path);
        return secrets.entrySet().stream()
                .map(e -> e.getKey().toUpperCase().replace(".", "_") + "=" + maskingService.mask(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
    }

    public String exportAsJson(String path) {
        Map<String, String> secrets = exportAsMap(path);
        Set<String> entries = secrets.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": \"" + maskingService.mask(e.getKey(), e.getValue()) + "\"")
                .collect(Collectors.toSet());
        return "{" + String.join(", ", entries) + "}";
    }
}
