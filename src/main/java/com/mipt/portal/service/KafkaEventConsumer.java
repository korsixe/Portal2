package com.mipt.portal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(KafkaEventConsumer.class);
  private final ObjectMapper objectMapper;

  public KafkaEventConsumer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "${app.kafka.topic.audit:portal.audit.events}")
  public void onAuditEvent(String payload) {
    logEvent("audit", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.user:portal.user.events}")
  public void onUserEvent(String payload) {
    logEvent("user", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.announcement:portal.announcement.events}")
  public void onAnnouncementEvent(String payload) {
    logEvent("announcement", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.moderation:portal.moderation.events}")
  public void onModerationEvent(String payload) {
    logEvent("moderation", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.booking:portal.booking.events}")
  public void onBookingEvent(String payload) {
    logEvent("booking", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.comment:portal.comment.events}")
  public void onCommentEvent(String payload) {
    logEvent("comment", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.support:portal.support.events}")
  public void onSupportEvent(String payload) {
    logEvent("support", payload);
  }

  @KafkaListener(topics = "${app.kafka.topic.notification:portal.notification.events}")
  public void onNotificationEvent(String payload) {
    logEvent("notification", payload);
  }

  private void logEvent(String category, String payload) {
    try {
      Map<String, Object> envelope = objectMapper.readValue(payload, new TypeReference<>() {});
      String type = (String) envelope.get("type");
      String timestamp = (String) envelope.get("timestamp");
      logger.info("Kafka [{}] type={} timestamp={}", category, type, timestamp);
    } catch (JsonProcessingException e) {
      logger.warn("Kafka [{}] received non-JSON payload: {}", category, payload);
    }
  }
}
