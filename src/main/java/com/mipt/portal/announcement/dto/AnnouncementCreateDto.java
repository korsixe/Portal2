package com.mipt.portal.announcement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для создания нового объявления
 * Используется для отделения запроса клиента от внутренней сущности
 * Содержит аннотации валидации для проверки корректности входящих данных.
 */
@Data
public class AnnouncementCreateDto {
  @NotBlank(message = "Название обязательно")
  private String title;

  @NotBlank(message = "Описание обязательно")
  private String description;

  @Min(value = 0, message = "Цена не может быть отрицательной")
  private int price;

  @NotNull(message = "ID автора обязателен")
  private Long authorId;
}

