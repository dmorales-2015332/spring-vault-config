package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretAclAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretAclAutoConfiguration.class));

    @Test
    void shouldRegisterBeanWhenVaultOperationsPresent() {
        contextRunner
                .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
                .run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretAclService.class));
    }

    @Test
    void shouldNotRegisterBeanWhenDisabled() {
        contextRunner
                .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
                .withPropertyValues("vault.acl.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretAclService.class));
    }

    @Test
    void shouldNotRegisterBeanWhenVaultOperationsMissing() {
        contextRunner
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretAclService.class));
    }

    @Test
    void shouldRespectCustomBean() {
        contextRunner
                .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
                .withBean(VaultSecretAclService.class, () -> new VaultSecretAclService(mock(VaultOperations.class)))
                .run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretAclService.class));
    }
}
