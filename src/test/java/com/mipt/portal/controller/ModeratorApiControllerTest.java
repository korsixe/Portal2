package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.User;
import com.mipt.portal.repository.AdminActionAuditRepository;
import com.mipt.portal.repository.ModerationHistoryRepository;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.CommentService;
import com.mipt.portal.service.UserService;
import com.mipt.portal.service.AuditService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ModeratorApiControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private AnnouncementService announcementService;
  @MockBean private UserService userService;
  @MockBean private CommentService commentService;
  @MockBean private ModerationHistoryRepository moderationHistoryRepository;
  @MockBean private AdminActionAuditRepository adminActionAuditRepository;
  @MockBean private AuditService auditService;

  private void asMod() {
    User u = new User();
    u.setId(1L);
    u.setEmail("mod@phystech.edu");
    when(userService.findUserByEmail("mod@phystech.edu")).thenReturn(Optional.of(u));
  }

  @Test
  void dashboard_403WhenNotMod() throws Exception {
    mockMvc.perform(get("/api/moderator/dashboard").with(user("u").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void dashboard_okForModerator() throws Exception {
    asMod();
    when(announcementService.getPendingForModerator()).thenReturn(List.of(new Announcement()));
    when(userService.buildSystemStats())
        .thenReturn(new com.mipt.portal.dto.SystemStats(0, 0, 0, 0));
    mockMvc.perform(get("/api/moderator/dashboard").with(user("mod@phystech.edu").roles("MODERATOR")))
        .andExpect(status().isOk());
  }

  @Test
  void approve_success() throws Exception {
    asMod();
    when(announcementService.changeStatus(anyLong(), any(), any(), any()))
        .thenReturn(Optional.of(new Announcement()));
    mockMvc.perform(post("/api/moderator/approve")
            .with(user("mod@phystech.edu").roles("MODERATOR"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"adId\":1,\"reason\":\"\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void approve_failure() throws Exception {
    asMod();
    when(announcementService.changeStatus(anyLong(), any(), any(), any())).thenReturn(Optional.empty());
    mockMvc.perform(post("/api/moderator/approve")
            .with(user("mod@phystech.edu").roles("MODERATOR"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"adId\":1,\"reason\":\"\"}"))
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void reject_success() throws Exception {
    asMod();
    when(announcementService.changeStatus(anyLong(), any(), any(), any()))
        .thenReturn(Optional.of(new Announcement()));
    mockMvc.perform(post("/api/moderator/reject")
            .with(user("mod@phystech.edu").roles("MODERATOR"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"adId\":1,\"reason\":\"spam\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void delete_success() throws Exception {
    asMod();
    when(announcementService.changeStatus(anyLong(), any(), any(), any()))
        .thenReturn(Optional.of(new Announcement()));
    mockMvc.perform(post("/api/moderator/delete")
            .with(user("mod@phystech.edu").roles("MODERATOR"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"adId\":1,\"reason\":\"x\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteComment_success() throws Exception {
    asMod();
    mockMvc.perform(delete("/api/moderator/comments/1")
            .with(user("mod@phystech.edu").roles("MODERATOR")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void deleteComment_failureCaught() throws Exception {
    asMod();
    doThrow(new RuntimeException("db")).when(commentService).deleteComment(1L, 1L);
    mockMvc.perform(delete("/api/moderator/comments/1")
            .with(user("mod@phystech.edu").roles("MODERATOR")))
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void history_returnsBoth() throws Exception {
    when(moderationHistoryRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
    when(adminActionAuditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
    mockMvc.perform(get("/api/moderator/history")
            .with(user("mod@phystech.edu").roles("MODERATOR")))
        .andExpect(status().isOk());
  }
}

