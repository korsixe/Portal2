package com.mipt.portal.announcement.repository;

import com.mipt.portal.announcement.entity.Announcement;
import com.mipt.portal.announcement.enums.AdStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, JpaSpecificationExecutor<Announcement> {
  List<Announcement> findAllByStatus(AdStatus status);
}