package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for acquiring and releasing advisory locks on Vault secret paths,
 * preventing concurrent modifications during sensitive operations.
 */
public class VaultSecretLockService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretLockService.class);
    private static final String LOCK_SUFFIX = "/.lock";

    private final VaultOperations vaultOperations;
    private final Map<String, Instant> localLocks = new ConcurrentHashMap<>();

    public VaultSecretLockService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     * Attempts to acquire a lock for the given secret path.
     *
     * @param secretPath the Vault secret path to lock
     * @param owner      identifier of the lock owner
     * @return true if the lock was acquired, false otherwise
     */
    public boolean acquireLock(String secretPath, String owner) {
        String lockPath = secretPath + LOCK_SUFFIX;
        try {
            VaultResponse existing = vaultOperations.read(lockPath);
            if (existing != null && existing.getData() != null) {
                log.warn("Lock already held on path '{}' by owner '{}'", secretPath,
                        existing.getData().get("owner"));
                return false;
            }
            vaultOperations.write(lockPath, Map.of(
                    "owner", owner,
                    "acquiredAt", Instant.now().toString()
            ));
            localLocks.put(secretPath, Instant.now());
            log.info("Lock acquired on path '{}' by owner '{}'", secretPath, owner);
            return true;
        } catch (Exception e) {
            log.error("Failed to acquire lock on path '{}': {}", secretPath, e.getMessage());
            return false;
        }
    }

    /**
     * Releases the lock for the given secret path.
     *
     * @param secretPath the Vault secret path to unlock
     * @param owner      identifier of the lock owner (must match)
     * @return true if the lock was released, false otherwise
     */
    public boolean releaseLock(String secretPath, String owner) {
        String lockPath = secretPath + LOCK_SUFFIX;
        try {
            VaultResponse existing = vaultOperations.read(lockPath);
            if (existing == null || existing.getData() == null) {
                log.warn("No lock found on path '{}'", secretPath);
                return false;
            }
            Object lockOwner = existing.getData().get("owner");
            if (!owner.equals(lockOwner)) {
                log.warn("Cannot release lock on '{}': owner mismatch (expected '{}', got '{}')",
                        secretPath, lockOwner, owner);
                return false;
            }
            vaultOperations.delete(lockPath);
            localLocks.remove(secretPath);
            log.info("Lock released on path '{}' by owner '{}'", secretPath, owner);
            return true;
        } catch (Exception e) {
            log.error("Failed to release lock on path '{}': {}", secretPath, e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a lock is currently held on the given secret path.
     */
    public boolean isLocked(String secretPath) {
        try {
            VaultResponse response = vaultOperations.read(secretPath + LOCK_SUFFIX);
            return response != null && response.getData() != null;
        } catch (Exception e) {
            log.error("Failed to check lock status on path '{}': {}", secretPath, e.getMessage());
            return false;
        }
    }

    /**
     * Returns the owner of the lock if one exists.
     */
    public Optional<String> getLockOwner(String secretPath) {
        try {
            VaultResponse response = vaultOperations.read(secretPath + LOCK_SUFFIX);
            if (response != null && response.getData() != null) {
                return Optional.ofNullable((String) response.getData().get("owner"));
            }
        } catch (Exception e) {
            log.error("Failed to get lock owner for path '{}': {}", secretPath, e.getMessage());
        }
        return Optional.empty();
    }
}
