package com.mipt.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
  private Long id;
  private Long advertisementId;
  private Long userId;
  private String author;
  private String text;
  private LocalDateTime createdAt;
}