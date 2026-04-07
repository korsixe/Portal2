package com.mipt.portal.service;

import com.mipt.portal.entity.Comment;
import com.mipt.portal.entity.User;
import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.enums.Category;
import com.mipt.portal.enums.Condition;
import com.mipt.portal.enums.AdminActionType;
import com.mipt.portal.enums.AuditTargetType;
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
    private final ModerationHistoryService moderationHistoryService;
    private final AuditService auditService;
    private final CategoryService categoryService;
    private final CommentService commentService;

    @Transactional
    public Announcement create(AnnouncementCreateDto dto) {
        log.info("Creating new announcement: '{}' by authorId: {}", dto.getTitle(), dto.getAuthorId());

        Announcement ad = new Announcement();
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setPrice(dto.getPrice());
        ad.setAuthorId(dto.getAuthorId());

        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            ad.setCategory(parseCategory(dto.getCategory()));
        } else {
            ad.setCategory(Category.OTHER);
        }
        ad.setSubcategory(dto.getSubcategory());
        if (dto.getLocation() != null && !dto.getLocation().isBlank()) {
            ad.setLocation(dto.getLocation());
        }
        if (dto.getCondition() != null && !dto.getCondition().isBlank()) {
            try {
                ad.setCondition(Condition.valueOf(dto.getCondition()));
            } catch (IllegalArgumentException ex) {
                ad.setCondition(Condition.USED);
            }
        } else {
            ad.setCondition(Condition.USED);
        }

        if (dto.getPhotoUrls() != null) {
            ad.setPhotoUrls(dto.getPhotoUrls());
        }

        ad.setStatus(AdStatus.DRAFT);
        ad.setCreatedAt(Instant.now());
        ad.setUpdatedAt(Instant.now());

        Announcement savedAd = repository.save(ad);
        log.info("Announcement created successfully with ID: {}", savedAd.getId());
        return savedAd;
    }

    private Category parseCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return Category.OTHER;
        }
        try {
            return Category.valueOf(rawCategory);
        } catch (IllegalArgumentException ignored) {
            return Category.fromDisplayName(rawCategory);
        }
    }

    @Transactional(readOnly = true)
    public List<Announcement> searchApproved(AnnouncementFilterDto filter, String sortBy, String direction) {
        return repository.searchApproved(filter, sortBy, direction);
    }

    @Transactional(readOnly = true)
    public List<Announcement> getPendingForModerator() {
        return repository.findAllByStatus(AdStatus.UNDER_MODERATION);
    }

    @Transactional(readOnly = true)
    public List<Announcement> findAllByAuthorId(Long authorId) {
        return repository.findAllByAuthorId(authorId);
    }

    @Transactional
    public void sendToModeration(Long id) {
        repository.findById(id).ifPresent(ad -> {
            ad.sendToModeration();
            ad.setUpdatedAt(Instant.now());
            repository.save(ad);
            log.info("Announcement ID: {} sent to moderation", id);
        });
    }

    @Transactional
    public Optional<Announcement> changeStatus(Long id, AdStatus newStatus, Long moderatorId, String reason) {
        return repository.findById(id).map(ad -> {
            AdStatus previous = ad.getStatus();
            ad.setStatus(newStatus);
            ad.setUpdatedAt(Instant.now());
            Announcement saved = repository.save(ad);
            moderationHistoryService.record(id, moderatorId, previous, newStatus, reason);
            auditService.logAdminAction(moderatorId, null, AdminActionType.AD_STATUS_CHANGE, AuditTargetType.ANNOUNCEMENT, id,
                "Статус " + previous + " -> " + newStatus + (reason != null ? (". Причина: " + reason) : ""));
            log.info("Status changed for Ad ID: {}. New status: {}", id, newStatus);
            return saved;
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) {
        return categoryService.getSubcategoriesByCategory(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTagsWithValues() {
        return categoryService.getTagsWithValues();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTagsForAd(Long adId) {
        return categoryService.getTagsForAd(adId);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public int getPhotoCount(Long adId) {
        Announcement ad = findById(adId);
        if (ad == null) {
            return 0;
        }
        return (ad.getPhoto() != null && ad.getPhoto().length > 0) ? 1 : 0;
    }

    @Transactional
    public Announcement save(Announcement ad) {
        ad.setUpdatedAt(Instant.now());
        log.debug("Updating announcement data for ID: {}", ad.getId());
        return repository.save(ad);
    }
}