package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VaultSecretSnapshotAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretSnapshotAutoConfiguration.class))
            .withBean(VaultTemplate.class, () -> mock(VaultTemplate.class));

    @Test
    void shouldRegisterSnapshotServiceByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultSecretSnapshotService.class));
    }

    @Test
    void shouldNotRegisterWhenDisabled() {
        contextRunner
                .withPropertyValues("vault.snapshot.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretSnapshotService.class));
    }

    @Test
    void shouldRespectUserDefinedBean() {
        contextRunner
                .withBean(VaultSecretSnapshotService.class,
                        () -> new VaultSecretSnapshotService(mock(VaultTemplate.class)))
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultSecretSnapshotService.class));
    }
}
