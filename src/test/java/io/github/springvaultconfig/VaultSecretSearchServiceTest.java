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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretSearchServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretSearchService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretSearchService(vaultTemplate);
    }

    @Test
    void searchByKeyPattern_returnsMatchingKeys() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("db_password", "secret");
        data.put("db_user", "admin");
        data.put("api_key", "key123");
        response.setData(data);
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        Map<String, Object> result = service.searchByKeyPattern("secret/app", "db_.*");

        assertThat(result).containsKeys("db_password", "db_user").doesNotContainKey("api_key");
    }

    @Test
    void searchByKeyPattern_returnsEmptyWhenNoMatch() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("api_key", "value"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        Map<String, Object> result = service.searchByKeyPattern("secret/app", "db_.*");

        assertThat(result).isEmpty();
    }

    @Test
    void searchByKeyPattern_returnsEmptyWhenNullResponse() {
        when(vaultTemplate.read("secret/app")).thenReturn(null);
        assertThat(service.searchByKeyPattern("secret/app", ".*")).isEmpty();
    }

    @Test
    void listPaths_returnsEmptyWhenNullResponse() {
        when(vaultTemplate.read("secret/?list=true")).thenReturn(null);
        List<String> paths = service.listPaths("secret/");
        assertThat(paths).isEmpty();
    }

    @Test
    void searchByValueContains_returnsMatchingEntries() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "hello-world");
        data.put("key2", "foo-bar");
        response.setData(data);
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        Map<String, Object> result = service.searchByValueContains("secret/app", "hello");

        assertThat(result).containsKey("key1").doesNotContainKey("key2");
    }
}
