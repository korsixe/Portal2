package com.mipt.portal.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ModerationMessageTest {

    @Test
    void noArgsConstructor_yieldsAllNullFields() {
        ModerationMessage msg = new ModerationMessage();

        assertThat(msg.getId()).isNull();
        assertThat(msg.getAdId()).isNull();
        assertThat(msg.getModeratorEmail()).isNull();
        assertThat(msg.getAction()).isNull();
        assertThat(msg.getReason()).isNull();
        assertThat(msg.getCreatedAt()).isNull();
        assertThat(msg.getIsRead()).isNull();
    }

    @Test
    void allArgsConstructor_populatesFieldsAndStampsCreatedAt() {
        LocalDateTime before = LocalDateTime.now();
        ModerationMessage msg = new ModerationMessage(42L, "mod@phystech.edu",
                "approve", "ok", false);
        LocalDateTime after = LocalDateTime.now();

        assertThat(msg.getAdId()).isEqualTo(42L);
        assertThat(msg.getModeratorEmail()).isEqualTo("mod@phystech.edu");
        assertThat(msg.getAction()).isEqualTo("approve");
        assertThat(msg.getReason()).isEqualTo("ok");
        assertThat(msg.getIsRead()).isFalse();
        assertThat(msg.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void lombokSettersAndGetters_roundTrip() {
        ModerationMessage msg = new ModerationMessage();
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);

        msg.setId(1L);
        msg.setAdId(99L);
        msg.setModeratorEmail("a@b.c");
        msg.setAction("reject");
        msg.setReason("spam");
        msg.setCreatedAt(now);
        msg.setIsRead(true);

        assertThat(msg.getId()).isEqualTo(1L);
        assertThat(msg.getAdId()).isEqualTo(99L);
        assertThat(msg.getModeratorEmail()).isEqualTo("a@b.c");
        assertThat(msg.getAction()).isEqualTo("reject");
        assertThat(msg.getReason()).isEqualTo("spam");
        assertThat(msg.getCreatedAt()).isEqualTo(now);
        assertThat(msg.getIsRead()).isTrue();
    }
}
