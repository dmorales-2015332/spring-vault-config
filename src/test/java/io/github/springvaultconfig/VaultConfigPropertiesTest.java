package io.github.springvaultconfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = VaultConfigPropertiesTest.TestConfig.class)
@TestPropertySource(properties = {
        "vault.uri=https://vault.example.com:8200",
        "vault.token=s.testtoken",
        "vault.mount-path=kv",
        "vault.secret-path=myapp/prod",
        "vault.lease-renewal-enabled=false",
        "vault.lease-renewal-interval-seconds=120",
        "vault.namespace=my-org"
})
class VaultConfigPropertiesTest {

    @EnableConfigurationProperties(VaultConfigProperties.class)
    static class TestConfig {}

    @Autowired
    private VaultConfigProperties properties;

    @Test
    void shouldBindUriProperty() {
        assertThat(properties.getUri()).isEqualTo("https://vault.example.com:8200");
    }

    @Test
    void shouldBindTokenProperty() {
        assertThat(properties.getToken()).isEqualTo("s.testtoken");
    }

    @Test
    void shouldBindMountPathProperty() {
        assertThat(properties.getMountPath()).isEqualTo("kv");
    }

    @Test
    void shouldBindSecretPathProperty() {
        assertThat(properties.getSecretPath()).isEqualTo("myapp/prod");
    }

    @Test
    void shouldBindLeaseRenewalEnabledProperty() {
        assertThat(properties.isLeaseRenewalEnabled()).isFalse();
    }

    @Test
    void shouldBindLeaseRenewalIntervalProperty() {
        assertThat(properties.getLeaseRenewalIntervalSeconds()).isEqualTo(120L);
    }

    @Test
    void shouldBindNamespaceProperty() {
        assertThat(properties.getNamespace()).isEqualTo("my-org");
    }

    @Test
    void shouldHaveSensibleDefaults() {
        VaultConfigProperties defaults = new VaultConfigProperties();
        assertThat(defaults.getUri()).isEqualTo("http://localhost:8200");
        assertThat(defaults.getMountPath()).isEqualTo("secret");
        assertThat(defaults.getSecretPath()).isEqualTo("application");
        assertThat(defaults.isLeaseRenewalEnabled()).isTrue();
        assertThat(defaults.getLeaseRenewalIntervalSeconds()).isEqualTo(60L);
        assertThat(defaults.getNamespace()).isNull();
    }
}
