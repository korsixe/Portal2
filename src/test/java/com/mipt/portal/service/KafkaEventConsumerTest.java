package com.mipt.portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KafkaEventConsumerTest {

  private KafkaEventConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new KafkaEventConsumer(new ObjectMapper());
  }

  @Test
  void onAuditEvent_logsValid() {
    consumer.onAuditEvent("{\"type\":\"audit.event\",\"timestamp\":\"2026-01-01T00:00:00Z\",\"payload\":{}}");
  }

  @Test
  void onAuditEvent_handlesInvalidJson() {
    consumer.onAuditEvent("not json");
  }

  @Test
  void allListeners_invokedWithoutException() {
    String json = "{\"type\":\"x\",\"timestamp\":\"t\",\"payload\":null}";
    consumer.onUserEvent(json);
    consumer.onAnnouncementEvent(json);
    consumer.onModerationEvent(json);
    consumer.onBookingEvent(json);
    consumer.onCommentEvent(json);
    consumer.onSupportEvent(json);
    consumer.onNotificationEvent(json);
  }
}
