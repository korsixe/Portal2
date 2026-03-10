//package com.mipt.portal.notification;
//
//import com.mipt.portal.moderator.message.ModerationMessage;
//import com.mipt.portal.moderator.message.ModerationMessageRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/test/notifications")
//@RequiredArgsConstructor
//public class NotificationTestController {
//
//  private final NotificationService notificationService;
//  private final ModerationMessageRepository messageRepository;
//
//  // 1. Создать тестовые уведомления
//  @GetMapping("/setup")
//  public String setupTestData() {
//    messageRepository.deleteAll();
//
//    ModerationMessage msg1 = new ModerationMessage();
//    msg1.setAdId(1L);
//    msg1.setModeratorEmail("moderator@test.com");
//    msg1.setAction("approve");
//    msg1.setReason("Объявление одобрено");
//    msg1.setCreatedAt(LocalDateTime.now().minusHours(2));
//    msg1.setIsRead(false);
//
//    ModerationMessage msg2 = new ModerationMessage();
//    msg2.setAdId(1L);
//    msg2.setModeratorEmail("moderator@test.com");
//    msg2.setAction("reject");
//    msg2.setReason("Недостаточно фото");
//    msg2.setCreatedAt(LocalDateTime.now().minusHours(1));
//    msg2.setIsRead(false);
//
//    ModerationMessage msg3 = new ModerationMessage();
//    msg3.setAdId(2L);
//    msg3.setModeratorEmail("moderator@test.com");
//    msg3.setAction("approve");
//    msg3.setReason("Все хорошо");
//    msg3.setCreatedAt(LocalDateTime.now());
//    msg3.setIsRead(true);
//
//    messageRepository.saveAll(List.of(msg1, msg2, msg3));
//
//    return "✅ Тестовые уведомления созданы!";
//  }
//
//  // 2. Показать все уведомления для списка ID объявлений
//  @GetMapping("/user")
//  public String getUserNotifications(@RequestParam(defaultValue = "1,2") String adIds) {
//    List<Long> ids = List.of(adIds.split(",")).stream()
//      .map(String::trim)
//      .map(Long::parseLong)
//      .toList();
//
//    List<ModerationMessage> notifications = notificationService.getUserNotifications(ids);
//
//    StringBuilder result = new StringBuilder("📋 Уведомления для объявлений " + ids + ":\n\n");
//
//    if (notifications.isEmpty()) {
//      result.append("Нет уведомлений");
//    } else {
//      for (ModerationMessage msg : notifications) {
//        result.append("ID: ").append(msg.getId()).append("\n");
//        result.append("Объявление ID: ").append(msg.getAdId()).append("\n");
//        result.append("Действие: ").append(msg.getAction()).append("\n");
//        result.append("Причина: ").append(msg.getReason()).append("\n");
//        result.append("Прочитано: ").append(msg.getIsRead()).append("\n");
//        result.append("Дата: ").append(msg.getCreatedAt()).append("\n");
//        result.append("-------------------\n");
//      }
//    }
//
//    return result.toString().replace("\n", "<br>");
//  }
//
//  // 3. Получить количество непрочитанных
//  @GetMapping("/unread-count")
//  public String getUnreadCount(@RequestParam(defaultValue = "1,2") String adIds) {
//    List<Long> ids = List.of(adIds.split(",")).stream()
//      .map(String::trim)
//      .map(Long::parseLong)
//      .toList();
//
//    int count = notificationService.getUnreadCount(ids);
//    return "🔔 Непрочитанных уведомлений для объявлений " + ids + ": " + count;
//  }
//
//  // 4. Пометить как прочитанное
//  @GetMapping("/read/{id}")
//  public String markAsRead(@PathVariable Long id) {
//    boolean success = notificationService.markAsRead(id);
//    return success
//      ? "✅ Уведомление " + id + " помечено как прочитанное"
//      : "❌ Уведомление " + id + " не найдено";
//  }
//
//  // 5. Пометить все как прочитанные
//  @GetMapping("/read-all")
//  public String markAllAsRead(@RequestParam(defaultValue = "1,2") String adIds) {
//    List<Long> ids = List.of(adIds.split(",")).stream()
//      .map(String::trim)
//      .map(Long::parseLong)
//      .toList();
//
//    boolean success = notificationService.markAllAsRead(ids);
//    return success
//      ? "✅ Все уведомления для объявлений " + ids + " помечены как прочитанные"
//      : "❌ Ошибка";
//  }
//
//  // 6. Удалить уведомление
//  @GetMapping("/delete/{id}")
//  public String deleteNotification(@PathVariable Long id) {
//    boolean success = notificationService.deleteNotification(id);
//    return success
//      ? "✅ Уведомление " + id + " удалено"
//      : "❌ Уведомление " + id + " не найдено";
//  }
//
//  // 7. Удалить все уведомления для объявлений
//  @GetMapping("/delete-all")
//  public String deleteAllNotifications(@RequestParam(defaultValue = "1,2") String adIds) {
//    List<Long> ids = List.of(adIds.split(",")).stream()
//      .map(String::trim)
//      .map(Long::parseLong)
//      .toList();
//
//    boolean success = notificationService.deleteAllNotifications(ids);
//    return success
//      ? "✅ Все уведомления для объявлений " + ids + " удалены"
//      : "❌ Ошибка";
//  }
//
//  // 8. Показать все уведомления в БД
//  @GetMapping("/all")
//  public String showAllNotifications() {
//    List<ModerationMessage> all = messageRepository.findAll();
//
//    StringBuilder result = new StringBuilder("📋 ВСЕ УВЕДОМЛЕНИЯ В БД:\n\n");
//
//    if (all.isEmpty()) {
//      result.append("Нет уведомлений");
//    } else {
//      for (ModerationMessage msg : all) {
//        result.append("ID: ").append(msg.getId()).append("\n");
//        result.append("Объявление ID: ").append(msg.getAdId()).append("\n");
//        result.append("Действие: ").append(msg.getAction()).append("\n");
//        result.append("Прочитано: ").append(msg.getIsRead()).append("\n");
//        result.append("-------------------\n");
//      }
//    }
//
//    return result.toString().replace("\n", "<br>");
//  }
//
//  // 9. Полная проверка всех функций
//  @GetMapping("/full-test")
//  public String fullTest() {
//    StringBuilder result = new StringBuilder("🔍 ПОЛНОЕ ТЕСТИРОВАНИЕ УВЕДОМЛЕНИЙ\n\n");
//
//    try {
//      // 1. Создаем тестовые данные
//      result.append("1. ").append(setupTestData()).append("<br>");
//
//      // 2. Показываем все уведомления
//      result.append("<br>2. Все уведомления:<br>");
//      result.append(getUserNotifications("1,2")).append("<br>");
//
//      // 3. Считаем непрочитанные
//      result.append("<br>3. ").append(getUnreadCount("1,2")).append("<br>");
//
//      // Получаем ID первого уведомления для теста
//      List<ModerationMessage> notifications = notificationService.getUserNotifications(List.of(1L, 2L));
//      Long firstNotificationId = notifications.isEmpty() ? 1L : notifications.get(0).getId();
//
//      // 4. Помечаем одно как прочитанное
//      result.append("<br>4. ").append(markAsRead(firstNotificationId)).append("<br>");
//
//      // 5. Снова считаем непрочитанные
//      result.append("<br>5. После пометки: ").append(getUnreadCount("1,2")).append("<br>");
//
//      // 6. Помечаем все как прочитанные
//      result.append("<br>6. ").append(markAllAsRead("1,2")).append("<br>");
//
//      // 7. Проверяем счетчик
//      result.append("<br>7. После всего: ").append(getUnreadCount("1,2")).append("<br>");
//
//      // 8. Удаляем одно уведомление
//      result.append("<br>8. ").append(deleteNotification(firstNotificationId)).append("<br>");
//
//      // 9. Показываем оставшиеся
//      result.append("<br>9. Оставшиеся:<br>");
//      result.append(showAllNotifications()).append("<br>");
//
//      // 10. Удаляем все
//      result.append("<br>10. ").append(deleteAllNotifications("1,2")).append("<br>");
//
//      // 11. Проверяем что все удалилось
//      result.append("<br>11. Финальный результат:<br>");
//      result.append(showAllNotifications());
//
//    } catch (Exception e) {
//      result.append("❌ Ошибка: ").append(e.getMessage());
//    }
//
//    return result.toString().replace("\n", "<br>");
//  }
//}