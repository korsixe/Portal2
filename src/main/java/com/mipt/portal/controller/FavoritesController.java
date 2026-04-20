package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoritesController {

    private final UserService userService;
    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<Long>> getFavoriteIds(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getFavoriteIds(userId));
    }

    @GetMapping("/ads")
    public ResponseEntity<List<Announcement>> getFavoriteAds(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<Long> ids = userService.getFavoriteIds(userId);
        return ResponseEntity.ok(announcementService.findAllByIds(ids));
    }

    @PostMapping("/{adId}")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(
            @PathVariable Long adId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        boolean liked = userService.toggleFavorite(userId, adId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
