package com.mipt.portal.dto.kafka;

import java.util.List;

public final class KafkaEventPayloads {

  private KafkaEventPayloads() {
  }

  public record AnnouncementCreated(Long adId, Long authorId, String status) {
  }

  public record AnnouncementSentToModeration(Long adId, String status) {
  }

  public record AnnouncementStatusChanged(Long adId, String fromStatus, String toStatus, Long moderatorId, String reason) {
  }

  public record AnnouncementUpdated(Long adId, String status) {
  }

  public record BookingCreated(Long bookingId, Long adId, Long buyerId, String status) {
  }

  public record CommentCreated(Long commentId, Long adId, Long userId, String userName) {
  }

  public record CommentUpdated(Long commentId, Long adId, Long userId) {
  }

  public record CommentDeleted(Long commentId) {
  }

  public record SupportRequestCreated(Long requestId, Long userId, String userName) {
  }

  public record ModerationHistoryRecorded(Long historyId, Long adId, Long moderatorId, String fromStatus, String toStatus,
                                         String reason) {
  }

  public record ModerationActionLogged(Long messageId, Long adId, String action, Long moderatorId, String reason) {
  }

  public record ModerationMessageCreated(Long messageId, Long adId, String action, Long moderatorId, String reason) {
  }

  public record NotificationRead(Long notificationId) {
  }

  public record NotificationDeleted(Long notificationId) {
  }

  public record NotificationAllDeleted(List<Long> adIds) {
  }

  public record NotificationAllRead(List<Long> adIds) {
  }

  public record NotificationCreated(Long notificationId, Long adId, String action, String moderatorEmail, String reason) {
  }

  public record UserRegistered(Long userId, String email, String name) {
  }

  public record UserLogin(Long userId, String email) {
  }

  public record UserUpdated(Long userId, String email) {
  }

  public record UserAnnouncementChanged(Long userId, Long adId) {
  }

  public record UserDeleted(Long userId) {
  }

  public record UserRatingUpdated(Long userId, Double rating) {
  }

  public record UserFavoriteToggled(Long userId, Long adId, Boolean liked) {
  }

  public record UserCoinsChanged(Long userId, Integer coins, Integer balance) {
  }

  public record UserRoleChanged(Long userId, String role, String action) {
  }

  public record UserPasswordChanged(Long userId) {
  }

  public record UserDeletedSelf(Long userId) {
  }

  public record UserSanctionLifted(Long userId) {
  }

  public record UserSanctionApplied(Long userId, Long actorId, String type, Integer duration, String endAt, String reason) {
  }
}

