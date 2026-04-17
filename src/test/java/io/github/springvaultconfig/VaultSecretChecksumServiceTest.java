package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaultSecretChecksumServiceTest {

    private VaultSecretChecksumService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretChecksumService();
    }

    @Test
    void computeAndStore_returnsNonBlankChecksum() {
        String checksum = service.computeAndStore("secret/db", "p@ssw0rd");
        assertThat(checksum).isNotBlank().hasSize(64);
    }

    @Test
    void computeAndStore_sameValueProducesSameChecksum() {
        String c1 = service.computeAndStore("secret/db", "value");
        String c2 = service.computeAndStore("secret/db", "value");
        assertThat(c1).isEqualTo(c2);
    }

    @Test
    void hasChanged_returnsTrueWhenNoChecksumStored() {
        assertThat(service.hasChanged("secret/new", "anyValue")).isTrue();
    }

    @Test
    void hasChanged_returnsFalseWhenValueUnchanged() {
        service.computeAndStore("secret/api", "token123");
        assertThat(service.hasChanged("secret/api", "token123")).isFalse();
    }

    @Test
    void hasChanged_returnsTrueWhenValueChanged() {
        service.computeAndStore("secret/api", "oldToken");
        assertThat(service.hasChanged("secret/api", "newToken")).isTrue();
    }

    @Test
    void getChecksum_returnsStoredChecksum() {
        String stored = service.computeAndStore("secret/x", "val");
        assertThat(service.getChecksum("secret/x")).isEqualTo(stored);
    }

    @Test
    void getChecksum_returnsNullForUnknownPath() {
        assertThat(service.getChecksum("secret/unknown")).isNull();
    }

    @Test
    void evict_removesStoredChecksum() {
        service.computeAndStore("secret/temp", "value");
        service.evict("secret/temp");
        assertThat(service.getChecksum("secret/temp")).isNull();
    }

    @Test
    void differentValues_produceDifferentChecksums() {
        String c1 = service.computeAndStore("secret/a", "alpha");
        String c2 = service.computeAndStore("secret/b", "beta");
        assertThat(c1).isNotEqualTo(c2);
    }
}
