package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaultSecretTransformServiceTest {

    private VaultConfigProperties properties;
    private VaultSecretTransformService service;

    @BeforeEach
    void setUp() {
        properties = mock(VaultConfigProperties.class);
        when(properties.isTransformTrimWhitespace()).thenReturn(false);
        service = new VaultSecretTransformService(properties);
    }

    @Test
    void transformReturnsEmptyMapWhenSecretsAreNull() {
        Map<String, String> result = service.transform(null);
        assertThat(result).isEmpty();
    }

    @Test
    void transformReturnsEmptyMapWhenSecretsAreEmpty() {
        Map<String, String> result = service.transform(new HashMap<>());
        assertThat(result).isEmpty();
    }

    @Test
    void transformAppliesRegisteredKeyTransformer() {
        service.registerTransformer("db.password", String::toUpperCase);
        Map<String, String> secrets = Map.of("db.password", "secret", "db.user", "admin");

        Map<String, String> result = service.transform(secrets);

        assertThat(result.get("db.password")).isEqualTo("SECRET");
        assertThat(result.get("db.user")).isEqualTo("admin");
    }

    @Test
    void transformAppliesGlobalTrimWhenEnabled() {
        when(properties.isTransformTrimWhitespace()).thenReturn(true);
        service = new VaultSecretTransformService(properties);
        Map<String, String> secrets = Map.of("api.key", "  mykey  ");

        Map<String, String> result = service.transform(secrets);

        assertThat(result.get("api.key")).isEqualTo("mykey");
    }

    @Test
    void transformDoesNotMutateOriginalMap() {
        service.registerTransformer("token", v -> "REDACTED");
        Map<String, String> original = new HashMap<>();
        original.put("token", "abc123");

        service.transform(original);

        assertThat(original.get("token")).isEqualTo("abc123");
    }

    @Test
    void registerTransformerThrowsOnNullKey() {
        assertThatThrownBy(() -> service.registerTransformer(null, String::trim))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerTransformerThrowsOnNullFunction() {
        assertThatThrownBy(() -> service.registerTransformer("some.key", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removeTransformerDecrementsCount() {
        service.registerTransformer("key1", String::trim);
        int before = service.getTransformerCount();
        service.removeTransformer("key1");
        assertThat(service.getTransformerCount()).isEqualTo(before - 1);
    }
}
