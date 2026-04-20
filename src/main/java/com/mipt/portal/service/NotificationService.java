package com.mipt.portal.service;

import com.mipt.portal.dto.kafka.KafkaEventPayloads;
import com.mipt.portal.entity.ModerationMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  // Хранилище уведомлений
  private final Map<Long, List<ModerationMessage>> notificationStorage = new ConcurrentHashMap<>();
  private long nextId = 1;

  private final KafkaMessageService kafkaMessageService;

  // Получить уведомления для списка объявлений
  public List<ModerationMessage> getUserNotifications(List<Long> adIds) {
    log.info("Загрузка уведомлений для объявлений: {}", adIds);
    if (adIds == null || adIds.isEmpty()) {
      return List.of();
    }

    List<ModerationMessage> result = new ArrayList<>();
    for (Long adId : adIds) {
      List<ModerationMessage> notifications = notificationStorage.get(adId);
      if (notifications != null) {
        result.addAll(notifications);
      }
    }
    return result;
  }

  // Получить количество непрочитанных
  public int getUnreadCount(List<Long> adIds) {
    log.info("Подсчет непрочитанных уведомлений для объявлений: {}", adIds);

    if (adIds == null || adIds.isEmpty()) {
      return 0;
    }

    int count = 0;
    for (Long adId : adIds) {
      List<ModerationMessage> notifications = notificationStorage.get(adId);
      if (notifications != null) {
        count += notifications.stream().filter(n -> !n.getIsRead()).count();
      }
    }
    return count;
  }

  // Пометить как прочитанное
  public boolean markAsRead(Long notificationId) {
    log.info("Пометка уведомления {} как прочитанного", notificationId);

    for (List<ModerationMessage> notifications : notificationStorage.values()) {
      for (ModerationMessage msg : notifications) {
        if (msg.getId().equals(notificationId)) {
          msg.setIsRead(true);
          kafkaMessageService.sendNotificationEvent(
              "notification.read",
              String.valueOf(notificationId),
              new KafkaEventPayloads.NotificationRead(notificationId)
          );
          return true;
        }
      }
    }
    return false;
  }

  // Удалить уведомление
  public boolean deleteNotification(Long notificationId) {
    log.info("Удаление уведомления {}", notificationId);

    for (List<ModerationMessage> notifications : notificationStorage.values()) {
      if (notifications.removeIf(n -> n.getId().equals(notificationId))) {
        kafkaMessageService.sendNotificationEvent(
            "notification.deleted",
            String.valueOf(notificationId),
            new KafkaEventPayloads.NotificationDeleted(notificationId)
        );
        return true;
      }
    }
    return false;
  }

  // Удалить все уведомления для объявлений
  public boolean deleteAllNotifications(List<Long> adIds) {
    log.info("Удаление всех уведомлений для объявлений: {}", adIds);

    try {
      if (adIds == null || adIds.isEmpty()) {
        return true;
      }

      for (Long adId : adIds) {
        notificationStorage.remove(adId);
      }
      kafkaMessageService.sendNotificationEvent(
          "notification.all_deleted",
          "bulk",
          Map.of("adIds", adIds)
      );
      return true;
    } catch (Exception e) {
      log.error("Ошибка при удалении всех уведомлений: {}", e.getMessage());
      return false;
    }
  }

  public boolean markAllAsRead(List<Long> adIds) {
    log.info("Пометка всех уведомлений как прочитанных для объявлений: {}", adIds);

    if (adIds == null || adIds.isEmpty()) {
      return true;
    }

    for (Long adId : adIds) {
      List<ModerationMessage> notifications = notificationStorage.get(adId);
      if (notifications != null) {
        notifications.forEach(msg -> msg.setIsRead(true));
      }
    }
    kafkaMessageService.sendNotificationEvent(
        "notification.all_read",
        "bulk",
        Map.of("adIds", adIds)
    );
    return true;
  }

  // Создать тестовое уведомление
  public ModerationMessage createNotification(Long adId, String action, String reason, String moderatorEmail) {
    ModerationMessage msg = new ModerationMessage();
    msg.setId(nextId++);
    msg.setAdId(adId);
    msg.setAction(action);
    msg.setReason(reason);
    msg.setModeratorEmail(moderatorEmail);
    msg.setCreatedAt(LocalDateTime.now());
    msg.setIsRead(false);

    notificationStorage.computeIfAbsent(adId, k -> new ArrayList<>()).add(msg);
    kafkaMessageService.sendNotificationEvent(
        "notification.created",
        String.valueOf(msg.getId()),
        new KafkaEventPayloads.NotificationCreated(
            msg.getId(),
            adId,
            action,
            moderatorEmail,
            (reason != null && !reason.isBlank()) ? reason : null
        )
    );
    return msg;
  }
}