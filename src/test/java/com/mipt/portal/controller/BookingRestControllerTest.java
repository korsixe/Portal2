package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Booking;
import com.mipt.portal.service.BookingService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AbstractWebMvcTest.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class BookingRestControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private BookingService bookingService;

  private MockHttpSession sessionWithUser() {
    MockHttpSession s = new MockHttpSession();
    s.setAttribute("userId", 1L);
    return s;
  }

  @Test
  void book_unauthorizedNoSession() throws Exception {
    mockMvc.perform(post("/api/v1/bookings/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void book_okWhenSuccess() throws Exception {
    Booking b = new Booking();
    b.setId(99L);
    when(bookingService.bookAnnouncement(1L, 1L)).thenReturn(b);
    mockMvc.perform(post("/api/v1/bookings/1").session(sessionWithUser()))
        .andExpect(status().isOk());
  }

  @Test
  void book_400OnException() throws Exception {
    when(bookingService.bookAnnouncement(anyLong(), anyLong()))
        .thenThrow(new RuntimeException("not available"));
    mockMvc.perform(post("/api/v1/bookings/1").session(sessionWithUser()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void confirm_unauthorized() throws Exception {
    mockMvc.perform(post("/api/v1/bookings/1/confirm"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void confirm_ok() throws Exception {
    mockMvc.perform(post("/api/v1/bookings/1/confirm").session(sessionWithUser()))
        .andExpect(status().isOk());
  }

  @Test
  void confirm_400OnException() throws Exception {
    doThrow(new RuntimeException("err")).when(bookingService).confirmSale(anyLong(), anyLong());
    mockMvc.perform(post("/api/v1/bookings/1/confirm").session(sessionWithUser()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void cancel_unauthorized() throws Exception {
    mockMvc.perform(delete("/api/v1/bookings/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void cancel_ok() throws Exception {
    mockMvc.perform(delete("/api/v1/bookings/1").session(sessionWithUser()))
        .andExpect(status().isOk());
  }

  @Test
  void cancel_400OnException() throws Exception {
    doThrow(new RuntimeException("err")).when(bookingService).cancelBooking(anyLong(), anyLong());
    mockMvc.perform(delete("/api/v1/bookings/1").session(sessionWithUser()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void myBookedAds_unauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/bookings/my"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void myBookedAds_ok() throws Exception {
    when(bookingService.getBookedAdsForBuyer(1L)).thenReturn(List.of(new Announcement()));
    mockMvc.perform(get("/api/v1/bookings/my").session(sessionWithUser()))
        .andExpect(status().isOk());
  }
}
