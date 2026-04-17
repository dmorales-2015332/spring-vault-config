package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that computes and tracks SHA-256 checksums for Vault secrets,
 * allowing callers to detect whether a secret's value has changed.
 */
@Service
public class VaultSecretChecksum Service {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretChecksumService.class);

    private final Map<String, String> checksumStore = new ConcurrentHashMap<>();

    /**
     * Compute and store a checksum for the given secret path and value.
     *
     * @param path  the Vault secret path
     * @param value the secret value
     * @return hex-encoded SHA-256 checksum
     */
    public String computeAndStore(String path, String value) {
        String checksum = sha256(value);
        checksumStore.put(path, checksum);
        log.debug("Stored checksum for path '{}': {}", path, checksum);
        return checksum;
    }

    /**
     * Check whether the given value differs from the previously stored checksum.
     *
     * @param path  the Vault secret path
     * @param value the current secret value
     * @return true if the value has changed or no prior checksum exists
     */
    public boolean hasChanged(String path, String value) {
        String newChecksum = sha256(value);
        String existing = checksumStore.get(path);
        boolean changed = !newChecksum.equals(existing);
        if (changed) {
            log.info("Secret at path '{}' has changed (checksum mismatch).", path);
        }
        return changed;
    }

    /**
     * Retrieve the stored checksum for a path, or null if not present.
     */
    public String getChecksum(String path) {
        return checksumStore.get(path);
    }

    /**
     * Remove the stored checksum for a path.
     */
    public void evict(String path) {
        checksumStore.remove(path);
        log.debug("Evicted checksum for path '{}'", path);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
