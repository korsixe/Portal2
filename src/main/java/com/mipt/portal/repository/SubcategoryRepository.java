package com.mipt.portal.repository;

import com.mipt.portal.infrastructure.database.DatabaseConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubcategoryRepository {

  public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) throws SQLException {

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

  }

  public boolean isServiceSubcategory(Long subcategoryId) throws SQLException {

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

  }

  public Long getParentCategoryIdByName(String subcategoryName) throws SQLException {
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

  }

  public Map<String, Object> getSubcategoryWithParent(String subcategoryName) throws SQLException {

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


  }
}