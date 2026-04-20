package com.mipt.portal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageService {

  private static final Logger logger = LoggerFactory.getLogger(KafkaMessageService.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String auditTopic;
  private final String userTopic;
  private final String announcementTopic;
  private final String moderationTopic;
  private final String bookingTopic;
  private final String commentTopic;
  private final String supportTopic;
  private final String notificationTopic;

  public KafkaMessageService(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${app.kafka.topic.audit:portal.audit.events}") String auditTopic,
      @Value("${app.kafka.topic.user:portal.user.events}") String userTopic,
      @Value("${app.kafka.topic.announcement:portal.announcement.events}") String announcementTopic,
      @Value("${app.kafka.topic.moderation:portal.moderation.events}") String moderationTopic,
      @Value("${app.kafka.topic.booking:portal.booking.events}") String bookingTopic,
      @Value("${app.kafka.topic.comment:portal.comment.events}") String commentTopic,
      @Value("${app.kafka.topic.support:portal.support.events}") String supportTopic,
      @Value("${app.kafka.topic.notification:portal.notification.events}") String notificationTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.auditTopic = auditTopic;
    this.userTopic = userTopic;
    this.announcementTopic = announcementTopic;
    this.moderationTopic = moderationTopic;
    this.bookingTopic = bookingTopic;
    this.commentTopic = commentTopic;
    this.supportTopic = supportTopic;
    this.notificationTopic = notificationTopic;
  }

  public void sendAuditEvent(String key, String payload) {
    kafkaTemplate.send(auditTopic, key, payload);
  }

  public void sendUserEvent(String eventType, String key, Object payload) {
    sendEvent(userTopic, eventType, key, payload);
  }

  public void sendAnnouncementEvent(String eventType, String key, Object payload) {
    sendEvent(announcementTopic, eventType, key, payload);
  }

  public void sendModerationEvent(String eventType, String key, Object payload) {
    sendEvent(moderationTopic, eventType, key, payload);
  }

  public void sendBookingEvent(String eventType, String key, Object payload) {
    sendEvent(bookingTopic, eventType, key, payload);
  }

  public void sendCommentEvent(String eventType, String key, Object payload) {
    sendEvent(commentTopic, eventType, key, payload);
  }

  public void sendSupportEvent(String eventType, String key, Object payload) {
    sendEvent(supportTopic, eventType, key, payload);
  }

  public void sendNotificationEvent(String eventType, String key, Object payload) {
    sendEvent(notificationTopic, eventType, key, payload);
  }

  private void sendEvent(String topic, String eventType, String key, Object payload) {
    EventEnvelope envelope = new EventEnvelope(eventType, Instant.now().toString(), payload);

    try {
      String json = objectMapper.writeValueAsString(envelope);
      kafkaTemplate.send(topic, key, json);
    } catch (JsonProcessingException ex) {
      logger.warn("Failed to serialize Kafka event {}", eventType, ex);
    }
  }

  private record EventEnvelope(String type, String timestamp, Object payload) {
  }

  @KafkaListener(topics = "${app.kafka.topic.audit:portal.audit.events}")
  public void onAuditEvent(String payload) {
    logger.info("Kafka audit event received: {}", payload);
  }
}
