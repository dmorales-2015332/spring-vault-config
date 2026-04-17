package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.vault.core.VaultTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretArchiveServiceTest {

    private VaultSecretArchiveService archiveService;

    @BeforeEach
    void setUp() {
        VaultTemplate vaultTemplate = Mockito.mock(VaultTemplate.class);
        archiveService = new VaultSecretArchiveService(vaultTemplate);
    }

    @Test
    void shouldArchiveSecrets() {
        archiveService.archive("secret/app", Map.of("key", "value"));
        assertThat(archiveService.archiveSize("secret/app")).isEqualTo(1);
    }

    @Test
    void shouldReturnArchivedSecrets() {
        archiveService.archive("secret/app", Map.of("db.password", "s3cr3t"));
        List<VaultSecretArchiveService.ArchivedSecret> entries = archiveService.getArchive("secret/app");
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).path()).isEqualTo("secret/app");
        assertThat(entries.get(0).secrets()).containsKey("db.password");
        assertThat(entries.get(0).archivedAt()).isNotNull();
    }

    @Test
    void shouldAccumulateMultipleArchives() {
        archiveService.archive("secret/app", Map.of("k1", "v1"));
        archiveService.archive("secret/app", Map.of("k2", "v2"));
        assertThat(archiveService.archiveSize("secret/app")).isEqualTo(2);
    }

    @Test
    void shouldClearArchive() {
        archiveService.archive("secret/app", Map.of("key", "val"));
        archiveService.clearArchive("secret/app");
        assertThat(archiveService.archiveSize("secret/app")).isEqualTo(0);
    }

    @Test
    void shouldReturnEmptyListForUnknownPath() {
        assertThat(archiveService.getArchive("secret/unknown")).isEmpty();
    }

    @Test
    void shouldSkipArchiveForNullSecrets() {
        archiveService.archive("secret/app", null);
        assertThat(archiveService.archiveSize("secret/app")).isEqualTo(0);
    }

    @Test
    void shouldReturnAllArchives() {
        archiveService.archive("secret/app", Map.of("a", "1"));
        archiveService.archive("secret/db", Map.of("b", "2"));
        assertThat(archiveService.getAllArchives()).containsKeys("secret/app", "secret/db");
    }
}
