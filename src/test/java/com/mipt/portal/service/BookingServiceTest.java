package com.mipt.portal.service;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Booking;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.repository.BookingRepository;
import com.mipt.portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock private BookingRepository bookingRepository;
  @Mock private AnnouncementRepository announcementRepository;
  @Mock private KafkaMessageService kafkaMessageService;
  @Mock private UserRepository userRepository;

  @InjectMocks private BookingService bookingService;

  private Announcement ad;

  @BeforeEach
  void setUp() {
    ad = new Announcement();
    ad.setId(10L);
    ad.setAuthorId(1L);
    ad.setStatus(AdStatus.ACTIVE);
  }

  @Test
  void getBookedAdsForBuyer_loadsAds() {
    Booking b = new Booking();
    b.setAnnouncementId(10L);
    when(bookingRepository.findAllByBuyerIdAndCancelledAtIsNullAndConfirmedAtIsNull(2L))
        .thenReturn(List.of(b));
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    assertThat(bookingService.getBookedAdsForBuyer(2L)).containsExactly(ad);
  }

  @Test
  void getBookedAdsForBuyer_skipsMissing() {
    Booking b = new Booking();
    b.setAnnouncementId(99L);
    when(bookingRepository.findAllByBuyerIdAndCancelledAtIsNullAndConfirmedAtIsNull(2L))
        .thenReturn(List.of(b));
    when(announcementRepository.findById(99L)).thenReturn(Optional.empty());
    assertThat(bookingService.getBookedAdsForBuyer(2L)).isEmpty();
  }

  @Test
  void bookAnnouncement_notFound_throws() {
    when(announcementRepository.findByIdWithLock(10L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> bookingService.bookAnnouncement(10L, 2L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("не найдено");
  }

  @Test
  void bookAnnouncement_inactiveStatus_throws() {
    ad.setStatus(AdStatus.BOOKED);
    when(announcementRepository.findByIdWithLock(10L)).thenReturn(Optional.of(ad));
    assertThatThrownBy(() -> bookingService.bookAnnouncement(10L, 2L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("недоступно");
  }

  @Test
  void bookAnnouncement_alreadyBooked_throws() {
    when(announcementRepository.findByIdWithLock(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.existsByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(true);
    assertThatThrownBy(() -> bookingService.bookAnnouncement(10L, 2L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("уже забронирован");
  }

  @Test
  void bookAnnouncement_succeeds() {
    when(announcementRepository.findByIdWithLock(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.existsByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(false);
    when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
      Booking b = inv.getArgument(0);
      b.setId(100L);
      return b;
    });

    Booking saved = bookingService.bookAnnouncement(10L, 2L);
    assertThat(saved.getId()).isEqualTo(100L);
    assertThat(ad.getStatus()).isEqualTo(AdStatus.BOOKED);
    verify(announcementRepository).save(ad);
    verify(kafkaMessageService).sendBookingEvent(eq("booking.created"), eq("100"), any());
  }

  @Test
  void confirmSale_notFound_throws() {
    when(announcementRepository.findById(10L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> bookingService.confirmSale(10L, 1L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("не найдено");
  }

  @Test
  void confirmSale_notAuthor_throws() {
    ad.setStatus(AdStatus.BOOKED);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    assertThatThrownBy(() -> bookingService.confirmSale(10L, 999L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("Только автор");
  }

  @Test
  void confirmSale_wrongStatus_throws() {
    ad.setStatus(AdStatus.ACTIVE);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    assertThatThrownBy(() -> bookingService.confirmSale(10L, 1L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("статусе брони");
  }

  @Test
  void confirmSale_archivesAndConfirmsBooking() {
    ad.setStatus(AdStatus.BOOKED);
    Booking b = new Booking();
    b.setId(50L);
    b.setAnnouncementId(10L);
    b.setBuyerId(2L);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(Optional.of(b));

    bookingService.confirmSale(10L, 1L);
    assertThat(ad.getStatus()).isEqualTo(AdStatus.ARCHIVED);
    assertThat(b.getConfirmedAt()).isNotNull();
    verify(kafkaMessageService).sendBookingEvent(eq("booking.confirmed"), eq("50"), any());
  }

  @Test
  void confirmSale_noBookingFound_silentlyArchives() {
    ad.setStatus(AdStatus.BOOKED);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(Optional.empty());
    bookingService.confirmSale(10L, 1L);
    assertThat(ad.getStatus()).isEqualTo(AdStatus.ARCHIVED);
  }

  @Test
  void cancelBooking_notFound_throws() {
    when(announcementRepository.findById(10L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> bookingService.cancelBooking(10L, 2L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("не найдено");
  }

  @Test
  void cancelBooking_noBooking_throws() {
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(Optional.empty());
    assertThatThrownBy(() -> bookingService.cancelBooking(10L, 2L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("Бронь");
  }

  @Test
  void cancelBooking_unauthorized_throws() {
    Booking b = new Booking();
    b.setBuyerId(2L);
    b.setAnnouncementId(10L);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(Optional.of(b));
    assertThatThrownBy(() -> bookingService.cancelBooking(10L, 999L))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("прав");
  }

  @Test
  void cancelBooking_byBuyer_publishesEvent() {
    Booking b = new Booking();
    b.setId(50L);
    b.setBuyerId(2L);
    b.setAnnouncementId(10L);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(Optional.of(b));
    bookingService.cancelBooking(10L, 2L);
    assertThat(b.getCancelledAt()).isNotNull();
    assertThat(ad.getStatus()).isEqualTo(AdStatus.ACTIVE);
    verify(kafkaMessageService).sendBookingEvent(eq("booking.cancelled"), eq("50"), any());
  }

  @Test
  void cancelBooking_bySeller_works() {
    ad.setAuthorId(7L);
    Booking b = new Booking();
    b.setId(50L);
    b.setBuyerId(2L);
    b.setAnnouncementId(10L);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));
    when(bookingRepository.findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(10L))
        .thenReturn(Optional.of(b));
    bookingService.cancelBooking(10L, 7L);
    assertThat(b.getCancelledAt()).isNotNull();
  }

  @Test
  void autoCancelExpiredBookings_processesAndCancels() {
    Booking b1 = new Booking();
    b1.setId(1L);
    b1.setAnnouncementId(10L);
    b1.setBuyerId(2L);

    when(bookingRepository.findAllByCreatedAtBeforeAndCancelledAtIsNullAndConfirmedAtIsNull(any()))
        .thenReturn(List.of(b1));
    ad.setStatus(AdStatus.BOOKED);
    when(announcementRepository.findById(10L)).thenReturn(Optional.of(ad));

    bookingService.autoCancelExpiredBookings();

    assertThat(ad.getStatus()).isEqualTo(AdStatus.ACTIVE);
    assertThat(b1.getCancelledAt()).isNotNull();
    verify(kafkaMessageService).sendBookingEvent(eq("booking.cancelled"), anyString(), any());
  }

  @Test
  void autoCancelExpiredBookings_handlesMissingAd() {
    Booking b = new Booking();
    b.setId(1L);
    b.setAnnouncementId(99L);
    b.setBuyerId(2L);

    when(bookingRepository.findAllByCreatedAtBeforeAndCancelledAtIsNullAndConfirmedAtIsNull(any()))
        .thenReturn(List.of(b));
    when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

    bookingService.autoCancelExpiredBookings();
    assertThat(b.getCancelledAt()).isNotNull();
  }

  @Test
  void autoCancelExpiredBookings_swallowsExceptions() {
    Booking b = new Booking();
    b.setId(1L);
    b.setAnnouncementId(10L);

    when(bookingRepository.findAllByCreatedAtBeforeAndCancelledAtIsNullAndConfirmedAtIsNull(any()))
        .thenReturn(List.of(b));
    when(announcementRepository.findById(10L)).thenThrow(new RuntimeException("db"));

    // Should not throw
    bookingService.autoCancelExpiredBookings();
  }
}
