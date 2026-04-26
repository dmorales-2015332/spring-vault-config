package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service responsible for sanitizing secret values before they are used in
 * logging, error messages, or other non-secure outputs.
 *
 * <p>Sanitization strips or masks sensitive patterns such as passwords, tokens,
 * private keys, and connection strings to prevent accidental secret leakage.
 */
@Service
public class VaultSecretSanitizationService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretSanitizationService.class);

    private static final String MASKED_VALUE = "[REDACTED]";

    /** Key name patterns that indicate a value is sensitive. */
    private static final Set<Pattern> SENSITIVE_KEY_PATTERNS = Set.of(
            Pattern.compile("(?i).*password.*"),
            Pattern.compile("(?i).*secret.*"),
            Pattern.compile("(?i).*token.*"),
            Pattern.compile("(?i).*apikey.*"),
            Pattern.compile("(?i).*api_key.*"),
            Pattern.compile("(?i).*private[_-]?key.*"),
            Pattern.compile("(?i).*credential.*"),
            Pattern.compile("(?i).*auth.*")
    );

    /** Value patterns that indicate raw sensitive data regardless of key name. */
    private static final Set<Pattern> SENSITIVE_VALUE_PATTERNS = Set.of(
            // Private key blocks (PEM format)
            Pattern.compile("-----BEGIN[\\s\\S]*?PRIVATE KEY-----[\\s\\S]*?-----END[\\s\\S]*?PRIVATE KEY-----"),
            // JWT tokens (three base64url segments separated by dots)
            Pattern.compile("[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}"),
            // Generic high-entropy strings resembling API keys (32+ hex chars)
            Pattern.compile("[0-9a-fA-F]{32,}")
    );

    /**
     * Sanitizes a map of secret key-value pairs, masking any values whose key
     * or value matches a sensitive pattern.
     *
     * @param secrets the raw secret map to sanitize
     * @return a new map with sensitive values replaced by {@code [REDACTED]}
     */
    public Map<String, String> sanitize(Map<String, String> secrets) {
        if (secrets == null || secrets.isEmpty()) {
            return Map.of();
        }

        Map<String, String> sanitized = new HashMap<>(secrets.size());
        for (Map.Entry<String, String> entry : secrets.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sanitized.put(key, isSensitive(key, value) ? MASKED_VALUE : value);
        }

        logger.debug("Sanitized {} secret entries ({} masked)",
                sanitized.size(),
                sanitized.values().stream().filter(MASKED_VALUE::equals).count());

        return sanitized;
    }

    /**
     * Sanitizes a single secret value by key name. Returns the original value
     * if the key is not considered sensitive.
     *
     * @param key   the secret key name
     * @param value the secret value
     * @return the original value or {@code [REDACTED]}
     */
    public String sanitizeValue(String key, String value) {
        if (key == null || value == null) {
            return value;
        }
        return isSensitive(key, value) ? MASKED_VALUE : value;
    }

    /**
     * Determines whether a given key-value pair should be treated as sensitive.
     *
     * @param key   the secret key
     * @param value the secret value
     * @return {@code true} if the pair is considered sensitive
     */
    public boolean isSensitive(String key, String value) {
        for (Pattern keyPattern : SENSITIVE_KEY_PATTERNS) {
            if (keyPattern.matcher(key).matches()) {
                return true;
            }
        }
        if (value != null) {
            for (Pattern valuePattern : SENSITIVE_VALUE_PATTERNS) {
                if (valuePattern.matcher(value).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the placeholder used to replace sensitive values.
     *
     * @return the masked value string
     */
    public String getMaskedValue() {
        return MASKED_VALUE;
    }
}
