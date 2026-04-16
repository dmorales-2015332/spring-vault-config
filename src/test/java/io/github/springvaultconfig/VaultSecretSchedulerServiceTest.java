package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretSchedulerServiceTest {

    @Mock TaskScheduler taskScheduler;
    @Mock VaultSecretRefreshService refreshService;
    @Mock ScheduledFuture<?> scheduledFuture;

    VaultSecretSchedulerService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretSchedulerService(taskScheduler, refreshService);
    }

    @SuppressWarnings("unchecked")
    @Test
    void scheduleRefresh_shouldRegisterTask() {
        when(taskScheduler.schedule(any(Runnable.class), any(Trigger.class)))
                .thenReturn((ScheduledFuture) scheduledFuture);
        when(scheduledFuture.isCancelled()).thenReturn(false);
        when(scheduledFuture.isDone()).thenReturn(false);

        service.scheduleRefresh("secret/app", Duration.ofMinutes(5));

        assertThat(service.isScheduled("secret/app")).isTrue();
        verify(taskScheduler).schedule(any(Runnable.class), any(Trigger.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void cancelSchedule_shouldCancelFuture() {
        when(taskScheduler.schedule(any(Runnable.class), any(Trigger.class)))
                .thenReturn((ScheduledFuture) scheduledFuture);

        service.scheduleRefresh("secret/app", Duration.ofMinutes(5));
        service.cancelSchedule("secret/app");

        verify(scheduledFuture).cancel(false);
        assertThat(service.isScheduled("secret/app")).isFalse();
    }

    @Test
    void isScheduled_returnsFalse_whenNotScheduled() {
        assertThat(service.isScheduled("secret/unknown")).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void cancelAll_shouldCancelAllTasks() {
        when(taskScheduler.schedule(any(Runnable.class), any(Trigger.class)))
                .thenReturn((ScheduledFuture) scheduledFuture);

        service.scheduleRefresh("secret/a", Duration.ofMinutes(1));
        service.scheduleRefresh("secret/b", Duration.ofMinutes(2));
        service.cancelAll();

        verify(scheduledFuture, atLeast(2)).cancel(false);
    }
}
