package com.mipt.portal.controller;

import com.mipt.portal.entity.Comment;
import com.mipt.portal.repository.CommentRepository;
import com.mipt.portal.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/test/comments")
@RequiredArgsConstructor
public class CommentTestController {

  private final CommentService commentService;
  private final CommentRepository commentRepository;

  @GetMapping("/service/create")
  public ResponseEntity<String> testServiceCreate() {
    try {
      Comment comment = commentService.createComment(1L, 42L, "Тестовый комментарий");
      return ResponseEntity.ok("✅ Service create: " + comment);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
    }
  }

  @GetMapping("/service/get/{id}")
  public ResponseEntity<String> testServiceGet(@PathVariable Long id) {
    try {
      Comment comment = commentService.getComment(id);
      return ResponseEntity.ok("✅ Service get: " + comment);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
    }
  }

  @GetMapping("/repository/save")
  public ResponseEntity<String> testRepositorySave() {
    try {
      Comment comment = new Comment();
      comment.setAdvertisementId(1L);
      comment.setUserId(42L);
      comment.setAuthor("Test User");
      comment.setText("Test text");
      comment.setCreatedAt(java.time.LocalDateTime.now());

      Comment saved = commentRepository.save(comment);
      return ResponseEntity.ok("✅ Repository save: " + saved);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
    }
  }

  @GetMapping("/repository/find/{id}")
  public ResponseEntity<String> testRepositoryFind(@PathVariable Long id) {
    try {
      Comment comment = commentRepository.findById(id);
      return ResponseEntity.ok("✅ Repository find: " + comment);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
    }
  }

  @GetMapping("/all")
  public ResponseEntity<String> testAll() {
    StringBuilder result = new StringBuilder();

    result.append("🔍 ТЕСТИРОВАНИЕ КОММЕНТАРИЕВ\n\n");

    // Тест 1: createComment
    try {
      Comment comment = commentService.createComment(1L, 42L, "Тест");
      result.append("✅ createComment: ").append(comment).append("\n");
    } catch (Exception e) {
      result.append("❌ createComment: ").append(e.getMessage()).append("\n");
    }

    // Тест 2: getComment
    try {
      Comment comment = commentService.getComment(1L);
      result.append("✅ getComment: ").append(comment).append("\n");
    } catch (Exception e) {
      result.append("❌ getComment: ").append(e.getMessage()).append("\n");
    }

    // Тест 3: updateComment
    try {
      commentService.updateComment(1L, "Новый текст");
      result.append("✅ updateComment: OK\n");
    } catch (Exception e) {
      result.append("❌ updateComment: ").append(e.getMessage()).append("\n");
    }

    // Тест 4: deleteComment
    try {
      commentService.deleteComment(1L);
      result.append("✅ deleteComment: OK\n");
    } catch (Exception e) {
      result.append("❌ deleteComment: ").append(e.getMessage()).append("\n");
    }

    // Тест 5: repository save
    try {
      Comment comment = new Comment();
      comment.setAdvertisementId(2L);
      comment.setUserId(43L);
      comment.setAuthor("Test");
      comment.setText("Repo test");
      Comment saved = commentRepository.save(comment);
      result.append("✅ repository save: ").append(saved).append("\n");
    } catch (Exception e) {
      result.append("❌ repository save: ").append(e.getMessage()).append("\n");
    }

    return ResponseEntity.ok(result.toString());
  }
}