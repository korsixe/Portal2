package com.mipt.portal.service;

import com.mipt.portal.entity.ModerationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private KafkaMessageService kafka;
  @InjectMocks private NotificationService service;

  @Test
  void getUserNotifications_emptyForNullOrEmpty() {
    assertThat(service.getUserNotifications(null)).isEmpty();
    assertThat(service.getUserNotifications(List.of())).isEmpty();
  }

  @Test
  void getUserNotifications_returnsCreatedItems() {
    ModerationMessage m = service.createNotification(10L, "reject", "spam", "mod@x.com");
    assertThat(service.getUserNotifications(List.of(10L))).contains(m);
  }

  @Test
  void getUnreadCount_zeroForNullOrEmpty() {
    assertThat(service.getUnreadCount(null)).isZero();
    assertThat(service.getUnreadCount(List.of())).isZero();
  }

  @Test
  void getUnreadCount_countsUnread() {
    service.createNotification(10L, "reject", "spam", "mod@x.com");
    service.createNotification(10L, "approve", "ok", "mod@x.com");
    assertThat(service.getUnreadCount(List.of(10L))).isEqualTo(2);
  }

  @Test
  void markAsRead_findsAndMarks() {
    ModerationMessage m = service.createNotification(10L, "approve", "ok", "mod@x.com");
    boolean ok = service.markAsRead(m.getId());
    assertThat(ok).isTrue();
    assertThat(m.getIsRead()).isTrue();
    verify(kafka).sendNotificationEvent(eq("notification.read"), anyString(), any());
  }

  @Test
  void markAsRead_falseWhenAbsent() {
    assertThat(service.markAsRead(99999L)).isFalse();
  }

  @Test
  void deleteNotification_removes() {
    ModerationMessage m = service.createNotification(10L, "approve", "ok", "mod@x.com");
    boolean ok = service.deleteNotification(m.getId());
    assertThat(ok).isTrue();
    assertThat(service.getUserNotifications(List.of(10L))).doesNotContain(m);
  }

  @Test
  void deleteNotification_falseWhenAbsent() {
    assertThat(service.deleteNotification(99999L)).isFalse();
  }

  @Test
  void deleteAllNotifications_clearsList() {
    service.createNotification(10L, "approve", "ok", "mod@x.com");
    boolean ok = service.deleteAllNotifications(List.of(10L));
    assertThat(ok).isTrue();
    assertThat(service.getUserNotifications(List.of(10L))).isEmpty();
    verify(kafka).sendNotificationEvent(eq("notification.all_deleted"), anyString(), any());
  }

  @Test
  void deleteAllNotifications_trueOnEmptyOrNull() {
    assertThat(service.deleteAllNotifications(null)).isTrue();
    assertThat(service.deleteAllNotifications(List.of())).isTrue();
  }

  @Test
  void markAllAsRead_marksAll() {
    ModerationMessage a = service.createNotification(10L, "approve", "ok", "m@x.com");
    ModerationMessage b = service.createNotification(10L, "reject", "no", "m@x.com");
    boolean ok = service.markAllAsRead(List.of(10L));
    assertThat(ok).isTrue();
    assertThat(a.getIsRead()).isTrue();
    assertThat(b.getIsRead()).isTrue();
    verify(kafka).sendNotificationEvent(eq("notification.all_read"), anyString(), any());
  }

  @Test
  void markAllAsRead_trueOnEmpty() {
    assertThat(service.markAllAsRead(null)).isTrue();
    assertThat(service.markAllAsRead(List.of())).isTrue();
  }

  @Test
  void createNotification_publishesEvent() {
    service.createNotification(10L, "approve", "ok", "mod@x.com");
    verify(kafka).sendNotificationEvent(eq("notification.created"), anyString(), any());
  }

  @Test
  void createNotification_handlesBlankReason() {
    ModerationMessage m = service.createNotification(10L, "approve", "", "mod@x.com");
    assertThat(m).isNotNull();
  }
}
