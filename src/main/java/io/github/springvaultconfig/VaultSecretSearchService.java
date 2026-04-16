package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VaultSecretSearchService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretSearchService.class);

    private final VaultTemplate vaultTemplate;

    public VaultSecretSearchService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public Map<String, Object> searchByKeyPattern(String path, String keyPattern) {
        Pattern pattern = Pattern.compile(keyPattern);
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            log.warn("No secrets found at path: {}", path);
            return Collections.emptyMap();
        }
        return response.getData().entrySet().stream()
                .filter(e -> pattern.matcher(e.getKey()).matches())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<String> listPaths(String mountPath) {
        VaultResponse response = vaultTemplate.read(mountPath + "?list=true");
        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }
        Object keys = response.getData().get("keys");
        if (keys instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Map<String, Object> searchByValueContains(String path, String substring) {
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            return Collections.emptyMap();
        }
        return response.getData().entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().toString().contains(substring))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
