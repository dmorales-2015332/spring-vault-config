package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VaultSecretObfuscationServiceTest {

    private VaultSecretObfuscationService service;

    @BeforeEach
    void setUp() {
        VaultConfigProperties props = mock(VaultConfigProperties.class);
        VaultConfigProperties.ObfuscationProperties obfProps = mock(VaultConfigProperties.ObfuscationProperties.class);
        when(props.getObfuscation()).thenReturn(obfProps);
        when(obfProps.getSensitiveKeyPatterns()).thenReturn(List.of("password", "secret", "token", "key"));
        service = new VaultSecretObfuscationService(props);
    }

    @Test
    void isSensitive_matchesPassword() {
        assertThat(service.isSensitive("db_password")).isTrue();
    }

    @Test
    void isSensitive_matchesToken() {
        assertThat(service.isSensitive("auth_token")).isTrue();
    }

    @Test
    void isSensitive_nonSensitiveKey() {
        assertThat(service.isSensitive("app_name")).isFalse();
    }

    @Test
    void isSensitive_nullKey() {
        assertThat(service.isSensitive(null)).isFalse();
    }

    @Test
    void obfuscate_sensitiveReturnsStars() {
        assertThat(service.obfuscate("db_password", "s3cr3t")).isEqualTo("****");
    }

    @Test
    void obfuscate_nonSensitiveReturnsValue() {
        assertThat(service.obfuscate("app_name", "myapp")).isEqualTo("myapp");
    }

    @Test
    void obfuscateAll_masksOnlySensitiveKeys() {
        Map<String, String> secrets = Map.of(
                "db_password", "secret123",
                "app_name", "myapp",
                "api_key", "abc"
        );
        Map<String, String> result = service.obfuscateAll(secrets);
        assertThat(result.get("db_password")).isEqualTo("****");
        assertThat(result.get("app_name")).isEqualTo("myapp");
        assertThat(result.get("api_key")).isEqualTo("****");
    }
}
