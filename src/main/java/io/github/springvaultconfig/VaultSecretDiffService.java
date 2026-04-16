package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service that computes the diff between two versions of Vault secrets,
 * identifying added, removed, and changed keys (values are masked).
 */
@Service
public class VaultSecretDiffService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretDiffService.class);

    private final VaultSecretMaskingService maskingService;

    public VaultSecretDiffService(VaultSecretMaskingService maskingService) {
        this.maskingService = maskingService;
    }

    public SecretDiffResult diff(Map<String, String> previous, Map<String, String> current) {
        Set<String> added = new HashSet<>(current.keySet());
        added.removeAll(previous.keySet());

        Set<String> removed = new HashSet<>(previous.keySet());
        removed.removeAll(current.keySet());

        Set<String> changed = new HashSet<>();
        for (String key : current.keySet()) {
            if (previous.containsKey(key) && !previous.get(key).equals(current.get(key))) {
                changed.add(key);
            }
        }

        if (!added.isEmpty()) log.info("Vault secrets added: {}", added);
        if (!removed.isEmpty()) log.info("Vault secrets removed: {}", removed);
        if (!changed.isEmpty()) log.info("Vault secrets changed: {}", changed);

        return new SecretDiffResult(added, removed, changed);
    }

    public record SecretDiffResult(Set<String> added, Set<String> removed, Set<String> changed) {
        public boolean hasChanges() {
            return !added.isEmpty() || !removed.isEmpty() || !changed.isEmpty();
        }
    }
}
