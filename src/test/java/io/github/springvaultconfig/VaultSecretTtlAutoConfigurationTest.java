package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretTtlAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretTtlAutoConfiguration.class));

    @Test
    void shouldRegisterTtlServiceWhenVaultOperationsPresent() {
        contextRunner
                .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
                .run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretTtlService.class));
    }

    @Test
    void shouldNotRegisterTtlServiceWhenVaultOperationsMissing() {
        contextRunner
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretTtlService.class));
    }

    @Test
    void shouldNotRegisterTtlServiceWhenDisabledViaProperty() {
        contextRunner
                .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
                .withPropertyValues("vault.ttl.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretTtlService.class));
    }

    @Test
    void shouldRespectUserDefinedTtlServiceBean() {
        VaultSecretTtlService customBean = new VaultSecretTtlService(mock(VaultOperations.class));
        contextRunner
                .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
                .withBean(VaultSecretTtlService.class, () -> customBean)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(VaultSecretTtlService.class);
                    assertThat(ctx.getBean(VaultSecretTtlService.class)).isSameAs(customBean);
                });
    }
}
