package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VaultSecretImportServiceTest {

    private VaultTemplate vaultTemplate;
    private VaultSecretImportService service;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);
        service = new VaultSecretImportService(vaultTemplate);
    }

    @Test
    void importSecrets_writesSecretsToPath() {
        Map<String, Object> secrets = Map.of("key1", "value1", "key2", "value2");
        service.importSecrets("secret/myapp", secrets);
        verify(vaultTemplate).write("secret/myapp", secrets);
    }

    @Test
    void importSecrets_throwsOnNullPath() {
        assertThatThrownBy(() -> service.importSecrets(null, Map.of("k", "v")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void importSecrets_throwsOnBlankPath() {
        assertThatThrownBy(() -> service.importSecrets("  ", Map.of("k", "v")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void importSecrets_doesNothingOnEmptySecrets() {
        service.importSecrets("secret/myapp", Map.of());
        verifyNoInteractions(vaultTemplate);
    }

    @Test
    void importSecretsWithNamespace_prependsNamespace() {
        Map<String, Object> secrets = Map.of("token", "abc");
        service.importSecretsWithNamespace("ns1", "myapp/db", secrets);
        verify(vaultTemplate).write("ns1/myapp/db", secrets);
    }

    @Test
    void mergeSecrets_mergesWithExisting() {
        VaultResponse existing = mock(VaultResponse.class);
        when(existing.getData()).thenReturn(Map.of("old", "oldval"));
        when(vaultTemplate.read("secret/myapp")).thenReturn(existing);

        service.mergeSecrets("secret/myapp", Map.of("new", "newval"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(vaultTemplate).write(eq("secret/myapp"), captor.capture());
        Map<String, Object> written = captor.getValue();
        assertThat(written).containsEntry("old", "oldval").containsEntry("new", "newval");
    }

    @Test
    void mergeSecrets_worksWhenNoExistingSecrets() {
        when(vaultTemplate.read("secret/myapp")).thenReturn(null);
        service.mergeSecrets("secret/myapp", Map.of("key", "val"));
        verify(vaultTemplate).write(eq("secret/myapp"), eq(Map.of("key", "val")));
    }
}
