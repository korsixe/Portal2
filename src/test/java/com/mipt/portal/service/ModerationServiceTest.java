package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

  @Mock private UserService userService;
  @InjectMocks private ModerationService service;

  @Test
  void isUserModerator_delegates() {
    when(userService.isUserModerator(1L)).thenReturn(true);
    assertThat(service.isUserModerator(1L)).isTrue();
  }

  @Test
  void promoteModerator_falseWhenNotAdmin() {
    when(userService.isUserAdmin(1L)).thenReturn(false);
    assertThat(service.promoteModerator(2L, 1L)).contains(false);
  }

  @Test
  void promoteModerator_trueWhenInnerSucceeds() {
    when(userService.isUserAdmin(1L)).thenReturn(true);
    when(userService.assignModeratorRole(2L)).thenReturn(Optional.of(true));
    assertThat(service.promoteModerator(2L, 1L)).contains(true);
  }

  @Test
  void promoteModerator_falseWhenInnerFails() {
    when(userService.isUserAdmin(1L)).thenReturn(true);
    when(userService.assignModeratorRole(2L)).thenReturn(Optional.empty());
    assertThat(service.promoteModerator(2L, 1L)).contains(false);
  }

  @Test
  void demoteModerator_paths() {
    when(userService.isUserAdmin(1L)).thenReturn(false);
    assertThat(service.demoteModerator(2L, 1L)).contains(false);

    when(userService.isUserAdmin(1L)).thenReturn(true);
    when(userService.revokeModeratorRole(2L)).thenReturn(Optional.of(true));
    assertThat(service.demoteModerator(2L, 1L)).contains(true);

    when(userService.revokeModeratorRole(3L)).thenReturn(Optional.empty());
    assertThat(service.demoteModerator(3L, 1L)).contains(false);
  }

  @Test
  void getAllModerators_delegates() {
    when(userService.getAllModerators()).thenReturn(List.of(new User()));
    assertThat(service.getAllModerators()).hasSize(1);
  }

  @Test
  void hasPermissionForAction_falseWhenNotMod() {
    when(userService.isUserModerator(1L)).thenReturn(false);
    assertThat(service.hasPermissionForAction(1L, "X")).isFalse();
  }

  @Test
  void hasPermissionForAction_trueWhenMod() {
    when(userService.isUserModerator(1L)).thenReturn(true);
    assertThat(service.hasPermissionForAction(1L, "X")).isTrue();
  }

  @Test
  void getModeratorInfo_returnsWhenModerator() {
    User u = new User();
    u.addRole(Role.MODERATOR);
    when(userService.findUserById(1L)).thenReturn(Optional.of(u));
    assertThat(service.getModeratorInfo(1L)).isPresent();
  }

  @Test
  void getModeratorInfo_emptyWhenNotMod() {
    User u = new User();
    when(userService.findUserById(1L)).thenReturn(Optional.of(u));
    assertThat(service.getModeratorInfo(1L)).isEmpty();
  }

  @Test
  void getModeratorInfo_emptyWhenAbsent() {
    when(userService.findUserById(1L)).thenReturn(Optional.empty());
    assertThat(service.getModeratorInfo(1L)).isEmpty();
  }

  @Test
  void canModerateAds_delegates() {
    when(userService.isUserModerator(1L)).thenReturn(true);
    assertThat(service.canModerateAds(1L)).isTrue();
  }

  @Test
  void canModerateUsers_delegates() {
    when(userService.isUserModerator(1L)).thenReturn(true);
    assertThat(service.canModerateUsers(1L)).isTrue();
  }

  @Test
  void canSendModerationMessages_delegates() {
    when(userService.isUserModerator(1L)).thenReturn(true);
    assertThat(service.canSendModerationMessages(1L)).isTrue();
  }
}
