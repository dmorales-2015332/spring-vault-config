package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretAlertServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VaultSecretAlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new VaultSecretAlertService(eventPublisher, 3);
    }

    @Test
    void recordAccess_belowThreshold_noAlert() {
        alertService.recordAccess("secret/db");
        alertService.recordAccess("secret/db");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void recordAccess_atThreshold_publishesAlert() {
        alertService.recordAccess("secret/db");
        alertService.recordAccess("secret/db");
        alertService.recordAccess("secret/db");

        ArgumentCaptor<VaultSecretAlertEvent> captor = ArgumentCaptor.forClass(VaultSecretAlertEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        VaultSecretAlertEvent event = captor.getValue();
        assertThat(event.getSecretPath()).isEqualTo("secret/db");
        assertThat(event.getAccessCount()).isEqualTo(3);
        assertThat(event.getThreshold()).isEqualTo(3);
    }

    @Test
    void recordAccess_aboveThreshold_publishesMultipleAlerts() {
        for (int i = 0; i < 5; i++) {
            alertService.recordAccess("secret/api");
        }
        verify(eventPublisher, times(3)).publishEvent(any(VaultSecretAlertEvent.class));
    }

    @Test
    void getAccessCount_returnsCorrectCount() {
        alertService.recordAccess("secret/x");
        alertService.recordAccess("secret/x");
        assertThat(alertService.getAccessCount("secret/x")).isEqualTo(2);
    }

    @Test
    void getAccessCount_unknownPath_returnsZero() {
        assertThat(alertService.getAccessCount("secret/unknown")).isZero();
    }

    @Test
    void resetCounts_clearsAllCounts() {
        alertService.recordAccess("secret/y");
        alertService.resetCounts();
        assertThat(alertService.getAccessCount("secret/y")).isZero();
    }
}
