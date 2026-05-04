package com.mipt.portal.controller;

import com.mipt.portal.entity.SupportRequest;
import com.mipt.portal.entity.User;
import com.mipt.portal.service.SupportRequestService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class SupportControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private SupportRequestService supportRequestService;

  private MockHttpSession sessionFor(User u) {
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("user", u);
    return s;
  }

  private User user(String name, String email) {
    User u = new User();
    u.setId(1L);
    u.setName(name);
    u.setEmail(email);
    return u;
  }

  @Test
  void getMessages_unauthorized() throws Exception {
    mockMvc.perform(get("/api/support/messages"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getMessages_ok() throws Exception {
    when(supportRequestService.getByUserId(1L)).thenReturn(List.of(new SupportRequest()));
    mockMvc.perform(get("/api/support/messages").session(sessionFor(user("Ivan", "i@x.com"))))
        .andExpect(status().isOk());
  }

  @Test
  void postMessage_unauthorized() throws Exception {
    mockMvc.perform(post("/api/support/messages")
            .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"hi\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void postMessage_ok() throws Exception {
    when(supportRequestService.create(anyLong(), anyString(), anyString()))
        .thenReturn(new SupportRequest());
    mockMvc.perform(post("/api/support/messages")
            .session(sessionFor(user("Ivan", "i@x.com")))
            .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"hi\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void postMessage_fallbackUsername() throws Exception {
    when(supportRequestService.create(anyLong(), anyString(), anyString()))
        .thenReturn(new SupportRequest());
    User u = user(null, "i@x.com");
    mockMvc.perform(post("/api/support/messages")
            .session(sessionFor(u))
            .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"hi\"}"))
        .andExpect(status().isCreated());
  }
}
