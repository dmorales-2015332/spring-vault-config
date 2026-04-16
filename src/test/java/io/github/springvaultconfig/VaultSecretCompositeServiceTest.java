package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretCompositeServiceTest {

    @Mock
    private VaultSecretLoader secretLoader;

    private VaultSecretCompositeService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretCompositeService(secretLoader);
    }

    @Test
    void shouldReturnEmptyMapWhenNoPathsProvided() {
        assertThat(service.loadComposite(List.of())).isEmpty();
        verifyNoInteractions(secretLoader);
    }

    @Test
    void shouldReturnEmptyMapWhenPathsIsNull() {
        assertThat(service.loadComposite(null)).isEmpty();
        verifyNoInteractions(secretLoader);
    }

    @Test
    void shouldMergeSecretsFromMultiplePaths() {
        when(secretLoader.loadSecrets("secret/app/base")).thenReturn(Map.of("key1", "val1", "shared", "base"));
        when(secretLoader.loadSecrets("secret/app/override")).thenReturn(Map.of("key2", "val2", "shared", "override"));

        Map<String, String> result = service.loadComposite(List.of("secret/app/base", "secret/app/override"));

        assertThat(result).containsEntry("key1", "val1")
                          .containsEntry("key2", "val2")
                          .containsEntry("shared", "override");
    }

    @Test
    void shouldSkipPathOnLoadFailureAndContinue() {
        when(secretLoader.loadSecrets("secret/bad")).thenThrow(new VaultSecretLoadException("not found"));
        when(secretLoader.loadSecrets("secret/good")).thenReturn(Map.of("key", "value"));

        Map<String, String> result = service.loadComposite(List.of("secret/bad", "secret/good"));

        assertThat(result).containsEntry("key", "value").hasSize(1);
    }

    @Test
    void shouldReturnImmutableMap() {
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("k", "v"));
        Map<String, String> result = service.loadComposite(List.of("secret/app"));
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> result.put("new", "entry"));
    }
}
