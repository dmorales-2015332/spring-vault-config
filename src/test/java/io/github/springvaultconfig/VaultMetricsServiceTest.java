package io.github.springvaultconfig;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class VaultMetricsServiceTest {

    private MeterRegistry meterRegistry;
    private VaultMetricsService vaultMetricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        vaultMetricsService = new VaultMetricsService(meterRegistry);
    }

    @Test
    void recordSecretFetchSuccess_incrementsSuccessCounter() {
        vaultMetricsService.recordSecretFetchSuccess();
        vaultMetricsService.recordSecretFetchSuccess();

        Counter counter = meterRegistry.find("vault.secret.fetch")
                .tag("result", "success")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void recordSecretFetchFailure_incrementsFailureCounter() {
        vaultMetricsService.recordSecretFetchFailure();

        Counter counter = meterRegistry.find("vault.secret.fetch")
                .tag("result", "failure")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordSecretFetchDuration_recordsTimerSample() {
        vaultMetricsService.recordSecretFetchDuration(150);

        Timer timer = meterRegistry.find("vault.secret.fetch.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(150.0);
    }
void recordLeaseRenewalSuccess_incrementsCounter() {
        vaultMetricsService.recordLeaseRenewalSuccess();

        Counter counter = meterRegistry.find("vault.lease.renewal")
                .tag("result", "success")
That(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordLeaseRenewalFailure_incrementsCounter() {
        vaultMetricsService.recordLeaseRenewalFailure();

        Counter counter = meterRegistry.find("vault.lease.renewal")
                .tag("result", "failure")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordTokenRenewalSuccess_incrementsCounter() {
        vaultMetricsService.recordTokenRenewalSuccess();

        Counter counter = meterRegistry.find("vault.token.renewal")
                .tag("result", "success")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordTokenRenewalFailure_incrementsCounter() {
        vaultMetricsService.recordTokenRenewalFailure();

        Counter counter = meterRegistry.find("vault.token.renewal")
                .tag("result", "failure")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
