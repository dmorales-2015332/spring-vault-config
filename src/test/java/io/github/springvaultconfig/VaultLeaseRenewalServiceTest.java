package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultLeaseRenewalServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultConfigProperties properties;
    private VaultLeaseRenewalService service;

    @BeforeEach
    void setUp() {
        properties = new VaultConfigProperties();
        properties.setLeaseDurationSeconds(3600);
        properties.setLeaseRenewalThresholdSeconds(300);
        service = new VaultLeaseRenewalService(vaultOperations, properties);
    }

    @Test
    void registerLease_shouldAddLeaseToRegistry() {
        service.registerLease("lease/abc123", 3600);
        assertThat(service.getLeaseRegistry()).containsKey("lease/abc123");
    }

    @Test
    void registerLease_shouldIgnoreBlankLeaseId() {
        service.registerLease("", 3600);
        service.registerLease(null, 3600);
        assertThat(service.getLeaseRegistry()).isEmpty();
    }

    @Test
    void renewExpiringLeases_shouldRenewLeaseNearingExpiry() {
        // Register a lease that expires in 100ms (well within 300s threshold)
        service.registerLease("lease/near-expiry", 0);
        // Manually put a lease that expires in 10 seconds (within threshold)
        Map<String, Long> registry = new HashMap<>();
        long nearExpiry = System.currentTimeMillis() + 10_000L;
        service.registerLease("lease/expiring-soon", 10);

        VaultResponse mockResponse = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("lease_duration", 3600);
        mockResponse.setData(data);
        when(vaultOperations.write(eq("sys/leases/renew"), any())).thenReturn(mockResponse);

        service.renewExpiringLeases();

        verify(vaultOperations, atLeastOnce()).write(eq("sys/leases/renew"), any());
    }

    @Test
    void renewExpiringLeases_shouldRemoveExpiredLeases() {
        // Lease with 0 TTL is immediately expired
        service.registerLease("lease/expired", 0);
        // Force expiry by sleeping briefly
        service.renewExpiringLeases();
        // Expired leases should be cleaned up
        assertThat(service.getLeaseRegistry()).doesNotContainKey("lease/expired");
    }

    @Test
    void revokeLease_shouldCallVaultAndRemoveFromRegistry() {
        service.registerLease("lease/to-revoke", 3600);
        when(vaultOperations.write(eq("sys/leases/revoke"), any())).thenReturn(null);

        service.revokeLease("lease/to-revoke");

        verify(vaultOperations).write(eq("sys/leases/revoke"), any());
        assertThat(service.getLeaseRegistry()).doesNotContainKey("lease/to-revoke");
    }

    @Test
    void revokeLease_shouldHandleVaultException() {
        service.registerLease("lease/error", 3600);
        when(vaultOperations.write(eq("sys/leases/revoke"), any()))
                .thenThrow(new RuntimeException("Vault unavailable"));

        // Should not throw
        service.revokeLease("lease/error");
    }
}
