package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretScopeServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretScopeService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretScopeService(vaultOperations);
    }

    @Test
    void registerScope_addsPathToScope() {
        service.registerScope("prod", "secret/db/password");
        assertThat(service.getPathsForScope("prod")).contains("secret/db/password");
    }

    @Test
    void getPathsForScope_returnsEmptySetForUnknownScope() {
        assertThat(service.getPathsForScope("unknown")).isEmpty();
    }

    @Test
    void listScopes_returnsAllRegisteredScopes() {
        service.registerScope("prod", "secret/db/password");
        service.registerScope("dev", "secret/dev/api-key");
        Set<String> scopes = service.listScopes();
        assertThat(scopes).containsExactlyInAnyOrder("prod", "dev");
    }

    @Test
    void readScope_returnsSecretsForAllPathsInScope() {
        service.registerScope("prod", "secret/db/password");
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("password", "s3cr3t"));
        when(vaultOperations.read("secret/db/password")).thenReturn(response);

        Map<String, Map<String, Object>> result = service.readScope("prod");

        assertThat(result).containsKey("secret/db/password");
        assertThat(result.get("secret/db/password")).containsEntry("password", "s3cr3t");
    }

    @Test
    void readScope_skipsPathsWithNullResponse() {
        service.registerScope("prod", "secret/missing");
        when(vaultOperations.read("secret/missing")).thenReturn(null);

        Map<String, Map<String, Object>> result = service.readScope("prod");

        assertThat(result).doesNotContainKey("secret/missing");
    }

    @Test
    void readScope_returnsEmptyMapForUnregisteredScope() {
        Map<String, Map<String, Object>> result = service.readScope("nonexistent");
        assertThat(result).isEmpty();
        verifyNoInteractions(vaultOperations);
    }

    @Test
    void removeScope_removesExistingScope() {
        service.registerScope("temp", "secret/temp/key");
        boolean removed = service.removeScope("temp");
        assertThat(removed).isTrue();
        assertThat(service.listScopes()).doesNotContain("temp");
    }

    @Test
    void removeScope_returnsFalseForNonExistentScope() {
        assertThat(service.removeScope("ghost")).isFalse();
    }

    @Test
    void readScope_handlesVaultExceptionGracefully() {
        service.registerScope("prod", "secret/broken");
        when(vaultOperations.read("secret/broken")).thenThrow(new RuntimeException("Vault unavailable"));

        Map<String, Map<String, Object>> result = service.readScope("prod");

        assertThat(result).doesNotContainKey("secret/broken");
    }
}
