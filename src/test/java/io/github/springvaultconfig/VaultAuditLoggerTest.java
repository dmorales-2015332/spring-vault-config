package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaultAuditLoggerTest {

    private VaultAuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new VaultAuditLogger();
    }

    @Test
    void recordSecretLoad_incrementsCount() {
        auditLogger.recordSecretLoad("secret/myapp/db");
        auditLogger.recordSecretLoad("secret/myapp/api");

        assertThat(auditLogger.getSecretLoadCount()).isEqualTo(2);
    }

    @Test
    void recordSecretAccessFailure_doesNotIncrementLoadCount() {
        auditLogger.recordSecretAccessFailure("secret/myapp/db", "permission denied");

        assertThat(auditLogger.getSecretLoadCount()).isZero();
        assertThat(auditLogger.getRotationCount()).isZero();
    }

    @Test
    void onSecretRotated_incrementsRotationCountAndSetsTime() {
        VaultSecretRotatedEvent event = mock(VaultSecretRotatedEvent.class);
        when(event.getSecretPath()).thenReturn("secret/myapp/db");

        Instant before = Instant.now();
        auditLogger.onSecretRotated(event);
        Instant after = Instant.now();

        assertThat(auditLogger.getRotationCount()).isEqualTo(1);
        assertThat(auditLogger.getLastRotationTime())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    void multipleRotations_accumulateCount() {
        VaultSecretRotatedEvent event = mock(VaultSecretRotatedEvent.class);
        when(event.getSecretPath()).thenReturn("secret/myapp/db");

        auditLogger.onSecretRotated(event);
        auditLogger.onSecretRotated(event);
        auditLogger.onSecretRotated(event);

        assertThat(auditLogger.getRotationCount()).isEqualTo(3);
    }

    @Test
    void lastRotationTime_isNullBeforeAnyRotation() {
        assertThat(auditLogger.getLastRotationTime()).isNull();
    }

    @Test
    void secretLoadCount_startsAtZero() {
        assertThat(auditLogger.getSecretLoadCount()).isZero();
    }
}
