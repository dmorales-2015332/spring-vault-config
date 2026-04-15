package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretNamespaceServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretNamespaceService service;

    @BeforeEach
    void setUp() {
        when(properties.getBackend()).thenReturn("secret");
        service = new VaultSecretNamespaceService(vaultTemplate, properties);
    }

    @Test
    void listSecretKeys_returnsKeys_whenDataPresent() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();
        data.put("keys", Arrays.asList("db-password", "api-key"));
        when(response.getData()).thenReturn(data);
        when(vaultTemplate.read("secret/myapp")).thenReturn(response);

        List<String> keys = service.listSecretKeys("myapp");

        assertThat(keys).containsExactly("db-password", "api-key");
    }

    @Test
    void listSecretKeys_returnsEmpty_whenResponseIsNull() {
        when(vaultTemplate.read("secret/myapp")).thenReturn(null);

        List<String> keys = service.listSecretKeys("myapp");

        assertThat(keys).isEmpty();
    }

    @Test
    void listSecretKeys_throwsException_whenVaultFails() {
        when(vaultTemplate.read(anyString())).thenThrow(new RuntimeException("Vault unavailable"));

        assertThatThrownBy(() -> service.listSecretKeys("myapp"))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("myapp");
    }

    @Test
    void resolveSecrets_returnsSecretMap_whenDataPresent() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object>data.put("username", "admin");
        data.put("password", "s3cr3t");
        when(response.getData()).thenReturn(data);
        when(vaultTemplate.read("secret/myapp/db")).thenReturn(response);

        Map<String, Object> secrets = service.resolveSecrets("myapp/db");

        assertThat(secrets).containsEntry("username", "admin").containsEntry("password", "s3cr3t");
    }

    @Test
    void resolveSecrets_returnsEmpty_whenDataIsNull() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(null);
        when(vaultTemplate.read("secret/myapp/db")).thenReturn(response);

        Map<String, Object> secrets = service.resolveSecrets("myapp/db");

        assertThat(secrets).isEmpty();
    }

    @Test
    void constructor_throwsNullPointerException_whenVaultTemplateIsNull() {
        assertThatThrownBy(() -> new VaultSecretNamespaceService(null, properties))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_throwsNullPointerException_whenPropertiesIsNull() {
        assertThatThrownBy(() -> new VaultSecretNamespaceService(vaultTemplate, null))
                .isInstanceOf(NullPointerException.class);
    }
}
