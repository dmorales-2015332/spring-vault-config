package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that filters Vault secrets based on configurable include/exclude path patterns.
 * Supports Ant-style wildcard patterns (e.g., "secret/app/*", "secret/db/**").
 */
public class VaultSecretFilterService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretFilterService.class);

    private final List<String> includePatterns;
    private final List<String> excludePatterns;
    private final AntPathMatcher pathMatcher;

    public VaultSecretFilterService(List<String> includePatterns, List<String> excludePatterns) {
        this.includePatterns = includePatterns != null ? includePatterns : Collections.emptyList();
        this.excludePatterns = excludePatterns != null ? excludePatterns : Collections.emptyList();
        this.pathMatcher = new AntPathMatcher();
    }

    /**
     * Filters the provided secrets map, retaining only entries whose keys match
     * the configured include patterns and do not match any exclude patterns.
     *
     * @param secrets the raw secrets map from Vault
     * @return filtered map of secrets
     */
    public Map<String, Object> filter(Map<String, Object> secrets) {
        if (secrets == null || secrets.isEmpty()) {
            return Collections.emptyMap();
        }

        return secrets.entrySet().stream()
                .filter(entry -> isIncluded(entry.getKey()))
                .filter(entry -> !isExcluded(entry.getKey()))
                .peek(entry -> logger.debug("Secret key '{}' passed filter", entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns true if the key matches at least one include pattern, or if no include patterns are defined.
     */
    public boolean isIncluded(String key) {
        if (includePatterns.isEmpty()) {
            return true;
        }
        return includePatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, key));
    }

    /**
     * Returns true if the key matches at least one exclude pattern.
     */
    public boolean isExcluded(String key) {
        return excludePatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, key));
    }

    public List<String> getIncludePatterns() {
        return Collections.unmodifiableList(includePatterns);
    }

    public List<String> getExcludePatterns() {
        return Collections.unmodifiableList(excludePatterns);
    }
}
