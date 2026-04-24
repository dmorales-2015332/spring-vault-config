package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretDependencyAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretDependencyAutoConfiguration.class));

    @Test
    void beanRegistered_whenEnabledByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultSecretDependencyService.class));
    }

    @Test
    void beanNotRegistered_whenDisabled() {
        contextRunner
                .withPropertyValues("vault.secret-dependency.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretDependencyService.class));
    }

    @Test
    void customBeanTakesPrecedence() {
        contextRunner
                .withBean(VaultSecretDependencyService.class, VaultSecretDependencyService::new)
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultSecretDependencyService.class));
    }
}
