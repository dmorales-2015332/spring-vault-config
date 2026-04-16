package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretFilterServiceTest {

    private VaultSecretFilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new VaultSecretFilterService(
                List.of("db/*", "app/**"),
                List.of("app/internal/*")
        );
    }

    @Test
    void filterRetainsMatchingIncludedKeys() {
        Map<String, Object> secrets = Map.of(
                "db/password", "secret123",
                "app/api-key", "key456",
                "other/token", "ignored"
        );

        Map<String, Object> result = filterService.filter(secrets);

        assertThat(result).containsKey("db/password");
        assertThat(result).containsKey("app/api-key");
        assertThat(result).doesNotContainKey("other/token");
    }

    @Test
    void filterExcludesMatchingExcludedKeys() {
        Map<String, Object> secrets = Map.of(
                "app/api-key", "key456",
                "app/internal/secret", "hidden"
        );

        Map<String, Object> result = filterService.filter(secrets);

        assertThat(result).containsKey("app/api-key");
        assertThat(result).doesNotContainKey("app/internal/secret");
    }

    @Test
    void filterWithNoPatternIncludesAll() {
        VaultSecretFilterService noPatternService = new VaultSecretFilterService(null, null);
        Map<String, Object> secrets = Map.of("any/key", "value", "another/key", "value2");

        Map<String, Object> result = noPatternService.filter(secrets);

        assertThat(result).hasSize(2);
    }

    @Test
    void filterReturnsEmptyMapForNullInput() {
        Map<String, Object> result = filterService.filter(null);
        assertThat(result).isEmpty();
    }

    @Test
    void filterReturnsEmptyMapForEmptyInput() {
        Map<String, Object> result = filterService.filter(Map.of());
        assertThat(result).isEmpty();
    }

    @Test
    void isIncludedReturnsTrueWhenNoIncludePatternsConfigured() {
        VaultSecretFilterService service = new VaultSecretFilterService(null, List.of());
        assertThat(service.isIncluded("any/path")).isTrue();
    }

    @Test
    void isExcludedReturnsFalseWhenNoExcludePatternsConfigured() {
        VaultSecretFilterService service = new VaultSecretFilterService(List.of(), null);
        assertThat(service.isExcluded("any/path")).isFalse();
    }

    @Test
    void getIncludeAndExcludePatternsAreUnmodifiable() {
        assertThat(filterService.getIncludePatterns()).containsExactly("db/*", "app/**");
        assertThat(filterService.getExcludePatterns()).containsExactly("app/internal/*");
    }
}
