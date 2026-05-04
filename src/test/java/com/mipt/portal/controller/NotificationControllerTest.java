package com.mipt.portal.controller;

import com.mipt.portal.service.NotificationService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private NotificationService notificationService;

  @Test
  void getUserNotifications_returnsList() throws Exception {
    when(notificationService.getUserNotifications(any())).thenReturn(List.of());
    mockMvc.perform(post("/api/notifications/user")
            .contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
        .andExpect(status().isOk());
  }

  @Test
  void getUnreadCount_returnsCount() throws Exception {
    when(notificationService.getUnreadCount(any())).thenReturn(7);
    mockMvc.perform(post("/api/notifications/user/unread-count")
            .contentType(MediaType.APPLICATION_JSON).content("[1]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unreadCount").value(7));
  }

  @Test
  void markAsRead_returnsSuccess() throws Exception {
    when(notificationService.markAsRead(1L)).thenReturn(true);
    mockMvc.perform(post("/api/notifications/1/read"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void markAllAsRead_returnsSuccess() throws Exception {
    when(notificationService.markAllAsRead(any())).thenReturn(true);
    mockMvc.perform(post("/api/notifications/user/read-all")
            .contentType(MediaType.APPLICATION_JSON).content("[1]"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteNotification_returnsSuccess() throws Exception {
    when(notificationService.deleteNotification(1L)).thenReturn(true);
    mockMvc.perform(delete("/api/notifications/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void deleteAll_returnsSuccess() throws Exception {
    when(notificationService.deleteAllNotifications(any())).thenReturn(true);
    mockMvc.perform(delete("/api/notifications/user/all")
            .contentType(MediaType.APPLICATION_JSON).content("[1]"))
        .andExpect(status().isOk());
  }
}
