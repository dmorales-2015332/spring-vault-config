package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretPinningServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretPinningService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretPinningService(vaultOperations);
    }

    @Test
    void pin_shouldStoreVersionForPath() {
        service.pin("my/secret", 3);
        assertThat(service.getPinnedVersion("my/secret")).contains(3);
    }

    @Test
    void pin_shouldRejectBlankPath() {
        assertThatThrownBy(() -> service.pin(" ", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void pin_shouldRejectZeroVersion() {
        assertThatThrownBy(() -> service.pin("my/secret", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(">= 1");
    }

    @Test
    void unpin_shouldRemoveExistingPin() {
        service.pin("my/secret", 2);
        service.unpin("my/secret");
        assertThat(service.getPinnedVersion("my/secret")).isEmpty();
    }

    @Test
    void getPinnedVersion_returnsEmptyWhenNotPinned() {
        assertThat(service.getPinnedVersion("unknown/path")).isEqualTo(Optional.empty());
    }

    @Test
    void readPinned_returnsSecretDataForPinnedVersion() {
        service.pin("db/password", 5);

        Map<String, Object> inner = new HashMap<>();
        inner.put("password", "s3cr3t");
        Map<String, Object> outer = new HashMap<>();
        outer.put("data", inner);

        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(outer);
        when(vaultOperations.read("secret/data/db/password?version=5")).thenReturn(response);

        Map<String, Object> result = service.readPinned("secret", "db/password");

        assertThat(result).containsEntry("password", "s3cr3t");
    }

    @Test
    void readPinned_returnsEmptyMapWhenNotPinned() {
        Map<String, Object> result = service.readPinned("secret", "not/pinned");
        assertThat(result).isEmpty();
        verifyNoInteractions(vaultOperations);
    }

    @Test
    void readPinned_returnsEmptyMapWhenVaultReturnsNull() {
        service.pin("cfg/key", 1);
        when(vaultOperations.read(anyString())).thenReturn(null);
        assertThat(service.readPinned("secret", "cfg/key")).isEmpty();
    }

    @Test
    void getAllPins_returnsSnapshotOfAllPins() {
        service.pin("a", 1);
        service.pin("b", 2);
        Map<String, Integer> pins = service.getAllPins();
        assertThat(pins).containsEntry("a", 1).containsEntry("b", 2).hasSize(2);
    }
}
