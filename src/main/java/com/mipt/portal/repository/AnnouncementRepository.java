package com.mipt.portal.repository;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, JpaSpecificationExecutor<Announcement> {
  @EntityGraph(attributePaths = "photoUrls")
  List<Announcement> findAllByStatus(AdStatus status);
}