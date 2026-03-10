package com.mipt.portal.annoucementContent.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;
//import com.mipt.portal.database.DatabaseConnection;

@Service
@RequiredArgsConstructor
public class SubcategorySelector {

  public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) throws SQLException {
    /*
    List<Map<String, Object>> subcategories = new ArrayList<>();
    String sql = "SELECT id, name FROM categories WHERE parent_id = ? ORDER BY name";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, categoryId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> subcategory = new HashMap<>();
          subcategory.put("id", rs.getLong("id"));
          subcategory.put("name", rs.getString("name"));
          subcategories.add(subcategory);
        }
      }
    }
    return subcategories;
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    List<Map<String, Object>> subcategories = new ArrayList<>();

    if (categoryId == 1L) {
      subcategories.add(Map.of("id", 11L, "name", "Смартфоны"));
      subcategories.add(Map.of("id", 12L, "name", "Ноутбуки"));
      subcategories.add(Map.of("id", 13L, "name", "Планшеты"));
    } else if (categoryId == 2L) {
      subcategories.add(Map.of("id", 21L, "name", "Мужская"));
      subcategories.add(Map.of("id", 22L, "name", "Женская"));
      subcategories.add(Map.of("id", 23L, "name", "Детская"));
    } else if (categoryId == 3L) {
      subcategories.add(Map.of("id", 31L, "name", "Ремонт"));
      subcategories.add(Map.of("id", 32L, "name", "Уборка"));
      subcategories.add(Map.of("id", 33L, "name", "Перевозки"));
    }

    return subcategories;
  }

  public boolean isServiceSubcategory(Long subcategoryId) throws SQLException {
    /*
    String sql = """
            SELECT c.is_service FROM categories sc
            JOIN categories c ON sc.parent_id = c.id
            WHERE sc.id = ?
            """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, subcategoryId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() && rs.getBoolean("is_service");
      }
    }
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    return subcategoryId >= 30 && subcategoryId < 40;
  }

  public Long getParentCategoryIdByName(String subcategoryName) throws SQLException {
    /*
    String sql = """
            SELECT parent_id FROM categories
            WHERE name = ? AND parent_id IS NOT NULL
            """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, subcategoryName);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("parent_id");
        }
      }
    }
    return null;
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    Map<String, Long> map = Map.of(
      "Смартфоны", 1L,
      "Ноутбуки", 1L,
      "Планшеты", 1L,
      "Мужская", 2L,
      "Женская", 2L,
      "Детская", 2L,
      "Ремонт", 3L,
      "Уборка", 3L,
      "Перевозки", 3L
    );
    return map.get(subcategoryName);
  }

  public Map<String, Object> getSubcategoryWithParent(String subcategoryName) throws SQLException {
    /*
    String sql = """
            SELECT sc.id, sc.name, c.id as parent_id, c.name as parent_name
            FROM categories sc
            LEFT JOIN categories c ON sc.parent_id = c.id
            WHERE sc.name = ?
            """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, subcategoryName);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Map<String, Object> result = new HashMap<>();
          result.put("id", rs.getLong("id"));
          result.put("name", rs.getString("name"));
          result.put("parent_id", rs.getLong("parent_id"));
          result.put("parent_name", rs.getString("parent_name"));
          return result;
        }
      }
    }
    return null;
    */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    Map<String, Map<String, Object>> map = Map.of(
      "Смартфоны", Map.of("id", 11L, "name", "Смартфоны", "parent_id", 1L, "parent_name", "Электроника"),
      "Ноутбуки", Map.of("id", 12L, "name", "Ноутбуки", "parent_id", 1L, "parent_name", "Электроника"),
      "Планшеты", Map.of("id", 13L, "name", "Планшеты", "parent_id", 1L, "parent_name", "Электроника"),
      "Мужская", Map.of("id", 21L, "name", "Мужская", "parent_id", 2L, "parent_name", "Одежда"),
      "Женская", Map.of("id", 22L, "name", "Женская", "parent_id", 2L, "parent_name", "Одежда"),
      "Детская", Map.of("id", 23L, "name", "Детская", "parent_id", 2L, "parent_name", "Одежда"),
      "Ремонт", Map.of("id", 31L, "name", "Ремонт", "parent_id", 3L, "parent_name", "Услуги"),
      "Уборка", Map.of("id", 32L, "name", "Уборка", "parent_id", 3L, "parent_name", "Услуги"),
      "Перевозки", Map.of("id", 33L, "name", "Перевозки", "parent_id", 3L, "parent_name", "Услуги")
    );
    return map.get(subcategoryName);
  }
}