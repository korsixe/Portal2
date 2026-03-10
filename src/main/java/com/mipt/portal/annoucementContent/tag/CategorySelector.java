package com.mipt.portal.annoucementContent.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategorySelector {

  public List<Map<String, Object>> getAllCategories() throws SQLException {
    /*
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
    */

    // ЗАГЛУШКА
    List<Map<String, Object>> categories = new ArrayList<>();

    Map<String, Object> cat1 = new HashMap<>();
    cat1.put("id", 1L);
    cat1.put("name", "Электроника");
    cat1.put("isService", false);
    categories.add(cat1);

    Map<String, Object> cat2 = new HashMap<>();
    cat2.put("id", 2L);
    cat2.put("name", "Одежда");
    cat2.put("isService", false);
    categories.add(cat2);

    Map<String, Object> cat3 = new HashMap<>();
    cat3.put("id", 3L);
    cat3.put("name", "Услуги");
    cat3.put("isService", true);
    categories.add(cat3);

    Map<String, Object> cat4 = new HashMap<>();
    cat4.put("id", 4L);
    cat4.put("name", "Авто");
    cat4.put("isService", false);
    categories.add(cat4);

    Map<String, Object> cat5 = new HashMap<>();
    cat5.put("id", 5L);
    cat5.put("name", "Мебель");
    cat5.put("isService", false);
    categories.add(cat5);

    System.out.println("✅ Загружено " + categories.size() + " категорий (тестовые данные)");

    return categories;
  }
}