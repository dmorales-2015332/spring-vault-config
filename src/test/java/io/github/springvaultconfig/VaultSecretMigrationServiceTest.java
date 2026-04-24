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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretMigrationServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretMigrationService migrationService;

    @BeforeEach
    void setUp() {
        migrationService = new VaultSecretMigrationService(vaultTemplate);
    }

    @Test
    void constructorShouldRejectNullVaultTemplate() {
        assertThatThrownBy(() -> new VaultSecretMigrationService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("vaultTemplate must not be null");
    }

    @Test
    void copySecretShouldReturnFalseWhenSourceNotFound() {
        when(vaultTemplate.read("secret/source")).thenReturn(null);
        boolean result = migrationService.copySecret("secret/source", "secret/dest");
        assertThat(result).isFalse();
        verify(vaultTemplate, never()).write(any(), any());
    }

    @Test
    void copySecretShouldWriteToDestinationAndReturnTrue() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/source")).thenReturn(response);

        boolean result = migrationService.copySecret("secret/source", "secret/dest");

        assertThat(result).isTrue();
        verify(vaultTemplate).write(eq("secret/dest"), eq(Map.of("key", "value")));
        verify(vaultTemplate, never()).delete(any());
    }

    @Test
    void moveSecretShouldDeleteSourceAfterSuccessfulCopy() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("token", "abc123"));
        when(vaultTemplate.read("secret/old")).thenReturn(response);

        boolean result = migrationService.moveSecret("secret/old", "secret/new");

        assertThat(result).isTrue();
        verify(vaultTemplate).write(eq("secret/new"), any());
        verify(vaultTemplate).delete("secret/old");
    }

    @Test
    void moveSecretShouldNotDeleteSourceWhenCopyFails() {
        when(vaultTemplate.read("secret/old")).thenReturn(null);

        boolean result = migrationService.moveSecret("secret/old", "secret/new");

        assertThat(result).isFalse();
        verify(vaultTemplate, never()).delete(any());
    }

    @Test
    void bulkMigrateShouldReturnResultsForEachMapping() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("k", "v"));
        when(vaultTemplate.read("secret/a")).thenReturn(response);
        when(vaultTemplate.read("secret/b")).thenReturn(null);

        Map<String, Boolean> results = migrationService.bulkMigrate(
                Map.of("secret/a", "secret/a-new", "secret/b", "secret/b-new")
        );

        assertThat(results).containsEntry("secret/a", true)
                           .containsEntry("secret/b", false);
    }

    @Test
    void bulkMigrateShouldHandleExceptionsGracefully() {
        when(vaultTemplate.read("secret/err")).thenThrow(new RuntimeException("Vault unavailable"));

        Map<String, Boolean> results = migrationService.bulkMigrate(Map.of("secret/err", "secret/err-new"));

        assertThat(results).containsEntry("secret/err", false);
    }
}
