package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;

import java.util.*;

/**
 * Service for managing access control lists (ACLs) on Vault secrets.
 * Allows restricting which application roles can read specific secret paths.
 */
public class VaultSecretAclService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretAclService.class);

    private final VaultOperations vaultOperations;
    // path -> set of allowed roles
    private final Map<String, Set<String>> aclMap = new HashMap<>();

    public VaultSecretAclService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    public void grantAccess(String secretPath, String role) {
        aclMap.computeIfAbsent(secretPath, k -> new HashSet<>()).add(role);
        log.info("Granted role '{}' access to secret path '{}'", role, secretPath);
    }

    public void revokeAccess(String secretPath, String role) {
        Set<String> roles = aclMap.get(secretPath);
        if (roles != null) {
            roles.remove(role);
            log.info("Revoked role '{}' access to secret path '{}'", role, secretPath);
        }
    }

    public boolean isAccessAllowed(String secretPath, String role) {
        Set<String> roles = aclMap.get(secretPath);
        if (roles == null || roles.isEmpty()) {
            // No ACL defined — open by default
            return true;
        }
        boolean allowed = roles.contains(role);
        if (!allowed) {
            log.warn("Access denied for role '{}' on secret path '{}'", role, secretPath);
        }
        return allowed;
    }

    public Set<String> getAllowedRoles(String secretPath) {
        return Collections.unmodifiableSet(aclMap.getOrDefault(secretPath, Collections.emptySet()));
    }

    public Map<String, Set<String>> getAllAcls() {
        return Collections.unmodifiableMap(aclMap);
    }

    public void clearAcl(String secretPath) {
        aclMap.remove(secretPath);
        log.info("Cleared ACL for secret path '{}'", secretPath);
    }
}
