package io.github.springvaultconfig;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Service that records Vault-related metrics using Micrometer.
 * Tracks secret fetch attempts, lease renewals, token renewals, and errors.
 */
@Component
@ConditionalOnClass(MeterRegistry.class)
public class VaultMetricsService {

    private static final Logger log = LoggerFactory.getLogger(VaultMetricsService.class);

    private final Counter secretFetchSuccessCounter;
    private final Counter secretFetchFailureCounter;
    private final Counter leaseRenewalSuccessCounter;
    private final Counter leaseRenewalFailureCounter;
    private final Counter tokenRenewalSuccessCounter;
    private final Counter tokenRenewalFailureCounter;
    private final Timer secretFetchTimer;

    public VaultMetricsService(MeterRegistry meterRegistry) {
        this.secretFetchSuccessCounter = Counter.builder("vault.secret.fetch")
                .tag("result", "success")
                .description("Number of successful secret fetches from Vault")
                .register(meterRegistry);

        this.secretFetchFailureCounter = Counter.builder("vault.secret.fetch")
                .tag("result", "failure")
                .description("Number of failed secret fetches from Vault")
                .register(meterRegistry);

        this.leaseRenewalSuccessCounter = Counter.builder("vault.lease.renewal")
                .tag("result", "success")
                .description("Number of successful lease renewals")
                .register(meterRegistry);

        this.leaseRenewalFailureCounter = Counter.builder("vault.lease.renewal")
                .tag("result", "failure")
                .description("Number of failed lease renewals")
                .register(meterRegistry);

        this.tokenRenewalSuccessCounter = Counter.builder("vault.token.renewal")
                .tag("result", "success")
                .description("Number of successful token renewals")
                .register(meterRegistry);

        this.tokenRenewalFailureCounter = Counter.builder("vault.token.renewal")
                .tag("result", "failure")
                .description("Number of failed token renewals")
                .register(meterRegistry);

        this.secretFetchTimer = Timer.builder("vault.secret.fetch.duration")
                .description("Time taken to fetch secrets from Vault")
                .register(meterRegistry);
    }

    public void recordSecretFetchSuccess() {
        secretFetchSuccessCounter.increment();
        log.debug("Recorded successful secret fetch metric");
    }

    public void recordSecretFetchFailure() {
        secretFetchFailureCounter.increment();
        log.debug("Recorded failed secret fetch metric");
    }

    public void recordSecretFetchDuration(long durationMs) {
        secretFetch TimeUnit.MILLISECONDS);
    }

    public void recordLeaseRenewalSuccess() {
        leaseRenewalSuccessCounter.increment();
    }

    public void recordLeaseRenewalFailure() {
        leaseRenewalFailureCounter.increment();
    }

    public void recordTokenRenewalSuccess() {
        tokenRenewalSuccessCounter.increment();
    }

    public void recordTokenRenewalFailure() {
        tokenRenewalFailureCounter.increment();
    }
}
