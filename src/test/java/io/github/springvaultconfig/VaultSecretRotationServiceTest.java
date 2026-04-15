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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretRotationServiceTest {

    @Mock
    private VaultSecretLoader secretLoader;

    @Mock
    private VaultConfigProperties properties;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VaultSecretRotationService rotationService;

    @BeforeEach
    void setUp() {
        when(properties.getSecretPath()).thenReturn("secret/myapp");
        rotationService = new VaultSecretRotationService(secretLoader, properties, eventPublisher);
    }

    @Test
    void captureSnapshot_storesSecrets() {
        Map<String, String> secrets = Map.of("db.password", "initial");
        rotationService.captureSnapshot(secrets);
        assertThat(rotationService.getSecretSnapshot()).containsEntry("db.password", "initial");
    }

    @Test
    void checkForRotation_publishesEventWhenValueChanges() throws Exception {
        rotationService.captureSnapshot(Map.of("db.password", "old-secret"));
        when(secretLoader.loadSecrets("secret/myapp"))
                .thenReturn(Map.of("db.password", "new-secret"));

        rotationService.checkForRotation();

        ArgumentCaptor<VaultSecretRotatedEvent> captor =
                ArgumentCaptor.forClass(VaultSecretRotatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        VaultSecretRotatedEvent event = captor.getValue();
        assertThat(event.getSecretKey()).isEqualTo("db.password");
        assertThat(event.getVaultPath()).isEqualTo("secret/myapp");
        assertThat(event.getDetectedAt()).isNotNull();
    }

    @Test
    void checkForRotation_doesNotPublishEventWhenValueUnchanged() throws Exception {
        rotationService.captureSnapshot(Map.of("db.password", "same-secret"));
        when(secretLoader.loadSecrets("secret/myapp"))
                .thenReturn(Map.of("db.password", "same-secret"));

        rotationService.checkForRotation();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkForRotation_handlesLoaderExceptionGracefully() throws Exception {
        rotationService.captureSnapshot(Map.of("db.password", "secret"));
        when(secretLoader.loadSecrets("secret/myapp"))
                .thenThrow(new VaultSecretLoadException("Vault unavailable"));

        // Should not throw
        rotationService.checkForRotation();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkForRotation_updatesSnapshotAfterCheck() throws Exception {
        rotationService.captureSnapshot(Map.of("api.key", "v1"));
        when(secretLoader.loadSecrets("secret/myapp"))
                .thenReturn(Map.of("api.key", "v2"));

        rotationService.checkForRotation();

        assertThat(rotationService.getSecretSnapshot()).containsEntry("api.key", "v2");
    }
}
