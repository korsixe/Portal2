package com.mipt.portal.announcement.service;

import com.mipt.portal.announcement.dto.AnnouncementCreateDto;
import com.mipt.portal.announcement.dto.AnnouncementFilterDto;
import com.mipt.portal.announcement.entity.Announcement;
import com.mipt.portal.announcement.enums.AdStatus;
import com.mipt.portal.announcement.repository.AnnouncementRepository;
import com.mipt.portal.users.User;
import com.mipt.portal.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

  private final AnnouncementRepository repository;
  private final UserRepository userRepository;

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
}