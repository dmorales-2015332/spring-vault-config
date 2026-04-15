package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretCacheAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretCacheAutoConfiguration.class))
            .withPropertyValues(
                    "vault.config.uri=http://localhost:8200",
                    "vault.config.token=test-token"
            );

    @Test
    void shouldRegisterCacheServiceByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultSecretCacheService.class));
    }

    @Test
    void shouldNotRegisterCacheServiceWhenDisabled() {
        contextRunner
                .withPropertyValues("vault.config.cache.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultSecretCacheService.class));
    }

    @Test
    void shouldRespectExistingBean() {
        contextRunner
                .withBean(VaultSecretCacheService.class, () -> {
                    VaultConfigProperties p = new VaultConfigProperties();
                    p.setCacheTtlSeconds(120L);
                    return new VaultSecretCacheService(p);
                })
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultSecretCacheService.class));
    }
}
