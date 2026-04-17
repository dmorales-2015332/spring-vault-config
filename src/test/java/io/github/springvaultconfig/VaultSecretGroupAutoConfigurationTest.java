package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.vault.core.VaultTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VaultSecretGroupAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultSecretGroupAutoConfiguration.class))
            .withBean(VaultTemplate.class, () -> mock(VaultTemplate.class));

    @Test
    void beanIsRegisteredByDefault() {
        contextRunner.run(ctx -> assertThat(ctx).hasSingleBean(VaultSecretGroupService.class));
    }

    @Test
    void beanIsAbsentWhenDisabled() {
        contextRunner
                .withPropertyValues("vault.secret-groups.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(VaultSecretGroupService.class));
    }

    @Test
    void groupsAreLoadedOnStartup() {
        org.springframework.vault.support.VaultResponse response = new org.springframework.vault.support.VaultResponse();
        response.setData(Map.of("db.url", "jdbc:h2:mem"));

        VaultTemplate mockTemplate = mock(VaultTemplate.class);
        when(mockTemplate.read("secret/db")).thenReturn(response);

        contextRunner
                .withBean(VaultTemplate.class, () -> mockTemplate)
                .withPropertyValues("vault.secret-groups.groups.database=secret/db")
                .run(ctx -> {
                    VaultSecretGroupService svc = ctx.getBean(VaultSecretGroupService.class);
                    Optional<java.util.Map<String, Object>> group = svc.getGroup("database");
                    assertThat(group).isPresent();
                    assertThat(group.get()).containsEntry("db.url", "jdbc:h2:mem");
                });
    }
}
