package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultSecretComplianceServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretComplianceProperties properties;
    private VaultSecretComplianceService service;

    @BeforeEach
    void setUp() {
        properties = new VaultSecretComplianceProperties();
        service = new VaultSecretComplianceService(vaultTemplate, properties);
    }

    @Test
    void shouldReturnViolationWhenPathNotFound() {
        when(vaultTemplate.read("secret/missing")).thenReturn(null);
        List<String> violations = service.validate("secret/missing");
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("not found");
    }

    @Test
    void shouldReturnViolationForMissingRequiredKey() {
        properties.setRequiredKeys(List.of("password"));
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("username", "admin"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        List<String> violations = service.validate("secret/app");
        assertThat(violations).anyMatch(v -> v.contains("password"));
    }

    @Test
    void shouldReturnViolationWhenPatternDoesNotMatch() {
        properties.setKeyPatterns(Map.of("username", "[a-z]{3,10}"));
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("username", "ADMIN123"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        List<String> violations = service.validate("secret/app");
        assertThat(violations).anyMatch(v -> v.contains("username") && v.contains("pattern"));
    }

    @Test
    void shouldReturnViolationWhenValueTooShort() {
        properties.setMinValueLength(8);
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("token", "abc"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        List<String> violations = service.validate("secret/app");
        assertThat(violations).anyMatch(v -> v.contains("token") && v.contains("minimum length"));
    }

    @Test
    void shouldReturnEmptyViolationsForCompliantSecret() {
        properties.setRequiredKeys(List.of("password"));
        properties.setMinValueLength(4);
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("password", "s3cr3t!"));
        when(vaultTemplate.read("secret/app")).thenReturn(response);

        List<String> violations = service.validate("secret/app");
        assertThat(violations).isEmpty();
    }
}
