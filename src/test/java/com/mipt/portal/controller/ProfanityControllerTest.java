package com.mipt.portal.controller;

import com.mipt.portal.service.ProfanityChecker;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ProfanityControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private ProfanityChecker profanityChecker;

  @Test
  void check_postBody_returnsResult() throws Exception {
    when(profanityChecker.containsProfanity("hello")).thenReturn(false);
    mockMvc.perform(post("/api/profanity/check")
            .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"hello\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasProfanity").value(false));
  }

  @Test
  void check_postParam_returnsResult() throws Exception {
    when(profanityChecker.containsProfanity("hi")).thenReturn(true);
    mockMvc.perform(post("/api/profanity/check").param("text", "hi"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasProfanity").value(true));
  }

  @Test
  void check_postWithNullPayload() throws Exception {
    when(profanityChecker.containsProfanity(null)).thenReturn(false);
    mockMvc.perform(post("/api/profanity/check"))
        .andExpect(status().isOk());
  }

  @Test
  void checkForm_param() throws Exception {
    when(profanityChecker.containsProfanity("yo")).thenReturn(false);
    mockMvc.perform(post("/api/profanity/check-form").param("text", "yo"))
        .andExpect(status().isOk());
  }

  @Test
  void checkGet_param() throws Exception {
    when(profanityChecker.containsProfanity("yo")).thenReturn(false);
    mockMvc.perform(get("/api/profanity/check").param("text", "yo"))
        .andExpect(status().isOk());
  }

  @Test
  void check_bodyAndParam_prefersBody() throws Exception {
    when(profanityChecker.containsProfanity(eq("body"))).thenReturn(true);
    when(profanityChecker.containsProfanity(eq("param"))).thenReturn(false);
    mockMvc.perform(post("/api/profanity/check")
            .contentType(MediaType.APPLICATION_JSON)
            .param("text", "param")
            .content("{\"text\":\"body\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasProfanity").value(true));
  }
}
