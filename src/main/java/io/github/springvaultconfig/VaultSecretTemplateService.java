package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for resolving secret placeholder templates of the form ${vault:path#key}
 * within arbitrary strings, enabling inline secret injection into configuration values.
 */
@Service
public class VaultSecretTemplateService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretTemplateService.class);
    private static final Pattern VAULT_PLACEHOLDER = Pattern.compile("\\$\\{vault:([^#}]+)#([^}]+)\\}");

    private final VaultTemplate vaultTemplate;
    private final Map<String, Map<String, Object>> cache = new HashMap<>();

    public VaultSecretTemplateService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Resolves all ${vault:path#key} placeholders in the given template string.
     *
     * @param template the string potentially containing vault placeholders
     * @return the string with all placeholders replaced by their secret values
     * @throws VaultSecretLoadException if a referenced secret cannot be resolved
     */
    public String resolve(String template) {
        if (template == null || !template.contains("${vault:")) {
            return template;
        }
        Matcher matcher = VAULT_PLACEHOLDER.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String path = matcher.group(1);
            String key = matcher.group(2);
            String value = fetchSecret(path, key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Clears the internal secret cache, forcing re-fetch on next resolution.
     */
    public void clearCache() {
        cache.clear();
        log.debug("VaultSecretTemplateService cache cleared");
    }

    private String fetchSecret(String path, String key) {
        Map<String, Object> secrets = cache.computeIfAbsent(path, this::loadSecrets);
        Object value = secrets.get(key);
        if (value == null) {
            throw new VaultSecretLoadException(
                    "No secret found at path '" + path + "' for key '" + key + "'");
        }
        log.debug("Resolved vault placeholder for path='{}' key='{}'", path, key);
        return value.toString();
    }

    private Map<String, Object> loadSecrets(String path) {
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            throw new VaultSecretLoadException("No data returned from Vault at path: " + path);
        }
        return response.getData();
    }
}
