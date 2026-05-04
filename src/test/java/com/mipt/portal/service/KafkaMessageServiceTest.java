package com.mipt.portal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaMessageServiceTest {

  @SuppressWarnings("unchecked")
  private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
  private ObjectMapper objectMapper;
  private KafkaMessageService service;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    service = new KafkaMessageService(
        kafkaTemplate, objectMapper,
        "audit", "user", "announcement", "moderation",
        "booking", "comment", "support", "notification");
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(null));
  }

  @Test
  void sendAuditEvent_sendsToAuditTopic() {
    service.sendAuditEvent("k", "{}");
    verify(kafkaTemplate).send(eq("audit"), eq("k"), anyString());
  }

  @Test
  void sendUserEvent_sendsToUserTopic() {
    service.sendUserEvent("user.created", "k", "{}");
    verify(kafkaTemplate).send(eq("user"), eq("k"), anyString());
  }

  @Test
  void sendAnnouncementEvent_sendsToAnnouncementTopic() {
    service.sendAnnouncementEvent("a.created", "k", "{}");
    verify(kafkaTemplate).send(eq("announcement"), eq("k"), anyString());
  }

  @Test
  void sendModerationEvent_sendsToModerationTopic() {
    service.sendModerationEvent("m", "k", "{}");
    verify(kafkaTemplate).send(eq("moderation"), eq("k"), anyString());
  }

  @Test
  void sendBookingEvent_sendsToBookingTopic() {
    service.sendBookingEvent("b", "k", "{}");
    verify(kafkaTemplate).send(eq("booking"), eq("k"), anyString());
  }

  @Test
  void sendCommentEvent_sendsToCommentTopic() {
    service.sendCommentEvent("c", "k", "{}");
    verify(kafkaTemplate).send(eq("comment"), eq("k"), anyString());
  }

  @Test
  void sendSupportEvent_sendsToSupportTopic() {
    service.sendSupportEvent("s", "k", "{}");
    verify(kafkaTemplate).send(eq("support"), eq("k"), anyString());
  }

  @Test
  void sendNotificationEvent_sendsToNotificationTopic() {
    service.sendNotificationEvent("n", "k", "{}");
    verify(kafkaTemplate).send(eq("notification"), eq("k"), anyString());
  }

  @Test
  void sendEvent_swallowsSerializationFailures() throws Exception {
    ObjectMapper bad = mock(ObjectMapper.class);
    when(bad.writeValueAsString(org.mockito.ArgumentMatchers.any()))
        .thenThrow(new JsonProcessingException("oops") {});
    KafkaMessageService s2 = new KafkaMessageService(
        kafkaTemplate, bad,
        "a", "u", "an", "m", "b", "c", "s", "n");
    s2.sendUserEvent("e", "k", "p");
  }

  @Test
  void send_logsFailureFromKafkaCallback() {
    CompletableFuture<Object> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("send failed"));
    @SuppressWarnings({"unchecked", "rawtypes"})
    CompletableFuture cast = future;
    when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(cast);
    service.sendUserEvent("e", "k", "p");
    ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate).send(eq("user"), eq("k"), cap.capture());
  }
}
