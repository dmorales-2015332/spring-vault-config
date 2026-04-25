package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.vault.core.VaultTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretLineageServiceTest {

    private VaultSecretLineageService lineageService;

    @BeforeEach
    void setUp() {
        VaultTemplate vaultTemplate = Mockito.mock(VaultTemplate.class);
        lineageService = new VaultSecretLineageService(vaultTemplate);
    }

    @Test
    void recordAndRetrieveLineage() {
        lineageService.record("db.password", "secret/myapp", 1, "startup");

        List<VaultSecretLineageService.LineageEntry> lineage = lineageService.getLineage("db.password");

        assertThat(lineage).hasSize(1);
        VaultSecretLineageService.LineageEntry entry = lineage.get(0);
        assertThat(entry.secretKey()).isEqualTo("db.password");
        assertThat(entry.vaultPath()).isEqualTo("secret/myapp");
        assertThat(entry.version()).isEqualTo(1);
        assertThat(entry.source()).isEqualTo("startup");
        assertThat(entry.recordedAt()).isNotNull();
    }

    @Test
    void multipleRecordsAreOrderedChronologically() {
        lineageService.record("api.key", "secret/myapp", 1, "startup");
        lineageService.record("api.key", "secret/myapp", 2, "rotation");

        List<VaultSecretLineageService.LineageEntry> lineage = lineageService.getLineage("api.key");

        assertThat(lineage).hasSize(2);
        assertThat(lineage.get(0).source()).isEqualTo("startup");
        assertThat(lineage.get(1).source()).isEqualTo("rotation");
    }

    @Test
    void getLatestReturnsLastEntry() {
        lineageService.record("token", "secret/tokens", 1, "startup");
        lineageService.record("token", "secret/tokens", 2, "refresh");

        Optional<VaultSecretLineageService.LineageEntry> latest = lineageService.getLatest("token");

        assertThat(latest).isPresent();
        assertThat(latest.get().version()).isEqualTo(2);
        assertThat(latest.get().source()).isEqualTo("refresh");
    }

    @Test
    void getLatestReturnsEmptyForUnknownKey() {
        Optional<VaultSecretLineageService.LineageEntry> latest = lineageService.getLatest("nonexistent");
        assertThat(latest).isEmpty();
    }

    @Test
    void getLineageReturnsEmptyListForUnknownKey() {
        List<VaultSecretLineageService.LineageEntry> lineage = lineageService.getLineage("unknown");
        assertThat(lineage).isEmpty();
    }

    @Test
    void clearLineageRemovesHistory() {
        lineageService.record("db.url", "secret/myapp", 1, "startup");
        lineageService.clearLineage("db.url");

        assertThat(lineageService.getLineage("db.url")).isEmpty();
        assertThat(lineageService.trackedKeys()).doesNotContain("db.url");
    }

    @Test
    void trackedKeysReturnsAllRecordedKeys() {
        lineageService.record("key1", "secret/app", 1, "startup");
        lineageService.record("key2", "secret/app", 1, "startup");

        assertThat(lineageService.trackedKeys()).containsExactlyInAnyOrder("key1", "key2");
    }

    @Test
    void nullVersionIsAllowed() {
        lineageService.record("kv1.secret", "secret/legacy", null, "startup");

        Optional<VaultSecretLineageService.LineageEntry> latest = lineageService.getLatest("kv1.secret");
        assertThat(latest).isPresent();
        assertThat(latest.get().version()).isNull();
    }
}
