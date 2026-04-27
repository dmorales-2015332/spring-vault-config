package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretCleanupServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretCleanupService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretCleanupService(vaultOperations);
    }

    @Test
    void trackShouldRegisterPath() {
        service.track("secret/myapp/db", 60_000);
        assertThat(service.isTracked("secret/myapp/db")).isTrue();
        assertThat(service.trackedCount()).isEqualTo(1);
    }

    @Test
    void trackShouldRejectBlankPath() {
        assertThatThrownBy(() -> service.track("  ", 60_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void untrackShouldRemovePath() {
        service.track("secret/myapp/db", 60_000);
        service.untrack("secret/myapp/db");
        assertThat(service.isTracked("secret/myapp/db")).isFalse();
        assertThat(service.trackedCount()).isEqualTo(0);
    }

    @Test
    void cleanupExpiredShouldDeleteExpiredPaths() throws InterruptedException {
        service.track("secret/expired", 1);
        service.track("secret/active", 60_000);

        Thread.sleep(10);

        List<String> cleaned = service.cleanupExpired();

        assertThat(cleaned).containsExactly("secret/expired");
        assertThat(service.isTracked("secret/expired")).isFalse();
        assertThat(service.isTracked("secret/active")).isTrue();
        verify(vaultOperations, times(1)).delete("secret/expired");
        verify(vaultOperations, never()).delete("secret/active");
    }

    @Test
    void cleanupExpiredShouldHandleDeleteFailureGracefully() throws InterruptedException {
        service.track("secret/broken", 1);
        doThrow(new RuntimeException("Vault unavailable")).when(vaultOperations).delete("secret/broken");

        Thread.sleep(10);

        List<String> cleaned = service.cleanupExpired();

        assertThat(cleaned).isEmpty();
        assertThat(service.isTracked("secret/broken")).isFalse();
    }

    @Test
    void cleanupExpiredShouldReturnEmptyWhenNothingExpired() {
        service.track("secret/future", 60_000);

        List<String> cleaned = service.cleanupExpired();

        assertThat(cleaned).isEmpty();
        verifyNoInteractions(vaultOperations);
    }
}
