package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultList;
import org.springframework.vault.support.VaultResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretPaginationServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretPaginationService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretPaginationService(vaultOperations);
    }

    @Test
    void listSecretKeys_returnsKeys() {
        VaultList vaultList = mock(VaultList.class);
        when(vaultList.getData()).thenReturn(Map.of("keys", List.of("key1", "key2", "key3")));
        when(vaultOperations.list("secret/")).thenReturn(vaultList);

        List<String> keys = service.listSecretKeys("secret/");

        assertThat(keys).containsExactly("key1", "key2", "key3");
    }

    @Test
    void listSecretKeys_returnsEmpty_whenNull() {
        when(vaultOperations.list("secret/")).thenReturn(null);
        assertThat(service.listSecretKeys("secret/")).isEmpty();
    }

    @Test
    void listSecretKeysPage_returnsCorrectPage() {
        VaultList vaultList = mock(VaultList.class);
        when(vaultList.getData()).thenReturn(Map.of("keys", List.of("a", "b", "c", "d", "e")));
        when(vaultOperations.list("secret/")).thenReturn(vaultList);

        List<String> page = service.listSecretKeysPage("secret/", 1, 2);

        assertThat(page).containsExactly("c", "d");
    }

    @Test
    void listSecretKeysPage_returnsEmpty_whenPageOutOfBounds() {
        VaultList vaultList = mock(VaultList.class);
        when(vaultList.getData()).thenReturn(Map.of("keys", List.of("a", "b")));
        when(vaultOperations.list("secret/")).thenReturn(vaultList);

        List<String> page = service.listSecretKeysPage("secret/", 5, 10);

        assertThat(page).isEmpty();
    }

    @Test
    void countSecretKeys_returnsCount() {
        VaultList vaultList = mock(VaultList.class);
        when(vaultList.getData()).thenReturn(Map.of("keys", List.of("x", "y")));
        when(vaultOperations.list("secret/")).thenReturn(vaultList);

        assertThat(service.countSecretKeys("secret/")).isEqualTo(2);
    }

    @Test
    void getSecretAtPath_returnsData() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("password", "s3cr3t"));
        when(vaultOperations.read("secret/myapp")).thenReturn(response);

        Map<String, Object> data = service.getSecretAtPath("secret/myapp");

        assertThat(data).containsEntry("password", "s3cr3t");
    }
}
