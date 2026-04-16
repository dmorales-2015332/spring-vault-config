package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service that applies configurable transformations to Vault secret values
 * before they are injected into the Spring Environment.
 */
public class VaultSecretTransformService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretTransformService.class);

    private final Map<String, Function<String, String>> keyTransformers = new HashMap<>();
    private final VaultConfigProperties properties;

    public VaultSecretTransformService(VaultConfigProperties properties) {
        this.properties = properties;
        registerDefaultTransformers();
    }

    private void registerDefaultTransformers() {
        if (properties.isTransformTrimWhitespace()) {
            registerTransformer("__global_trim__", String::trim);
        }
    }

    /**
     * Register a named transformer for a specific secret key.
     *
     * @param key       the secret key to match
     * @param transform the transformation function to apply
     */
    public void registerTransformer(String key, Function<String, String> transform) {
        if (!StringUtils.hasText(key) || transform == null) {
            throw new IllegalArgumentException("Key and transform function must not be null or empty");
        }
        keyTransformers.put(key, transform);
        logger.debug("Registered transformer for key: {}", key);
    }

    /**
     * Apply registered transformations to all secrets in the provided map.
     *
     * @param secrets the raw secret map from Vault
     * @return a new map with transformed values
     */
    public Map<String, String> transform(Map<String, String> secrets) {
        if (secrets == null || secrets.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>(secrets);
        Function<String, String> globalTrim = keyTransformers.get("__global_trim__");

        result.replaceAll((key, value) -> {
            String transformed = value;
            if (globalTrim != null && transformed != null) {
                transformed = globalTrim.apply(transformed);
            }
            Function<String, String> keyTransformer = keyTransformers.get(key);
            if (keyTransformer != null && transformed != null) {
                transformed = keyTransformer.apply(transformed);
                logger.debug("Applied transformer to key: {}", key);
            }
            return transformed;
        });
        return result;
    }

    /**
     * Remove a previously registered transformer for a given key.
     *
     * @param key the secret key whose transformer should be removed
     */
    public void removeTransformer(String key) {
        keyTransformers.remove(key);
        logger.debug("Removed transformer for key: {}", key);
    }

    public int getTransformerCount() {
        return keyTransformers.size();
    }
}
