package com.mipt.portal.repository;

import com.mipt.portal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByAnnouncementId(Long announcementId);

    java.util.List<Booking> findAllByBuyerId(Long buyerId);
    Optional<Booking> findByAnnouncementId(Long announcementId);
    List<Booking> findAllByCreatedAtBefore(Instant timeLimit);
    void deleteByAnnouncementId(Long announcementId);
}