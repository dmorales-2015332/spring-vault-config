package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that resolves placeholder expressions in strings using Vault secrets.
 * Supports ${vault:path/to/secret#key} syntax for inline secret resolution.
 */
public class VaultSecretResolverService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretResolverService.class);
    private static final Pattern VAULT_PLACEHOLDER = Pattern.compile("\\$\\{vault:([^#}]+)(?:#([^}]+))?\\}");

    private final VaultSecretLoader secretLoader;
    private final Map<String, Map<String, String>> resolvedCache = new HashMap<>();

    public VaultSecretResolverService(VaultSecretLoader secretLoader) {
        this.secretLoader = secretLoader;
    }

    /**
     * Resolves all Vault placeholders in the given input string.
     *
     * @param input the string potentially containing ${vault:path#key} expressions
     * @return the string with all Vault placeholders replaced by their secret values
     */
    public String resolve(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        Matcher matcher = VAULT_PLACEHOLDER.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String path = matcher.group(1);
            String key = matcher.group(2);
            String replacement = resolveSecret(path, key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Resolves a single secret by path and optional key.
     *
     * @param path the Vault secret path
     * @param key  the key within the secret, or null to return the whole map as string
     * @return the resolved secret value, or empty string if not found
     */
    public String resolveSecret(String path, String key) {
        try {
            Map<String, String> secrets = resolvedCache.computeIfAbsent(path, secretLoader::loadSecrets);
            if (key != null) {
                return secrets.getOrDefault(key, "");
            }
            return secrets.toString();
        } catch (Exception e) {
            logger.warn("Failed to resolve Vault secret at path '{}' key '{}': {}", path, key, e.getMessage());
            return "";
        }
    }

    /**
     * Clears the internal resolution cache, forcing fresh lookups on next resolve call.
     */
    public void clearCache() {
        resolvedCache.clear();
        logger.debug("VaultSecretResolverService cache cleared");
    }
}
