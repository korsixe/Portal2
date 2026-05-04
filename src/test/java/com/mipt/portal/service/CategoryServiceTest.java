package com.mipt.portal.service;

import com.mipt.portal.repository.CategoryRepository;
import com.mipt.portal.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private TagRepository tagRepository;
  @InjectMocks private CategoryService service;

  @Test
  void getAllCategories_delegates() {
    List<Map<String, Object>> data = List.of(Map.of("id", 1, "name", "Books"));
    when(categoryRepository.getAllCategories()).thenReturn(data);
    assertThat(service.getAllCategories()).isEqualTo(data);
  }

  @Test
  void getSubcategoriesByCategory_delegates() {
    when(categoryRepository.getSubcategoriesByCategory(1L))
        .thenReturn(List.of(Map.of("id", 2)));
    assertThat(service.getSubcategoriesByCategory(1L)).hasSize(1);
  }

  @Test
  void isServiceSubcategory_delegates() {
    when(categoryRepository.isServiceSubcategory(5L)).thenReturn(true);
    assertThat(service.isServiceSubcategory(5L)).isTrue();
  }

  @Test
  void getParentCategoryIdByName_delegates() {
    when(categoryRepository.getParentCategoryIdByName("foo")).thenReturn(7L);
    assertThat(service.getParentCategoryIdByName("foo")).isEqualTo(7L);
  }

  @Test
  void getSubcategoryWithParent_delegates() {
    Map<String, Object> data = Map.of("id", 1, "parent_id", 5);
    when(categoryRepository.getSubcategoryWithParent("foo")).thenReturn(data);
    assertThat(service.getSubcategoryWithParent("foo")).isEqualTo(data);
  }

  @Test
  void tag_methodsDelegate() {
    when(tagRepository.getTagsWithValues()).thenReturn(List.of(Map.of("k", "v")));
    when(tagRepository.getAvailableTagsForSubcategory("sub")).thenReturn(List.of(Map.of("a", "b")));
    when(tagRepository.getTagsForAd(1L)).thenReturn(List.of(Map.of("c", "d")));
    assertThat(service.getTagsWithValues()).hasSize(1);
    assertThat(service.getAvailableTagsForSubcategory("sub")).hasSize(1);
    assertThat(service.getTagsForAd(1L)).hasSize(1);
  }

  @Test
  void saveAdTags_delegates() {
    List<Map<String, Object>> tags = List.of(Map.of("id", 1));
    service.saveAdTags(10L, tags);
    verify(tagRepository).saveAdTags(10L, tags);
  }
}
