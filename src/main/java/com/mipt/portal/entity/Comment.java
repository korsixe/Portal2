package com.mipt.portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ad_id", nullable = false)
  private Long advertisementId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}