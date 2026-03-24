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
public class CategoryRepository {

  public List<Map<String, Object>> getAllCategories() throws SQLException {
    List<Map<String, Object>> categories = new ArrayList<>();
    String sql = "SELECT id, name, is_service FROM categories WHERE parent_id IS NULL ORDER BY name";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", rs.getLong("id"));
        category.put("name", rs.getString("name"));
        category.put("isService", rs.getBoolean("is_service"));
        categories.add(category);
      }
    }
    return categories;

  }
}