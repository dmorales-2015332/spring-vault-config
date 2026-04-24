package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretDependencyServiceTest {

    private VaultSecretDependencyService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretDependencyService();
    }

    @Test
    void registerDependency_shouldTrackBothDirections() {
        service.registerDependency("app/db", "infra/db-password");

        assertThat(service.getDependencies("app/db")).containsExactly("infra/db-password");
        assertThat(service.getDependents("infra/db-password")).containsExactly("app/db");
    }

    @Test
    void getRefreshOrder_singleDependent_returnsCorrectOrder() {
        service.registerDependency("app/db", "infra/db-password");

        List<String> order = service.getRefreshOrder("infra/db-password");

        assertThat(order).containsExactly("infra/db-password", "app/db");
    }

    @Test
    void getRefreshOrder_transitiveChain_returnsAllInOrder() {
        service.registerDependency("app/db", "infra/db-password");
        service.registerDependency("service/report", "app/db");

        List<String> order = service.getRefreshOrder("infra/db-password");

        assertThat(order).containsExactlyInAnyOrder("infra/db-password", "app/db", "service/report");
        assertThat(order.indexOf("infra/db-password")).isLessThan(order.indexOf("app/db"));
        assertThat(order.indexOf("app/db")).isLessThan(order.indexOf("service/report"));
    }

    @Test
    void getRefreshOrder_noDependent_returnsSelf() {
        List<String> order = service.getRefreshOrder("standalone/secret");

        assertThat(order).containsExactly("standalone/secret");
    }

    @Test
    void removeDependency_shouldClearBothDirections() {
        service.registerDependency("app/db", "infra/db-password");
        service.removeDependency("app/db", "infra/db-password");

        assertThat(service.getDependencies("app/db")).isEmpty();
        assertThat(service.getDependents("infra/db-password")).isEmpty();
    }

    @Test
    void getDependencies_unknownKey_returnsEmptySet() {
        Set<String> deps = service.getDependencies("unknown/key");
        assertThat(deps).isEmpty();
    }

    @Test
    void getDependents_unknownKey_returnsEmptySet() {
        Set<String> deps = service.getDependents("unknown/key");
        assertThat(deps).isEmpty();
    }

    @Test
    void registerDependency_multipleDependents_allTracked() {
        service.registerDependency("app/service-a", "infra/shared-token");
        service.registerDependency("app/service-b", "infra/shared-token");

        assertThat(service.getDependents("infra/shared-token"))
                .containsExactlyInAnyOrder("app/service-a", "app/service-b");
    }
}
