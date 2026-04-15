package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link EnvironmentPostProcessor} that loads secrets from HashiCorp Vault
 * and injects them into the Spring {@link ConfigurableEnvironment} before
 * the application context is refreshed.
 */
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(VaultEnvironmentPostProcessor.class);
    private static final String VAULT_PROPERTY_SOURCE_NAME = "vaultSecrets";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        VaultConfigProperties props = new VaultConfigProperties();

        String uri = environment.getProperty("vault.uri", "http://127.0.0.1:8200");
        String token = environment.getProperty("vault.token", "");
        String secretPath = environment.getProperty("vault.secret-path", "");
        boolean enabled = Boolean.parseBoolean(environment.getProperty("vault.enabled", "true"));

        props.setUri(uri);
        props.setToken(token);
        props.setSecretPath(secretPath);
        props.setEnabled(enabled);

        if (!props.isEnabled()) {
            log.info("spring-vault-config is disabled, skipping Vault secret loading.");
            return;
        }

        if (secretPath == null || secretPath.isBlank()) {
            log.warn("vault.secret-path is not configured, skipping Vault secret loading.");
            return;
        }

        try {
            VaultEndpoint endpoint = VaultEndpoint.from(URI.create(uri));
            VaultTemplate vaultTemplate = new VaultTemplate(endpoint, new TokenAuthentication(token));
            VaultSecretLoader loader = new VaultSecretLoader(vaultTemplate);

            Map<String, Object> secrets = loader.loadSecrets(secretPath);
            if (secrets == null || secrets.isEmpty()) {
                log.warn("No secrets found at Vault path: {}", secretPath);
                return;
            }

            Map<String, Object> flatSecrets = new HashMap<>();
            secrets.forEach((k, v) -> flatSecrets.put(k, v));

            MutablePropertySources propertySources = environment.getPropertySources();
            propertySources.addFirst(new MapPropertySource(VAULT_PROPERTY_SOURCE_NAME, flatSecrets));
            log.info("Loaded {} secret(s) from Vault path '{}' into environment.", flatSecrets.size(), secretPath);
        } catch (Exception ex) {
            throw new VaultSecretLoadException(
                    "Failed to load secrets from Vault at path '" + secretPath + "': " + ex.getMessage(), ex);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
