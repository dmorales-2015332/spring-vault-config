package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultSecretExportServiceTest {

    @Mock
    private VaultSecretLoader secretLoader;

    @Mock
    private VaultSecretMaskingService maskingService;

    private VaultSecretExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new VaultSecretExportService(secretLoader, maskingService);
    }

    @Test
    void exportAsMap_returnsStringValues() {
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("db.password", "s3cr3t"));
        Map<String, String> result = exportService.exportAsMap("secret/app");
        assertThat(result).containsEntry("db.password", "s3cr3t");
    }

    @Test
    void exportAsProperties_returnsMaskedProperties() {
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("db.password", "s3cr3t"));
        when(maskingService.mask(eq("db.password"), anyString())).thenReturn("****");
        String result = exportService.exportAsProperties("secret/app");
        assertThat(result).contains("db.password=****");
    }

    @Test
    void exportAsEnv_returnsUppercaseKeys() {
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("db.password", "s3cr3t"));
        when(maskingService.mask(eq("db.password"), anyString())).thenReturn("****");
        String result = exportService.exportAsEnv("secret/app");
        assertThat(result).contains("DB_PASSWORD=****");
    }

    @Test
    void exportAsJson_returnsJsonFormat() {
        when(secretLoader.loadSecrets("secret/app")).thenReturn(Map.of("api.key", "abc123"));
        when(maskingService.mask(eq("api.key"), anyString())).thenReturn("****");
        String result = exportService.exportAsJson("secret/app");
        assertThat(result).startsWith("{").endsWith("}");
        assertThat(result).contains("\"api.key\"");
    }

    @Test
    void exportAsMap_handlesNullValue() {
        Map<String, Object> secrets = new java.util.HashMap<>();
        secrets.put("key", null);
        when(secretLoader.loadSecrets("secret/app")).thenReturn(secrets);
        Map<String, String> result = exportService.exportAsMap("secret/app");
        assertThat(result).containsEntry("key", "");
    }
}
