package com.mipt.portal.repository;

import java.util.List;
import java.util.Map;

public interface CustomTagRepository {
  List<Map<String, Object>> getTagsWithValues();

  List<Map<String, Object>> getAvailableTagsForSubcategory(String subcategoryName);

  List<Map<String, Object>> getTagsForAd(Long adId);

  void saveAdTags(Long adId, List<Map<String, Object>> tagSelections);
}
