package com.mipt.portal.service;

import com.mipt.portal.entity.Comment;
import com.mipt.portal.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  @Transactional
  public Comment createComment(Long advertisementId, Long userId, String content) {
    Comment comment = new Comment();
    comment.setAdvertisementId(advertisementId);
    comment.setUserId(userId);
    comment.setContent(content);
    comment.setCreatedAt(LocalDateTime.now());

    Comment savedComment = commentRepository.save(comment);
    log.info("Комментарий добавлен к объявлению {} от пользователя {}", advertisementId, userId);
    return savedComment;
  }

  @Transactional
  public Comment createComment(Long advertisementId, Long userId, String userName, String content) {
    Comment comment = new Comment();
    comment.setAdvertisementId(advertisementId);
    comment.setUserId(userId);
    comment.setUserName(userName);
    comment.setContent(content);
    comment.setCreatedAt(LocalDateTime.now());

    Comment savedComment = commentRepository.save(comment);
    log.info("Комментарий добавлен к объявлению {} от пользователя {}", advertisementId, userName);
    return savedComment;
  }

  @Transactional(readOnly = true)
  public List<Comment> getCommentsByAdId(Long advertisementId) {
    return commentRepository.findByAdvertisementIdOrderByCreatedAtDesc(advertisementId);
  }

  @Transactional(readOnly = true)
  public Comment findById(Long id) {
    return commentRepository.findById(id).orElse(null);
  }

  @Transactional
  public void deleteComment(Long id) {
    commentRepository.deleteById(id);
    log.info("Комментарий {} удален", id);
  }


  @Transactional
  public Comment updateComment(Long id, String newContent) {
    Comment comment = findById(id);
    if (comment != null) {
      comment.setContent(newContent);
      return commentRepository.save(comment);
    }
    return null;
  }

  @Transactional(readOnly = true)
  public long getCommentsCount(Long advertisementId) {
    return commentRepository.countByAdvertisementId(advertisementId);
  }
}
