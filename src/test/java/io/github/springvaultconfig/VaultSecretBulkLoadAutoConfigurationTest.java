package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretBulkLoadAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretBulkLoadAutoConfiguration.class))
            .withBean(VaultTemplate.class, () -> mock(VaultTemplate.class))
            .withBean(VaultConfigProperties.class, VaultConfigProperties::new);

    @Test
    void beanRegistered_whenEnabledByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultSecretBulkLoadService.class));
    }

    @Test
    void beanNotRegistered_whenDisabled() {
        contextRunner
                .withPropertyValues("vault.bulk-load.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretBulkLoadService.class));
    }

    @Test
    void customBeanNotOverridden() {
        contextRunner
                .withBean(VaultSecretBulkLoadService.class,
                        () -> new VaultSecretBulkLoadService(
                                mock(VaultTemplate.class), new VaultConfigProperties()))
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultSecretBulkLoadService.class));
    }
}
