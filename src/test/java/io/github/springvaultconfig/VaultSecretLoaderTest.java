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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretLoaderTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultConfigProperties properties;
    private VaultSecretLoader loader;

    @BeforeEach
    void setUp() {
        properties = new VaultConfigProperties();
        properties.setBackend("secret");
        properties.setPaths(List.of("myapp/config"));
        properties.setFailFast(true);
        loader = new VaultSecretLoader(vaultTemplate, properties);
    }

    @Test
    void shouldLoadSecretsFromConfiguredPath() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "s3cr3t", "api.key", "abc123"));
        when(vaultTemplate.read("secret/myapp/config")).thenReturn(response);

        Map<String, Object> secrets = loader.loadSecrets();

        assertThat(secrets).containsEntry("db.password", "s3cr3t");
        assertThat(secrets).containsEntry("api.key", "abc123");
        verify(vaultTemplate).read("secret/myapp/config");
    }

    @Test
    void shouldReturnEmptyMapWhenResponseIsNull() {
        when(vaultTemplate.read("secret/myapp/config")).thenReturn(null);

        Map<String, Object> secrets = loader.loadSecrets();

        assertThat(secrets).isEmpty();
    }

    @Test
    void shouldThrowExceptionOnFailureWhenFailFastEnabled() {
        when(vaultTemplate.read("secret/myapp/config"))
                .thenThrow(new RuntimeException("Vault unavailable"));

        assertThatThrownBy(() -> loader.loadSecrets())
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("secret/myapp/config");
    }

    @Test
    void shouldSkipPathOnFailureWhenFailFastDisabled() {
        properties.setFailFast(false);
        when(vaultTemplate.read("secret/myapp/config"))
                .thenThrow(new RuntimeException("Vault unavailable"));

        Map<String, Object> secrets = loader.loadSecrets();

        assertThat(secrets).isEmpty();
    }

    @Test
    void shouldUsePathDirectlyWhenBackendIsBlank() {
        properties.setBackend("");
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("token", "xyz"));
        when(vaultTemplate.read("myapp/config")).thenReturn(response);

        Map<String, Object> secrets = loader.loadSecrets();

        assertThat(secrets).containsEntry("token", "xyz");
    }
}
