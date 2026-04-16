package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretDiffServiceTest {

    private VaultSecretDiffService diffService;

    @BeforeEach
    void setUp() {
        VaultSecretMaskingService maskingService = Mockito.mock(VaultSecretMaskingService.class);
        diffService = new VaultSecretDiffService(maskingService);
    }

    @Test
    void detectsAddedKeys() {
        var previous = Map.of("key1", "val1");
        var current = Map.of("key1", "val1", "key2", "val2");
        var result = diffService.diff(previous, current);
        assertThat(result.added()).containsExactly("key2");
        assertThat(result.removed()).isEmpty();
        assertThat(result.changed()).isEmpty();
        assertThat(result.hasChanges()).isTrue();
    }

    @Test
    void detectsRemovedKeys() {
        var previous = Map.of("key1", "val1", "key2", "val2");
        var current = Map.of("key1", "val1");
        var result = diffService.diff(previous, current);
        assertThat(result.removed()).containsExactly("key2");
        assertThat(result.added()).isEmpty();
        assertThat(result.changed()).isEmpty();
    }

    @Test
    void detectsChangedKeys() {
        var previous = Map.of("key1", "oldVal");
        var current = Map.of("key1", "newVal");
        var result = diffService.diff(previous, current);
        assertThat(result.changed()).containsExactly("key1");
        assertThat(result.added()).isEmpty();
        assertThat(result.removed()).isEmpty();
        assertThat(result.hasChanges()).isTrue();
    }

    @Test
    void noChangesWhenIdentical() {
        var secrets = Map.of("key1", "val1", "key2", "val2");
        var result = diffService.diff(secrets, secrets);
        assertThat(result.hasChanges()).isFalse();
    }

    @Test
    void handlesEmptyPrevious() {
        var current = Map.of("key1", "val1");
        var result = diffService.diff(Map.of(), current);
        assertThat(result.added()).containsExactly("key1");
        assertThat(result.hasChanges()).isTrue();
    }
}
