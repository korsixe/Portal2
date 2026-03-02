package com.mipt.portal.announcement.dto;

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
}