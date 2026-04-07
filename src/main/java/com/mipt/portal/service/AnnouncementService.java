package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import com.mipt.portal.dto.AnnouncementCreateDto;
import com.mipt.portal.dto.AnnouncementFilterDto;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository repository;
    private final UserRepository userRepository;
    private final ModerationHistoryService moderationHistoryService;
    private final AuditService auditService;

    @Transactional
    public Announcement create(AnnouncementCreateDto dto) {
        log.info("Creating new announcement: '{}' by authorId: {}", dto.getTitle(), dto.getAuthorId());

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

        Announcement savedAd = repository.save(ad);
        log.info("Announcement created successfully with ID: {}", savedAd.getId());
        return savedAd;
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

    @Transactional
    public Announcement save(Announcement ad) {
        ad.setUpdatedAt(Instant.now());
        log.debug("Updating announcement data for ID: {}", ad.getId());
        return repository.save(ad);
    }
}