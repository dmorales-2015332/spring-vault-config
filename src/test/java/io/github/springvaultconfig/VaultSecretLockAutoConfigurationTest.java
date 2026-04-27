package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretLockAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretLockAutoConfiguration.class))
            .withBean(VaultOperations.class, () -> mock(VaultOperations.class));

    @Test
    void shouldRegisterLockServiceByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultSecretLockService.class));
    }

    @Test
    void shouldNotRegisterLockService_whenDisabled() {
        contextRunner
                .withPropertyValues("vault.secret.lock.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretLockService.class));
    }

    @Test
    void shouldNotRegisterLockService_whenVaultOperationsMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(VaultSecretLockAutoConfiguration.class))
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretLockService.class));
    }

    @Test
    void shouldRespectUserDefinedBean() {
        VaultSecretLockService customBean = mock(VaultSecretLockService.class);
        contextRunner
                .withBean(VaultSecretLockService.class, () -> customBean)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(VaultSecretLockService.class);
                    assertThat(ctx.getBean(VaultSecretLockService.class)).isSameAs(customBean);
                });
    }
}
