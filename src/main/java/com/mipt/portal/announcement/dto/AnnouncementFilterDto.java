package com.mipt.portal.announcement.dto;

import com.mipt.portal.announcement.enums.Category;
import com.mipt.portal.announcement.enums.Condition;
import java.time.Instant;
import lombok.Data;

/**
 * DTO для инкапсуляции параметров поиска и фильтрации объявлений
 * Передается в GET-запросах
 */
@Data
public class AnnouncementFilterDto {
  private String text;
  private Integer minPrice;
  private Integer maxPrice;
  private Category category;
  private String subcategory;
  private Condition condition;
  private Instant createdAfter;
  private String sortBy;
  private String sortDirection;
}