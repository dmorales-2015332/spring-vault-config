package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class VaultSecretFallbackServiceTest {

    @Mock
    private VaultConfigProperties properties;

    private VaultSecretFallbackService fallbackService;

    @BeforeEach
    void setUp() {
        fallbackService = new VaultSecretFallbackService(properties);
    }

    @Test
    void registerAndRetrieveFallback() {
        fallbackService.registerFallback("db.password", "fallback-pass");
        Optional<String> result = fallbackService.getFallback("db.password");
        assertThat(result).isPresent().contains("fallback-pass");
    }

    @Test
    void getFallback_returnsEmpty_whenNotRegistered() {
        Optional<String> result = fallbackService.getFallback("missing.key");
        assertThat(result).isEmpty();
    }

    @Test
    void registerFallback_throwsException_forBlankKey() {
        assertThatThrownBy(() -> fallbackService.registerFallback(" ", "value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fallback key must not be null or blank");
    }

    @Test
    void registerFallback_throwsException_forNullKey() {
        assertThatThrownBy(() -> fallbackService.registerFallback(null, "value"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hasFallback_returnsTrueWhenRegistered() {
        fallbackService.registerFallback("api.key", "secret");
        assertThat(fallbackService.hasFallback("api.key")).isTrue();
    }

    @Test
    void hasFallback_returnsFalseWhenNotRegistered() {
        assertThat(fallbackService.hasFallback("nonexistent")).isFalse();
    }

    @Test
    void removeFallback_removesRegisteredKey() {
        fallbackService.registerFallback("token", "abc123");
        fallbackService.removeFallback("token");
        assertThat(fallbackService.hasFallback("token")).isFalse();
    }

    @Test
    void getAllFallbacks_returnsAllRegistered() {
        fallbackService.registerFallback("key1", "val1");
        fallbackService.registerFallback("key2", "val2");
        assertThat(fallbackService.getAllFallbacks()).containsKeys("key1", "key2");
    }

    @Test
    void clearFallbacks_removesAll() {
        fallbackService.registerFallback("key1", "val1");
        fallbackService.registerFallback("key2", "val2");
        fallbackService.clearFallbacks();
        assertThat(fallbackService.getAllFallbacks()).isEmpty();
    }
}
