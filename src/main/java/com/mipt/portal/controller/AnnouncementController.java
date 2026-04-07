package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.Comment;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.MediaService;
import com.mipt.portal.service.ModerationHistoryService;
import com.mipt.portal.service.ProfanityChecker;
import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.dto.AnnouncementUpdateDto;
import com.mipt.portal.dto.CommentCreateDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService service;
  private final MediaService mediaService;
  private final ModerationHistoryService moderationHistoryService;
  private final ProfanityChecker profanityChecker;

  @PostMapping
  public ResponseEntity<Announcement> create(@RequestBody @Valid AnnouncementCreateDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @PostMapping(path = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadPhoto(@PathVariable Long id, @RequestParam("photo") MultipartFile photo) {
    Announcement ad = service.findById(id);
    if (ad == null) {
      return ResponseEntity.notFound().build();
    }
    if (photo == null || photo.isEmpty()) {
      return ResponseEntity.badRequest().body("Файл фото пустой");
    }

    try {
      mediaService.savePhoto(id, mediaService.multipartFileToBytes(photo));
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Ошибка при сохранении фото для объявления {}: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Не удалось сохранить фото");
    }
  }

  @GetMapping("/categories")
  public ResponseEntity<List<Map<String, Object>>> getCategories() {
    return ResponseEntity.ok(service.getAllCategories());
  }

  @GetMapping("/categories/{categoryId}/subcategories")
  public ResponseEntity<List<Map<String, Object>>> getSubcategories(@PathVariable Long categoryId) {
    return ResponseEntity.ok(service.getSubcategoriesByCategory(categoryId));
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

  @GetMapping("/{id}/details")
  public ResponseEntity<?> getAnnouncementDetails(@PathVariable Long id) {
    Announcement ad = service.findById(id);
    if (ad == null) {
      return ResponseEntity.notFound().build();
    }

    String authorName = service.getAuthorName(ad.getAuthorId());
    int photoCount = service.getPhotoCount(id);
    return ResponseEntity.ok(Map.of(
        "authorName", authorName,
        "photoCount", photoCount
    ));
  }

  @GetMapping("/{id}/comments")
  public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
    Announcement ad = service.findById(id);
    if (ad == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(service.getCommentsByAdId(id));
  }

  @PostMapping("/{id}/comments")
  public ResponseEntity<?> addComment(
      @PathVariable Long id,
      @RequestBody @Valid CommentCreateDto dto,
      HttpSession session) {
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
    }

    Announcement ad = service.findById(id);
    if (ad == null) {
      return ResponseEntity.notFound().build();
    }

    String commentText = dto.getContent().trim();
    if (profanityChecker.containsProfanity(commentText)) {
      return ResponseEntity.badRequest().body("Комментарий содержит ненормативную лексику");
    }

    String safeUserName = currentUser.getName();
    if (safeUserName == null || safeUserName.trim().isEmpty()) {
      safeUserName = (currentUser.getEmail() != null && !currentUser.getEmail().isBlank())
          ? currentUser.getEmail()
          : "Пользователь";
    }

    try {
      service.addComment(id, currentUser.getId(), safeUserName, commentText);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (Exception e) {
      log.error("Ошибка при добавлении комментария к объявлению {}: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Ошибка при сохранении комментария");
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateAnnouncement(
      @PathVariable Long id,
      @RequestBody @Valid AnnouncementUpdateDto dto,
      HttpSession session) {
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
          .body("Нет прав на редактирование этого объявления");
    }

    ad.setTitle(dto.getTitle());
    ad.setDescription(dto.getDescription());
    ad.setCategory(Category.valueOf(dto.getCategory()));
    ad.setSubcategory(dto.getSubcategory());
    ad.setLocation(dto.getLocation());
    ad.setCondition(Condition.valueOf(dto.getCondition()));
    ad.setPrice(dto.getPrice());

    Announcement saved = service.save(ad);
    if ("publish".equals(dto.getAction())) {
      service.sendToModeration(saved.getId());
    }

    return ResponseEntity.ok(saved);
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
    service.changeStatus(id, AdStatus.DELETED, null, null);
    log.info("Announcement {} deleted by user {}", id, currentUser.getEmail());

    return ResponseEntity.ok().body("Объявление удалено");
  }
}
