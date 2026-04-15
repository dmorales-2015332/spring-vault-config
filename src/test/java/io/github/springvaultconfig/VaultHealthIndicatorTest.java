package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.support.VaultHealth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultHealthIndicatorTest {

    @Mock
    private VaultOperations vaultOperations;

    @Mock
    private VaultSysOperations vaultSysOperations;

    @Mock
    private VaultHealth vaultHealth;

    private VaultHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        when(vaultOperations.opsForSys()).thenReturn(vaultSysOperations);
        when(vaultSysOperations.health()).thenReturn(vaultHealth);
        indicator = new VaultHealthIndicator(vaultOperations);
    }

    @Test
    void healthIsUpWhenVaultIsActiveAndUnsealed() {
        when(vaultHealth.isInitialized()).thenReturn(true);
        when(vaultHealth.isSealed()).thenReturn(false);
        when(vaultHealth.isStandby()).thenReturn(false);
        when(vaultHealth.getVersion()).thenReturn("1.15.0");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("sealed", false);
        assertThat(health.getDetails()).containsEntry("version", "1.15.0");
    }

    @Test
    void healthIsDownWhenVaultIsSealed() {
        when(vaultHealth.isInitialized()).thenReturn(true);
        when(vaultHealth.isSealed()).thenReturn(true);
        when(vaultHealth.isStandby()).thenReturn(false);
        when(vaultHealth.getVersion()).thenReturn("1.15.0");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("sealed", true);
    }

    @Test
    void healthIsDownWhenVaultIsStandby() {
        when(vaultHealth.isInitialized()).thenReturn(true);
        when(vaultHealth.isSealed()).thenReturn(false);
        when(vaultHealth.isStandby()).thenReturn(true);
        when(vaultHealth.getVersion()).thenReturn("1.15.0");

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("standby", true);
    }

    @Test
    void healthIsDownWhenExceptionThrown() {
        when(vaultOperations.opsForSys()).thenThrow(new RuntimeException("connection refused"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }
}
