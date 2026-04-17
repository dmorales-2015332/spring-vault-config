package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for sorting and ordering Vault secrets by key or value.
 */
@Service
public class VaultSecretSortService {

    private static final Logger logger = LoggerFactory.getLogger(VaultSecretSortService.class);

    public enum SortOrder { ASC, DESC }

    /**
     * Sort secrets map by key.
     */
    public Map<String, String> sortByKey(Map<String, String> secrets, SortOrder order) {
        if (secrets == null || secrets.isEmpty()) {
            return Collections.emptyMap();
        }
        Comparator<Map.Entry<String, String>> comparator = Map.Entry.comparingByKey();
        if (order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        Map<String, String> sorted = secrets.entrySet().stream()
                .sorted(comparator)
                .collect(Collectors.toLinkedHashMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
        logger.debug("Sorted {} secrets by key {}", sorted.size(), order);
        return sorted;
    }

    /**
     * Sort secrets map by value.
     */
    public Map<String, String> sortByValue(Map<String, String> secrets, SortOrder order) {
        if (secrets == null || secrets.isEmpty()) {
            return Collections.emptyMap();
        }
        Comparator<Map.Entry<String, String>> comparator = Map.Entry.comparingByValue();
        if (order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        Map<String, String> sorted = secrets.entrySet().stream()
                .sorted(comparator)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
        logger.debug("Sorted {} secrets by value {}", sorted.size(), order);
        return sorted;
    }

    /**
     * Return sorted list of secret keys only.
     */
    public List<String> sortedKeys(Map<String, String> secrets, SortOrder order) {
        if (secrets == null || secrets.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>(secrets.keySet());
        if (order == SortOrder.DESC) {
            keys.sort(Comparator.reverseOrder());
        } else {
            Collections.sort(keys);
        }
        return keys;
    }
}
