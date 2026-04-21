package com.mipt.portal.repository;

import com.mipt.portal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(Long announcementId);

    List<Booking> findAllByBuyerIdAndCancelledAtIsNullAndConfirmedAtIsNull(Long buyerId);
    Optional<Booking> findByAnnouncementIdAndCancelledAtIsNullAndConfirmedAtIsNull(Long announcementId);
    List<Booking> findAllByCreatedAtBeforeAndCancelledAtIsNullAndConfirmedAtIsNull(Instant timeLimit);
    void deleteByAnnouncementId(Long announcementId);

    List<Booking> findByCancelledAtIsNotNullAndCancelNotificationSentAtIsNull();
    List<Booking> findByConfirmedAtIsNotNullAndConfirmNotificationSentAtIsNull();
}