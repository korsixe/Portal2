package com.mipt.portal.service;

import com.mipt.portal.dto.kafka.KafkaEventPayloads;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Booking;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AnnouncementRepository announcementRepository;
    private final KafkaMessageService kafkaMessageService;

    @Transactional(readOnly = true)
    public List<Announcement> getBookedAdsForBuyer(Long buyerId) {
        return bookingRepository.findAllByBuyerId(buyerId).stream()
                .map(b -> announcementRepository.findById(b.getAnnouncementId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public Booking bookAnnouncement(Long adId, Long buyerId) {
        log.info("Starting booking process for adId={} by buyerId={}", adId, buyerId);

        Announcement ad = announcementRepository.findByIdWithLock(adId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        if (ad.getStatus() != AdStatus.ACTIVE) {
            log.warn("Failed to book adId={}. Status is not ACTIVE", adId);
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
}