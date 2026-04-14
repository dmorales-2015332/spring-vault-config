package io.github.springvaultconfig;

/**
 * Exception thrown when secrets cannot be loaded from HashiCorp Vault
 * and fail-fast mode is enabled.
 */
public class VaultSecretLoadException extends RuntimeException {

    public VaultSecretLoadException(String message) {
        super(message);
    }

    public VaultSecretLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
