package com.mipt.portal.controller;

import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ChangePasswordControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;

  private User user() {
    User u = new User();
    u.setId(1L);
    u.setEmail("u@phystech.edu");
    return u;
  }

  private RequestBuilder postWithUser(String body) {
    return post("/api/user/change-password")
        .requestAttr("user", user())
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
  }

  @Test
  void invalidCurrentPassword_returns400() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.empty());
    mockMvc.perform(postWithUser("{\"currentPassword\":\"wrong\",\"newPassword\":\"newPassword1\",\"confirmPassword\":\"newPassword1\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shortNewPassword_returns400() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(user()));
    mockMvc.perform(postWithUser("{\"currentPassword\":\"old\",\"newPassword\":\"abc\",\"confirmPassword\":\"abc\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void mismatchedPasswords_returns400() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(user()));
    mockMvc.perform(postWithUser("{\"currentPassword\":\"old\",\"newPassword\":\"newPassword1\",\"confirmPassword\":\"differentX\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateFailure_returns500() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(user()));
    when(userService.updateUser(any(User.class))).thenReturn(Optional.empty());
    mockMvc.perform(postWithUser("{\"currentPassword\":\"old\",\"newPassword\":\"newPassword1\",\"confirmPassword\":\"newPassword1\"}"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void success_returns200() throws Exception {
    when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(user()));
    when(userService.updateUser(any(User.class))).thenReturn(Optional.of(user()));
    mockMvc.perform(postWithUser("{\"currentPassword\":\"old\",\"newPassword\":\"newPassword1\",\"confirmPassword\":\"newPassword1\"}"))
        .andExpect(status().isOk());
  }
}
