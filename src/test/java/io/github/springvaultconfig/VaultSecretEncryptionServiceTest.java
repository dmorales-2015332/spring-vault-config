package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultSecretEncryptionServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new VaultSecretEncryptionService(vaultOperations, "my-key");
    }

    @Test
    void encrypt_returnsCiphertext() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("ciphertext", "vault:v1:abc123"));
        when(vaultOperations.write(eq("transit/encrypt/my-key"), anyMap())).thenReturn(response);

        String result = encryptionService.encrypt("dGVzdA==");

        assertThat(result).isEqualTo("vault:v1:abc123");
    }

    @Test
    void decrypt_returnsPlaintext() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("plaintext", "dGVzdA=="));
        when(vaultOperations.write(eq("transit/decrypt/my-key"), anyMap())).thenReturn(response);

        String result = encryptionService.decrypt("vault:v1:abc123");

        assertThat(result).isEqualTo("dGVzdA==");
    }

    @Test
    void encrypt_throwsWhenVaultReturnsNull() {
        when(vaultOperations.write(eq("transit/encrypt/my-key"), anyMap())).thenReturn(null);

        assertThatThrownBy(() -> encryptionService.encrypt("dGVzdA=="))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("null response during encryption");
    }

    @Test
    void decrypt_throwsWhenVaultReturnsNull() {
        when(vaultOperations.write(eq("transit/decrypt/my-key"), anyMap())).thenReturn(null);

        assertThatThrownBy(() -> encryptionService.decrypt("vault:v1:abc123"))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("null response during decryption");
    }

    @Test
    void encrypt_throwsWhenNullPlaintext() {
        assertThatThrownBy(() -> encryptionService.encrypt(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void decrypt_throwsWhenNullCiphertext() {
        assertThatThrownBy(() -> encryptionService.decrypt(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_throwsWhenVaultOperationsNull() {
        assertThatThrownBy(() -> new VaultSecretEncryptionService(null, "key"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_throwsWhenKeyNameNull() {
        assertThatThrownBy(() -> new VaultSecretEncryptionService(vaultOperations, null))
                .isInstanceOf(NullPointerException.class);
    }
}
