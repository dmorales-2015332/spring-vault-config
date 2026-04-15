package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service responsible for masking sensitive secret values in logs and output.
 * Prevents accidental exposure of secrets in application logs.
 */
@Service
public class VaultSecretMaskingService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretMaskingService.class);
    private static final String MASK = "***MASKED***";
    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(?i).*(password|secret|token|key|credential|api[_-]?key|private).*"
    );

    private final Set<String> maskedValues = new HashSet<>();
    private final Set<String> sensitiveKeys = new HashSet<>();

    /**
     * Registers secret values that should be masked in any string output.
     *
     * @param secrets map of secret key-value pairs to register for masking
     */
    public void registerSecrets(Map<String, String> secrets) {
        if (secrets == null || secrets.isEmpty()) {
            return;
        }
        secrets.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                maskedValues.add(value);
                sensitiveKeys.add(key);
                log.debug("Registered secret key '{}' for masking", key);
            }
        });
    }

    /**
     * Masks all registered secret values found within the given input string.
     *
     * @param input the string potentially containing secret values
     * @return the input string with all secret values replaced by the mask
     */
    public String mask(String input) {
        if (input == null || input.isBlank() || maskedValues.isEmpty()) {
            return input;
        }
        String result = input;
        for (String secret : maskedValues) {
            result = result.replace(secret, MASK);
        }
        return result;
    }

    /**
     * Determines whether a given key name is considered sensitive based on naming patterns.
     *
     * @param key the property or secret key name
     * @return true if the key matches a sensitive naming pattern
     */
    public boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        return sensitiveKeys.contains(key) || SENSITIVE_KEY_PATTERN.matcher(key).matches();
    }

    /**
     * Returns the number of registered masked values.
     *
     * @return count of registered secrets
     */
    public int getMaskedValueCount() {
        return maskedValues.size();
    }

    /**
     * Clears all registered secrets and sensitive keys.
     */
    public void clear() {
        maskedValues.clear();
        sensitiveKeys.clear();
        log.debug("Cleared all registered masked secrets");
    }
}
