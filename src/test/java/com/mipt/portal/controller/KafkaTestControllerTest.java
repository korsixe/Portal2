package com.mipt.portal.controller;

import com.mipt.portal.service.KafkaMessageService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class KafkaTestControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private KafkaMessageService kafkaMessageService;

  @Test
  void sendTestMessage_callsAuditEvent() throws Exception {
    mockMvc.perform(post("/api/kafka/test")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"key\":\"k\",\"payload\":\"v\"}"))
        .andExpect(status().isOk());
    verify(kafkaMessageService).sendAuditEvent(eq("k"), eq("v"));
  }

  @Test
  void sendTestMessage_defaultsKey() throws Exception {
    mockMvc.perform(post("/api/kafka/test")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
    verify(kafkaMessageService).sendAuditEvent(eq("test"), eq(""));
  }
}
