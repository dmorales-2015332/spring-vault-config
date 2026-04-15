package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretRefreshAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(VaultSecretRefreshAutoConfiguration.class)
            .withBean(VaultSecretLoader.class, () -> mock(VaultSecretLoader.class))
            .withBean(ApplicationEventPublisher.class, () -> mock(ApplicationEventPublisher.class))
            .withBean(VaultConfigProperties.class);

    @Test
    void autoConfiguration_registersRefreshServiceByDefault() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(VaultSecretRefreshService.class));
    }

    @Test
    void autoConfiguration_disabledWhenPropertyFalse() {
        contextRunner
                .withPropertyValues("spring.vault.refresh-enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(VaultSecretRefreshService.class));
    }

    @Test
    void autoConfiguration_doesNotOverrideExistingBean() {
        contextRunner
                .withBean(VaultSecretRefreshService.class,
                        () -> new VaultSecretRefreshService(
                                mock(VaultSecretLoader.class),
                                mock(ApplicationEventPublisher.class),
                                mock(VaultConfigProperties.class)))
                .run(context ->
                        assertThat(context).hasSingleBean(VaultSecretRefreshService.class));
    }
}
