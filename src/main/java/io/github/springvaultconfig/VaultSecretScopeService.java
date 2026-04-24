package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing secret scopes, allowing secrets to be grouped
 * and accessed within a defined scope (e.g., environment, team, application).
 */
public class VaultSecretScopeService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretScopeService.class);

    private final VaultOperations vaultOperations;
    private final Map<String, Set<String>> scopeRegistry = new ConcurrentHashMap<>();

    public VaultSecretScopeService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     * Registers a secret path under a named scope.
     */
    public void registerScope(String scopeName, String secretPath) {
        scopeRegistry.computeIfAbsent(scopeName, k -> ConcurrentHashMap.newKeySet()).add(secretPath);
        logger.debug("Registered secret path '{}' under scope '{}'", secretPath, scopeName);
    }

    /**
     * Returns all secret paths registered under the given scope.
     */
    public Set<String> getPathsForScope(String scopeName) {
        return Collections.unmodifiableSet(
                scopeRegistry.getOrDefault(scopeName, Collections.emptySet()));
    }

    /**
     * Reads all secrets for every path within the given scope.
     */
    public Map<String, Map<String, Object>> readScope(String scopeName) {
        Set<String> paths = getPathsForScope(scopeName);
        if (paths.isEmpty()) {
            logger.warn("No paths registered for scope '{}'", scopeName);
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String path : paths) {
            try {
                VaultResponse response = vaultOperations.read(path);
                if (response != null && response.getData() != null) {
                    result.put(path, response.getData());
                } else {
                    logger.warn("No data found at path '{}' for scope '{}'", path, scopeName);
                }
            } catch (Exception e) {
                logger.error("Failed to read secret at path '{}' for scope '{}': {}", path, scopeName, e.getMessage());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Removes a scope and all its registered paths.
     */
    public boolean removeScope(String scopeName) {
        boolean removed = scopeRegistry.remove(scopeName) != null;
        if (removed) {
            logger.info("Removed scope '{}'", scopeName);
        }
        return removed;
    }

    /**
     * Returns all registered scope names.
     */
    public Set<String> listScopes() {
        return Collections.unmodifiableSet(scopeRegistry.keySet());
    }
}
