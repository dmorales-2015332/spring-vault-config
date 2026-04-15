package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretTaggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretTaggingAutoConfiguration.class))
            .withBean(VaultOperations.class, () -> mock(VaultOperations.class))
            .withBean(VaultConfigProperties.class, () -> {
                VaultConfigProperties p = mock(VaultConfigProperties.class);
                org.mockito.Mockito.when(p.getBackend()).thenReturn("secret");
                return p;
            });

    @Test
    void beanRegistered_whenTaggingEnabled() {
        contextRunner
                .withPropertyValues("spring.vault.tagging.enabled=true")
                .run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretTaggingService.class));
    }

    @Test
    void beanNotRegistered_whenTaggingDisabled() {
        contextRunner
                .withPropertyValues("spring.vault.tagging.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretTaggingService.class));
    }

    @Test
    void beanNotRegistered_whenPropertyAbsent() {
        contextRunner
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretTaggingService.class));
    }

    @Test
    void customBeanNotOverridden_whenUserDefinesOwn() {
        VaultSecretTaggingService custom = mock(VaultSecretTaggingService.class);
        contextRunner
                .withPropertyValues("spring.vault.tagging.enabled=true")
                .withBean(VaultSecretTaggingService.class, () -> custom)
                .run(ctx -> assertThat(ctx.getBean(VaultSecretTaggingService.class)).isSameAs(custom));
    }
}
