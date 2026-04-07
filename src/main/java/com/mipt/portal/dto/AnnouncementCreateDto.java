package com.mipt.portal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AnnouncementCreateDto {
  @NotBlank(message = "Название обязательно")
  private String title;

  @NotBlank(message = "Описание обязательно")
  private String description;

  private String category;

  private String subcategory;

  private String location;

  private String condition;

  @Min(value = -1, message = "Цена не может быть меньше -1")
  private int price;

  @NotNull(message = "ID автора обязателен")
  private Long authorId;

  private List<String> photoUrls;
}