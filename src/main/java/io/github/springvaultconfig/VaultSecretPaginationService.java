package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VaultSecretPaginationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretPaginationService.class);

    private final VaultOperations vaultOperations;

    public VaultSecretPaginationService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    public List<String> listSecretKeys(String path) {
        var response = vaultOperations.list(path);
        if (response == null || response.getData() == null) {
            log.warn("No keys found at path: {}", path);
            return Collections.emptyList();
        }
        Object keys = response.getData().get("keys");
        if (keys instanceof List<?> keyList) {
            return keyList.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<String> listSecretKeysPage(String path, int page, int pageSize) {
        List<String> all = listSecretKeys(path);
        int fromIndex = page * pageSize;
        if (fromIndex >= all.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        return all.subList(fromIndex, toIndex);
    }

    public int countSecretKeys(String path) {
        return listSecretKeys(path).size();
    }

    public Map<String, Object> getSecretAtPath(String fullPath) {
        var response = vaultOperations.read(fullPath);
        if (response == null || response.getData() == null) {
            log.warn("No data found at path: {}", fullPath);
            return Collections.emptyMap();
        }
        return response.getData();
    }
}
