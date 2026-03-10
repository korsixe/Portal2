package com.mipt.portal.annoucementContent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
  private Long id;
  private String author;
  private String text;
  private LocalDateTime createdAt;
  private Long advertisementId;
}