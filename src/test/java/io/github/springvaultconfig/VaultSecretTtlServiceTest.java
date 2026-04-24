package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VaultSecretTtlServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretTtlService ttlService;

    @BeforeEach
    void setUp() {
        ttlService = new VaultSecretTtlService(vaultOperations);
    }

    @Test
    void registerTtl_shouldStoreExpiryForPath() {
        ttlService.registerTtl("secret/db", Duration.ofMinutes(10));

        Map<String, Instant> registrations = ttlService.getAllRegistrations();
        assertThat(registrations).containsKey("secret/db");
        assertThat(registrations.get("secret/db")).isAfter(Instant.now());
    }

    @Test
    void getRemainingTtl_shouldReturnNonEmptyForActivePath() {
        ttlService.registerTtl("secret/app", Duration.ofHours(1));

        Optional<Duration> remaining = ttlService.getRemainingTtl("secret/app");

        assertThat(remaining).isPresent();
        assertThat(remaining.get()).isPositive();
        assertThat(remaining.get()).isLessThanOrEqualTo(Duration.ofHours(1));
    }

    @Test
    void getRemainingTtl_shouldReturnEmptyForUnregisteredPath() {
        Optional<Duration> remaining = ttlService.getRemainingTtl("secret/unknown");

        assertThat(remaining).isEmpty();
    }

    @Test
    void getRemainingTtl_shouldReturnEmptyForExpiredPath() {
        ttlService.registerTtl("secret/expired", Duration.ofMillis(1));

        // Wait for expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        Optional<Duration> remaining = ttlService.getRemainingTtl("secret/expired");
        assertThat(remaining).isEmpty();
    }

    @Test
    void isExpired_shouldReturnFalseForActivePath() {
        ttlService.registerTtl("secret/active", Duration.ofMinutes(5));

        assertThat(ttlService.isExpired("secret/active")).isFalse();
    }

    @Test
    void isExpired_shouldReturnTrueForUnregisteredPath() {
        assertThat(ttlService.isExpired("secret/missing")).isTrue();
    }

    @Test
    void deregister_shouldRemovePathFromRegistry() {
        ttlService.registerTtl("secret/temp", Duration.ofMinutes(1));
        ttlService.deregister("secret/temp");

        assertThat(ttlService.getAllRegistrations()).doesNotContainKey("secret/temp");
        assertThat(ttlService.isExpired("secret/temp")).isTrue();
    }

    @Test
    void getAllRegistrations_shouldReturnImmutableSnapshot() {
        ttlService.registerTtl("secret/a", Duration.ofMinutes(1));
        ttlService.registerTtl("secret/b", Duration.ofMinutes(2));

        Map<String, Instant> snapshot = ttlService.getAllRegistrations();
        assertThat(snapshot).hasSize(2);
    }
}
