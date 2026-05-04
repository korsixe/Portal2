package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AdPhotoControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private AnnouncementService announcementService;

  @Test
  void getPhoto_404WhenAdMissing() throws Exception {
    when(announcementService.findById(1L)).thenReturn(null);
    mockMvc.perform(get("/ad-photo").param("adId", "1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getPhoto_404WhenNoPhoto() throws Exception {
    Announcement ad = new Announcement();
    ad.setPhoto(new byte[0]);
    when(announcementService.findById(1L)).thenReturn(ad);
    mockMvc.perform(get("/ad-photo").param("adId", "1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getPhoto_okWithBytes() throws Exception {
    Announcement ad = new Announcement();
    ad.setPhoto("hello".getBytes());
    when(announcementService.findById(1L)).thenReturn(ad);
    mockMvc.perform(get("/ad-photo").param("adId", "1"))
        .andExpect(status().isOk());
  }

  @Test
  void getPhoto_500OnException() throws Exception {
    when(announcementService.findById(1L)).thenThrow(new RuntimeException("err"));
    mockMvc.perform(get("/ad-photo").param("adId", "1"))
        .andExpect(status().isInternalServerError());
  }
}
