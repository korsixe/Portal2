package com.mipt.portal.controller;

import com.mipt.portal.entity.ModerationMessage;
import com.mipt.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test/notifications")
@RequiredArgsConstructor
public class NotificationTestController {

  private final NotificationService notificationService;

  // 1. Создать тестовые уведомления
  @GetMapping("/setup")
  public String setupTestData() {
    // Очищаем старые данные
    notificationService.deleteAllNotifications(List.of(1L, 2L));

    // Создаем новые
    notificationService.createNotification(1L, "approve", "Объявление одобрено", "moderator@test.com");
    notificationService.createNotification(1L, "reject", "Недостаточно фото", "moderator@test.com");

    ModerationMessage msg3 = notificationService.createNotification(2L, "approve", "Все хорошо", "moderator@test.com");
    msg3.setIsRead(true); // помечаем как прочитанное

    return "✅ Тестовые уведомления созданы!";
  }

  // 2. Показать все уведомления для списка ID объявлений
  @GetMapping("/user")
  public String getUserNotifications(@RequestParam(defaultValue = "1,2") String adIds) {
    List<Long> ids = List.of(adIds.split(",")).stream()
      .map(String::trim)
      .map(Long::parseLong)
      .toList();

    List<ModerationMessage> notifications = notificationService.getUserNotifications(ids);

    StringBuilder result = new StringBuilder("📋 Уведомления для объявлений " + ids + ":\n\n");

    if (notifications.isEmpty()) {
      result.append("Нет уведомлений");
    } else {
      for (ModerationMessage msg : notifications) {
        result.append("ID: ").append(msg.getId()).append("\n");
        result.append("Объявление ID: ").append(msg.getAdId()).append("\n");
        result.append("Действие: ").append(msg.getAction()).append("\n");
        result.append("Причина: ").append(msg.getReason()).append("\n");
        result.append("Прочитано: ").append(msg.getIsRead()).append("\n");
        result.append("Дата: ").append(msg.getCreatedAt()).append("\n");
        result.append("-------------------\n");
      }
    }

    return result.toString().replace("\n", "<br>");
  }

  // 3. Получить количество непрочитанных
  @GetMapping("/unread-count")
  public String getUnreadCount(@RequestParam(defaultValue = "1,2") String adIds) {
    List<Long> ids = List.of(adIds.split(",")).stream()
      .map(String::trim)
      .map(Long::parseLong)
      .toList();

    int count = notificationService.getUnreadCount(ids);
    return "🔔 Непрочитанных уведомлений для объявлений " + ids + ": " + count;
  }

  // 4. Пометить как прочитанное
  @GetMapping("/read/{id}")
  public String markAsRead(@PathVariable Long id) {
    boolean success = notificationService.markAsRead(id);
    return success
      ? "✅ Уведомление " + id + " помечено как прочитанное"
      : "❌ Уведомление " + id + " не найдено";
  }

  // 6. Удалить уведомление
  @GetMapping("/delete/{id}")
  public String deleteNotification(@PathVariable Long id) {
    boolean success = notificationService.deleteNotification(id);
    return success
      ? "✅ Уведомление " + id + " удалено"
      : "❌ Уведомление " + id + " не найдено";
  }

  // 7. Удалить все уведомления для объявлений
  @GetMapping("/delete-all")
  public String deleteAllNotifications(@RequestParam(defaultValue = "1,2") String adIds) {
    List<Long> ids = List.of(adIds.split(",")).stream()
      .map(String::trim)
      .map(Long::parseLong)
      .toList();

    boolean success = notificationService.deleteAllNotifications(ids);
    return success
      ? "✅ Все уведомления для объявлений " + ids + " удалены"
      : "❌ Ошибка";
  }

  // 8. Показать все уведомления
  @GetMapping("/all")
  public String showAllNotifications() {
    List<ModerationMessage> all = notificationService.getUserNotifications(List.of(1L, 2L));

    StringBuilder result = new StringBuilder("📋 ВСЕ УВЕДОМЛЕНИЯ:\n\n");

    if (all.isEmpty()) {
      result.append("Нет уведомлений");
    } else {
      for (ModerationMessage msg : all) {
        result.append("ID: ").append(msg.getId()).append("\n");
        result.append("Объявление ID: ").append(msg.getAdId()).append("\n");
        result.append("Действие: ").append(msg.getAction()).append("\n");
        result.append("Прочитано: ").append(msg.getIsRead()).append("\n");
        result.append("-------------------\n");
      }
    }

    return result.toString().replace("\n", "<br>");
  }

  // 9. Полная проверка всех функций
  @GetMapping("/full-test")
  public String fullTest() {
    StringBuilder result = new StringBuilder("🔍 ПОЛНОЕ ТЕСТИРОВАНИЕ УВЕДОМЛЕНИЙ\n\n");

    try {
      // 1. Создаем тестовые данные
      result.append("1. ").append(setupTestData()).append("<br>");

      // 2. Показываем все уведомления
      result.append("<br>2. Все уведомления:<br>");
      result.append(getUserNotifications("1,2")).append("<br>");

      // 3. Считаем непрочитанные
      result.append("<br>3. ").append(getUnreadCount("1,2")).append("<br>");

      // Получаем ID первого уведомления
      List<ModerationMessage> notifications = notificationService.getUserNotifications(List.of(1L));
      Long firstNotificationId = notifications.isEmpty() ? 1L : notifications.get(0).getId();

      // 4. Помечаем одно как прочитанное
      result.append("<br>4. ").append(markAsRead(firstNotificationId)).append("<br>");

      // 5. Снова считаем непрочитанные
      result.append("<br>5. После пометки: ").append(getUnreadCount("1,2")).append("<br>");


      // 6. Проверяем счетчик
      result.append("<br>7. После всего: ").append(getUnreadCount("1,2")).append("<br>");

      // 7. Удаляем одно уведомление
      result.append("<br>8. ").append(deleteNotification(firstNotificationId)).append("<br>");

      // 8. Показываем оставшиеся
      result.append("<br>9. Оставшиеся:<br>");
      result.append(showAllNotifications()).append("<br>");

      // 9. Удаляем все
      result.append("<br>10. ").append(deleteAllNotifications("1,2")).append("<br>");

      // 10. Проверяем что все удалилось
      result.append("<br>11. Финальный результат:<br>");
      result.append(showAllNotifications());

    } catch (Exception e) {
      result.append("❌ Ошибка: ").append(e.getMessage());
    }

    return result.toString().replace("\n", "<br>");
  }
}