package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretTemplateServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretTemplateService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretTemplateService(vaultTemplate);
    }

    @Test
    void resolveReturnsOriginalStringWhenNoPlaceholders() {
        String input = "jdbc:postgresql://localhost:5432/mydb";
        assertThat(service.resolve(input)).isEqualTo(input);
        verifyNoInteractions(vaultTemplate);
    }

    @Test
    void resolveReturnsNullWhenInputIsNull() {
        assertThat(service.resolve(null)).isNull();
    }

    @Test
    void resolveSinglePlaceholder() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("password", "s3cr3t"));
        when(vaultTemplate.read("secret/db")).thenReturn(response);

        String result = service.resolve("jdbc:postgresql://localhost/${vault:secret/db#password}");

        assertThat(result).isEqualTo("jdbc:postgresql://localhost/s3cr3t");
    }

    @Test
    void resolveMultiplePlaceholdersInSameString() {
        VaultResponse dbResponse = new VaultResponse();
        dbResponse.setData(Map.of("user", "admin", "password", "pass123"));
        when(vaultTemplate.read("secret/db")).thenReturn(dbResponse);

        String result = service.resolve("${vault:secret/db#user}:${vault:secret/db#password}@host");

        assertThat(result).isEqualTo("admin:pass123@host");
        verify(vaultTemplate, times(1)).read("secret/db");
    }

    @Test
    void resolveUsesCache() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("token", "abc"));
        when(vaultTemplate.read("secret/api")).thenReturn(response);

        service.resolve("${vault:secret/api#token}");
        service.resolve("${vault:secret/api#token}");

        verify(vaultTemplate, times(1)).read("secret/api");
    }

    @Test
    void clearCacheForcesFreshFetch() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("key", "value"));
        when(vaultTemplate.read("secret/x")).thenReturn(response);

        service.resolve("${vault:secret/x#key}");
        service.clearCache();
        service.resolve("${vault:secret/x#key}");

        verify(vaultTemplate, times(2)).read("secret/x");
    }

    @Test
    void resolveThrowsWhenKeyNotFoundInSecrets() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("other", "value"));
        when(vaultTemplate.read("secret/db")).thenReturn(response);

        assertThatThrownBy(() -> service.resolve("${vault:secret/db#missing}"))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void resolveThrowsWhenVaultReturnsNull() {
        when(vaultTemplate.read("secret/empty")).thenReturn(null);

        assertThatThrownBy(() -> service.resolve("${vault:secret/empty#key}"))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("secret/empty");
    }
}
