package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretNotificationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VaultSecretNotificationService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretNotificationService(eventPublisher);
    }

    @Test
    void subscribe_shouldRegisterListener() {
        service.subscribe("secret/db", event -> {});
        assertThat(service.listenerCount("secret/db")).isEqualTo(1);
    }

    @Test
    void subscribe_withBlankPath_shouldThrow() {
        assertThatThrownBy(() -> service.subscribe(" ", event -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Secret path must not be null or blank");
    }

    @Test
    void notify_shouldInvokeListenersAndPublishEvent() {
        List<VaultSecretNotificationEvent> received = new ArrayList<>();
        service.subscribe("secret/db", received::add);

        Map<String, String> oldVals = Map.of("password", "old");
        Map<String, String> newVals = Map.of("password", "new");
        service.notify("secret/db", oldVals, newVals);

        assertThat(received).hasSize(1);
        VaultSecretNotificationEvent event = received.get(0);
        assertThat(event.getPath()).isEqualTo("secret/db");
        assertThat(event.getOldValues()).containsEntry("password", "old");
        assertThat(event.getNewValues()).containsEntry("password", "new");
        assertThat(event.getOccurredAt()).isNotNull();

        ArgumentCaptor<VaultSecretNotificationEvent> captor =
                ArgumentCaptor.forClass(VaultSecretNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getPath()).isEqualTo("secret/db");
    }

    @Test
    void notify_withNoListeners_shouldOnlyPublishEvent() {
        service.notify("secret/other", null, Map.of("key", "value"));
        verify(eventPublisher, times(1)).publishEvent(any(VaultSecretNotificationEvent.class));
    }

    @Test
    void notify_whenListenerThrows_shouldContinueAndNotPropagate() {
        List<VaultSecretNotificationEvent> received = new ArrayList<>();
        service.subscribe("secret/db", event -> { throw new RuntimeException("boom"); });
        service.subscribe("secret/db", received::add);

        service.notify("secret/db", null, Map.of("k", "v"));

        assertThat(received).hasSize(1);
    }

    @Test
    void unsubscribeAll_shouldRemoveAllListeners() {
        service.subscribe("secret/db", event -> {});
        service.subscribe("secret/db", event -> {});
        service.unsubscribeAll("secret/db");
        assertThat(service.listenerCount("secret/db")).isZero();
    }

    @Test
    void listenerCount_forUnknownPath_shouldReturnZero() {
        assertThat(service.listenerCount("secret/unknown")).isZero();
    }
}
