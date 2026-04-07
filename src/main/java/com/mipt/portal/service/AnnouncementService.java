package com.mipt.portal.service;

import com.mipt.portal.entity.Comment;
import com.mipt.portal.entity.User;
import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AnnouncementRepository;
import com.mipt.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

  private final AnnouncementRepository repository;
  private final UserRepository userRepository;
  private final CategoryService categoryService;
  private final CommentService commentService;

  @Transactional
  public Announcement create(AnnouncementCreateDto dto) {
    Announcement ad = new Announcement();
    ad.setTitle(dto.getTitle());
    ad.setDescription(dto.getDescription());
    ad.setPrice(dto.getPrice());
    ad.setAuthorId(dto.getAuthorId());

    if (dto.getPhotoUrls() != null) {
      ad.setPhotoUrls(dto.getPhotoUrls());
    }

    ad.setStatus(AdStatus.DRAFT);
    ad.setCreatedAt(Instant.now());
    ad.setUpdatedAt(Instant.now());

    return repository.save(ad);
  }

  @Transactional(readOnly = true)
  public List<Announcement> searchApproved(AnnouncementFilterDto filter, String sortBy, String direction) {
    return repository.searchApproved(filter, sortBy, direction);
  }

  @Transactional(readOnly = true)
  public List<Announcement> getPendingForModerator() {
    return repository.findAllByStatus(AdStatus.UNDER_MODERATION);
  }

  @Transactional
  public void sendToModeration(Long id) {
    repository.findById(id).ifPresent(ad -> {
      ad.sendToModeration();
      ad.setUpdatedAt(Instant.now());
      repository.save(ad);
    });
  }

  @Transactional
  public Optional<Announcement> changeStatus(Long id, AdStatus newStatus) {
    return repository.findById(id).map(ad -> {
      ad.setStatus(newStatus);
      ad.setUpdatedAt(Instant.now());
      return repository.save(ad);
    });
  }

  @Transactional(readOnly = true)
  public Long getUserIdByEmail(String email) {
    return userRepository.findByEmail(email)
            .map(User::getId)
            .orElse(null);
  }

  @Transactional(readOnly = true)
  public Announcement findById(Long id) {
    return repository.findById(id).orElse(null);
  }

  @Transactional
  public Announcement save(Announcement ad) {
    ad.setUpdatedAt(java.time.Instant.now());
    return repository.save(ad);
  }


  public List<Map<String, Object>> getAllCategories() {
    return categoryService.getAllCategories();
  }

  public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) {
    return categoryService.getSubcategoriesByCategory(categoryId);
  }

  public List<Map<String, Object>> getTagsWithValues() {
    return categoryService.getTagsWithValues();
  }

  public List<Map<String, Object>> getTagsForAd(Long adId) {
    return categoryService.getTagsForAd(adId);
  }

  public void saveAdTags(Long adId, List<Map<String, Object>> selectedTags) {
    categoryService.saveAdTags(adId, selectedTags);
  }

  @Transactional
  public void addComment(Long adId, Long userId, String userName, String content) {
    commentService.createComment(adId, userId, userName, content);
  }

  @Transactional(readOnly = true)
  public List<Comment> getCommentsByAdId(Long adId) {
    return commentService.getCommentsByAdId(adId);
  }

  @Transactional(readOnly = true)
  public String getAuthorName(Long authorId) {
    return userRepository.findById(authorId)
      .map(User::getName)
      .orElse("Неизвестный пользователь");
  }



  public int getPhotoCount(Long adId) {
    Announcement ad = findById(adId);
    if (ad == null) return 0;

    return (ad.getPhoto() != null && ad.getPhoto().length > 0) ? 1 : 0;
  }
}