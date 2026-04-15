package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretEncryptionAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretEncryptionAutoConfiguration.class))
            .withBean(VaultOperations.class, () -> mock(VaultOperations.class));

    @Test
    void beanRegistered_whenEncryptionEnabled() {
        contextRunner
                .withPropertyValues(
                        "spring.vault.encryption.enabled=true",
                        "spring.vault.uri=http://localhost:8200",
                        "spring.vault.token=test-token"
                )
                .run(context -> assertThat(context).hasSingleBean(VaultSecretEncryptionService.class));
    }

    @Test
    void beanNotRegistered_whenEncryptionDisabled() {
        contextRunner
                .withPropertyValues("spring.vault.encryption.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(VaultSecretEncryptionService.class));
    }

    @Test
    void beanNotRegistered_whenPropertyAbsent() {
        contextRunner
                .run(context -> assertThat(context).doesNotHaveBean(VaultSecretEncryptionService.class));
    }

    @Test
    void usesCustomKeyName_whenConfigured() {
        contextRunner
                .withPropertyValues(
                        "spring.vault.encryption.enabled=true",
                        "spring.vault.uri=http://localhost:8200",
                        "spring.vault.token=test-token",
                        "spring.vault.encryption-key-name=custom-key"
                )
                .run(context -> assertThat(context).hasSingleBean(VaultSecretEncryptionService.class));
    }
}
