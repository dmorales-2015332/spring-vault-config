package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretRateLimiterServiceTest {

    private VaultSecretRateLimiterService rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new VaultSecretRateLimiterService(3, 60);
    }

    @Test
    void allowsRequestsWithinLimit() {
        assertThat(rateLimiter.isAllowed("secret/app")).isTrue();
        assertThat(rateLimiter.isAllowed("secret/app")).isTrue();
        assertThat(rateLimiter.isAllowed("secret/app")).isTrue();
    }

    @Test
    void blocksRequestsExceedingLimit() {
        rateLimiter.isAllowed("secret/db");
        rateLimiter.isAllowed("secret/db");
        rateLimiter.isAllowed("secret/db");
        assertThat(rateLimiter.isAllowed("secret/db")).isFalse();
    }

    @Test
    void tracksCountsPerPath() {
        rateLimiter.isAllowed("secret/a");
        rateLimiter.isAllowed("secret/b");
        rateLimiter.isAllowed("secret/b");
        assertThat(rateLimiter.getCurrentCount("secret/a")).isEqualTo(1);
        assertThat(rateLimiter.getCurrentCount("secret/b")).isEqualTo(2);
    }

    @Test
    void resetClearsCountForPath() {
        rateLimiter.isAllowed("secret/x");
        rateLimiter.isAllowed("secret/x");
        rateLimiter.reset("secret/x");
        assertThat(rateLimiter.getCurrentCount("secret/x")).isEqualTo(0);
        assertThat(rateLimiter.isAllowed("secret/x")).isTrue();
    }

    @Test
    void returnsZeroCountForUnknownPath() {
        assertThat(rateLimiter.getCurrentCount("secret/unknown")).isEqualTo(0);
    }

    @Test
    void independentLimitsPerPath() {
        rateLimiter.isAllowed("secret/p1");
        rateLimiter.isAllowed("secret/p1");
        rateLimiter.isAllowed("secret/p1");
        boolean p1Blocked = rateLimiter.isAllowed("secret/p1");
        boolean p2Allowed = rateLimiter.isAllowed("secret/p2");
        assertThat(p1Blocked).isFalse();
        assertThat(p2Allowed).isTrue();
    }
}
