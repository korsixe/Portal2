package com.mipt.portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDto {
  @NotBlank
  private String content;
}

