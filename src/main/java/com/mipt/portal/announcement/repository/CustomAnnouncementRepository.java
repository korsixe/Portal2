package com.mipt.portal.announcement.repository;

import com.mipt.portal.announcement.dto.AnnouncementFilterDto;
import com.mipt.portal.announcement.entity.Announcement;
import java.util.List;

public interface CustomAnnouncementRepository {
  List<Announcement> searchApproved(AnnouncementFilterDto filter, String sortBy, String direction);
}