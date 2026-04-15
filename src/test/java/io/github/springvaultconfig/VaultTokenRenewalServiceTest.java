package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTokenOperations;
import org.springframework.vault.support.VaultTokenResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultTokenRenewalServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    @Mock
    private VaultTokenOperations tokenOperations;

    private VaultConfigProperties properties;
    private VaultTokenRenewalService service;

    @BeforeEach
    void setUp() {
        properties = new VaultConfigProperties();
        properties.setTokenRenewalThresholdSeconds(300);
        properties.setTokenRenewalIncrementSeconds(3600);
        when(vaultOperations.opsForToken()).thenReturn(tokenOperations);
        service = new VaultTokenRenewalService(vaultOperations, properties);
    }

    @Test
    void shouldRenewTokenWhenTtlBelowThreshold() {
        Map<String, Object> data = new HashMap<>();
        data.put("ttl", 120L);
        data.put("renewable", true);
        VaultTokenResponse response = mock(VaultTokenResponse.class);
        when(response.getData()).thenReturn(data);
        when(tokenOperations.lookupSelf()).thenReturn(response);

        service.renewTokenIfNeeded();

        verify(tokenOperations).renewSelf(any());
        assertThat(service.getLastRenewalTime()).isNotNull();
    }

    @Test
    void shouldNotRenewTokenWhenTtlAboveThreshold() {
        Map<String, Object> data = new HashMap<>();
        data.put("ttl", 3600L);
        data.put("renewable", true);
        VaultTokenResponse response = mock(VaultTokenResponse.class);
        when(response.getData()).thenReturn(data);
        when(tokenOperations.lookupSelf()).thenReturn(response);

        service.renewTokenIfNeeded();

        verify(tokenOperations, never()).renewSelf(any());
    }

    @Test
    void shouldNotRenewNonRenewableToken() {
        Map<String, Object> data = new HashMap<>();
        data.put("ttl", 100L);
        data.put("renewable", false);
        VaultTokenResponse response = mock(VaultTokenResponse.class);
        when(response.getData()).thenReturn(data);
        when(tokenOperations.lookupSelf()).thenReturn(response);

        service.renewTokenIfNeeded();

        verify(tokenOperations, never()).renewSelf(any());
    }

    @Test
    void shouldHandleExceptionGracefully() {
        when(tokenOperations.lookupSelf()).thenThrow(new RuntimeException("Vault unavailable"));

        // Should not throw
        service.renewTokenIfNeeded();

        verify(tokenOperations, never()).renewSelf(any());
    }

    @Test
    void shouldSkipRenewalWhenTokenInfoIsNull() {
        when(tokenOperations.lookupSelf()).thenReturn(null);

        service.renewTokenIfNeeded();

        verify(tokenOperations, never()).renewSelf(any());
    }
}
