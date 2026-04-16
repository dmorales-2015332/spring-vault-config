package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class VaultSecretValidationServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretValidationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getPath()).thenReturn("secret/app");
        service = new VaultSecretValidationService(vaultTemplate, properties);
    }

    @Test
    void validateRequiredSecrets_allPresent_doesNotThrow() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("db.password", "secret123", "api.key", "key456"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        service.validateRequiredSecrets(List.of("db.password", "api.key"));

        verify(vaultTemplate).read("secret/app");
    }

    @Test
    void validateRequiredSecrets_missingKey_throwsException() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("db.password", "secret123"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        assertThatThrownBy(() -> service.validateRequiredSecrets(List.of("db.password", "api.key")))
                .isInstanceOf(VaultSecretValidationException.class)
                .hasMessageContaining("api.key");
    }

    @Test
    void validateRequiredSecrets_blankValue_throwsException() {
        Map<String, Object> data = new HashMap<>();
        data.put("db.password", "   ");
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(data);
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        assertThatThrownBy(() -> service.validateRequiredSecrets(List.of("db.password")))
                .isInstanceOf(VaultSecretValidationException.class);
    }

    @Test
    void validateRequiredSecrets_emptyList_skipsValidation() {
        service.validateRequiredSecrets(List.of());
        verifyNoInteractions(vaultTemplate);
    }

    @Test
    void secretExists_keyPresent_returnsTrue() {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(Map.of("mykey", "myvalue"));
        when(vaultTemplate.read("secret/test")).thenReturn(response);

        assertThat(service.secretExists("secret/test", "mykey")).isTrue();
    }

    @Test
    void secretExists_nullResponse_returnsFalse() {
        when(vaultTemplate.read("secret/test")).thenReturn(null);
        assertThat(service.secretExists("secret/test", "mykey")).isFalse();
    }
}
