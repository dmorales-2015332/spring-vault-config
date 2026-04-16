package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class VaultSecretSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final VaultSecretRefreshService refreshService;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public VaultSecretSchedulerService(TaskScheduler taskScheduler, VaultSecretRefreshService refreshService) {
        this.taskScheduler = taskScheduler;
        this.refreshService = refreshService;
    }

    public void scheduleRefresh(String path, Duration interval) {
        cancelSchedule(path);
        PeriodicTrigger trigger = new PeriodicTrigger(interval);
        trigger.setInitialDelay(interval);
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            log.info("Scheduled refresh triggered for vault path: {}", path);
            try {
                refreshService.refresh(path);
            } catch (Exception e) {
                log.error("Scheduled refresh failed for path: {}", path, e);
            }
        }, trigger);
        scheduledTasks.put(path, future);
        log.info("Scheduled secret refresh for path '{}' every {}", path, interval);
    }

    public void cancelSchedule(String path) {
        ScheduledFuture<?> existing = scheduledTasks.remove(path);
        if (existing != null) {
            existing.cancel(false);
            log.info("Cancelled scheduled refresh for path: {}", path);
        }
    }

    public boolean isScheduled(String path) {
        ScheduledFuture<?> future = scheduledTasks.get(path);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    public void cancelAll() {
        scheduledTasks.keySet().forEach(this::cancelSchedule);
    }
}
