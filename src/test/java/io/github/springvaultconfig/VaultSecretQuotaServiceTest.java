package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VaultSecretQuotaServiceTest {

    private VaultOperations vaultOperations;
    private VaultSecretQuotaService quotaService;

    @BeforeEach
    void setUp() {
        vaultOperations = mock(VaultOperations.class);
        quotaService = new VaultSecretQuotaService(vaultOperations, 3);
    }

    @Test
    void readSecret_withinQuota_returnsData() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("key", "value"));
        when(vaultOperations.read("secret/app")).thenReturn(response);

        Map<String, Object> result = quotaService.readSecret("secret/app");

        assertThat(result).containsEntry("key", "value");
        assertThat(quotaService.getReadCount("secret/app")).isEqualTo(1);
    }

    @Test
    void readSecret_exceedsQuota_throwsException() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("k", "v"));
        when(vaultOperations.read("secret/app")).thenReturn(response);

        quotaService.readSecret("secret/app");
        quotaService.readSecret("secret/app");
        quotaService.readSecret("secret/app");

        assertThatThrownBy(() -> quotaService.readSecret("secret/app"))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("quota exceeded");
    }

    @Test
    void readSecret_nullResponse_returnsEmptyMap() {
        when(vaultOperations.read("secret/empty")).thenReturn(null);

        Map<String, Object> result = quotaService.readSecret("secret/empty");

        assertThat(result).isEmpty();
    }

    @Test
    void resetQuota_clearsCountForPath() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("k", "v"));
        when(vaultOperations.read("secret/app")).thenReturn(response);

        quotaService.readSecret("secret/app");
        quotaService.resetQuota("secret/app");

        assertThat(quotaService.getReadCount("secret/app")).isEqualTo(0);
    }

    @Test
    void resetAllQuotas_clearsAllCounts() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("k", "v"));
        when(vaultOperations.read(anyString())).thenReturn(response);

        quotaService.readSecret("secret/a");
        quotaService.readSecret("secret/b");
        quotaService.resetAllQuotas();

        assertThat(quotaService.getAllReadCounts()).isEmpty();
    }
}
