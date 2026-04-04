package com.mipt.portal.controller;

import com.mipt.portal.entity.Booking;
import com.mipt.portal.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;

    @PostMapping("/{adId}")
    public ResponseEntity<?> bookAd(@PathVariable Long adId, @RequestParam Long buyerId) {
        try {
            Booking booking = bookingService.bookAnnouncement(adId, buyerId);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}