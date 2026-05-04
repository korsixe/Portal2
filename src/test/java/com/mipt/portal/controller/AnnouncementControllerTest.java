package com.mipt.portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Comment;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.ElasticSearchService;
import com.mipt.portal.service.MediaService;
import com.mipt.portal.service.ModerationHistoryService;
import com.mipt.portal.service.ProfanityChecker;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AnnouncementControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private AnnouncementService service;
  @MockBean private MediaService mediaService;
  @MockBean private ModerationHistoryService moderationHistoryService;
  @MockBean private ProfanityChecker profanityChecker;
  @MockBean private ElasticSearchService elasticSearchService;

  private User currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("u@phystech.edu");
    currentUser.setName("Ivan");
  }

  @Test
  void create_returns201() throws Exception {
    Announcement ad = new Announcement();
    ad.setId(1L);
    when(service.create(any())).thenReturn(ad);
    mockMvc.perform(post("/api/announcements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"t","description":"d","category":"ELECTRONICS","subcategory":"x",
                 "location":"M","condition":"USED","price":100,"authorId":1}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void uploadPhoto_returnsOk() throws Exception {
    Announcement ad = new Announcement();
    ad.setId(1L);
    when(service.findById(1L)).thenReturn(ad);
    when(mediaService.multipartFileToBytes(any())).thenReturn(new byte[]{1});
    MockMultipartFile photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", new byte[]{1, 2, 3});
    mockMvc.perform(multipart("/api/announcements/1/photo").file(photo))
        .andExpect(status().isOk());
  }

  @Test
  void uploadPhoto_404WhenAdMissing() throws Exception {
    when(service.findById(1L)).thenReturn(null);
    MockMultipartFile photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", new byte[]{1});
    mockMvc.perform(multipart("/api/announcements/1/photo").file(photo))
        .andExpect(status().isNotFound());
  }

  @Test
  void uploadPhoto_400WhenEmpty() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    MockMultipartFile photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", new byte[0]);
    mockMvc.perform(multipart("/api/announcements/1/photo").file(photo))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uploadPhoto_500WhenError() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    when(mediaService.multipartFileToBytes(any())).thenThrow(new IOException("boom"));
    MockMultipartFile photo = new MockMultipartFile("photo", "p.jpg", "image/jpeg", new byte[]{1});
    mockMvc.perform(multipart("/api/announcements/1/photo").file(photo))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getCategories_returnsList() throws Exception {
    when(service.getAllCategories()).thenReturn(List.of());
    mockMvc.perform(get("/api/announcements/categories"))
        .andExpect(status().isOk());
  }

  @Test
  void getSubcategories_returnsList() throws Exception {
    when(service.getSubcategoriesByCategory(1L)).thenReturn(List.of());
    mockMvc.perform(get("/api/announcements/categories/1/subcategories"))
        .andExpect(status().isOk());
  }

  @Test
  void search_returnsList() throws Exception {
    when(service.searchApproved(any(), any(), any())).thenReturn(List.of());
    mockMvc.perform(get("/api/announcements/search").param("sortBy", "price"))
        .andExpect(status().isOk());
  }

  @Test
  void getPending_returnsList() throws Exception {
    when(service.getPendingForModerator()).thenReturn(List.of());
    mockMvc.perform(get("/api/announcements/moderator/pending"))
        .andExpect(status().isOk());
  }

  @Test
  void sendToModeration_returnsOk() throws Exception {
    mockMvc.perform(post("/api/announcements/1/send-to-moderation"))
        .andExpect(status().isOk());
  }

  @Test
  void approve_ok() throws Exception {
    Announcement ad = new Announcement();
    ad.setId(1L);
    when(service.changeStatus(eq(1L), eq(AdStatus.ACTIVE), eq(null), eq(null)))
        .thenReturn(Optional.of(ad));
    mockMvc.perform(post("/api/announcements/1/approve").with(user("mod").roles("MODERATOR")))
        .andExpect(status().isOk());
  }

  @Test
  void approve_404WhenAbsent() throws Exception {
    when(service.changeStatus(anyLong(), any(), any(), any())).thenReturn(Optional.empty());
    mockMvc.perform(post("/api/announcements/1/approve").with(user("mod").roles("MODERATOR")))
        .andExpect(status().isNotFound());
  }

  @Test
  void getHistory_returnsList() throws Exception {
    when(moderationHistoryService.getHistory(1L)).thenReturn(List.of());
    mockMvc.perform(get("/api/announcements/1/history").with(user("mod").roles("MODERATOR")))
        .andExpect(status().isOk());
  }

  @Test
  void myAds_unauthorized() throws Exception {
    mockMvc.perform(get("/api/announcements/my"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void myAds_returnsActive() throws Exception {
    Announcement live = new Announcement();
    live.setStatus(AdStatus.ACTIVE);
    Announcement deleted = new Announcement();
    deleted.setStatus(AdStatus.DELETED);
    when(service.findAllByAuthorId(1L)).thenReturn(List.of(live, deleted));
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(get("/api/announcements/my").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getById_ok() throws Exception {
    Announcement ad = new Announcement();
    ad.setId(1L);
    when(service.findById(1L)).thenReturn(ad);
    mockMvc.perform(get("/api/announcements/1"))
        .andExpect(status().isOk());
  }

  @Test
  void getById_404() throws Exception {
    when(service.findById(99L)).thenReturn(null);
    mockMvc.perform(get("/api/announcements/99"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getDetails_ok() throws Exception {
    Announcement ad = new Announcement();
    ad.setAuthorId(1L);
    when(service.findById(1L)).thenReturn(ad);
    when(service.getAuthorName(1L)).thenReturn("Ivan");
    when(service.getPhotoCount(1L)).thenReturn(2);
    mockMvc.perform(get("/api/announcements/1/details"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authorName").value("Ivan"))
        .andExpect(jsonPath("$.photoCount").value(2));
  }

  @Test
  void getDetails_404() throws Exception {
    when(service.findById(99L)).thenReturn(null);
    mockMvc.perform(get("/api/announcements/99/details"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getComments_ok() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    when(service.getCommentsByAdId(1L)).thenReturn(List.of(new Comment()));
    mockMvc.perform(get("/api/announcements/1/comments"))
        .andExpect(status().isOk());
  }

  @Test
  void getComments_404() throws Exception {
    when(service.findById(99L)).thenReturn(null);
    mockMvc.perform(get("/api/announcements/99/comments"))
        .andExpect(status().isNotFound());
  }

  @Test
  void addComment_unauthorized() throws Exception {
    mockMvc.perform(post("/api/announcements/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"hi\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void addComment_404WhenAdMissing() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    when(service.findById(1L)).thenReturn(null);
    mockMvc.perform(post("/api/announcements/1/comments")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"hi\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void addComment_badRequestOnProfanity() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    when(profanityChecker.containsProfanity("bad")).thenReturn(true);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(post("/api/announcements/1/comments")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"bad\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addComment_created() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    when(profanityChecker.containsProfanity("hi")).thenReturn(false);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(post("/api/announcements/1/comments")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"hi\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void addComment_internalErrorOnException() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    when(profanityChecker.containsProfanity("hi")).thenReturn(false);
    doThrow(new RuntimeException("db")).when(service).addComment(any(), any(), any(), any());
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(post("/api/announcements/1/comments")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"hi\"}"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void addComment_fallbackUsernameToEmail() throws Exception {
    Announcement ad = new Announcement();
    when(service.findById(1L)).thenReturn(ad);
    when(profanityChecker.containsProfanity("hi")).thenReturn(false);
    User noName = new User();
    noName.setId(1L);
    noName.setEmail("e@phystech.edu");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", noName);
    mockMvc.perform(post("/api/announcements/1/comments")
            .session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"hi\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void update_unauthorized() throws Exception {
    mockMvc.perform(put("/api/announcements/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"t","description":"d","category":"Электроника","subcategory":"x",
                 "location":"M","condition":"USED","action":"draft","price":100}
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void update_404WhenAdMissing() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    when(service.findById(1L)).thenReturn(null);
    mockMvc.perform(put("/api/announcements/1").session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"t","description":"d","category":"Электроника","subcategory":"x",
                 "location":"M","condition":"USED","action":"draft","price":100}
                """))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_403WhenNotOwner() throws Exception {
    Announcement ad = new Announcement();
    ad.setAuthorId(999L);
    when(service.findById(1L)).thenReturn(ad);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(put("/api/announcements/1").session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"t","description":"d","category":"Электроника","subcategory":"x",
                 "location":"M","condition":"USED","action":"draft","price":100}
                """))
        .andExpect(status().isForbidden());
  }

  @Test
  void update_succeedsAndPublishesWhenAction() throws Exception {
    Announcement ad = new Announcement();
    ad.setAuthorId(1L);
    ad.setId(1L);
    when(service.findById(1L)).thenReturn(ad);
    when(service.save(any())).thenReturn(ad);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(put("/api/announcements/1").session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"t","description":"d","category":"Электроника","subcategory":"x",
                 "location":"M","condition":"USED","action":"publish","price":100}
                """))
        .andExpect(status().isOk());
  }

  @Test
  void delete_unauthorized() throws Exception {
    mockMvc.perform(delete("/api/announcements/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void delete_404() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    when(service.findById(1L)).thenReturn(null);
    mockMvc.perform(delete("/api/announcements/1").session(session))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_403WhenNotOwner() throws Exception {
    Announcement ad = new Announcement();
    ad.setAuthorId(999L);
    when(service.findById(1L)).thenReturn(ad);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(delete("/api/announcements/1").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void delete_okWhenOwner() throws Exception {
    Announcement ad = new Announcement();
    ad.setAuthorId(1L);
    when(service.findById(1L)).thenReturn(ad);
    when(service.changeStatus(any(), any(), any(), any())).thenReturn(Optional.of(ad));
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("user", currentUser);
    mockMvc.perform(delete("/api/announcements/1").session(session))
        .andExpect(status().isOk());
  }

  @Test
  void elasticSearch_ok() throws Exception {
    when(elasticSearchService.searchWithTypos("foo")).thenReturn(List.of());
    mockMvc.perform(get("/api/announcements/elastic-search").param("query", "foo"))
        .andExpect(status().isOk());
  }

  @Test
  void reindex_ok() throws Exception {
    when(elasticSearchService.reindexAll()).thenReturn(5);
    mockMvc.perform(post("/api/announcements/reindex"))
        .andExpect(status().isOk());
  }
}
