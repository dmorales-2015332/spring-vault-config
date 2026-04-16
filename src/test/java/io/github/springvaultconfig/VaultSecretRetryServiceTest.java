package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VaultSecretRetryServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretRetryService retryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        retryService = new VaultSecretRetryService(vaultTemplate, properties);
    }

    @Test
    void readWithRetry_successOnFirstAttempt() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        Map<String, Object> result = retryService.readWithRetry("secret/app", 3, Duration.ofMillis(1));

        assertThat(result).containsEntry("key", "value");
        verify(vaultTemplate, times(1)).read("secret/app");
    }

    @Test
    void readWithRetry_successOnSecondAttempt() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("token", "abc123"));
        when(vaultTemplate.read("secret/db"))
                .thenThrow(new RuntimeException("transient error"))
                .thenReturn(response);

        Map<String, Object> result = retryService.readWithRetry("secret/db", 3, Duration.ofMillis(1));

        assertThat(result).containsEntry("token", "abc123");
        verify(vaultTemplate, times(2)).read("secret/db");
    }

    @Test
    void readWithRetry_exhaustsAllAttempts_throwsException() {
        when(vaultTemplate.read("secret/missing")).thenThrow(new RuntimeException("vault error"));

        assertThatThrownBy(() -> retryService.readWithRetry("secret/missing", 3, Duration.ofMillis(1)))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("after 3 attempts");

        verify(vaultTemplate, times(3)).read("secret/missing");
    }

    @Test
    void readWithRetry_nullResponse_throwsException() {
        when(vaultTemplate.read("secret/null")).thenReturn(null);

        assertThatThrownBy(() -> retryService.readWithRetry("secret/null", 2, Duration.ofMillis(1)))
                .isInstanceOf(VaultSecretLoadException.class);
    }

    @Test
    void readWithRetry_invalidMaxAttempts_throwsIllegalArgument() {
        assertThatThrownBy(() -> retryService.readWithRetry("secret/app", 0, Duration.ofMillis(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
