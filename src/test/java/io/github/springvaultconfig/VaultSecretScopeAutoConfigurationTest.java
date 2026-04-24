package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretScopeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretScopeAutoConfiguration.class))
            .withBean(VaultOperations.class, () -> mock(VaultOperations.class));

    @Test
    void beanIsCreatedWhenVaultOperationsPresent() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultSecretScopeService.class));
    }

    @Test
    void beanIsNotCreatedWhenDisabled() {
        contextRunner
                .withPropertyValues("vault.scope.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretScopeService.class));
    }

    @Test
    void customBeanTakesPrecedence() {
        contextRunner
                .withBean(VaultSecretScopeService.class,
                        () -> new VaultSecretScopeService(mock(VaultOperations.class)))
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultSecretScopeService.class));
    }

    @Test
    void beanIsNotCreatedWithoutVaultOperations() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(VaultSecretScopeAutoConfiguration.class))
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretScopeService.class));
    }
}
