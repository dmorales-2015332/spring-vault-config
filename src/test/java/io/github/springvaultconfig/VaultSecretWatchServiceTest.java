package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretWatchServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VaultSecretWatchService watchService;

    @BeforeEach
    void setUp() {
        watchService = new VaultSecretWatchService(vaultOperations, eventPublisher, 60);
    }

    @Test
    void watchRegistersPathAndSeeds() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value1"));
        when(vaultOperations.read("secret/app")).thenReturn(response);

        watchService.watch("secret/app");

        assertThat(watchService.isWatching("secret/app")).isTrue();
        verify(vaultOperations, times(1)).read("secret/app");
    }

    @Test
    void watchSamePathTwiceIsIdempotent() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value1"));
        when(vaultOperations.read("secret/app")).thenReturn(response);

        watchService.watch("secret/app");
        watchService.watch("secret/app");

        // seed should only be called once
        verify(vaultOperations, times(1)).read("secret/app");
    }

    @Test
    void unwatchStopsWatching() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value1"));
        when(vaultOperations.read("secret/app")).thenReturn(response);

        watchService.watch("secret/app");
        watchService.unwatch("secret/app");

        assertThat(watchService.isWatching("secret/app")).isFalse();
    }

    @Test
    void unwatchUnknownPathDoesNotThrow() {
        watchService.unwatch("secret/nonexistent");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void seedHandlesNullVaultResponse() {
        when(vaultOperations.read("secret/app")).thenReturn(null);

        watchService.watch("secret/app");

        assertThat(watchService.isWatching("secret/app")).isTrue();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void seedHandlesVaultException() {
        when(vaultOperations.read(anyString())).thenThrow(new RuntimeException("Vault unavailable"));

        watchService.watch("secret/app");

        assertThat(watchService.isWatching("secret/app")).isTrue();
        verifyNoInteractions(eventPublisher);
    }
}
