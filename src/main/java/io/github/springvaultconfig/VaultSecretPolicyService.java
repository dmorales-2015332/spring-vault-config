package io.github.springvaultconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for reading and evaluating Vault ACL policies associated with secrets.
 */
public class VaultSecretPolicyService {

    private static final Logger log = LoggerFactory.getLogger(VaultSecretPolicyService.class);
    private static final String POLICY_PATH_PREFIX = "sys/policy/";

    private final VaultOperations vaultOperations;

    public VaultSecretPolicyService(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    /**
     * Returns the list of policy names attached to the current Vault token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getCurrentTokenPolicies() {
        try {
            VaultResponse response = vaultOperations.read("auth/token/lookup-self");
            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }
            Object policies = response.getData().get("policies");
            if (policies instanceof List) {
                return (List<String>) policies;
            }
        } catch (Exception e) {
            log.warn("Failed to look up current token policies: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Reads the raw rules of a named policy.
     */
    public Optional<String> getPolicyRules(String policyName) {
        try {
            VaultResponse response = vaultOperations.read(POLICY_PATH_PREFIX + policyName);
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }
            Object rules = response.getData().get("rules");
            return Optional.ofNullable(rules).map(Object::toString);
        } catch (Exception e) {
            log.warn("Failed to read policy '{}': {}", policyName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Checks whether the current token has a specific policy.
     */
    public boolean hasPolicy(String policyName) {
        return getCurrentTokenPolicies().contains(policyName);
    }

    /**
     * Returns a map of policyName -> rules for all policies on the current token.
     */
    public Map<String, String> getAllCurrentPolicyRules() {
        List<String> policies = getCurrentTokenPolicies();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        for (String policy : policies) {
            getPolicyRules(policy).ifPresent(rules -> result.put(policy, rules));
        }
        return Collections.unmodifiableMap(result);
    }
}
