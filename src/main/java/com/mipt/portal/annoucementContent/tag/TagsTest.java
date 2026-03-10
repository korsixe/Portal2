package com.mipt.portal.annoucementContent.tag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class TagsTest {

  public static void main(String[] args) throws SQLException {
        /*
        ConfigurableApplicationContext context = SpringApplication.run(TagsTest.class, args);

        CategorySelector categorySelector = context.getBean(CategorySelector.class);
        SubcategorySelector subcategorySelector = context.getBean(SubcategorySelector.class);
        TagSelector tagSelector = context.getBean(TagSelector.class);

        System.out.println(" Все категории:");
        List<Map<String, Object>> categories = categorySelector.getAllCategories();
        for (Map<String, Object> category : categories) {
            System.out.println("  - " + category.get("name") + " (ID: " + category.get("id") + ")");
        }
        System.out.println();

        System.out.println(" Подкатегории для категории 1:");
        List<Map<String, Object>> subcategories = subcategorySelector.getSubcategoriesByCategory(1L);
        for (Map<String, Object> subcat : subcategories) {
            System.out.println("  - " + subcat.get("name") + " (ID: " + subcat.get("id") + ")");
        }
        System.out.println();

        Long subcatId = 2L;
        boolean isService = subcategorySelector.isServiceSubcategory(subcatId);
        System.out.println(" Подкатегория ID " + subcatId + " является сервисной: " + isService);
        System.out.println();

        String subcatName = "Смартфоны";
        Long parentId = subcategorySelector.getParentCategoryIdByName(subcatName);
        System.out.println(" Родительская категория для '" + subcatName + "': " + parentId);
        System.out.println();

        System.out.println(" Детальная информация о подкатегории '" + subcatName + "':");
        Map<String, Object> subcatWithParent = subcategorySelector.getSubcategoryWithParent(subcatName);
        if (subcatWithParent != null) {
            System.out.println("  ID: " + subcatWithParent.get("id"));
            System.out.println("  Название: " + subcatWithParent.get("name"));
            System.out.println("  Parent ID: " + subcatWithParent.get("parent_id"));
            System.out.println("  Parent Name: " + subcatWithParent.get("parent_name"));
        }
        System.out.println();

        System.out.println("Все теги со значениями:");
        List<Map<String, Object>> tags = tagSelector.getTagsWithValues();
        for (Map<String, Object> tag : tags) {
            System.out.println("  - " + tag.get("name") + " (ID: " + tag.get("id") + ")");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
            if (values != null && !values.isEmpty()) {
                for (Map<String, Object> value : values) {
                    System.out.println("      * " + value.get("name") + " (ID: " + value.get("id") + ")");
                }
            }
        }
        System.out.println();

        System.out.println("Доступные теги для подкатегории 'Электроника':");
        List<Map<String, Object>> availableTags = tagSelector.getAvailableTagsForSubcategory("Электроника");
        for (Map<String, Object> tag : availableTags) {
            System.out.println("  - " + tag.get("name"));
        }
        System.out.println();

        Long adId = 123L;
        System.out.println("Теги для объявления ID " + adId + ":");
        List<Map<String, Object>> adTags = tagSelector.getTagsForAd(adId);
        for (Map<String, Object> tag : adTags) {
            System.out.println("  - " + tag);
        }
        System.out.println();

        System.out.println("Сохранение тегов для объявления...");
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
        System.out.println("Теги сохранены");
        System.out.println();

        context.close();
        */

    System.out.println("TagsTest заглушка: тест пропущен");
  }
}