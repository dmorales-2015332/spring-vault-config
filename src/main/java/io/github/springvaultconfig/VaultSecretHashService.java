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
 * Service for computing and comparing cryptographic hashes of secret values.
 * Useful for detecting changes in secrets without exposing the actual values.
 */
@Service
public class VaultSecretHashService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretHashService.class);
    private static final String DEFAULT_ALGORITHM = "SHA-256";

    private final Map<String, String> hashCache = new ConcurrentHashMap<>();
    private final String algorithm;

    public VaultSecretHashService() {
        this(DEFAULT_ALGORITHM);
    }

    public VaultSecretHashService(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Computes a hex-encoded hash of the given secret value.
     *
     * @param secretValue the plaintext secret value
     * @return hex-encoded hash string
     */
    public String hash(String secretValue) {
        if (secretValue == null) {
            throw new IllegalArgumentException("Secret value must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(secretValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Stores the hash of a secret under the given key.
     *
     * @param secretKey   the key identifying the secret
     * @param secretValue the plaintext secret value to hash and store
     */
    public void storeHash(String secretKey, String secretValue) {
        String computed = hash(secretValue);
        hashCache.put(secretKey, computed);
        log.debug("Stored hash for secret key '{}'", secretKey);
    }

    /**
     * Checks whether the given secret value matches the previously stored hash.
     *
     * @param secretKey   the key identifying the secret
     * @param secretValue the plaintext secret value to verify
     * @return true if the hash matches, false otherwise
     */
    public boolean matches(String secretKey, String secretValue) {
        String stored = hashCache.get(secretKey);
        if (stored == null) {
            log.warn("No stored hash found for secret key '{}'", secretKey);
            return false;
        }
        boolean match = stored.equals(hash(secretValue));
        log.debug("Hash match for secret key '{}': {}", secretKey, match);
        return match;
    }

    /**
     * Returns true if the secret value has changed compared to the stored hash.
     *
     * @param secretKey   the key identifying the secret
     * @param secretValue the new plaintext secret value
     * @return true if the secret has changed or no previous hash exists
     */
    public boolean hasChanged(String secretKey, String secretValue) {
        return !matches(secretKey, secretValue);
    }

    /**
     * Removes the stored hash for the given secret key.
     *
     * @param secretKey the key identifying the secret
     */
    public void evict(String secretKey) {
        hashCache.remove(secretKey);
        log.debug("Evicted hash for secret key '{}'", secretKey);
    }

    /**
     * Returns the number of hashes currently stored in the cache.
     */
    public int size() {
        return hashCache.size();
    }
}
