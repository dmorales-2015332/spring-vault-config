package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretBulkLoadServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretBulkLoadService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretBulkLoadService(vaultTemplate, properties);
    }

    @Test
    void bulkLoad_returnsMergedSecrets() {
        VaultResponse resp1 = new VaultResponse();
        resp1.setData(Map.of("db.password", "secret1"));
        VaultResponse resp2 = new VaultResponse();
        resp2.setData(Map.of("api.key", "secret2"));

        when(vaultTemplate.read("secret/db")).thenReturn(resp1);
        when(vaultTemplate.read("secret/api")).thenReturn(resp2);

        Map<String, Object> result = service.bulkLoad(List.of("secret/db", "secret/api"));

        assertThat(result).containsEntry("db.password", "secret1");
        assertThat(result).containsEntry("api.key", "secret2");
    }

    @Test
    void bulkLoad_emptyPaths_returnsEmptyMap() {
        Map<String, Object> result = service.bulkLoad(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(vaultTemplate);
    }

    @Test
    void bulkLoad_nullResponse_skipsPath() {
        when(vaultTemplate.read("secret/missing")).thenReturn(null);
        Map<String, Object> result = service.bulkLoad(List.of("secret/missing"));
        assertThat(result).isEmpty();
    }

    @Test
    void getCached_returnsLoadedSecrets() {
        VaultResponse resp = new VaultResponse();
        resp.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/app")).thenReturn(resp);

        service.bulkLoad(List.of("secret/app"));

        assertThat(service.getCached("secret/app")).containsEntry("key", "value");
    }

    @Test
    void getCached_unknownPath_returnsEmptyMap() {
        assertThat(service.getCached("secret/unknown")).isEmpty();
    }

    @Test
    void clearCache_removesAllEntries() {
        VaultResponse resp = new VaultResponse();
        resp.setData(Map.of("k", "v"));
        when(vaultTemplate.read("secret/x")).thenReturn(resp);
        service.bulkLoad(List.of("secret/x"));

        service.clearCache();

        assertThat(service.getCached("secret/x")).isEmpty();
    }

    @Test
    void bulkLoad_exceptionOnPath_continuesOtherPaths() {
        when(vaultTemplate.read("secret/bad")).thenThrow(new RuntimeException("Vault error"));
        VaultResponse resp = new VaultResponse();
        resp.setData(Map.of("good.key", "goodVal"));
        when(vaultTemplate.read("secret/good")).thenReturn(resp);

        Map<String, Object> result = service.bulkLoad(List.of("secret/bad", "secret/good"));

        assertThat(result).containsEntry("good.key", "goodVal");
        assertThat(result).doesNotContainKey("secret/bad");
    }
}
