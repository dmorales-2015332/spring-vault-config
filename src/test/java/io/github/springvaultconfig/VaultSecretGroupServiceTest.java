package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VaultSecretGroupServiceTest {

    private VaultTemplate vaultTemplate;
    private VaultSecretGroupService service;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);
        service = new VaultSecretGroupService(vaultTemplate);
    }

    @Test
    void loadGroup_mergesSecretsFromMultiplePaths() {
        VaultResponse r1 = new VaultResponse();
        r1.setData(Map.of("user", "admin"));
        VaultResponse r2 = new VaultResponse();
        r2.setData(Map.of("password", "secret"));

        when(vaultTemplate.read("secret/db/primary")).thenReturn(r1);
        when(vaultTemplate.read("secret/db/replica")).thenReturn(r2);

        Map<String, Object> result = service.loadGroup("database", List.of("secret/db/primary", "secret/db/replica"));

        assertThat(result).containsEntry("user", "admin").containsEntry("password", "secret");
    }

    @Test
    void loadGroup_handlesNullResponse() {
        when(vaultTemplate.read("secret/missing")).thenReturn(null);

        Map<String, Object> result = service.loadGroup("empty", List.of("secret/missing"));

        assertThat(result).isEmpty();
    }

    @Test
    void getGroup_returnsCachedGroup() {
        VaultResponse r = new VaultResponse();
        r.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/app")).thenReturn(r);

        service.loadGroup("app", List.of("secret/app"));

        assertThat(service.getGroup("app")).isPresent().get().containsEntry("key", "value");
    }

    @Test
    void getGroup_returnsEmptyForUnknownGroup() {
        assertThat(service.getGroup("nonexistent")).isEmpty();
    }

    @Test
    void listGroups_returnsAllGroupNames() {
        VaultResponse r = new VaultResponse();
        r.setData(Map.of("x", "y"));
        when(vaultTemplate.read(anyString())).thenReturn(r);

        service.loadGroup("g1", List.of("secret/g1"));
        service.loadGroup("g2", List.of("secret/g2"));

        assertThat(service.listGroups()).containsExactlyInAnyOrder("g1", "g2");
    }

    @Test
    void evictGroup_removesGroupFromCache() {
        VaultResponse r = new VaultResponse();
        r.setData(Map.of("k", "v"));
        when(vaultTemplate.read("secret/temp")).thenReturn(r);

        service.loadGroup("temp", List.of("secret/temp"));
        service.evictGroup("temp");

        assertThat(service.getGroup("temp")).isEmpty();
    }

    @Test
    void loadGroup_handlesExceptionGracefully() {
        when(vaultTemplate.read("secret/broken")).thenThrow(new RuntimeException("vault error"));

        Map<String, Object> result = service.loadGroup("broken", List.of("secret/broken"));

        assertThat(result).isEmpty();
    }
}
