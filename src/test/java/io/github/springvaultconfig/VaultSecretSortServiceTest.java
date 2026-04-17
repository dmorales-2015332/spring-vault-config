package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretSortServiceTest {

    private VaultSecretSortService sortService;

    @BeforeEach
    void setUp() {
        sortService = new VaultSecretSortService();
    }

    private Map<String, String> sampleSecrets() {
        Map<String, String> secrets = new LinkedHashMap<>();
        secrets.put("zebra", "alpha");
        secrets.put("apple", "mango");
        secrets.put("mango", "zebra");
        return secrets;
    }

    @Test
    void sortByKey_ascending_returnsKeysInOrder() {
        Map<String, String> result = sortService.sortByKey(sampleSecrets(), VaultSecretSortService.SortOrder.ASC);
        assertThat(new ArrayList<>(result.keySet())).containsExactly("apple", "mango", "zebra");
    }

    @Test
    void sortByKey_descending_returnsKeysInReverseOrder() {
        Map<String, String> result = sortService.sortByKey(sampleSecrets(), VaultSecretSortService.SortOrder.DESC);
        assertThat(new ArrayList<>(result.keySet())).containsExactly("zebra", "mango", "apple");
    }

    @Test
    void sortByValue_ascending_returnsByValueOrder() {
        Map<String, String> result = sortService.sortByValue(sampleSecrets(), VaultSecretSortService.SortOrder.ASC);
        assertThat(new ArrayList<>(result.values())).containsExactly("alpha", "mango", "zebra");
    }

    @Test
    void sortByValue_descending_returnsByValueReverseOrder() {
        Map<String, String> result = sortService.sortByValue(sampleSecrets(), VaultSecretSortService.SortOrder.DESC);
        assertThat(new ArrayList<>(result.values())).containsExactly("zebra", "mango", "alpha");
    }

    @Test
    void sortedKeys_ascending_returnsKeyList() {
        List<String> keys = sortService.sortedKeys(sampleSecrets(), VaultSecretSortService.SortOrder.ASC);
        assertThat(keys).containsExactly("apple", "mango", "zebra");
    }

    @Test
    void sortedKeys_descending_returnsReverseKeyList() {
        List<String> keys = sortService.sortedKeys(sampleSecrets(), VaultSecretSortService.SortOrder.DESC);
        assertThat(keys).containsExactly("zebra", "mango", "apple");
    }

    @Test
    void sortByKey_emptyMap_returnsEmpty() {
        Map<String, String> result = sortService.sortByKey(Collections.emptyMap(), VaultSecretSortService.SortOrder.ASC);
        assertThat(result).isEmpty();
    }

    @Test
    void sortByKey_nullMap_returnsEmpty() {
        Map<String, String> result = sortService.sortByKey(null, VaultSecretSortService.SortOrder.ASC);
        assertThat(result).isEmpty();
    }
}
