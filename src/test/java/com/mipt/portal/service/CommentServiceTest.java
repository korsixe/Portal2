package com.mipt.portal.service;

import com.mipt.portal.entity.Comment;
import com.mipt.portal.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock private CommentRepository commentRepository;
  @Mock private KafkaMessageService kafkaMessageService;
  @InjectMocks private CommentService service;

  @Test
  void createComment_threeArgs_savesAndPublishes() {
    when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
      Comment c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });
    Comment saved = service.createComment(10L, 2L, "hello");
    assertThat(saved.getId()).isEqualTo(1L);
    assertThat(saved.getAdvertisementId()).isEqualTo(10L);
    assertThat(saved.getUserId()).isEqualTo(2L);
    assertThat(saved.getContent()).isEqualTo("hello");
    verify(kafkaMessageService).sendCommentEvent(eq("comment.created"), eq("1"), any());
  }

  @Test
  void createComment_fourArgs_setsUserName() {
    when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
      Comment c = inv.getArgument(0);
      c.setId(2L);
      return c;
    });
    Comment saved = service.createComment(10L, 2L, "Ivan", "hello");
    assertThat(saved.getUserName()).isEqualTo("Ivan");
    verify(kafkaMessageService).sendCommentEvent(eq("comment.created"), eq("2"), any());
  }

  @Test
  void getCommentsByAdId_delegates() {
    Comment c = new Comment();
    when(commentRepository.findByAdvertisementIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(c));
    assertThat(service.getCommentsByAdId(10L)).hasSize(1);
  }

  @Test
  void findById_present() {
    Comment c = new Comment();
    when(commentRepository.findById(1L)).thenReturn(Optional.of(c));
    assertThat(service.findById(1L)).isSameAs(c);
  }

  @Test
  void findById_absent() {
    when(commentRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(service.findById(1L)).isNull();
  }

  @Test
  void deleteComment_callsRepoAndPublishes() {
    service.deleteComment(5L);
    verify(commentRepository).deleteById(5L);
    verify(kafkaMessageService).sendCommentEvent(eq("comment.deleted"), eq("5"), any());
  }

  @Test
  void updateComment_setsContentAndPublishes() {
    Comment c = new Comment();
    c.setId(1L);
    c.setAdvertisementId(10L);
    c.setUserId(2L);
    when(commentRepository.findById(1L)).thenReturn(Optional.of(c));
    when(commentRepository.save(c)).thenAnswer(inv -> inv.getArgument(0));

    Comment updated = service.updateComment(1L, "newContent");
    assertThat(updated.getContent()).isEqualTo("newContent");
    verify(kafkaMessageService).sendCommentEvent(eq("comment.updated"), anyString(), any());
  }

  @Test
  void updateComment_returnsNullWhenAbsent() {
    when(commentRepository.findById(99L)).thenReturn(Optional.empty());
    assertThat(service.updateComment(99L, "x")).isNull();
  }

  @Test
  void getCommentsCount_delegates() {
    when(commentRepository.countByAdvertisementId(10L)).thenReturn(5L);
    assertThat(service.getCommentsCount(10L)).isEqualTo(5L);
  }
}
