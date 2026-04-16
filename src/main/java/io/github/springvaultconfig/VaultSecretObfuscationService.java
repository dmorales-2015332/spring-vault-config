package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for obfuscating secret values in logs and output based on key patterns.
 */
@Service
public class VaultSecretObfuscationService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretObfuscationService.class);
    private static final String OBFUSCATED = "****";

    private final Set<Pattern> sensitivePatterns;

    public VaultSecretObfuscationService(VaultConfigProperties properties) {
        this.sensitivePatterns = properties.getObfuscation().getSensitiveKeyPatterns().stream()
                .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toSet());
    }

    public boolean isSensitive(String key) {
        if (key == null) return false;
        return sensitivePatterns.stream().anyMatch(p -> p.matcher(key).find());
    }

    public String obfuscate(String key, String value) {
        if (isSensitive(key)) {
            return OBFUSCATED;
        }
        return value;
    }

    public Map<String, String> obfuscateAll(Map<String, String> secrets) {
        return secrets.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> obfuscate(e.getKey(), e.getValue())
                ));
    }

    public void logObfuscated(String context, Map<String, String> secrets) {
        Map<String, String> safe = obfuscateAll(secrets);
        log.info("[{}] secrets: {}", context, safe);
    }
}
