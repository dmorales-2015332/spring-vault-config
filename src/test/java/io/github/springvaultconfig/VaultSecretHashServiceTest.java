package io.github.springvaultconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class VaultSecretHashServiceTest {

    private VaultSecretHashService service;

    @BeforeEach
    void setUp() {
        service = new VaultSecretHashService();
    }

    @Test
    void hashReturnsNonNullHexString() {
        String hash = service.hash("my-secret-value");
        assertThat(hash).isNotNull().hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void hashIsDeterministic() {
        String h1 = service.hash("same-value");
        String h2 = service.hash("same-value");
        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void hashDiffersForDifferentValues() {
        assertThat(service.hash("value-a")).isNotEqualTo(service.hash("value-b"));
    }

    @Test
    void hashThrowsOnNullValue() {
        assertThatThrownBy(() -> service.hash(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storeHashAndMatchesReturnsTrueForSameValue() {
        service.storeHash("db.password", "s3cr3t");
        assertThat(service.matches("db.password", "s3cr3t")).isTrue();
    }

    @Test
    void matchesReturnsFalseForDifferentValue() {
        service.storeHash("db.password", "s3cr3t");
        assertThat(service.matches("db.password", "wrong")).isFalse();
    }

    @Test
    void matchesReturnsFalseWhenNoHashStored() {
        assertThat(service.matches("unknown.key", "value")).isFalse();
    }

    @Test
    void hasChangedReturnsTrueWhenValueChanged() {
        service.storeHash("api.key", "original");
        assertThat(service.hasChanged("api.key", "updated")).isTrue();
    }

    @Test
    void hasChangedReturnsFalseWhenValueUnchanged() {
        service.storeHash("api.key", "original");
        assertThat(service.hasChanged("api.key", "original")).isFalse();
    }

    @Test
    void evictRemovesStoredHash() {
        service.storeHash("token", "abc123");
        assertThat(service.size()).isEqualTo(1);
        service.evict("token");
        assertThat(service.size()).isZero();
        assertThat(service.matches("token", "abc123")).isFalse();
    }

    @Test
    void sizeReflectsStoredHashes() {
        service.storeHash("key1", "val1");
        service.storeHash("key2", "val2");
        assertThat(service.size()).isEqualTo(2);
    }

    @Test
    void customAlgorithmIsUsed() {
        VaultSecretHashService md5Service = new VaultSecretHashService("MD5");
        String hash = md5Service.hash("test");
        assertThat(hash).isNotNull().hasSize(32);
    }

    @Test
    void unknownAlgorithmThrowsIllegalState() {
        VaultSecretHashService bad = new VaultSecretHashService("NO-SUCH-ALGO");
        assertThatThrownBy(() -> bad.hash("value"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NO-SUCH-ALGO");
    }
}
