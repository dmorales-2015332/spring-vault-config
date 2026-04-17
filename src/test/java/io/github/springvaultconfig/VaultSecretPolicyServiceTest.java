package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VaultSecretPolicyServiceTest {

    @Mock
    private VaultOperations vaultOperations;

    private VaultSecretPolicyService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VaultSecretPolicyService(vaultOperations);
    }

    @Test
    void getCurrentTokenPolicies_returnsPolicies() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("policies", Arrays.asList("default", "my-app"));
        response.setData(data);
        when(vaultOperations.read("auth/token/lookup-self")).thenReturn(response);

        List<String> policies = service.getCurrentTokenPolicies();

        assertThat(policies).containsExactly("default", "my-app");
    }

    @Test
    void getCurrentTokenPolicies_returnsEmptyOnNullResponse() {
        when(vaultOperations.read("auth/token/lookup-self")).thenReturn(null);
        assertThat(service.getCurrentTokenPolicies()).isEmpty();
    }

    @Test
    void getPolicyRules_returnsRules() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("rules", "path \"secret/*\" { capabilities = [\"read\"] }");
        response.setData(data);
        when(vaultOperations.read("sys/policy/my-app")).thenReturn(response);

        Optional<String> rules = service.getPolicyRules("my-app");

        assertThat(rules).isPresent();
        assertThat(rules.get()).contains("secret/*");
    }

    @Test
    void getPolicyRules_returnsEmptyOnException() {
        when(vaultOperations.read(anyString())).thenThrow(new RuntimeException("vault error"));
        assertThat(service.getPolicyRules("bad-policy")).isEmpty();
    }

    @Test
    void hasPolicy_trueWhenPolicyPresent() {
        VaultResponse response = new VaultResponse();
        Map<String, Object> data = new HashMap<>();
        data.put("policies", Arrays.asList("default", "my-app"));
        response.setData(data);
        when(vaultOperations.read("auth/token/lookup-self")).thenReturn(response);

        assertThat(service.hasPolicy("my-app")).isTrue();
        assertThat(service.hasPolicy("admin")).isFalse();
    }
}
