package com.mipt.portal.announcement;

import lombok.Data;

/**
 * Модель объявления
 * Представляет собой сущность, которая хранится в текущей реализации — в памяти
 */
@Data
public class Announcement {
  private Long id;
  private String title;
  private String description;
  private int price;
  private Long authorId;
  private AdStatus status = AdStatus.PENDING;
}