package com.mipt.portal.repository;

import com.mipt.portal.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

  private final JdbcTemplate jdbcTemplate;

  public List<Map<String, Object>> getAllCategories() {
    String sql = "SELECT id, name, is_service FROM categories WHERE parent_id IS NULL ORDER BY name";
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Map<String, Object> category = new HashMap<>();
      category.put("id", rs.getLong("id"));
      category.put("name", rs.getString("name"));
      category.put("isService", rs.getBoolean("is_service"));
      return category;
    });
  }

  public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) {
    String sql = "SELECT id, name FROM categories WHERE parent_id = ? ORDER BY name";
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Map<String, Object> subcategory = new HashMap<>();
      subcategory.put("id", rs.getLong("id"));
      subcategory.put("name", rs.getString("name"));
      return subcategory;
    }, categoryId);
  }

  public boolean isServiceSubcategory(Long subcategoryId) {
    String sql = """
        SELECT c.is_service FROM categories sc 
        JOIN categories c ON sc.parent_id = c.id 
        WHERE sc.id = ?
    """;
    try {
      Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, subcategoryId);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public Long getParentCategoryIdByName(String subcategoryName) {
    String sql = """
        SELECT parent_id FROM categories 
        WHERE name = ? AND parent_id IS NOT NULL
    """;
    try {
      return jdbcTemplate.queryForObject(sql, Long.class, subcategoryName);
    } catch (Exception e) {
      return null;
    }
  }

  public Map<String, Object> getSubcategoryWithParent(String subcategoryName) {
    String sql = """
        SELECT sc.id, sc.name, c.id as parent_id, c.name as parent_name 
        FROM categories sc 
        LEFT JOIN categories c ON sc.parent_id = c.id 
        WHERE sc.name = ?
    """;
    try {
      return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
        Map<String, Object> result = new HashMap<>();
        result.put("id", rs.getLong("id"));
        result.put("name", rs.getString("name"));
        result.put("parent_id", rs.getLong("parent_id"));
        result.put("parent_name", rs.getString("parent_name"));
        return result;
      }, subcategoryName);
    } catch (Exception e) {
      return null;
    }
  }
}