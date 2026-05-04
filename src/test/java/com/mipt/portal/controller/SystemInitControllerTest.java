package com.mipt.portal.controller;

import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class SystemInitControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;

  @Test
  void initAdmin_failsIfAdminsExist() throws Exception {
    when(userService.getAllAdmins()).thenReturn(List.of(new User()));
    mockMvc.perform(post("/api/system/init-admin")
            .param("email", "x@phystech.edu")
            .param("name", "X")
            .param("password", "Password123!")
            .param("passwordConfirm", "Password123!"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void initAdmin_failsWhenRegistrationReturnsEmpty() throws Exception {
    when(userService.getAllAdmins()).thenReturn(List.of());
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.empty());
    mockMvc.perform(post("/api/system/init-admin")
            .param("email", "x@phystech.edu")
            .param("name", "X")
            .param("password", "Password123!")
            .param("passwordConfirm", "Password123!"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void initAdmin_failsWhenRoleAssignFails() throws Exception {
    User user = new User();
    user.setId(1L);
    when(userService.getAllAdmins()).thenReturn(List.of());
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.of(user));
    when(userService.assignAdminRole(anyLong())).thenReturn(Optional.empty());
    mockMvc.perform(post("/api/system/init-admin")
            .param("email", "x@phystech.edu")
            .param("name", "X")
            .param("password", "Password123!")
            .param("passwordConfirm", "Password123!"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void initAdmin_succeeds() throws Exception {
    User user = new User();
    user.setId(1L);
    when(userService.getAllAdmins()).thenReturn(List.of());
    when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyInt()))
        .thenReturn(Optional.of(user));
    when(userService.assignAdminRole(anyLong())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/system/init-admin")
            .param("email", "x@phystech.edu")
            .param("name", "X")
            .param("password", "Password123!")
            .param("passwordConfirm", "Password123!"))
        .andExpect(status().isOk());
  }

  @Test
  void initAdmin_handlesException() throws Exception {
    when(userService.getAllAdmins()).thenThrow(new RuntimeException("err"));
    mockMvc.perform(post("/api/system/init-admin")
            .param("email", "x@phystech.edu")
            .param("name", "X")
            .param("password", "Password123!")
            .param("passwordConfirm", "Password123!"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void status_returnsCounts() throws Exception {
    when(userService.getAllAdmins()).thenReturn(List.of());
    when(userService.getAllModerators()).thenReturn(List.of());
    when(userService.getAllUsers()).thenReturn(List.of());
    mockMvc.perform(get("/api/system/status"))
        .andExpect(status().isOk());
  }

  @Test
  void status_handlesException() throws Exception {
    when(userService.getAllAdmins()).thenThrow(new RuntimeException("err"));
    mockMvc.perform(get("/api/system/status"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void needsInit_trueWhenNoAdmins() throws Exception {
    when(userService.getAllAdmins()).thenReturn(List.of());
    mockMvc.perform(get("/api/system/needs-init"))
        .andExpect(status().isOk());
  }

  @Test
  void needsInit_handlesException() throws Exception {
    when(userService.getAllAdmins()).thenThrow(new RuntimeException("err"));
    mockMvc.perform(get("/api/system/needs-init"))
        .andExpect(status().isInternalServerError());
  }
}
