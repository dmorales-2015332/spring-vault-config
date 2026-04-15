package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.vault.core.VaultOperations;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretExpiryNotificationServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretExpiryNotificationService service;

    @BeforeEach
    void setUp() {
        when(properties.getExpiryWarningThreshold()).thenReturn(Duration.ofMinutes(10));
        service = new VaultSecretExpiryNotificationService(vaultOperations, eventPublisher, properties);
    }

    @Test
    void registerSecretExpiry_shouldAddToMonitoredSecrets() {
        Instant expiry = Instant.now().plusSeconds(3600);
        service.registerSecretExpiry("secret/myapp/db", expiry);

        assertThat(service.getMonitoredSecrets())
                .containsEntry("secret/myapp/db", expiry);
    }

    @Test
    void deregisterSecret_shouldRemoveFromMonitoredSecrets() {
        Instant expiry = Instant.now().plusSeconds(3600);
        service.registerSecretExpiry("secret/myapp/db", expiry);
        service.deregisterSecret("secret/myapp/db");

        assertThat(service.getMonitoredSecrets()).doesNotContainKey("secret/myapp/db");
    }

    @Test
    void checkSecretExpiry_shouldPublishEvent_whenSecretWithinWarningThreshold() {
        Instant expiry = Instant.now().plusSeconds(300); // 5 minutes — within 10-minute threshold
        service.registerSecretExpiry("secret/myapp/api", expiry);

        service.checkSecretExpiry();

        ArgumentCaptor<VaultSecretExpiryEvent> captor = ArgumentCaptor.forClass(VaultSecretExpiryEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        VaultSecretExpiryEvent event = captor.getValue();
        assertThat(event.getSecretPath()).isEqualTo("secret/myapp/api");
        assertThat(event.getExpiry()).isEqualTo(expiry);
    }

    @Test
    void checkSecretExpiry_shouldNotPublishEvent_whenSecretFarFromExpiry() {
        Instant expiry = Instant.now().plusSeconds(7200); // 2 hours — outside threshold
        service.registerSecretExpiry("secret/myapp/safe", expiry);

        service.checkSecretExpiry();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkSecretExpiry_shouldPublishEvent_whenSecretAlreadyExpired() {
        Instant expiry = Instant.now().minusSeconds(60);
        service.registerSecretExpiry("secret/myapp/expired", expiry);

        service.checkSecretExpiry();

        verify(eventPublisher, times(1)).publishEvent(any(VaultSecretExpiryEvent.class));
    }

    @Test
    void getMonitoredSecrets_shouldReturnUnmodifiableView() {
        service.registerSecretExpiry("secret/test", Instant.now().plusSeconds(100));
        assertThat(service.getMonitoredSecrets()).hasSize(1);
    }
}
