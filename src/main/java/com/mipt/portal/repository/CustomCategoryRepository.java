package com.mipt.portal.repository;

import java.util.List;
import java.util.Map;

public interface CustomCategoryRepository {
  List<Map<String, Object>> getAllCategories();

  List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId);

  boolean isServiceSubcategory(Long subcategoryId);

  Long getParentCategoryIdByName(String subcategoryName);

  Map<String, Object> getSubcategoryWithParent(String subcategoryName);
}
