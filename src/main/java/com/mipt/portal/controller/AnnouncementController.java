package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.ModerationHistoryService;
import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService service;
  private final ModerationHistoryService moderationHistoryService;

  @PostMapping
  public ResponseEntity<Announcement> create(@RequestBody @Valid AnnouncementCreateDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @GetMapping("/search")
  public List<Announcement> search(
      AnnouncementFilterDto filter,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") String direction) {
    return service.searchApproved(filter, sortBy, direction);
  }

  @GetMapping("/moderator/pending")
  public List<Announcement> getPending() {
    return service.getPendingForModerator();
  }

  @PostMapping("/{id}/send-to-moderation")
  public ResponseEntity<Void> sendToModeration(@PathVariable Long id) {
    service.sendToModeration(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
  public ResponseEntity<Announcement> approve(@PathVariable Long id) {
    return service.changeStatus(id, AdStatus.ACTIVE)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/history")
  @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
  public ResponseEntity<List<ModerationHistory>> getHistory(@PathVariable Long id) {
    return ResponseEntity.ok(moderationHistoryService.getHistory(id));
  }

  /**
   * Получить объявления текущего пользователя GET /api/announcements/my
   */
  @GetMapping("/my")
  public ResponseEntity<List<Announcement>> getUserAnnouncements(HttpSession session) {
    User currentUser = (User) session.getAttribute("user");

    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    List<Announcement> allUserAds = service.findAllByAuthorId(currentUser.getId());

    List<Announcement> activeAds = new ArrayList<>();
    for (Announcement ad : allUserAds) {
      if (ad.getStatus() != AdStatus.DELETED) {
        activeAds.add(ad);
      }
    }

    log.info("Found {} active announcements for user: {}", activeAds.size(),
        currentUser.getEmail());
    return ResponseEntity.ok(activeAds);
  }

  /**
   * Получить объявление по ID GET /api/announcements/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Long id) {
    Announcement ad = service.findById(id);
    if (ad == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(ad);
  }

  /**
   * Удалить объявление (soft delete) DELETE /api/announcements/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id, HttpSession session) {
    User currentUser = (User) session.getAttribute("user");

    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
    }

    Announcement ad = service.findById(id);
    if (ad == null) {
      return ResponseEntity.notFound().build();
    }
    if (!ad.getAuthorId().equals(currentUser.getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("Нет прав на удаление этого объявления");
    }
    service.changeStatus(id, AdStatus.DELETED);
    log.info("Announcement {} deleted by user {}", id, currentUser.getEmail());

    return ResponseEntity.ok().body("Объявление удалено");
  }
}
