package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Booking;
import com.mipt.portal.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;

    /**
     * POST /api/v1/bookings/{adId} — забронировать объявление
     */
    @PostMapping("/{adId}")
    public ResponseEntity<?> bookAd(@PathVariable Long adId, HttpSession session) {
        Long buyerId = (Long) session.getAttribute("userId");
        if (buyerId == null) {
            return ResponseEntity.status(401).body("Требуется авторизация");
        }
        try {
            Booking booking = bookingService.bookAnnouncement(adId, buyerId);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.warn("Booking failed for adId={}: {}", adId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/v1/bookings/{adId}/confirm — продавец подтверждает продажу
     * Объявление уходит в ARCHIVED
     */
    @PostMapping("/{adId}/confirm")
    public ResponseEntity<?> confirmSale(@PathVariable Long adId, HttpSession session) {
        Long sellerId = (Long) session.getAttribute("userId");
        if (sellerId == null) {
            return ResponseEntity.status(401).body("Требуется авторизация");
        }
        try {
            bookingService.confirmSale(adId, sellerId);
            return ResponseEntity.ok("Продажа подтверждена");
        } catch (Exception e) {
            log.warn("Confirm sale failed for adId={}: {}", adId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/bookings/{adId} — снять бронь
     * Может сделать и покупатель, и продавец
     */
    @DeleteMapping("/{adId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long adId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Требуется авторизация");
        }
        try {
            bookingService.cancelBooking(adId, userId);
            return ResponseEntity.ok("Бронь отменена");
        } catch (Exception e) {
            log.warn("Cancel booking failed for adId={}: {}", adId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/bookings/my — мои бронирования
     */
    @GetMapping("/my")
    public ResponseEntity<List<Announcement>> getMyBookedAds(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(bookingService.getBookedAdsForBuyer(userId));
    }
}