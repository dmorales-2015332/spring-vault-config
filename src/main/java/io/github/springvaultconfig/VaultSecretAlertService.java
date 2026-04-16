package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that monitors secret access patterns and publishes alerts
 * when anomalies or threshold breaches are detected.
 */
public class VaultSecretAlertService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretAlertService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final int accessThreshold;
    private final Map<String, Integer> accessCounts = new ConcurrentHashMap<>();

    public VaultSecretAlertService(ApplicationEventPublisher eventPublisher, int accessThreshold) {
        this.eventPublisher = eventPublisher;
        this.accessThreshold = accessThreshold;
    }

    public void recordAccess(String secretPath) {
        int count = accessCounts.merge(secretPath, 1, Integer::sum);
        log.debug("Secret '{}' accessed {} time(s)", secretPath, count);
        if (count >= accessThreshold) {
            publishAlert(secretPath, count);
        }
    }

    public int getAccessCount(String secretPath) {
        return accessCounts.getOrDefault(secretPath, 0);
    }

    public void resetCounts() {
        accessCounts.clear();
        log.info("Secret access counts reset");
    }

    private void publishAlert(String secretPath, int count) {
        log.warn("Alert: secret '{}' accessed {} times, threshold is {}", secretPath, count, accessThreshold);
        eventPublisher.publishEvent(new VaultSecretAlertEvent(this, secretPath, count, accessThreshold));
    }
}
