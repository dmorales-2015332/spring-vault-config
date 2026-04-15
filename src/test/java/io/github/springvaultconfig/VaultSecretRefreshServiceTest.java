package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretRefreshServiceTest {

    @Mock
    private VaultSecretLoader secretLoader;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretRefreshService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretRefreshService(secretLoader, eventPublisher, properties);
    }

    @Test
    void refreshSecrets_firstLoad_doesNotPublishEvent() throws Exception {
        when(properties.getPaths()).thenReturn(Set.of("secret/app"));
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("key", "value"));

        service.refreshSecrets();

        verify(eventPublisher, never()).publishEvent(any());
        assertThat(service.getCachedSecrets("secret/app")).containsEntry("key", "value");
    }

    @Test
    void refreshSecrets_secretChanged_publishesEvent() throws Exception {
        when(properties.getPaths()).thenReturn(Set.of("secret/app"));
        when(secretLoader.loadSecrets("secret/app"))
                .thenReturn(Map.of("key", "value1"))
                .thenReturn(Map.of("key", "value2"));

        service.refreshSecrets();
        service.refreshSecrets();

        ArgumentCaptor<VaultSecretRotatedEvent> captor = ArgumentCaptor.forClass(VaultSecretRotatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().getPath()).isEqualTo("secret/app");
        assertThat(captor.getValue().getNewSecrets()).containsEntry("key", "value2");
    }

    @Test
    void refreshSecrets_secretUnchanged_doesNotPublishEvent() throws Exception {
        when(properties.getPaths()).thenReturn(Set.of("secret/app"));
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("key", "value"));

        service.refreshSecrets();
        service.refreshSecrets();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void refreshSecrets_loaderThrows_doesNotPropagateException() throws Exception {
        when(properties.getPaths()).thenReturn(Set.of("secret/app"));
        when(secretLoader.loadSecrets("secret/app")).thenThrow(new VaultSecretLoadException("error"));

        service.refreshSecrets(); // should not throw

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void refreshSecrets_noPaths_doesNothing() throws Exception {
        when(properties.getPaths()).thenReturn(Set.of());

        service.refreshSecrets();

        verifyNoInteractions(secretLoader, eventPublisher);
    }

    @Test
    void getCachedSecrets_unknownPath_returnsEmptyMap() {
        assertThat(service.getCachedSecrets("unknown/path")).isEmpty();
    }
}
