package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@AutoConfiguration
@ConditionalOnProperty(prefix = "vault", name = "scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class VaultSecretSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "vaultTaskScheduler")
    public TaskScheduler vaultTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("vault-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({TaskScheduler.class, VaultSecretRefreshService.class})
    public VaultSecretSchedulerService vaultSecretSchedulerService(
            TaskScheduler vaultTaskScheduler,
            VaultSecretRefreshService vaultSecretRefreshService) {
        return new VaultSecretSchedulerService(vaultTaskScheduler, vaultSecretRefreshService);
    }
}
