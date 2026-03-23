package com.mipt.portal.announcement.service;

import com.mipt.portal.announcement.specification.AnnouncementSpecification;
import com.mipt.portal.announcement.dto.AnnouncementCreateDto;
import com.mipt.portal.announcement.dto.AnnouncementFilterDto;
import com.mipt.portal.announcement.entity.Announcement;
import com.mipt.portal.announcement.enums.AdStatus;
import com.mipt.portal.announcement.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

  private final AnnouncementRepository repository;

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
    return repository.save(ad);
  }

  @Transactional(readOnly = true)
  public List<Announcement> searchApproved(AnnouncementFilterDto filter, String sortBy, String direction) {
    Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
    return repository.findAll(AnnouncementSpecification.build(filter, AdStatus.ACTIVE), sort);
  }

  @Transactional(readOnly = true)
  public List<Announcement> getPendingForModerator() {
    return repository.findAllByStatus(AdStatus.UNDER_MODERATION);
  }

  @Transactional
  public void sendToModeration(Long id) {
    repository.findById(id).ifPresent(ad -> {
      ad.sendToModeration();
      repository.save(ad);
    });
  }

  @Transactional
  public Optional<Announcement> changeStatus(Long id, AdStatus newStatus) {
    return repository.findById(id).map(ad -> {
      ad.setStatus(newStatus);
      return repository.save(ad);
    });
  }
}