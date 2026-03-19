package com.mipt.portal.announcementContent.tag;

import lombok.RequiredArgsConstructor;
import java.sql.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
//import com.mipt.portal.database.DatabaseConnection;

@Service
@RequiredArgsConstructor
public class TagSelector {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<Map<String, Object>> getTagsWithValues() throws SQLException {
    /*
    List<Map<String, Object>> tags = new ArrayList<>();

    String sql = """
                SELECT t.id as tag_id, t.name as tag_name,
                       tv.id as value_id, tv.value as value_name
                FROM tags t
                LEFT JOIN tag_values tv ON t.id = tv.tag_id
                ORDER BY t.name, tv.value
                """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      Map<Long, Map<String, Object>> tagsMap = new HashMap<>();

      while (rs.next()) {
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

        if (rs.getObject("value_id") != null) {
          Map<String, Object> value = new HashMap<>();
          value.put("id", rs.getLong("value_id"));
          value.put("name", rs.getString("value_name"));

          @SuppressWarnings("unchecked")
          List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
          values.add(value);
        }
      }

      System.out.println(" Loaded " + tags.size() + " tags from database");

    } catch (SQLException e) {
      System.err.println(" Error loading tags from database: " + e.getMessage());
      throw e;
    }
    return tags;
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    List<Map<String, Object>> tags = new ArrayList<>();

    // Тег "Цвет"
    Map<String, Object> colorTag = new HashMap<>();
    colorTag.put("id", 1L);
    colorTag.put("name", "Цвет");
    colorTag.put("values", List.of(
      Map.of("id", 101L, "name", "Красный"),
      Map.of("id", 102L, "name", "Синий"),
      Map.of("id", 103L, "name", "Черный")
    ));
    tags.add(colorTag);

    // Тег "Размер"
    Map<String, Object> sizeTag = new HashMap<>();
    sizeTag.put("id", 2L);
    sizeTag.put("name", "Размер");
    sizeTag.put("values", List.of(
      Map.of("id", 201L, "name", "S"),
      Map.of("id", 202L, "name", "M"),
      Map.of("id", 203L, "name", "L")
    ));
    tags.add(sizeTag);

    return tags;
  }

  public List<Map<String, Object>> getAvailableTagsForSubcategory(String subcategoryName) throws SQLException {
    // return getTagsWithValues();
    return getTagsWithValues();
  }

  public List<Map<String, Object>> getTagsForAd(Long adId) throws SQLException {
    /*
    String sql = "SELECT tags FROM ads WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, adId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String tagsJson = rs.getString("tags");
          if (tagsJson != null && !tagsJson.trim().isEmpty()) {
            return objectMapper.readValue(tagsJson,
              objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
          }
        }
      }
    } catch (Exception e) {
      throw new SQLException("");
    }
    return new ArrayList<>();
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    List<Map<String, Object>> tags = new ArrayList<>();

    Map<String, Object> tag1 = new HashMap<>();
    tag1.put("id", 1L);
    tag1.put("name", "Цвет");
    tag1.put("value", Map.of("id", 101L, "name", "Красный"));
    tags.add(tag1);

    return tags;
  }

  public void saveAdTags(Long adId, List<Map<String, Object>> tagSelections) throws SQLException {
    /*
    if (adId == null || adId <= 0) {
      throw new IllegalArgumentException("Invalid ad ID");
    }

    if (!adExists(adId)) {
      throw new SQLException("Ad not found with ID: " + adId);
    }

    saveTagsToAd(adId, tagSelections);
    updateTagsCount(adId, tagSelections.size());
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    System.out.println("✅ Теги сохранены для объявления ID: " + adId);
  }

  private boolean adExists(Long adId) throws SQLException {
    /*
    String sql = "SELECT COUNT(*) FROM ads WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, adId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() && rs.getInt(1) > 0;
      }
    }
    */
    return true;
  }

  private void saveTagsToAd(Long adId, List<Map<String, Object>> tagSelections) throws SQLException {
    /*
    String sql = "UPDATE ads SET tags = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      String tagsJson = objectMapper.writeValueAsString(tagSelections);
      stmt.setString(1, tagsJson);
      stmt.setLong(2, adId);

      int updatedRows = stmt.executeUpdate();
      if (updatedRows == 0) {
        throw new SQLException("Failed to update tags for ad " + adId);
      }
    } catch (Exception e) {
      throw new SQLException("Error serializing tags to JSON for ad " + adId, e);
    }
    */
  }

  private void updateTagsCount(Long adId, int tagsCount) throws SQLException {
    /*
    String sql = "UPDATE ads SET tags_count = ? WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, tagsCount);
      stmt.setLong(2, adId);
      stmt.executeUpdate();
    }
    */
  }
}