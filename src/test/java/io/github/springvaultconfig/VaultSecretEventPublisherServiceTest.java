package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretEventPublisherServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VaultSecretEventPublisherService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretEventPublisherService(eventPublisher);
    }

    @Test
    void publishSecretsLoaded_shouldPublishLoadedEvent() {
        Map<String, String> secrets = Map.of("db.password", "s3cr3t");
        service.publishSecretsLoaded("secret/myapp", secrets);

        ArgumentCaptor<VaultSecretEvent> captor = ArgumentCaptor.forClass(VaultSecretEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        VaultSecretEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo(VaultSecretEvent.Type.LOADED);
        assertThat(event.getPath()).isEqualTo("secret/myapp");
        assertThat(event.getSecrets()).containsKey("db.password");
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void publishSecretsRotated_shouldPublishRotatedEvent() {
        Map<String, String> secrets = Map.of("api.key", "newkey");
        service.publishSecretsRotated("secret/api", secrets);

        ArgumentCaptor<VaultSecretEvent> captor = ArgumentCaptor.forClass(VaultSecretEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(VaultSecretEvent.Type.ROTATED);
    }

    @Test
    void publishSecretsExpired_shouldPublishExpiredEventWithEmptySecrets() {
        service.publishSecretsExpired("secret/old");

        ArgumentCaptor<VaultSecretEvent> captor = ArgumentCaptor.forClass(VaultSecretEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        VaultSecretEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo(VaultSecretEvent.Type.EXPIRED);
        assertThat(event.getSecrets()).isEmpty();
    }

    @Test
    void publishSecretsLoaded_shouldIncrementEventCount() {
        service.publishSecretsLoaded("secret/a", Map.of());
        service.publishSecretsLoaded("secret/b", Map.of());
        assertThat(service.getPublishedEventCount()).isEqualTo(2L);
    }

    @Test
    void publishSecretsLoaded_shouldThrowForBlankPath() {
        assertThatThrownBy(() -> service.publishSecretsLoaded(" ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void publishSecretsExpired_shouldThrowForNullPath() {
        assertThatThrownBy(() -> service.publishSecretsExpired(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void vaultSecretEvent_toStringShouldContainTypeAndPath() {
        service.publishSecretsRotated("secret/svc", Map.of("token", "abc"));
        ArgumentCaptor<VaultSecretEvent> captor = ArgumentCaptor.forClass(VaultSecretEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        String str = captor.getValue().toString();
        assertThat(str).contains("ROTATED").contains("secret/svc");
    }
}
