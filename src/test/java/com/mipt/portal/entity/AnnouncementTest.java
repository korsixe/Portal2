package com.mipt.portal.entity;

import com.mipt.portal.enums.AdStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the non-Lombok behaviour of {@link Announcement}.
 *
 * <p>Lombok's @Data covers boilerplate getters/setters and equals/hashCode; here we
 * focus on the hand-written state-transition methods.</p>
 */
class AnnouncementTest {

    @Test
    void sendToModeration_setsStatusToUnderModeration() {
        Announcement ad = new Announcement();
        ad.sendToModeration();
        assertThat(ad.getStatus()).isEqualTo(AdStatus.UNDER_MODERATION);
    }

    @Test
    void activate_fromUnderModeration_setsStatusActive() {
        Announcement ad = new Announcement();
        ad.sendToModeration();
        ad.activate();
        assertThat(ad.getStatus()).isEqualTo(AdStatus.ACTIVE);
    }

    @Test
    void activate_fromAnyOtherStatus_throwsIllegalState() {
        Announcement ad = new Announcement();
        // default status is DRAFT
        assertThatThrownBy(ad::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Можно активировать");
    }

    @Test
    void reject_setsStatusRejected() {
        Announcement ad = new Announcement();
        ad.reject();
        assertThat(ad.getStatus()).isEqualTo(AdStatus.REJECTED);
    }

    @Test
    void setPhotoUrls_isNoOp_butStillExecutes() {
        // setPhotoUrls intentionally does nothing — call it just to cover the method body
        Announcement ad = new Announcement();
        ad.setPhotoUrls(List.of("http://localhost/photo/1"));
        // ничего не должно сломаться, состояние объявления не меняется
        assertThat(ad.getPhoto()).isNull();
    }
}
