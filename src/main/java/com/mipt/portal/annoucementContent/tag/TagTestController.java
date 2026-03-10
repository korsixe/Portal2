package com.mipt.portal.annoucementContent.tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/test/tags")
@RequiredArgsConstructor
public class TagTestController {

  private final CategorySelector categorySelector;
  private final SubcategorySelector subcategorySelector;
  private final TagSelector tagSelector;

  @GetMapping("/categories")
  public ResponseEntity<String> testCategories() {
    try {
      List<Map<String, Object>> categories = categorySelector.getAllCategories();
      StringBuilder result = new StringBuilder("✅ Все категории:\n");
      for (Map<String, Object> cat : categories) {
        result.append("  - ").append(cat.get("name"))
          .append(" (ID: ").append(cat.get("id"))
          .append(", isService: ").append(cat.get("isService")).append(")\n");
      }
      return ResponseEntity.ok(result.toString());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/subcategories")
  public ResponseEntity<String> testSubcategories(@RequestParam(defaultValue = "1") Long categoryId) {
    try {
      List<Map<String, Object>> subcategories = subcategorySelector.getSubcategoriesByCategory(categoryId);
      StringBuilder result = new StringBuilder("✅ Подкатегории для категории " + categoryId + ":\n");
      for (Map<String, Object> sub : subcategories) {
        result.append("  - ").append(sub.get("name"))
          .append(" (ID: ").append(sub.get("id")).append(")\n");
      }
      return ResponseEntity.ok(result.toString());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/subcategories/service/{id}")
  public ResponseEntity<String> testIsService(@PathVariable Long id) {
    try {
      boolean isService = subcategorySelector.isServiceSubcategory(id);
      return ResponseEntity.ok("✅ Подкатегория ID " + id + " является сервисной: " + isService);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/subcategories/parent")
  public ResponseEntity<String> testGetParent(@RequestParam String name) {
    try {
      Long parentId = subcategorySelector.getParentCategoryIdByName(name);
      return ResponseEntity.ok("✅ Родительская категория для '" + name + "': " + parentId);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/subcategories/with-parent")
  public ResponseEntity<String> testSubcategoryWithParent(@RequestParam String name) {
    try {
      Map<String, Object> result = subcategorySelector.getSubcategoryWithParent(name);
      if (result != null) {
        return ResponseEntity.ok(
          "✅ Информация о подкатегории '" + name + "':\n" +
            "  ID: " + result.get("id") + "\n" +
            "  Название: " + result.get("name") + "\n" +
            "  Parent ID: " + result.get("parent_id") + "\n" +
            "  Parent Name: " + result.get("parent_name")
        );
      } else {
        return ResponseEntity.ok("❌ Подкатегория '" + name + "' не найдена");
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/tags")
  public ResponseEntity<String> testTags() {
    try {
      List<Map<String, Object>> tags = tagSelector.getTagsWithValues();
      StringBuilder result = new StringBuilder("✅ Все теги со значениями:\n");
      for (Map<String, Object> tag : tags) {
        result.append("  - ").append(tag.get("name")).append(" (ID: ").append(tag.get("id")).append("):\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
        if (values != null) {
          for (Map<String, Object> value : values) {
            result.append("      * ").append(value.get("name"))
              .append(" (ID: ").append(value.get("id")).append(")\n");
          }
        }
      }
      return ResponseEntity.ok(result.toString());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/tags/available")
  public ResponseEntity<String> testAvailableTags(@RequestParam String subcategory) {
    try {
      List<Map<String, Object>> tags = tagSelector.getAvailableTagsForSubcategory(subcategory);
      StringBuilder result = new StringBuilder("✅ Доступные теги для '" + subcategory + "':\n");
      for (Map<String, Object> tag : tags) {
        result.append("  - ").append(tag.get("name")).append("\n");
      }
      return ResponseEntity.ok(result.toString());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/tags/ad/{adId}")
  public ResponseEntity<String> testTagsForAd(@PathVariable Long adId) {
    try {
      List<Map<String, Object>> tags = tagSelector.getTagsForAd(adId);
      StringBuilder result = new StringBuilder("✅ Теги для объявления ID " + adId + ":\n");
      for (Map<String, Object> tag : tags) {
        result.append("  - ").append(tag).append("\n");
      }
      return ResponseEntity.ok(result.toString());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @PostMapping("/tags/save/{adId}")
  public ResponseEntity<String> testSaveTags(@PathVariable Long adId) {
    try {
      List<Map<String, Object>> newTags = new ArrayList<>();

      Map<String, Object> tag1 = new HashMap<>();
      tag1.put("id", 1);
      tag1.put("name", "Цвет");

      Map<String, Object> value1 = new HashMap<>();
      value1.put("id", 101);
      value1.put("name", "Красный");
      tag1.put("values", List.of(value1));

      newTags.add(tag1);

      tagSelector.saveAdTags(adId, newTags);
      return ResponseEntity.ok("✅ Теги сохранены для объявления ID: " + adId);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }

  @GetMapping("/all")
  public ResponseEntity<String> testAll() {
    StringBuilder result = new StringBuilder("ТЕСТИРОВАНИЕ ТЕГОВ\n\n");

    try {
      // Тест 1: Категории
      result.append("📁 Тест 1: Категории\n");
      List<Map<String, Object>> categories = categorySelector.getAllCategories();
      for (Map<String, Object> cat : categories) {
        result.append("  - ").append(cat.get("name")).append("\n");
      }
      result.append("\n");

      // Тест 2: Подкатегории для категории 1
      result.append("📂 Тест 2: Подкатегории для категории 1\n");
      List<Map<String, Object>> subcats = subcategorySelector.getSubcategoriesByCategory(1L);
      for (Map<String, Object> sub : subcats) {
        result.append("  - ").append(sub.get("name")).append("\n");
      }
      result.append("\n");

      // Тест 3: Проверка сервисной подкатегории
      result.append("🔍 Тест 3: Проверка сервисной подкатегории\n");
      boolean isService = subcategorySelector.isServiceSubcategory(31L);
      result.append("  Подкатегория ID 31 является сервисной: ").append(isService).append("\n\n");

      // Тест 4: Родительская категория
      result.append("👆 Тест 4: Родительская категория для 'Смартфоны'\n");
      Long parentId = subcategorySelector.getParentCategoryIdByName("Смартфоны");
      result.append("  Parent ID: ").append(parentId).append("\n\n");

      // Тест 5: Подкатегория с родителем
      result.append("📊 Тест 5: Подкатегория 'Смартфоны' с родителем\n");
      Map<String, Object> subWithParent = subcategorySelector.getSubcategoryWithParent("Смартфоны");
      if (subWithParent != null) {
        result.append("  ID: ").append(subWithParent.get("id")).append("\n");
        result.append("  Name: ").append(subWithParent.get("name")).append("\n");
        result.append("  Parent: ").append(subWithParent.get("parent_name")).append("\n");
      }
      result.append("\n");

      // Тест 6: Теги со значениями
      result.append("🏷️ Тест 6: Теги со значениями\n");
      List<Map<String, Object>> tags = tagSelector.getTagsWithValues();
      for (Map<String, Object> tag : tags) {
        result.append("  - ").append(tag.get("name")).append(":\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
        if (values != null) {
          for (Map<String, Object> value : values) {
            result.append("      * ").append(value.get("name")).append("\n");
          }
        }
      }
      result.append("\n");

      // Тест 7: Доступные теги
      result.append("🔖 Тест 7: Доступные теги для 'Электроника'\n");
      List<Map<String, Object>> availableTags = tagSelector.getAvailableTagsForSubcategory("Электроника");
      for (Map<String, Object> tag : availableTags) {
        result.append("  - ").append(tag.get("name")).append("\n");
      }
      result.append("\n");

      // Тест 8: Теги для объявления
      result.append("📝 Тест 8: Теги для объявления ID 123\n");
      List<Map<String, Object>> adTags = tagSelector.getTagsForAd(123L);
      for (Map<String, Object> tag : adTags) {
        result.append("  - ").append(tag).append("\n");
      }
      result.append("\n");

      // Тест 9: Сохранение тегов
      result.append("💾 Тест 9: Сохранение тегов\n");
      tagSelector.saveAdTags(123L, new ArrayList<>());
      result.append("  ✅ Теги сохранены\n");

      return ResponseEntity.ok(result.toString());

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
    }
  }
}
