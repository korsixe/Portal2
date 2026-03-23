package com.mipt.portal.announcement.controler;

import com.mipt.portal.announcement.enums.AdStatus;
import com.mipt.portal.announcement.entity.Announcement;
import com.mipt.portal.announcement.service.AnnouncementService;
import com.mipt.portal.announcement.dto.AnnouncementCreateDto;
import com.mipt.portal.announcement.dto.AnnouncementFilterDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService service;

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
  public ResponseEntity<Announcement> approve(@PathVariable Long id) {
    return service.changeStatus(id, AdStatus.ACTIVE)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

}