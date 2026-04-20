package com.mipt.portal.controller;

import com.mipt.portal.service.KafkaMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class KafkaTestController {

  private final KafkaMessageService kafkaMessageService;

  public KafkaTestController(KafkaMessageService kafkaMessageService) {
    this.kafkaMessageService = kafkaMessageService;
  }

  @PostMapping("/test")
  public ResponseEntity<String> sendTestMessage(@RequestBody TestMessageRequest request) {
    String key = request.key() == null ? "test" : request.key();
    String payload = request.payload() == null ? "" : request.payload();
    kafkaMessageService.sendAuditEvent(key, payload);
    return ResponseEntity.ok("Message sent to Kafka");
  }

  public record TestMessageRequest(String key, String payload) {
  }
}

