package com.mipt.portal.controller;

import com.mipt.portal.entity.ModerationMessage;
import com.mipt.portal.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationTestController}.
 *
 * <p>The controller is a manual smoke-test helper: each endpoint formats notification
 * data into an HTML-ish string. Tests cover happy paths and "no data" branches, plus
 * the {@code fullTest} flow which chains everything together and traps exceptions.</p>
 */
@ExtendWith(MockitoExtension.class)
class NotificationTestControllerTest {

    @Mock private NotificationService notificationService;
    private NotificationTestController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationTestController(notificationService);
    }

    private ModerationMessage msg(long id, long adId, String action, String reason, boolean read) {
        ModerationMessage m = new ModerationMessage();
        m.setId(id);
        m.setAdId(adId);
        m.setAction(action);
        m.setReason(reason);
        m.setIsRead(read);
        m.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        return m;
    }

    @Test
    void setupTestData_clearsAndRecreatesNotifications() {
        when(notificationService.deleteAllNotifications(any())).thenReturn(true);
        when(notificationService.createNotification(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(msg(1, 1, "approve", "ok", false));

        String result = controller.setupTestData();

        assertThat(result).contains("Тестовые уведомления созданы");
        verify(notificationService).deleteAllNotifications(any());
        verify(notificationService).createNotification(1L, "approve", "Объявление одобрено", "moderator@test.com");
        verify(notificationService).createNotification(1L, "reject", "Недостаточно фото", "moderator@test.com");
        verify(notificationService).createNotification(2L, "approve", "Все хорошо", "moderator@test.com");
    }

    @Test
    void getUserNotifications_emptyList_returnsNoNotificationsMessage() {
        when(notificationService.getUserNotifications(any())).thenReturn(List.of());

        String result = controller.getUserNotifications("1,2");

        assertThat(result).contains("Нет уведомлений");
    }

    @Test
    void getUserNotifications_listsAllFields() {
        when(notificationService.getUserNotifications(any()))
                .thenReturn(List.of(msg(10, 1, "approve", "ok", false)));

        String result = controller.getUserNotifications("1");

        assertThat(result)
                .contains("ID: 10")
                .contains("Объявление ID: 1")
                .contains("Действие: approve")
                .contains("Причина: ok");
    }

    @Test
    void getUnreadCount_returnsFormattedNumber() {
        when(notificationService.getUnreadCount(any())).thenReturn(3);

        String result = controller.getUnreadCount("1,2");

        assertThat(result).contains("3");
    }

    @Test
    void markAsRead_success_returnsConfirmation() {
        when(notificationService.markAsRead(5L)).thenReturn(true);

        assertThat(controller.markAsRead(5L)).contains("помечено как прочитанное");
    }

    @Test
    void markAsRead_failure_returnsNotFoundMessage() {
        when(notificationService.markAsRead(99L)).thenReturn(false);

        assertThat(controller.markAsRead(99L)).contains("не найдено");
    }

    @Test
    void deleteNotification_success() {
        when(notificationService.deleteNotification(5L)).thenReturn(true);
        assertThat(controller.deleteNotification(5L)).contains("удалено");
    }

    @Test
    void deleteNotification_failure() {
        when(notificationService.deleteNotification(99L)).thenReturn(false);
        assertThat(controller.deleteNotification(99L)).contains("не найдено");
    }

    @Test
    void deleteAllNotifications_success() {
        when(notificationService.deleteAllNotifications(any())).thenReturn(true);
        assertThat(controller.deleteAllNotifications("1,2")).contains("удалены");
    }

    @Test
    void deleteAllNotifications_failure() {
        when(notificationService.deleteAllNotifications(any())).thenReturn(false);
        assertThat(controller.deleteAllNotifications("1,2")).contains("Ошибка");
    }

    @Test
    void showAllNotifications_emptyList() {
        when(notificationService.getUserNotifications(any())).thenReturn(List.of());

        assertThat(controller.showAllNotifications()).contains("Нет уведомлений");
    }

    @Test
    void showAllNotifications_nonEmpty_listsRecords() {
        when(notificationService.getUserNotifications(any()))
                .thenReturn(List.of(msg(1, 1, "approve", "ok", false)));

        assertThat(controller.showAllNotifications())
                .contains("ID: 1")
                .contains("Действие: approve");
    }

    @Test
    void fullTest_happyPath_runsAllSteps() {
        when(notificationService.deleteAllNotifications(any())).thenReturn(true);
        when(notificationService.createNotification(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(msg(1, 1, "approve", "ok", false));
        when(notificationService.getUserNotifications(any()))
                .thenReturn(List.of(msg(1, 1, "approve", "ok", false)));
        when(notificationService.getUnreadCount(any())).thenReturn(1);
        when(notificationService.markAsRead(anyLong())).thenReturn(true);
        when(notificationService.deleteNotification(anyLong())).thenReturn(true);

        String result = controller.fullTest();

        assertThat(result).contains("ПОЛНОЕ ТЕСТИРОВАНИЕ УВЕДОМЛЕНИЙ");
    }

    @Test
    void fullTest_noNotifications_fallsBackToFirstNotificationId1() {
        when(notificationService.deleteAllNotifications(any())).thenReturn(true);
        when(notificationService.createNotification(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(msg(1, 1, "approve", "ok", false));
        // первый вызов — для setup, второй — внутри fullTest для "первого id"
        when(notificationService.getUserNotifications(any())).thenReturn(List.of());
        when(notificationService.getUnreadCount(any())).thenReturn(0);
        when(notificationService.markAsRead(anyLong())).thenReturn(false);
        when(notificationService.deleteNotification(anyLong())).thenReturn(false);

        String result = controller.fullTest();

        assertThat(result).contains("ПОЛНОЕ ТЕСТИРОВАНИЕ УВЕДОМЛЕНИЙ");
    }

    @Test
    void fullTest_serviceThrows_exceptionIsCaughtAndReported() {
        when(notificationService.deleteAllNotifications(any()))
                .thenThrow(new RuntimeException("boom"));

        String result = controller.fullTest();

        assertThat(result).contains("Ошибка").contains("boom");
    }
}
