package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretTaggingServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretTaggingService service;

    @BeforeEach
    void setUp() {
        when(properties.getBackend()).thenReturn("secret");
        service = new VaultSecretTaggingService(vaultOperations, properties);
    }

    @Test
    void tagSecret_writesTagsToMetadataPath() {
        Map<String, String> tags = Map.of("owner", "team-a", "env", "prod");

        service.tagSecret("myapp/db", tags);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(vaultOperations).write(eq("secret/metadata/myapp/db"), captor.capture());
        assertThat(captor.getValue()).containsKey("custom_metadata");
        assertThat(captor.getValue().get("custom_metadata")).isEqualTo(tags);
    }

    @Test
    void getTagsForSecret_returnsTagsFromMetadata() {
        Map<String, Object> data = new HashMap<>();
        data.put("custom_metadata", Map.of("team", "platform"));
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(data);
        when(vaultOperations.read("secret/metadata/myapp/db")).thenReturn(response);

        Map<String, String> tags = service.getTagsForSecret("myapp/db");

        assertThat(tags).containsEntry("team", "platform");
    }

    @Test
    void getTagsForSecret_returnsEmptyMapWhenNoMetadata() {
        when(vaultOperations.read(anyString())).thenReturn(null);

        Map<String, String> tags = service.getTagsForSecret("myapp/db");

        assertThat(tags).isEmpty();
    }

    @Test
    void clearTags_writesEmptyMetadata() {
        service.clearTags("myapp/db");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(vaultOperations).write(eq("secret/metadata/myapp/db"), captor.capture());
        assertThat(captor.getValue().get("custom_metadata")).isEqualTo(Collections.emptyMap());
    }

    @Test
    void constructor_throwsOnNullVaultOperations() {
        assertThatNullPointerException()
                .isThrownBy(() -> new VaultSecretTaggingService(null, properties));
    }

    @Test
    void constructor_throwsOnNullProperties() {
        assertThatNullPointerException()
                .isThrownBy(() -> new VaultSecretTaggingService(vaultOperations, null));
    }
}
