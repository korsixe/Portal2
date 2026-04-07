package com.mipt.portal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnnouncementUpdateDto {

  @NotBlank
  private String title;

  @NotBlank
  private String description;

  @NotBlank
  private String category;

  @NotBlank
  private String subcategory;

  @NotBlank
  private String location;

  @NotBlank
  private String condition;

  @NotNull
  @Max(1000000000)
  private Integer price;

  private String action;
}
