//package com.mipt.portal.notification;
//
//import com.mipt.portal.moderator.message.ModerationMessage;
//import com.mipt.portal.moderator.message.ModerationMessageRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class NotificationService {
//
//  private final ModerationMessageRepository messageRepository;
//
//  @Transactional(readOnly = true)
//  public List<ModerationMessage> getUserNotifications(List<Long> adIds) {
//    log.info("Загрузка уведомлений для объявлений: {}", adIds);
//
//    if (adIds == null || adIds.isEmpty()) {
//      return List.of();
//    }
//
//    return messageRepository.findByAdIds(adIds);
//  }
//
//  @Transactional(readOnly = true)
//  public int getUnreadCount(List<Long> adIds) {
//    log.info("Подсчет непрочитанных уведомлений для объявлений: {}", adIds);
//
//    if (adIds == null || adIds.isEmpty()) {
//      return 0;
//    }
//
//    return messageRepository.countUnreadByAdIds(adIds);
//  }
//
//  @Transactional
//  public boolean markAsRead(Long notificationId) {
//    log.info("Пометка уведомления {} как прочитанного", notificationId);
//
//    try {
//      int updated = messageRepository.markAsRead(notificationId);
//      return updated > 0;
//    } catch (Exception e) {
//      log.error("Ошибка при пометке уведомления {}: {}", notificationId, e.getMessage());
//      return false;
//    }
//  }
//
//  @Transactional
//  public boolean markAllAsRead(List<Long> adIds) {
//    log.info("Пометка всех уведомлений как прочитанных для объявлений: {}", adIds);
//
//    try {
//      if (adIds == null || adIds.isEmpty()) {
//        return true;
//      }
//      messageRepository.markAllAsReadByAdIds(adIds);
//      log.info("Все уведомления для объявлений {} помечены как прочитанные", adIds);
//      return true;
//    } catch (Exception e) {
//      log.error("Ошибка при пометке всех уведомлений: {}", e.getMessage());
//      return false;
//    }
//  }
//
//  @Transactional
//  public boolean deleteNotification(Long notificationId) {
//    log.info("Удаление уведомления {}", notificationId);
//
//    try {
//      int deleted = messageRepository.deleteByMessageId(notificationId);
//      return deleted > 0;
//    } catch (Exception e) {
//      log.error("Ошибка при удалении уведомления {}: {}", notificationId, e.getMessage());
//      return false;
//    }
//  }
//
//  @Transactional
//  public boolean deleteAllNotifications(List<Long> adIds) {
//    log.info("Удаление всех уведомлений для объявлений: {}", adIds);
//
//    try {
//      if (adIds == null || adIds.isEmpty()) {
//        return true;
//      }
//      messageRepository.deleteByAdIds(adIds);
//      log.info("Удалены уведомления для объявлений: {}", adIds);
//      return true;
//    } catch (Exception e) {
//      log.error("Ошибка при удалении всех уведомлений: {}", e.getMessage());
//      return false;
//    }
//  }
//}