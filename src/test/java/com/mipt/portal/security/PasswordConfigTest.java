package com.mipt.portal.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PasswordConfig}.
 *
 * <p>Verifies that the produced {@link PasswordEncoder} encodes raw passwords with the
 * configured pepper and matches them back, and that wrong passwords are rejected.</p>
 */
class PasswordConfigTest {

    private PasswordEncoder buildEncoder(String pepper) {
        PasswordConfig cfg = new PasswordConfig();
        ReflectionTestUtils.setField(cfg, "pepper", pepper);
        return cfg.passwordEncoder();
    }

    @Test
    void encode_thenMatches_withSamePepper() {
        PasswordEncoder encoder = buildEncoder("super-pepper");
        String hash = encoder.encode("hunter2");

        assertThat(hash).isNotEqualTo("hunter2");
        assertThat(encoder.matches("hunter2", hash)).isTrue();
    }

    @Test
    void matches_returnsFalse_forWrongPassword() {
        PasswordEncoder encoder = buildEncoder("super-pepper");
        String hash = encoder.encode("hunter2");

        assertThat(encoder.matches("not-the-password", hash)).isFalse();
    }

    @Test
    void differentPeppers_produceIncompatibleHashes() {
        String hashFromA = buildEncoder("pepper-A").encode("password");
        PasswordEncoder encoderB = buildEncoder("pepper-B");

        assertThat(encoderB.matches("password", hashFromA)).isFalse();
    }
}
