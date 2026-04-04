package com.mipt.portal.repository;

import com.mipt.portal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByAnnouncementId(Long announcementId);
}