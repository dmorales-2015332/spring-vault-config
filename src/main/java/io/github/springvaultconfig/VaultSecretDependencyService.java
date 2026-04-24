package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Tracks dependencies between secrets so that when one secret changes,
 * all dependent secrets can be refreshed in the correct order.
 */
@Service
public class VaultSecretDependencyService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretDependencyService.class);

    /** dependents: key -> set of keys that depend on key */
    private final Map<String, Set<String>> dependents = new HashMap<>();

    /** dependencies: key -> set of keys that key depends on */
    private final Map<String, Set<String>> dependencies = new HashMap<>();

    /**
     * Registers that {@code dependent} depends on {@code dependency}.
     */
    public void registerDependency(String dependent, String dependency) {
        dependencies.computeIfAbsent(dependent, k -> new HashSet<>()).add(dependency);
        dependents.computeIfAbsent(dependency, k -> new HashSet<>()).add(dependent);
        log.debug("Registered dependency: {} -> {}", dependent, dependency);
    }

    /**
     * Returns all secrets that directly or transitively depend on {@code secretKey},
     * in topological order (dependencies before dependents).
     */
    public List<String> getRefreshOrder(String secretKey) {
        List<String> order = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        collectDependents(secretKey, visited);
        order.addAll(visited);
        log.debug("Refresh order for {}: {}", secretKey, order);
        return Collections.unmodifiableList(order);
    }

    /**
     * Returns the direct dependencies of a secret.
     */
    public Set<String> getDependencies(String secretKey) {
        return Collections.unmodifiableSet(
                dependencies.getOrDefault(secretKey, Collections.emptySet()));
    }

    /**
     * Returns the direct dependents of a secret.
     */
    public Set<String> getDependents(String secretKey) {
        return Collections.unmodifiableSet(
                dependents.getOrDefault(secretKey, Collections.emptySet()));
    }

    /**
     * Removes all dependency registrations for the given secret.
     */
    public void removeDependency(String dependent, String dependency) {
        dependencies.getOrDefault(dependent, Collections.emptySet()).remove(dependency);
        dependents.getOrDefault(dependency, Collections.emptySet()).remove(dependent);
        log.debug("Removed dependency: {} -> {}", dependent, dependency);
    }

    private void collectDependents(String key, Set<String> visited) {
        if (visited.contains(key)) {
            return;
        }
        visited.add(key);
        for (String dep : dependents.getOrDefault(key, Collections.emptySet())) {
            collectDependents(dep, visited);
        }
    }
}
