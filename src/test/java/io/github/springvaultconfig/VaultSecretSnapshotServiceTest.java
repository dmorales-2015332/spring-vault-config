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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretSnapshotServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretSnapshotService(vaultTemplate);
    }

    @Test
    void takeSnapshot_shouldStoreSnapshot() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        VaultSecretSnapshot snapshot = service.takeSnapshot("secret/app");

        assertThat(snapshot.path()).isEqualTo("secret/app");
        assertThat(snapshot.data()).containsEntry("key", "value");
        assertThat(snapshot.takenAt()).isNotNull();
        assertThat(service.hasSnapshot("secret/app")).isTrue();
    }

    @Test
    void takeSnapshot_shouldThrowWhenNoData() {
        when(vaultTemplate.read("secret/missing")).thenReturn(null);

        assertThatThrownBy(() -> service.takeSnapshot("secret/missing"))
                .isInstanceOf(VaultSecretLoadException.class);
    }

    @Test
    void getSnapshot_shouldReturnStoredSnapshot() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "secret"));
        when(vaultTemplate.read("secret/db")).thenReturn(response);
        service.takeSnapshot("secret/db");

        VaultSecretSnapshot snapshot = service.getSnapshot("secret/db");
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.get("db.password")).isEqualTo("secret");
    }

    @Test
    void clearSnapshot_shouldRemoveSnapshot() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("k", "v"));
        when(vaultTemplate.read("secret/tmp")).thenReturn(response);
        service.takeSnapshot("secret/tmp");

        service.clearSnapshot("secret/tmp");
        assertThat(service.hasSnapshot("secret/tmp")).isFalse();
    }

    @Test
    void getAllSnapshots_shouldReturnUnmodifiableView() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("x", "y"));
        when(vaultTemplate.read("secret/a")).thenReturn(response);
        service.takeSnapshot("secret/a");

        var all = service.getAllSnapshots();
        assertThat(all).hasSize(1);
        assertThatThrownBy(() -> all.put("secret/b", null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
