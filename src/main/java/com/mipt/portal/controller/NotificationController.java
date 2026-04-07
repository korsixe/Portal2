package com.mipt.portal.controller;

import com.mipt.portal.entity.ModerationMessage;
import com.mipt.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/user")
  public ResponseEntity<List<ModerationMessage>> getUserNotifications(
    @RequestBody List<Long> adIds) {
    log.info("REST request to get notifications for adIds: {}", adIds);
    List<ModerationMessage> notifications = notificationService.getUserNotifications(adIds);
    return ResponseEntity.ok(notifications);
  }

  @PostMapping("/user/unread-count")
  public ResponseEntity<Map<String, Integer>> getUnreadCount(
    @RequestBody List<Long> adIds) {
    log.info("REST request to get unread count for adIds: {}", adIds);
    int count = notificationService.getUnreadCount(adIds);
    return ResponseEntity.ok(Map.of("unreadCount", count));
  }

  @PostMapping("/{notificationId}/read")
  public ResponseEntity<Map<String, Boolean>> markAsRead(
    @PathVariable Long notificationId) {
    log.info("REST request to mark notification {} as read", notificationId);
    boolean success = notificationService.markAsRead(notificationId);
    return ResponseEntity.ok(Map.of("success", success));
  }

  @PostMapping("/user/read-all")
  public ResponseEntity<Map<String, Boolean>> markAllAsRead(
    @RequestBody List<Long> adIds) {
    log.info("REST request to mark all notifications as read for adIds: {}", adIds);
    boolean success = notificationService.markAllAsRead(adIds);
    return ResponseEntity.ok(Map.of("success", success));
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Map<String, Boolean>> deleteNotification(
    @PathVariable Long notificationId) {
    log.info("REST request to delete notification {}", notificationId);
    boolean success = notificationService.deleteNotification(notificationId);
    return ResponseEntity.ok(Map.of("success", success));
  }

  @DeleteMapping("/user/all")
  public ResponseEntity<Map<String, Boolean>> deleteAllNotifications(
    @RequestBody List<Long> adIds) {
    log.info("REST request to delete all notifications for adIds: {}", adIds);
    boolean success = notificationService.deleteAllNotifications(adIds);
    return ResponseEntity.ok(Map.of("success", success));
  }
}