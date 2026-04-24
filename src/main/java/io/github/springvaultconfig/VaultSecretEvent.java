package io.github.springvaultconfig;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Spring application event representing a Vault secret lifecycle change.
 * Carries the event type, the affected secret path, a snapshot of the
 * secrets (may be empty for expiry events), and the timestamp.
 */
public class VaultSecretEvent extends ApplicationEvent {

    /** Lifecycle types for a Vault secret event. */
    public enum Type {
        /** Secrets were successfully loaded from Vault. */
        LOADED,
        /** Secrets were rotated (a new version is now active). */
        ROTATED,
        /** Secrets have expired and are no longer valid. */
        EXPIRED
    }

    private final Type type;
    private final String path;
    private final Map<String, String> secrets;
    private final Instant occurredAt;

    public VaultSecretEvent(Object source, Type type, String path,
                            Map<String, String> secrets, Instant occurredAt) {
        super(source);
        this.type = type;
        this.path = path;
        this.secrets = Collections.unmodifiableMap(secrets);
        this.occurredAt = occurredAt;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    /** Returns an unmodifiable view of the secrets associated with this event. */
    public Map<String, String> getSecrets() {
        return secrets;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "VaultSecretEvent{type=" + type +
               ", path='" + path + "'" +
               ", secretKeys=" + secrets.keySet() +
               ", occurredAt=" + occurredAt + "}";
    }
}
