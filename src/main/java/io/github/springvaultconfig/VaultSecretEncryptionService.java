package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Objects;

/**
 * Service for encrypting and decrypting data using HashiCorp Vault's Transit secrets engine.
 */
public class VaultSecretEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretEncryptionService.class);

    private static final String TRANSIT_PATH = "transit";
    private static final String CIPHERTEXT_KEY = "ciphertext";
    private static final String PLAINTEXT_KEY = "plaintext";

    private final VaultOperations vaultOperations;
    private final String keyName;

    public VaultSecretEncryptionService(VaultOperations vaultOperations, String keyName) {
        this.vaultOperations = Objects.requireNonNull(vaultOperations, "vaultOperations must not be null");
        this.keyName = Objects.requireNonNull(keyName, "keyName must not be null");
    }

    /**
     * Encrypts plaintext using Vault Transit engine.
     *
     * @param plaintext base64-encoded plaintext to encrypt
     * @return ciphertext returned by Vault
     */
    public String encrypt(String plaintext) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        log.debug("Encrypting data using Vault Transit key: {}", keyName);
        String path = TRANSIT_PATH + "/encrypt/" + keyName;
        VaultResponse response = vaultOperations.write(path, Map.of(PLAINTEXT_KEY, plaintext));
        if (response == null || response.getData() == null) {
            throw new VaultSecretLoadException("Vault returned null response during encryption");
        }
        String ciphertext = (String) response.getData().get(CIPHERTEXT_KEY);
        if (ciphertext == null) {
            throw new VaultSecretLoadException("No ciphertext returned from Vault Transit encrypt");
        }
        log.debug("Encryption successful for key: {}", keyName);
        return ciphertext;
    }

    /**
     * Decrypts ciphertext using Vault Transit engine.
     *
     * @param ciphertext ciphertext to decrypt
     * @return base64-encoded plaintext
     */
    public String decrypt(String ciphertext) {
        Objects.requireNonNull(ciphertext, "ciphertext must not be null");
        log.debug("Decrypting data using Vault Transit key: {}", keyName);
        String path = TRANSIT_PATH + "/decrypt/" + keyName;
        VaultResponse response = vaultOperations.write(path, Map.of(CIPHERTEXT_KEY, ciphertext));
        if (response == null || response.getData() == null) {
            throw new VaultSecretLoadException("Vault returned null response during decryption");
        }
        String plaintext = (String) response.getData().get(PLAINTEXT_KEY);
        if (plaintext == null) {
            throw new VaultSecretLoadException("No plaintext returned from Vault Transit decrypt");
        }
        log.debug("Decryption successful for key: {}", keyName);
        return plaintext;
    }
}
