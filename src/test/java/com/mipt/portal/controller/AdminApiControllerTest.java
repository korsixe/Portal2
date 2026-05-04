package com.mipt.portal.controller;

import com.mipt.portal.entity.User;
import com.mipt.portal.exception.InsufficientCoinsException;
import com.mipt.portal.repository.AdminActionAuditRepository;
import com.mipt.portal.service.AdminService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminApiControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;
  @MockBean private AdminService adminService;
  @MockBean private AdminActionAuditRepository adminActionAuditRepository;

  private void asAdmin() {
    User u = new User();
    u.setId(1L);
    u.setEmail("admin@phystech.edu");
    when(userService.findUserByEmail("admin@phystech.edu")).thenReturn(Optional.of(u));
  }

  @Test
  void dashboard_returnsUsersAndStats() throws Exception {
    asAdmin();
    when(userService.getAllUsers()).thenReturn(List.of());
    when(userService.buildSystemStats())
        .thenReturn(new com.mipt.portal.dto.SystemStats(0, 0, 0, 0));
    mockMvc.perform(get("/api/admin/dashboard").with(user("admin@phystech.edu").roles("ADMIN")))
        .andExpect(status().isOk());
  }

  @Test
  void dashboard_403WhenNotAdmin() throws Exception {
    mockMvc.perform(get("/api/admin/dashboard").with(user("u").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void actions_returnsList() throws Exception {
    when(adminActionAuditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
    mockMvc.perform(get("/api/admin/actions").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk());
  }

  @Test
  void manageRole_promoteModerator_success() throws Exception {
    asAdmin();
    when(adminService.promoteToModerator(anyLong(), anyLong())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/role")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"action\":\"assign\",\"role\":\"MODERATOR\",\"reason\":\"\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void manageRole_demoteModerator() throws Exception {
    asAdmin();
    when(adminService.demoteFromModerator(anyLong(), anyLong())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/role")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"action\":\"revoke\",\"role\":\"MODERATOR\",\"reason\":\"\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageRole_promoteAdmin() throws Exception {
    asAdmin();
    when(adminService.promoteToAdmin(anyLong(), anyLong())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/role")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"action\":\"assign\",\"role\":\"ADMIN\",\"reason\":\"\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageRole_demoteAdmin() throws Exception {
    asAdmin();
    when(adminService.demoteFromAdmin(anyLong(), anyLong())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/role")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"action\":\"revoke\",\"role\":\"ADMIN\",\"reason\":\"\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageRole_unknownRoleFails() throws Exception {
    asAdmin();
    mockMvc.perform(post("/api/admin/role")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"action\":\"assign\",\"role\":\"NONE\",\"reason\":\"\"}"))
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void manageCoins_addOk() throws Exception {
    asAdmin();
    when(adminService.addCoinsToUser(anyLong(), anyLong(), anyInt())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/coins")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"amount\":50,\"action\":\"add\",\"reason\":\"\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageCoins_deductOk() throws Exception {
    asAdmin();
    when(adminService.deductCoinsFromUser(anyLong(), anyLong(), anyInt())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/coins")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"amount\":10,\"action\":\"deduct\",\"reason\":\"\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageCoins_invalidAction400() throws Exception {
    asAdmin();
    mockMvc.perform(post("/api/admin/coins")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"amount\":1,\"action\":\"foo\",\"reason\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void manageCoins_failureReturnsBadRequest() throws Exception {
    asAdmin();
    when(adminService.addCoinsToUser(anyLong(), anyLong(), anyInt())).thenReturn(Optional.of(false));
    mockMvc.perform(post("/api/admin/coins")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"amount\":1,\"action\":\"add\",\"reason\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void manageCoins_insufficient409() throws Exception {
    asAdmin();
    when(adminService.deductCoinsFromUser(anyLong(), anyLong(), anyInt()))
        .thenThrow(new InsufficientCoinsException(2L, 5, 100));
    mockMvc.perform(post("/api/admin/coins")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"amount\":100,\"action\":\"deduct\",\"reason\":\"\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void manageSanction_freezeOk() throws Exception {
    asAdmin();
    when(adminService.freezeUser(anyLong(), anyLong(), anyString(), anyInt())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/sanction")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"reason\":\"x\",\"duration\":24,\"type\":\"freeze\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageSanction_banOk() throws Exception {
    asAdmin();
    when(adminService.banUser(anyLong(), anyLong(), anyString(), anyInt())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/sanction")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"reason\":\"x\",\"duration\":7,\"type\":\"ban\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageSanction_liftOk() throws Exception {
    asAdmin();
    when(adminService.liftSanctions(anyLong(), anyLong())).thenReturn(Optional.of(true));
    mockMvc.perform(post("/api/admin/sanction")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"reason\":\"\",\"duration\":0,\"type\":\"lift\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void manageSanction_unknown_failure() throws Exception {
    asAdmin();
    mockMvc.perform(post("/api/admin/sanction")
            .with(user("admin@phystech.edu").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetUserId\":2,\"reason\":\"\",\"duration\":0,\"type\":\"foo\"}"))
        .andExpect(jsonPath("$.success").value(false));
  }
}
