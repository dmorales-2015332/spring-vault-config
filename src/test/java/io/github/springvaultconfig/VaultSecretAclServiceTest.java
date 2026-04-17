package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.vault.core.VaultOperations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretAclServiceTest {

    private VaultSecretAclService aclService;

    @BeforeEach
    void setUp() {
        VaultOperations vaultOperations = Mockito.mock(VaultOperations.class);
        aclService = new VaultSecretAclService(vaultOperations);
    }

    @Test
    void shouldAllowAccessWhenNoAclDefined() {
        assertThat(aclService.isAccessAllowed("secret/app/db", "admin")).isTrue();
    }

    @Test
    void shouldGrantAndAllowAccess() {
        aclService.grantAccess("secret/app/db", "admin");
        assertThat(aclService.isAccessAllowed("secret/app/db", "admin")).isTrue();
    }

    @Test
    void shouldDenyAccessForUnauthorizedRole() {
        aclService.grantAccess("secret/app/db", "admin");
        assertThat(aclService.isAccessAllowed("secret/app/db", "guest")).isFalse();
    }

    @Test
    void shouldRevokeAccess() {
        aclService.grantAccess("secret/app/db", "admin");
        aclService.revokeAccess("secret/app/db", "admin");
        // After revoke, set is empty -> open by default
        assertThat(aclService.isAccessAllowed("secret/app/db", "admin")).isTrue();
    }

    @Test
    void shouldReturnAllowedRoles() {
        aclService.grantAccess("secret/app/db", "admin");
        aclService.grantAccess("secret/app/db", "developer");
        Set<String> roles = aclService.getAllowedRoles("secret/app/db");
        assertThat(roles).containsExactlyInAnyOrder("admin", "developer");
    }

    @Test
    void shouldClearAcl() {
        aclService.grantAccess("secret/app/db", "admin");
        aclService.clearAcl("secret/app/db");
        assertThat(aclService.getAllowedRoles("secret/app/db")).isEmpty();
        assertThat(aclService.getAllAcls()).doesNotContainKey("secret/app/db");
    }

    @Test
    void shouldReturnAllAcls() {
        aclService.grantAccess("secret/app/db", "admin");
        aclService.grantAccess("secret/app/cache", "service");
        assertThat(aclService.getAllAcls()).hasSize(2);
    }
}
