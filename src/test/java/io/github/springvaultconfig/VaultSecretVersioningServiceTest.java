package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretVersioningServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretVersioningService service;

    @BeforeEach
    void setUp() {
        when(properties.getBackend()).thenReturn("secret");
        service = new VaultSecretVersioningService(vaultTemplate, properties);
    }

    @Test
    void readSecretAtVersion_returnsData_whenFound() {
        Map<String, Object> innerData = Map.of("password", "s3cr3t");
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", innerData);

        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(responseData);
        when(vaultTemplate.read("secret/data/myapp/db?version=3")).thenReturn(response);

        Map<String, Object> result = service.readSecretAtVersion("myapp/db", 3);

        assertThat(result).containsEntry("password", "s3cr3t");
    }

    @Test
    void readSecretAtVersion_returnsEmptyMap_whenResponseIsNull() {
        when(vaultTemplate.read(anyString())).thenReturn(null);

        Map<String, Object> result = service.readSecretAtVersion("myapp/db", 1);

        assertThat(result).isEmpty();
    }

    @Test
    void readSecretAtVersion_throwsException_onVaultError() {
        when(vaultTemplate.read(anyString())).thenThrow(new RuntimeException("Vault unavailable"));

        assertThatThrownBy(() -> service.readSecretAtVersion("myapp/db", 2))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("myapp/db");
    }

    @Test
    void readSecretMetadata_returnsMetadata_whenFound() {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("current_version", 5);

        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(metaData);
        when(vaultTemplate.read("secret/metadata/myapp/db")).thenReturn(response);

        Map<String, Object> result = service.readSecretMetadata("myapp/db");

        assertThat(result).containsEntry("current_version", 5);
    }

    @Test
    void readSecretMetadata_returnsEmptyMap_whenNotFound() {
        when(vaultTemplate.read(anyString())).thenReturn(null);

        Map<String, Object> result = service.readSecretMetadata("myapp/missing");

        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentVersion_returnsVersion_whenMetadataPresent() {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("current_version", 7);

        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(metaData);
        when(vaultTemplate.read("secret/metadata/myapp/db")).thenReturn(response);

        Optional<Integer> version = service.getCurrentVersion("myapp/db");

        assertThat(version).contains(7);
    }

    @Test
    void getCurrentVersion_returnsEmpty_whenMetadataMissing() {
        when(vaultTemplate.read(anyString())).thenReturn(null);

        Optional<Integer> version = service.getCurrentVersion("myapp/unknown");

        assertThat(version).isEmpty();
    }
}
