package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretPaginationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretPaginationAutoConfiguration.class))
            .withBean(VaultOperations.class, () -> mock(VaultOperations.class));

    @Test
    void beanRegistered_whenEnabled() {
        contextRunner
                .withPropertyValues("vault.pagination.enabled=true")
                .run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretPaginationService.class));
    }

    @Test
    void beanRegistered_byDefault() {
        contextRunner.run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretPaginationService.class));
    }

    @Test
    void beanNotRegistered_whenDisabled() {
        contextRunner
                .withPropertyValues("vault.pagination.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretPaginationService.class));
    }

    @Test
    void customBeanNotOverridden() {
        contextRunner
                .withBean(VaultSecretPaginationService.class,
                        () -> new VaultSecretPaginationService(mock(VaultOperations.class)))
                .run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretPaginationService.class));
    }
}
