package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VaultAuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VaultAuditAutoConfiguration.class));

    @Test
    void auditLoggerRegisteredByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(VaultAuditLogger.class)
        );
    }

    @Test
    void auditLoggerRegisteredWhenExplicitlyEnabled() {
        contextRunner
                .withPropertyValues("spring.vault.audit.enabled=true")
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultAuditLogger.class)
                );
    }

    @Test
    void auditLoggerNotRegisteredWhenDisabled() {
        contextRunner
                .withPropertyValues("spring.vault.audit.enabled=false")
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(VaultAuditLogger.class)
                );
    }

    @Test
    void customAuditLoggerBeanIsRespected() {
        contextRunner
                .withBean(VaultAuditLogger.class, VaultAuditLogger::new)
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(VaultAuditLogger.class)
                );
    }
}
