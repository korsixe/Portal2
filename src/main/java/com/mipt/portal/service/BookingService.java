package com.mipt.portal.service;

import com.mipt.portal.dto.kafka.KafkaEventPayloads;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Booking;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AnnouncementRepository announcementRepository;
    private final KafkaMessageService kafkaMessageService;

    @Transactional(readOnly = true)
    public List<Announcement> getBookedAdsForBuyer(Long buyerId) {
        log.info("Fetching all booked announcements for buyerId={}", buyerId);
        List<Booking> bookings = bookingRepository.findAllByBuyerId(buyerId);
        return bookings.stream()
            .map(booking -> announcementRepository.findById(booking.getAnnouncementId()).orElse(null))
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Transactional
    public Booking bookAnnouncement(Long adId, Long buyerId) {
        log.info("Starting booking process for adId={} by buyerId={}", adId, buyerId);

        Announcement ad = announcementRepository.findByIdWithLock(adId)
            .orElseThrow(() -> {
                log.error("Booking failed: adId={} not found", adId);
                return new RuntimeException("Объявление не найдено");
            });

        if (ad.getStatus() != AdStatus.ACTIVE) {
            log.warn("Failed to book adId={}. Current status is {}", adId, ad.getStatus());
            throw new RuntimeException("Объявление недоступно для бронирования");
        }

        if (bookingRepository.existsByAnnouncementId(adId)) {
            log.error("Conflict! adId={} is already booked!", adId);
            throw new RuntimeException("Товар уже забронирован кем-то другим!");
        }

        Booking booking = new Booking();
        booking.setAnnouncementId(adId);
        booking.setBuyerId(buyerId);
        Booking savedBooking = bookingRepository.save(booking);

        ad.setStatus(AdStatus.BOOKED);
        announcementRepository.save(ad);

        log.info("Successfully booked adId={} with bookingId={}", adId, savedBooking.getId());
        kafkaMessageService.sendBookingEvent(
                "booking.created",
                String.valueOf(savedBooking.getId()),
                new KafkaEventPayloads.BookingCreated(
                        savedBooking.getId(),
                        adId,
                        buyerId,
                        AdStatus.BOOKED.name()
                )
        );
        return savedBooking;
    }

    @Transactional
    public void confirmSale(Long adId, Long sellerId) {
        log.info("Seller id={} attempting to confirm sale for adId={}", sellerId, adId);

        Announcement ad = announcementRepository.findById(adId)
            .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        if (!ad.getAuthorId().equals(sellerId)) {
            log.warn("Security violation: User id={} tried to confirm sale for adId={} without being the author", sellerId, adId);
            throw new RuntimeException("Только автор может подтвердить продажу");
        }

        if (ad.getStatus() != AdStatus.BOOKED) {
            log.warn("Cannot confirm sale for adId={}. Status is {}, expected BOOKED", adId, ad.getStatus());
            throw new RuntimeException("Объявление не находится в статусе брони");
        }

        ad.setStatus(AdStatus.ARCHIVED);
        announcementRepository.save(ad);

        bookingRepository.deleteByAnnouncementId(adId);
        log.info("Sale successfully confirmed for adId={}. Ad moved to ARCHIVED. Booking deleted.", adId);
    }

    @Transactional
    public void cancelBooking(Long adId, Long userId) {
        log.info("User id={} attempting to cancel booking for adId={}", userId, adId);

        Announcement ad = announcementRepository.findById(adId)
            .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        Booking booking = bookingRepository.findByAnnouncementId(adId)
            .orElseThrow(() -> new RuntimeException("Бронь не найдена"));

        if (!booking.getBuyerId().equals(userId) && !ad.getAuthorId().equals(userId)) {
            log.error("Security violation: User id={} tried to cancel booking id={} without permission", userId, booking.getId());
            throw new RuntimeException("У вас нет прав отменить эту бронь");
        }

        ad.setStatus(AdStatus.ACTIVE);
        announcementRepository.save(ad);
        bookingRepository.delete(booking);
        log.info("Booking cancelled for adId={} by userId={}. Ad moved back to ACTIVE.", adId, userId);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoCancelExpiredBookings() {
        log.info("Scheduled job started: checking for expired bookings (older than 24h)");
        Instant timeLimit = Instant.now().minus(24, ChronoUnit.HOURS);

        List<Booking> expiredBookings = bookingRepository.findAllByCreatedAtBefore(timeLimit);
        log.debug("Found {} expired bookings to process", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                Announcement ad = announcementRepository.findById(booking.getAnnouncementId()).orElse(null);
                if (ad != null && ad.getStatus() == AdStatus.BOOKED) {
                    ad.setStatus(AdStatus.ACTIVE);
                    announcementRepository.save(ad);
                }
                bookingRepository.delete(booking);
                log.info("Auto-cancelled expired booking id={} for adId={}", booking.getId(), booking.getAnnouncementId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel booking id={}", booking.getId(), e);
            }
        }
        log.info("Scheduled job for expired bookings completed.");
    }
}