package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.ModerationHistoryService;
import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    return service.changeStatus(id, AdStatus.ACTIVE, null, null)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/history")
  @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
  public ResponseEntity<List<ModerationHistory>> getHistory(@PathVariable Long id) {
    return ResponseEntity.ok(moderationHistoryService.getHistory(id));
  }

}