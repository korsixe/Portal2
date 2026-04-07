package com.mipt.portal.service;

import com.mipt.portal.repository.CategoryRepository;
import com.mipt.portal.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;

  public List<Map<String, Object>> getAllCategories() {
    return categoryRepository.getAllCategories();
  }

  public List<Map<String, Object>> getSubcategoriesByCategory(Long categoryId) {
    return categoryRepository.getSubcategoriesByCategory(categoryId);
  }

  public boolean isServiceSubcategory(Long subcategoryId) {
    return categoryRepository.isServiceSubcategory(subcategoryId);
  }

  public Long getParentCategoryIdByName(String subcategoryName) {
    return categoryRepository.getParentCategoryIdByName(subcategoryName);
  }

  public Map<String, Object> getSubcategoryWithParent(String subcategoryName) {
    return categoryRepository.getSubcategoryWithParent(subcategoryName);
  }


  public List<Map<String, Object>> getTagsWithValues() {
    return tagRepository.getTagsWithValues();
  }

  public List<Map<String, Object>> getAvailableTagsForSubcategory(String subcategoryName) {
    return tagRepository.getAvailableTagsForSubcategory(subcategoryName);
  }

  public List<Map<String, Object>> getTagsForAd(Long adId) {
    return tagRepository.getTagsForAd(adId);
  }

  public void saveAdTags(Long adId, List<Map<String, Object>> tagSelections) {
    tagRepository.saveAdTags(adId, tagSelections);
  }
}