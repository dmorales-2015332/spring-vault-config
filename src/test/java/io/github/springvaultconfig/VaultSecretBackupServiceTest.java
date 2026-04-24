package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretBackupServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretBackupService backupService;

    private static final String SECRET_PATH = "secret/myapp/config";

    @BeforeEach
    void setUp() {
        backupService = new VaultSecretBackupService(vaultTemplate, properties);
    }

    @Test
    void backup_shouldStoreSecretsWhenVaultReturnsData() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "s3cr3t", "api.key", "abc123"));
        when(vaultTemplate.read(SECRET_PATH)).thenReturn(response);

        backupService.backup(SECRET_PATH);

        assertThat(backupService.hasBackup(SECRET_PATH)).isTrue();
        assertThat(backupService.getBackupTimestamp(SECRET_PATH)).isNotNull();
    }

    @Test
    void backup_shouldNotStoreWhenVaultReturnsNull() {
        when(vaultTemplate.read(SECRET_PATH)).thenReturn(null);

        backupService.backup(SECRET_PATH);

        assertThat(backupService.hasBackup(SECRET_PATH)).isFalse();
    }

    @Test
    void backup_shouldNotStoreWhenVaultThrowsException() {
        when(vaultTemplate.read(SECRET_PATH)).thenThrow(new RuntimeException("Vault unavailable"));

        backupService.backup(SECRET_PATH);

        assertThat(backupService.hasBackup(SECRET_PATH)).isFalse();
    }

    @Test
    void restore_shouldReturnBackedUpSecrets() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "s3cr3t"));
        when(vaultTemplate.read(SECRET_PATH)).thenReturn(response);
        backupService.backup(SECRET_PATH);

        Map<String, Object> restored = backupService.restore(SECRET_PATH);

        assertThat(restored).containsEntry("db.password", "s3cr3t");
    }

    @Test
    void restore_shouldReturnEmptyMapWhenNoBackupExists() {
        Map<String, Object> restored = backupService.restore(SECRET_PATH);

        assertThat(restored).isEmpty();
    }

    @Test
    void clearBackup_shouldRemoveEntry() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value"));
        when(vaultTemplate.read(SECRET_PATH)).thenReturn(response);
        backupService.backup(SECRET_PATH);

        backupService.clearBackup(SECRET_PATH);

        assertThat(backupService.hasBackup(SECRET_PATH)).isFalse();
    }

    @Test
    void getBackupTimestamp_shouldReturnNullWhenNoBackup() {
        assertThat(backupService.getBackupTimestamp(SECRET_PATH)).isNull();
    }

    @Test
    void backup_shouldOverwritePreviousBackupWithLatestData() {
        VaultResponse firstResponse = new VaultResponse();
        firstResponse.setData(Map.of("db.password", "old-secret"));
        when(vaultTemplate.read(SECRET_PATH)).thenReturn(firstResponse);
        backupService.backup(SECRET_PATH);

        VaultResponse secondResponse = new VaultResponse();
        secondResponse.setData(Map.of("db.password", "new-secret"));
        when(vaultTemplate.read(SECRET_PATH)).thenReturn(secondResponse);
        backupService.backup(SECRET_PATH);

        Map<String, Object> restored = backupService.restore(SECRET_PATH);
        assertThat(restored).containsEntry("db.password", "new-secret");
    }
}
