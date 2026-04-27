package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretLockServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretLockService lockService;

    @BeforeEach
    void setUp() {
        lockService = new VaultSecretLockService(vaultOperations);
    }

    @Test
    void acquireLock_shouldReturnTrue_whenNoExistingLock() {
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(null);

        boolean result = lockService.acquireLock("secret/myapp", "service-a");

        assertThat(result).isTrue();
        verify(vaultOperations).write(eq("secret/myapp/.lock"), anyMap());
    }

    @Test
    void acquireLock_shouldReturnFalse_whenLockAlreadyHeld() {
        VaultResponse existing = new VaultResponse();
        existing.setData(Map.of("owner", "service-b", "acquiredAt", "2024-01-01T00:00:00Z"));
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(existing);

        boolean result = lockService.acquireLock("secret/myapp", "service-a");

        assertThat(result).isFalse();
        verify(vaultOperations, never()).write(anyString(), anyMap());
    }

    @Test
    void releaseLock_shouldReturnTrue_whenOwnerMatches() {
        VaultResponse existing = new VaultResponse();
        existing.setData(Map.of("owner", "service-a"));
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(existing);

        boolean result = lockService.releaseLock("secret/myapp", "service-a");

        assertThat(result).isTrue();
        verify(vaultOperations).delete("secret/myapp/.lock");
    }

    @Test
    void releaseLock_shouldReturnFalse_whenOwnerMismatch() {
        VaultResponse existing = new VaultResponse();
        existing.setData(Map.of("owner", "service-b"));
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(existing);

        boolean result = lockService.releaseLock("secret/myapp", "service-a");

        assertThat(result).isFalse();
        verify(vaultOperations, never()).delete(anyString());
    }

    @Test
    void releaseLock_shouldReturnFalse_whenNoLockExists() {
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(null);

        boolean result = lockService.releaseLock("secret/myapp", "service-a");

        assertThat(result).isFalse();
    }

    @Test
    void isLocked_shouldReturnTrue_whenLockExists() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("owner", "service-a"));
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(response);

        assertThat(lockService.isLocked("secret/myapp")).isTrue();
    }

    @Test
    void isLocked_shouldReturnFalse_whenNoLock() {
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(null);

        assertThat(lockService.isLocked("secret/myapp")).isFalse();
    }

    @Test
    void getLockOwner_shouldReturnOwner_whenLockExists() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("owner", "service-a"));
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(response);

        Optional<String> owner = lockService.getLockOwner("secret/myapp");

        assertThat(owner).isPresent().contains("service-a");
    }

    @Test
    void getLockOwner_shouldReturnEmpty_whenNoLock() {
        when(vaultOperations.read("secret/myapp/.lock")).thenReturn(null);

        Optional<String> owner = lockService.getLockOwner("secret/myapp");

        assertThat(owner).isEmpty();
    }

    @Test
    void acquireLock_shouldReturnFalse_onException() {
        when(vaultOperations.read(anyString())).thenThrow(new RuntimeException("Vault unavailable"));

        boolean result = lockService.acquireLock("secret/myapp", "service-a");

        assertThat(result).isFalse();
    }
}
