package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretRollbackServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    @Mock
    private VaultSecretVersioningService versioningService;

    private VaultSecretRollbackService rollbackService;

    @BeforeEach
    void setUp() {
        rollbackService = new VaultSecretRollbackService(vaultOperations, versioningService);
    }

    @Test
    void rollback_shouldWritePreviousVersionData_whenVersionExists() {
        Map<String, Object> secretData = Map.of("password", "old-secret");
        when(versioningService.readVersion("secret", "myapp/db", 2))
                .thenReturn(Optional.of(secretData));

        boolean result = rollbackService.rollback("secret", "myapp/db", 2);

        assertThat(result).isTrue();
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(vaultOperations).write(eq("secret/data/myapp/db"), payloadCaptor.capture());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertThat(payload).containsKey("data");
        assertThat(payload.get("data")).isEqualTo(secretData);
    }

    @Test
    void rollback_shouldReturnFalse_whenVersionNotFound() {
        when(versioningService.readVersion("secret", "myapp/db", 5))
                .thenReturn(Optional.empty());

        boolean result = rollbackService.rollback("secret", "myapp/db", 5);

        assertThat(result).isFalse();
        verifyNoInteractions(vaultOperations);
    }

    @Test
    void rollback_shouldReturnFalse_whenVaultWriteFails() {
        Map<String, Object> secretData = Map.of("key", "value");
        when(versioningService.readVersion("secret", "myapp/db", 3))
                .thenReturn(Optional.of(secretData));
        doThrow(new RuntimeException("Vault unavailable"))
                .when(vaultOperations).write(anyString(), any());

        boolean result = rollbackService.rollback("secret", "myapp/db", 3);

        assertThat(result).isFalse();
    }

    @Test
    void rollbackToPrevious_shouldRollBackToCurrentMinusOne() {
        Map<String, Object> secretData = Map.of("token", "abc123");
        when(versioningService.getCurrentVersion("secret", "myapp/token")).thenReturn(4);
        when(versioningService.readVersion("secret", "myapp/token", 3))
                .thenReturn(Optional.of(secretData));

        boolean result = rollbackService.rollbackToPrevious("secret", "myapp/token");

        assertThat(result).isTrue();
        verify(vaultOperations).write(eq("secret/data/myapp/token"), any());
    }

    @Test
    void rollbackToPrevious_shouldReturnFalse_whenAlreadyAtVersionOne() {
        when(versioningService.getCurrentVersion("secret", "myapp/token")).thenReturn(1);

        boolean result = rollbackService.rollbackToPrevious("secret", "myapp/token");

        assertThat(result).isFalse();
        verifyNoInteractions(vaultOperations);
    }
}
