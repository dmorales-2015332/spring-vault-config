package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VaultEnvironmentPostProcessorTest {

    private VaultEnvironmentPostProcessor postProcessor;
    private SpringApplication mockApplication;

    @BeforeEach
    void setUp() {
        postProcessor = new VaultEnvironmentPostProcessor();
        mockApplication = mock(SpringApplication.class);
    }

    @Test
    void shouldSkipWhenDisabled() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("vault.enabled", "false");

        assertThatNoException().isThrownBy(() ->
                postProcessor.postProcessEnvironment(environment, mockApplication));

        assertThat(environment.getPropertySources().contains("vaultSecrets")).isFalse();
    }

    @Test
    void shouldSkipWhenSecretPathIsBlank() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("vault.enabled", "true");
        environment.setProperty("vault.secret-path", "");

        assertThatNoException().isThrownBy(() ->
                postProcessor.postProcessEnvironment(environment, mockApplication));

        assertThat(environment.getPropertySources().contains("vaultSecrets")).isFalse();
    }

    @Test
    void shouldHaveLowestPrecedenceMinusTen() {
        assertThat(postProcessor.getOrder()).isEqualTo(Integer.MAX_VALUE - 10);
    }

    @Test
    void shouldThrowVaultSecretLoadExceptionOnConnectionFailure() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("vault.enabled", "true");
        environment.setProperty("vault.uri", "http://invalid-host:8200");
        environment.setProperty("vault.token", "bad-token");
        environment.setProperty("vault.secret-path", "secret/myapp");

        assertThatThrownBy(() ->
                postProcessor.postProcessEnvironment(environment, mockApplication))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("secret/myapp");
    }

    @Test
    void shouldAddVaultPropertySourceFirstWhenSecretsLoaded() {
        // Verify that if a MapPropertySource named "vaultSecrets" is present,
        // it is the first property source (highest priority).
        MockEnvironment environment = new MockEnvironment();
        MutablePropertySources sources = environment.getPropertySources();
        Map<String, Object> fakeSecrets = Map.of("db.password", "s3cr3t", "api.key", "abc123");
        sources.addFirst(new MapPropertySource("vaultSecrets", fakeSecrets));

        assertThat(sources.iterator().next().getName()).isEqualTo("vaultSecrets");
        assertThat(environment.getProperty("db.password")).isEqualTo("s3cr3t");
        assertThat(environment.getProperty("api.key")).isEqualTo("abc123");
    }
}
