package io.github.springvaultconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.VaultOperations;

@AutoConfiguration
@ConditionalOnBean(VaultOperations.class)
@ConditionalOnProperty(name = "vault.acl.enabled", havingValue = "true", matchIfMissing = true)
public class VaultSecretAclAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public VaultSecretAclService vaultSecretAclService(VaultOperations vaultOperations) {
        return new VaultSecretAclService(vaultOperations);
    }
}
