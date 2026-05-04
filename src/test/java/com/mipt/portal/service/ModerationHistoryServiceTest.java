package com.mipt.portal.service;

import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.ModerationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationHistoryServiceTest {

  @Mock private ModerationHistoryRepository repo;
  @Mock private KafkaMessageService kafka;
  @InjectMocks private ModerationHistoryService service;

  @Test
  void record_savesAndPublishes() {
    when(repo.save(any(ModerationHistory.class))).thenAnswer(inv -> {
      ModerationHistory h = inv.getArgument(0);
      h.setId(99L);
      return h;
    });
    service.record(1L, 7L, AdStatus.UNDER_MODERATION, AdStatus.ACTIVE, "ok");
    ArgumentCaptor<ModerationHistory> cap = ArgumentCaptor.forClass(ModerationHistory.class);
    verify(repo).save(cap.capture());
    assertThat(cap.getValue().getAdId()).isEqualTo(1L);
    assertThat(cap.getValue().getModeratorId()).isEqualTo(7L);
    verify(kafka).sendModerationEvent(eq("moderation.history.recorded"), anyString(), any());
  }

  @Test
  void record_blankReasonBecomesNull() {
    when(repo.save(any(ModerationHistory.class))).thenAnswer(inv -> {
      ModerationHistory h = inv.getArgument(0);
      h.setId(1L);
      return h;
    });
    service.record(1L, 7L, AdStatus.DRAFT, AdStatus.UNDER_MODERATION, "");
    verify(kafka).sendModerationEvent(any(), any(), any());
  }

  @Test
  void getHistory_delegates() {
    ModerationHistory h = new ModerationHistory();
    when(repo.findAllByAdIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(h));
    assertThat(service.getHistory(1L)).containsExactly(h);
  }
}
