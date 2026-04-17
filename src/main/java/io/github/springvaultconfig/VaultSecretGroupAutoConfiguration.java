package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

/**
 * Auto-configuration for {@link VaultSecretGroupService}.
 */
@Configuration
@EnableConfigurationProperties(VaultSecretGroupProperties.class)
@ConditionalOnProperty(prefix = "vault.secret-groups", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretGroupAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretGroupService vaultSecretGroupService(VaultTemplate vaultTemplate,
                                                           VaultSecretGroupProperties properties) {
        VaultSecretGroupService service = new VaultSecretGroupService(vaultTemplate);
        properties.getGroups().forEach((groupName, paths) -> {
            if (paths != null && !paths.isEmpty()) {
                service.loadGroup(groupName, paths);
            }
        });
        return service;
    }
}
