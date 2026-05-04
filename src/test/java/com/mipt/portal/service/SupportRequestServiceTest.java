package com.mipt.portal.service;

import com.mipt.portal.entity.SupportRequest;
import com.mipt.portal.repository.SupportRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class SupportRequestServiceTest {

  @Mock private SupportRequestRepository repo;
  @Mock private KafkaMessageService kafka;
  @InjectMocks private SupportRequestService service;

  @Test
  void getByUserId_delegates() {
    SupportRequest r = new SupportRequest();
    when(repo.findByUserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(r));
    assertThat(service.getByUserId(1L)).hasSize(1);
  }

  @Test
  void create_savesAndPublishes() {
    when(repo.save(any(SupportRequest.class))).thenAnswer(inv -> {
      SupportRequest r = inv.getArgument(0);
      r.setId(1L);
      return r;
    });
    SupportRequest saved = service.create(2L, "Ivan", "help");
    assertThat(saved.getId()).isEqualTo(1L);
    assertThat(saved.getUserId()).isEqualTo(2L);
    assertThat(saved.getUserName()).isEqualTo("Ivan");
    assertThat(saved.getMessage()).isEqualTo("help");
    verify(kafka).sendSupportEvent(eq("support.request.created"), anyString(), any());
  }
}
