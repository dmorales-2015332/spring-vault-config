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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSecretPromotionServiceTest {

    @Mock
    private VaultTemplate vaultTemplate;

    private VaultSecretPromotionService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretPromotionService(vaultTemplate);
    }

    @Test
    void promote_shouldWriteAllKeysToTarget() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "secret", "api.key", "abc123"));
        when(vaultTemplate.read("secret/staging/app")).thenReturn(response);

        int count = service.promote("secret/staging/app", "secret/prod/app");

        assertThat(count).isEqualTo(2);
        verify(vaultTemplate).write(eq("secret/prod/app"), anyMap());
    }

    @Test
    void promote_shouldThrowWhenSourceNotFound() {
        when(vaultTemplate.read("secret/staging/app")).thenReturn(null);

        assertThatThrownBy(() -> service.promote("secret/staging/app", "secret/prod/app"))
                .isInstanceOf(VaultSecretLoadException.class)
                .hasMessageContaining("secret/staging/app");
    }

    @Test
    void promote_shouldThrowWhenSourceDataIsNull() {
        VaultResponse response = new VaultResponse();
        response.setData(null);
        when(vaultTemplate.read("secret/staging/app")).thenReturn(response);

        assertThatThrownBy(() -> service.promote("secret/staging/app", "secret/prod/app"))
                .isInstanceOf(VaultSecretLoadException.class);
    }

    @Test
    void promoteKeys_shouldWriteOnlySpecifiedKeys() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "secret", "api.key", "abc123", "unused", "x"));
        when(vaultTemplate.read("secret/staging/app")).thenReturn(response);

        int count = service.promoteKeys("secret/staging/app", "secret/prod/app",
                List.of("db.password", "api.key"));

        assertThat(count).isEqualTo(2);
        verify(vaultTemplate).write(eq("secret/prod/app"),
                argThat(m -> m.containsKey("db.password") && m.containsKey("api.key")
                        && !m.containsKey("unused")));
    }

    @Test
    void promoteKeys_shouldSkipMissingKeysGracefully() {
        VaultResponse response = new VaultResponse();
        response.setData(Map.of("db.password", "secret"));
        when(vaultTemplate.read("secret/staging/app")).thenReturn(response);

        int count = service.promoteKeys("secret/staging/app", "secret/prod/app",
                List.of("db.password", "nonexistent"));

        assertThat(count).isEqualTo(1);
    }

    @Test
    void constructor_shouldRejectNullVaultTemplate() {
        assertThatThrownBy(() -> new VaultSecretPromotionService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
