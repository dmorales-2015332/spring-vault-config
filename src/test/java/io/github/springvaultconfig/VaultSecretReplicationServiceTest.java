package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretReplicationServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretReplicationService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretReplicationService(vaultTemplate);
    }

    @Test
    void constructor_nullVaultTemplate_throwsException() {
        assertThatThrownBy(() -> new VaultSecretReplicationService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("vaultTemplate must not be null");
    }

    @Test
    void replicate_copiesAllKeysToAllTargets() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("username", "admin");
        data.put("password", "secret");
        response.setData(data);

        when(vaultTemplate.read("secret/source")).thenReturn(response);

        Map<String, Boolean> results = service.replicate("secret/source",
                List.of("secret/target1", "secret/target2"));

        assertThat(results).containsEntry("secret/target1", true)
                           .containsEntry("secret/target2", true);
        verify(vaultTemplate).write(eq("secret/target1"), eq(data));
        verify(vaultTemplate).write(eq("secret/target2"), eq(data));
    }

    @Test
    void replicate_sourceNotFound_returnsEmptyDataAndSucceeds() {
        when(vaultTemplate.read("secret/missing")).thenReturn(null);

        Map<String, Boolean> results = service.replicate("secret/missing", List.of("secret/target"));

        assertThat(results).containsEntry("secret/target", true);
        verify(vaultTemplate).write(eq("secret/target"), eq(new HashMap<>()));
    }

    @Test
    void replicate_writeThrowsException_returnsFalseForThatTarget() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/source")).thenReturn(response);
        doThrow(new RuntimeException("Vault unavailable"))
                .when(vaultTemplate).write(eq("secret/bad-target"), any());

        Map<String, Boolean> results = service.replicate("secret/source", List.of("secret/bad-target"));

        assertThat(results).containsEntry("secret/bad-target", false);
    }

    @Test
    void replicateKeys_filtersOnlyRequestedKeys() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("username", "admin");
        data.put("password", "secret");
        data.put("host", "localhost");
        response.setData(data);

        when(vaultTemplate.read("secret/source")).thenReturn(response);

        boolean result = service.replicateKeys("secret/source", "secret/target",
                List.of("username", "host"));

        assertThat(result).isTrue();
        verify(vaultTemplate).write(eq("secret/target"),
                eq(Map.of("username", "admin", "host", "localhost")));
    }

    @Test
    void replicateKeys_emptyKeyList_replicatesAllKeys() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = Map.of("a", "1", "b", "2");
        response.setData(data);
        when(vaultTemplate.read("secret/source")).thenReturn(response);

        boolean result = service.replicateKeys("secret/source", "secret/target", List.of());

        assertThat(result).isTrue();
        verify(vaultTemplate).write(eq("secret/target"), eq(data));
    }
}
