package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretMaskingServiceTest {

    private VaultSecretMaskingService maskingService;

    @BeforeEach
    void setUp() {
        maskingService = new VaultSecretMaskingService();
    }

    @Test
    void shouldMaskRegisteredSecretValue() {
        maskingService.registerSecrets(Map.of("db.password", "supersecret123"));

        String result = maskingService.mask("Connecting with password=supersecret123 to database");

        assertThat(result).doesNotContain("supersecret123");
        assertThat(result).contains("***MASKED***");
    }

    @Test
    void shouldReturnInputUnchangedWhenNoSecretsRegistered() {
        String input = "no secrets here";

        assertThat(maskingService.mask(input)).isEqualTo(input);
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        maskingService.registerSecrets(Map.of("key", "value"));

        assertThat(maskingService.mask(null)).isNull();
    }

    @Test
    void shouldMaskMultipleSecretsInSingleString() {
        maskingService.registerSecrets(Map.of(
                "api.key", "key-abc-123",
                "db.password", "pass-xyz-789"
        ));

        String result = maskingService.mask("api=key-abc-123 and db=pass-xyz-789");

        assertThat(result).doesNotContain("key-abc-123");
        assertThat(result).doesNotContain("pass-xyz-789");
    }

    @Test
    void shouldIdentifySensitiveKeyByPattern() {
        assertThat(maskingService.isSensitiveKey("db.password")).isTrue();
        assertThat(maskingService.isSensitiveKey("api_key")).isTrue();
        assertThat(maskingService.isSensitiveKey("auth.token")).isTrue();
        assertThat(maskingService.isSensitiveKey("private_key")).isTrue();
        assertThat(maskingService.isSensitiveKey("app.name")).isFalse();
        assertThat(maskingService.isSensitiveKey(null)).isFalse();
    }

    @Test
    void shouldTrackMaskedValueCount() {
        assertThat(maskingService.getMaskedValueCount()).isZero();

        maskingService.registerSecrets(Map.of("key1", "val1", "key2", "val2"));

        assertThat(maskingService.getMaskedValueCount()).isEqualTo(2);
    }

    @Test
    void shouldClearAllRegisteredSecrets() {
        maskingService.registerSecrets(Map.of("secret.key", "topsecret"));
        maskingService.clear();

        assertThat(maskingService.getMaskedValueCount()).isZero();
        assertThat(maskingService.mask("value=topsecret here")).contains("topsecret");
    }

    @Test
    void shouldIgnoreBlankSecretValues() {
        maskingService.registerSecrets(Map.of("empty.secret", "", "blank.secret", "  "));

        assertThat(maskingService.getMaskedValueCount()).isZero();
    }

    @Test
    void shouldHandleNullSecretsMap() {
        maskingService.registerSecrets(null);

        assertThat(maskingService.getMaskedValueCount()).isZero();
    }
}
