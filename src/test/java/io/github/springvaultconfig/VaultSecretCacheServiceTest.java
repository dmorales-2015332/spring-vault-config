package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VaultSecretCacheServiceTest {

    private VaultSecretCacheService cacheService;
    private VaultConfigProperties properties;

    @BeforeEach
    void setUp() {
        properties = mock(VaultConfigProperties.class);
        when(properties.getCacheTtlSeconds()).thenReturn(60L);
        cacheService = new VaultSecretCacheService(properties);
    }

    @Test
    void shouldReturnEmptyWhenPathNotCached() {
        Optional<Map<String, String>> result = cacheService.get("secret/missing");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnCachedSecretsWithinTtl() {
        Map<String, String> secrets = Map.of("key", "value");
        cacheService.put("secret/app", secrets);

        Optional<Map<String, String>> result = cacheService.get("secret/app");
        assertThat(result).isPresent();
        assertThat(result.get()).containsEntry("key", "value");
    }

    @Test
    void shouldInvalidateSinglePath() {
        cacheService.put("secret/app", Map.of("k", "v"));
        cacheService.invalidate("secret/app");

        assertThat(cacheService.get("secret/app")).isEmpty();
    }

    @Test
    void shouldInvalidateAll() {
        cacheService.put("secret/app1", Map.of("a", "1"));
        cacheService.put("secret/app2", Map.of("b", "2"));
        cacheService.invalidateAll();

        assertThat(cacheService.size()).isZero();
    }

    @Test
    void shouldExpireCacheAfterTtl() {
        when(properties.getCacheTtlSeconds()).thenReturn(0L);
        VaultSecretCacheService shortTtlCache = new VaultSecretCacheService(properties);
        shortTtlCache.put("secret/app", Map.of("key", "val"));

        Optional<Map<String, String>> result = shortTtlCache.get("secret/app");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldTrackCacheSize() {
        cacheService.put("secret/a", Map.of("x", "1"));
        cacheService.put("secret/b", Map.of("y", "2"));
        assertThat(cacheService.size()).isEqualTo(2);
    }
}
