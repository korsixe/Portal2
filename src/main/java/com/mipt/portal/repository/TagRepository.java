package com.mipt.portal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository {

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<Map<String, Object>> getTagsWithValues() {
    String sql = """
        SELECT t.id as tag_id, t.name as tag_name,
               tv.id as value_id, tv.value as value_name
        FROM tags t
        LEFT JOIN tag_values tv ON t.id = tv.tag_id
        ORDER BY t.name, tv.value
    """;

    Map<Long, Map<String, Object>> tagsMap = new HashMap<>();
    List<Map<String, Object>> tags = new ArrayList<>();

    jdbcTemplate.query(sql, rs -> {
      Long tagId = rs.getLong("tag_id");
      Map<String, Object> tag = tagsMap.get(tagId);

      if (tag == null) {
        tag = new HashMap<>();
        tag.put("id", tagId);
        tag.put("name", rs.getString("tag_name"));
        tag.put("values", new ArrayList<Map<String, Object>>());
        tagsMap.put(tagId, tag);
        tags.add(tag);
      }

      Long valueId = rs.getLong("value_id");
      if (!rs.wasNull()) {
        Map<String, Object> value = new HashMap<>();
        value.put("id", valueId);
        value.put("name", rs.getString("value_name"));
        ((List<Map<String, Object>>) tag.get("values")).add(value);
      }
    });

    log.info("✅ Loaded {} tags from database", tags.size());
    return tags;
  }

  public List<Map<String, Object>> getAvailableTagsForSubcategory(String subcategoryName) {
    return getTagsWithValues();
  }

  public List<Map<String, Object>> getTagsForAd(Long adId) {
    String sql = "SELECT tags FROM ads WHERE id = ?";
    try {
      String tagsJson = jdbcTemplate.queryForObject(sql, String.class, adId);
      if (tagsJson != null && !tagsJson.trim().isEmpty()) {
        return objectMapper.readValue(tagsJson,
          objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
      }
    } catch (Exception e) {
      log.error("Error getting tags for ad {}", adId, e);
    }
    return new ArrayList<>();
  }

  public void saveAdTags(Long adId, List<Map<String, Object>> tagSelections) {
    if (adId == null || adId <= 0) {
      throw new IllegalArgumentException("Invalid ad ID");
    }

    if (!adExists(adId)) {
      throw new RuntimeException("Ad not found with ID: " + adId);
    }

    saveTagsToAd(adId, tagSelections);
    updateTagsCount(adId, tagSelections.size());
  }

  private boolean adExists(Long adId) {
    String sql = "SELECT COUNT(*) FROM ads WHERE id = ?";
    try {
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class, adId);
      return count != null && count > 0;
    } catch (Exception e) {
      return false;
    }
  }

  private void saveTagsToAd(Long adId, List<Map<String, Object>> tagSelections) {
    try {
      String tagsJson = objectMapper.writeValueAsString(tagSelections);
      String sql = "UPDATE ads SET tags = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
      int updatedRows = jdbcTemplate.update(sql, tagsJson, adId);
      if (updatedRows == 0) {
        throw new RuntimeException("Failed to update tags for ad " + adId);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error serializing tags to JSON for ad " + adId, e);
    }
  }

  private void updateTagsCount(Long adId, int tagsCount) {
    String sql = "UPDATE ads SET tags_count = ? WHERE id = ?";
    jdbcTemplate.update(sql, tagsCount, adId);
  }
}