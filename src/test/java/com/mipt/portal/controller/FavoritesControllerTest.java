package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.UserService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class FavoritesControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserService userService;
  @MockBean private AnnouncementService announcementService;

  private MockHttpSession session() {
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("userId", 1L);
    return s;
  }

  @Test
  void getFavoriteIds_unauthorized() throws Exception {
    mockMvc.perform(get("/api/favorites"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getFavoriteIds_returnsList() throws Exception {
    when(userService.getFavoriteIds(1L)).thenReturn(List.of(10L));
    mockMvc.perform(get("/api/favorites").session(session()))
        .andExpect(status().isOk());
  }

  @Test
  void getFavoriteAds_unauthorized() throws Exception {
    mockMvc.perform(get("/api/favorites/ads"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getFavoriteAds_returnsList() throws Exception {
    when(userService.getFavoriteIds(1L)).thenReturn(List.of(10L));
    when(announcementService.findAllByIds(List.of(10L))).thenReturn(List.of(new Announcement()));
    mockMvc.perform(get("/api/favorites/ads").session(session()))
        .andExpect(status().isOk());
  }

  @Test
  void toggleFavorite_unauthorized() throws Exception {
    mockMvc.perform(post("/api/favorites/10"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void toggleFavorite_returnsLiked() throws Exception {
    when(userService.toggleFavorite(1L, 10L)).thenReturn(true);
    mockMvc.perform(post("/api/favorites/10").session(session()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").value(true));
  }
}
