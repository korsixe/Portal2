package com.mipt.portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportRequestCreateDto {
  @NotBlank
  private String message;
}

