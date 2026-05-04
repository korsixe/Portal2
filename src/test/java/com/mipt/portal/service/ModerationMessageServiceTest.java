package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationMessageServiceTest {

  @Mock private ModerationService moderationService;
  @Mock private UserService userService;
  @Mock private KafkaMessageService kafkaMessageService;
  @InjectMocks private ModerationMessageService service;

  private User mod;

  @BeforeEach
  void setUp() {
    mod = new User();
    mod.setId(1L);
    mod.setEmail("mod@phystech.edu");
    mod.setName("Mod");
  }

  @Test
  void logModerationAction_emptyWhenNotMod() {
    when(moderationService.isUserModerator(1L)).thenReturn(false);
    assertThat(service.logModerationAction(10L, "approve", "ok", 1L)).isEmpty();
  }

  @Test
  void logModerationAction_emptyWhenUserAbsent() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.empty());
    assertThat(service.logModerationAction(10L, "approve", "ok", 1L)).isEmpty();
  }

  @Test
  void logModerationAction_succeedsAndPublishes() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.of(mod));
    Optional<Long> id = service.logModerationAction(10L, "approve", "ok", 1L);
    assertThat(id).isPresent();
    verify(kafkaMessageService).sendModerationEvent(eq("moderation.action.logged"), anyString(), any());
  }

  @Test
  void logModerationAction_blankReasonBecomesNull() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.of(mod));
    assertThat(service.logModerationAction(10L, "approve", "", 1L)).isPresent();
  }

  @Test
  void logModerationAction_nullReasonOk() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.of(mod));
    assertThat(service.logModerationAction(10L, "approve", null, 1L)).isPresent();
  }

  @Test
  void canSendModerationMessage_delegates() {
    when(moderationService.canSendModerationMessages(1L)).thenReturn(true);
    assertThat(service.canSendModerationMessage(1L)).isTrue();
  }

  @Test
  void createModerationMessage_emptyWhenNotMod() {
    when(moderationService.isUserModerator(1L)).thenReturn(false);
    assertThat(service.createModerationMessage(10L, 1L, "approve", "ok")).isEmpty();
  }

  @Test
  void createModerationMessage_emptyWhenUserAbsent() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.empty());
    assertThat(service.createModerationMessage(10L, 1L, "approve", "ok")).isEmpty();
  }

  @Test
  void createModerationMessage_returnsAndPublishes() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.of(mod));
    assertThat(service.createModerationMessage(10L, 1L, "approve", "ok")).isPresent();
    verify(kafkaMessageService).sendModerationEvent(eq("moderation.message.created"), anyString(), any());
  }

  @Test
  void createModerationMessage_handlesNullReason() {
    when(moderationService.isUserModerator(1L)).thenReturn(true);
    when(userService.findUserById(1L)).thenReturn(Optional.of(mod));
    assertThat(service.createModerationMessage(10L, 1L, "approve", null)).isPresent();
  }
}
