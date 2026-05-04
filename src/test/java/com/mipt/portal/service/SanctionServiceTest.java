package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import com.mipt.portal.entity.UserSanction;
import com.mipt.portal.enums.SanctionType;
import com.mipt.portal.repository.UserRepository;
import com.mipt.portal.repository.UserSanctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanctionServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserSanctionRepository sanctionRepository;
  @Mock private KafkaMessageService kafka;
  @InjectMocks private SanctionService service;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(2L);
    user.setEmail("u@phystech.edu");
  }

  @Test
  void freezeUser_setsFrozenUntilAndPublishes() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    Optional<Boolean> r = service.freezeUser(1L, 2L, "spam", 24);
    assertThat(r).contains(true);
    assertThat(user.getFrozenUntil()).isAfter(Instant.now());
    assertThat(user.getFrozenReason()).isEqualTo("spam");

    ArgumentCaptor<UserSanction> cap = ArgumentCaptor.forClass(UserSanction.class);
    verify(sanctionRepository).save(cap.capture());
    assertThat(cap.getValue().getType()).isEqualTo(SanctionType.FREEZE);

    verify(kafka).sendUserEvent(eq("user.sanction.applied"), anyString(), any());
  }

  @Test
  void freezeUser_falseWhenAbsent() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThat(service.freezeUser(1L, 99L, "spam", 24)).contains(false);
  }

  @Test
  void banUser_setsBannedUntilAndPublishes() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    Optional<Boolean> r = service.banUser(1L, 2L, "really bad", 7);
    assertThat(r).contains(true);
    assertThat(user.getBannedUntil()).isAfter(Instant.now());
    assertThat(user.getBanReason()).isEqualTo("really bad");
    ArgumentCaptor<UserSanction> cap = ArgumentCaptor.forClass(UserSanction.class);
    verify(sanctionRepository).save(cap.capture());
    assertThat(cap.getValue().getType()).isEqualTo(SanctionType.BAN);
  }

  @Test
  void banUser_blankReasonHandled() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    Optional<Boolean> r = service.banUser(1L, 2L, "", 7);
    assertThat(r).contains(true);
  }

  @Test
  void banUser_falseWhenAbsent() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThat(service.banUser(1L, 99L, "x", 7)).contains(false);
  }

  @Test
  void liftSanctions_clearsState() {
    user.setFrozenUntil(Instant.now().plusSeconds(60));
    user.setFrozenReason("spam");
    user.setBannedUntil(Instant.now().plusSeconds(60));
    user.setBanReason("ban");
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    Optional<Boolean> r = service.liftSanctions(2L);
    assertThat(r).contains(true);
    assertThat(user.getFrozenUntil()).isNull();
    assertThat(user.getFrozenReason()).isNull();
    assertThat(user.getBannedUntil()).isNull();
    assertThat(user.getBanReason()).isNull();
    verify(kafka).sendUserEvent(eq("user.sanction.lifted"), anyString(), any());
  }

  @Test
  void liftSanctions_falseWhenAbsent() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThat(service.liftSanctions(99L)).contains(false);
  }
}
