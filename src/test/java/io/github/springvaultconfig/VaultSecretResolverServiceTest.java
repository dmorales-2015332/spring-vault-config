package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretResolverServiceTest {

    @Mock
    private VaultSecretLoader secretLoader;

    private VaultSecretResolverService resolverService;

    @BeforeEach
    void setUp() {
        resolverService = new VaultSecretResolverService(secretLoader);
    }

    @Test
    void resolve_shouldReplaceSinglePlaceholder() {
        when(secretLoader.loadSecrets("secret/db")).thenReturn(Map.of("password", "s3cr3t"));
        String result = resolverService.resolve("jdbc:password=${vault:secret/db#password}");
        assertThat(result).isEqualTo("jdbc:password=s3cr3t");
    }

    @Test
    void resolve_shouldReplaceMultiplePlaceholders() {
        when(secretLoader.loadSecrets("secret/db")).thenReturn(Map.of("user", "admin", "password", "pass123"));
        String result = resolverService.resolve("${vault:secret/db#user}:${vault:secret/db#password}");
        assertThat(result).isEqualTo("admin:pass123");
    }

    @Test
    void resolve_shouldReturnOriginalWhenNoPlaceholder() {
        String input = "no-vault-here";
        String result = resolverService.resolve(input);
        assertThat(result).isEqualTo(input);
        verifyNoInteractions(secretLoader);
    }

    @Test
    void resolve_shouldReturnEmptyStringForMissingKey() {
        when(secretLoader.loadSecrets("secret/db")).thenReturn(Map.of());
        String result = resolverService.resolve("value=${vault:secret/db#missing}");
        assertThat(result).isEqualTo("value=");
    }

    @Test
    void resolve_shouldHandleNullInput() {
        String result = resolverService.resolve(null);
        assertThat(result).isNull();
        verifyNoInteractions(secretLoader);
    }

    @Test
    void resolve_shouldReturnEmptyStringOnLoaderException() {
        when(secretLoader.loadSecrets("secret/bad")).thenThrow(new VaultSecretLoadException("connection error"));
        String result = resolverService.resolve("val=${vault:secret/bad#key}");
        assertThat(result).isEqualTo("val=");
    }

    @Test
    void resolve_shouldUseCacheForRepeatedPath() {
        when(secretLoader.loadSecrets("secret/db")).thenReturn(Map.of("key", "value"));
        resolverService.resolve("${vault:secret/db#key}");
        resolverService.resolve("${vault:secret/db#key}");
        verify(secretLoader, times(1)).loadSecrets("secret/db");
    }

    @Test
    void clearCache_shouldForceReload() {
        when(secretLoader.loadSecrets("secret/db")).thenReturn(Map.of("key", "value"));
        resolverService.resolve("${vault:secret/db#key}");
        resolverService.clearCache();
        resolverService.resolve("${vault:secret/db#key}");
        verify(secretLoader, times(2)).loadSecrets("secret/db");
    }
}
