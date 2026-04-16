package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class VaultSecretAccessLogServiceTest {

    private VaultSecretAccessLogService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretAccessLogService(3);
    }

    @Test
    void recordAccess_storesEntry() {
        service.recordAccess("secret/db", "app-service");
        List<VaultSecretAccessLogService.AccessEntry> log = service.getAccessLog("secret/db");
        assertThat(log).hasSize(1);
        assertThat(log.get(0).secretPath()).isEqualTo("secret/db");
        assertThat(log.get(0).accessor()).isEqualTo("app-service");
        assertThat(log.get(0).timestamp()).isNotNull();
    }

    @Test
    void recordAccess_evictsOldestWhenMaxReached() {
        service.recordAccess("secret/db", "a");
        service.recordAccess("secret/db", "b");
        service.recordAccess("secret/db", "c");
        service.recordAccess("secret/db", "d");
        List<VaultSecretAccessLogService.AccessEntry> log = service.getAccessLog("secret/db");
        assertThat(log).hasSize(3);
        assertThat(log.get(0).accessor()).isEqualTo("b");
    }

    @Test
    void recordAccess_throwsOnBlankPath() {
        assertThatThrownBy(() -> service.recordAccess("", "user"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getAccessLog_returnsEmptyForUnknownPath() {
        assertThat(service.getAccessLog("secret/unknown")).isEmpty();
    }

    @Test
    void clearLog_removesEntries() {
        service.recordAccess("secret/db", "app");
        service.clearLog("secret/db");
        assertThat(service.getAccessLog("secret/db")).isEmpty();
    }

    @Test
    void recordAccess_isolatesEntriesByPath() {
        service.recordAccess("secret/db", "app");
        service.recordAccess("secret/cache", "worker");
        assertThat(service.getAccessLog("secret/db")).hasSize(1);
        assertThat(service.getAccessLog("secret/cache")).hasSize(1);
    }
}
