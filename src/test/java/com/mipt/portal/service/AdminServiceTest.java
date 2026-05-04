package com.mipt.portal.service;

import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdminActionType;
import com.mipt.portal.enums.AuditTargetType;
import com.mipt.portal.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock private UserService userService;
  @Mock private ModerationService moderationService;
  @Mock private SanctionService sanctionService;
  @Mock private AuditService auditService;
  @InjectMocks private AdminService adminService;

  private User adminUser;
  private User regularUser;

  @BeforeEach
  void setUp() {
    adminUser = new User();
    adminUser.setId(1L);
    adminUser.setEmail("admin@phystech.edu");
    adminUser.addRole(Role.ADMIN);

    regularUser = new User();
    regularUser.setId(2L);
    regularUser.setEmail("user@phystech.edu");
    regularUser.addRole(Role.USER);
  }

  private void asAdmin() {
    when(userService.isUserAdmin(1L)).thenReturn(true);
  }

  @Test
  void isUserAdmin_delegates() {
    when(userService.isUserAdmin(5L)).thenReturn(true);
    assertThat(adminService.isUserAdmin(5L)).isTrue();
  }

  @Test
  void getAdminInfo_returnsWhenAdmin() {
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.getAdminInfo(1L)).isPresent();
  }

  @Test
  void getAdminInfo_emptyWhenNotAdmin() {
    when(userService.findUserById(2L)).thenReturn(Optional.of(regularUser));
    assertThat(adminService.getAdminInfo(2L)).isEmpty();
  }

  @Test
  void getAdminInfo_emptyWhenAbsent() {
    when(userService.findUserById(99L)).thenReturn(Optional.empty());
    assertThat(adminService.getAdminInfo(99L)).isEmpty();
  }

  @Test
  void getAllUsers_emptyWhenNotAdmin() {
    when(userService.isUserAdmin(2L)).thenReturn(false);
    assertThat(adminService.getAllUsers(2L)).isEmpty();
  }

  @Test
  void getAllUsers_returnsListWhenAdmin() {
    asAdmin();
    when(userService.getAllUsers()).thenReturn(List.of(regularUser));
    assertThat(adminService.getAllUsers(1L)).hasSize(1);
  }

  @Test
  void getUserById_emptyWhenNotAdmin() {
    when(userService.isUserAdmin(2L)).thenReturn(false);
    assertThat(adminService.getUserById(2L, 3L)).isEmpty();
  }

  @Test
  void getUserById_returnsWhenAdmin() {
    asAdmin();
    when(userService.findUserById(2L)).thenReturn(Optional.of(regularUser));
    assertThat(adminService.getUserById(1L, 2L)).isPresent();
  }

  @Test
  void deleteUser_falseWhenNotAdmin() {
    when(userService.isUserAdmin(2L)).thenReturn(false);
    assertThat(adminService.deleteUser(2L, 3L)).contains(false);
  }

  @Test
  void deleteUser_falseWhenSelfDelete() {
    asAdmin();
    assertThat(adminService.deleteUser(1L, 1L)).contains(false);
  }

  @Test
  void deleteUser_trueAndAuditWhenSuccess() {
    asAdmin();
    when(userService.deleteUser(2L)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));

    assertThat(adminService.deleteUser(1L, 2L)).contains(true);
    verify(auditService).logAdminAction(
        eq(1L), eq("admin@phystech.edu"),
        eq(AdminActionType.USER_DELETE), eq(AuditTargetType.USER),
        eq(2L), any());
  }

  @Test
  void deleteUser_falseWhenInnerFails() {
    asAdmin();
    when(userService.deleteUser(2L)).thenReturn(Optional.empty());
    assertThat(adminService.deleteUser(1L, 2L)).contains(false);
  }

  @Test
  void updateUserRating_pathThroughAdmin() {
    asAdmin();
    when(userService.updateUserRating(2L, 4.0)).thenReturn(Optional.of(true));
    assertThat(adminService.updateUserRating(1L, 2L, 4.0)).contains(true);
  }

  @Test
  void updateUserRating_falseWhenNotAdmin() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.updateUserRating(99L, 2L, 4.0)).contains(false);
  }

  @Test
  void updateUserRating_falseWhenInnerFails() {
    asAdmin();
    when(userService.updateUserRating(2L, 4.0)).thenReturn(Optional.empty());
    assertThat(adminService.updateUserRating(1L, 2L, 4.0)).contains(false);
  }

  @Test
  void addCoinsToUser_audits() {
    asAdmin();
    when(userService.addCoins(2L, 50)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.addCoinsToUser(1L, 2L, 50)).contains(true);
    verify(auditService).logAdminAction(eq(1L), any(), eq(AdminActionType.COINS_CHANGE),
        eq(AuditTargetType.COINS), eq(2L), any());
  }

  @Test
  void addCoinsToUser_falseWhenNotAdmin() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.addCoinsToUser(99L, 2L, 50)).contains(false);
  }

  @Test
  void addCoinsToUser_falseWhenInnerFails() {
    asAdmin();
    when(userService.addCoins(anyLong(), eq(50))).thenReturn(Optional.empty());
    assertThat(adminService.addCoinsToUser(1L, 2L, 50)).contains(false);
  }

  @Test
  void deductCoinsFromUser_audits() {
    asAdmin();
    when(userService.deductCoins(2L, 30)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.deductCoinsFromUser(1L, 2L, 30)).contains(true);
    verify(auditService).logAdminAction(any(), any(), eq(AdminActionType.COINS_CHANGE),
        eq(AuditTargetType.COINS), any(), any());
  }

  @Test
  void deductCoinsFromUser_falseWhenNotAdmin() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.deductCoinsFromUser(99L, 2L, 30)).contains(false);
  }

  @Test
  void deductCoinsFromUser_innerFailsToFalse() {
    asAdmin();
    when(userService.deductCoins(2L, 30)).thenReturn(Optional.empty());
    assertThat(adminService.deductCoinsFromUser(1L, 2L, 30)).contains(false);
  }

  @Test
  void promoteToModerator_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.promoteToModerator(99L, 2L)).contains(false);

    asAdmin();
    when(userService.assignModeratorRole(2L)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.promoteToModerator(1L, 2L)).contains(true);

    when(userService.assignModeratorRole(3L)).thenReturn(Optional.empty());
    assertThat(adminService.promoteToModerator(1L, 3L)).contains(false);
  }

  @Test
  void demoteFromModerator_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.demoteFromModerator(99L, 2L)).contains(false);

    asAdmin();
    when(userService.revokeModeratorRole(2L)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.demoteFromModerator(1L, 2L)).contains(true);

    when(userService.revokeModeratorRole(3L)).thenReturn(Optional.empty());
    assertThat(adminService.demoteFromModerator(1L, 3L)).contains(false);
  }

  @Test
  void promoteToAdmin_blocksSelfPromotion() {
    asAdmin();
    assertThat(adminService.promoteToAdmin(1L, 1L)).contains(false);
    verify(userService, never()).assignAdminRole(any());
  }

  @Test
  void promoteToAdmin_pathsOk() {
    asAdmin();
    when(userService.assignAdminRole(2L)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.promoteToAdmin(1L, 2L)).contains(true);

    when(userService.assignAdminRole(3L)).thenReturn(Optional.empty());
    assertThat(adminService.promoteToAdmin(1L, 3L)).contains(false);
  }

  @Test
  void promoteToAdmin_notAdmin() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.promoteToAdmin(99L, 2L)).contains(false);
  }

  @Test
  void demoteFromAdmin_blocksSelf() {
    asAdmin();
    assertThat(adminService.demoteFromAdmin(1L, 1L)).contains(false);
  }

  @Test
  void demoteFromAdmin_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.demoteFromAdmin(99L, 2L)).contains(false);

    asAdmin();
    when(userService.revokeAdminRole(2L)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.demoteFromAdmin(1L, 2L)).contains(true);

    when(userService.revokeAdminRole(3L)).thenReturn(Optional.empty());
    assertThat(adminService.demoteFromAdmin(1L, 3L)).contains(false);
  }

  @Test
  void getAllModerators_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.getAllModerators(99L)).isEmpty();

    asAdmin();
    when(userService.getAllModerators()).thenReturn(List.of(regularUser));
    assertThat(adminService.getAllModerators(1L)).hasSize(1);
  }

  @Test
  void getAllAdmins_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.getAllAdmins(99L)).isEmpty();

    asAdmin();
    when(userService.getAllAdmins()).thenReturn(List.of(adminUser));
    assertThat(adminService.getAllAdmins(1L)).hasSize(1);
  }

  @Test
  void getSystemStatistics_aggregates() {
    asAdmin();
    User u1 = new User(); u1.addRole(Role.USER);
    User u2 = new User(); u2.addRole(Role.MODERATOR);
    User u3 = new User(); u3.addRole(Role.ADMIN);
    when(userService.getAllUsers()).thenReturn(List.of(u1, u2, u3));

    AdminService.AdminStatistics stats = adminService.getSystemStatistics(1L);
    assertThat(stats.getTotalUsers()).isEqualTo(3L);
    assertThat(stats.getModerators()).isEqualTo(2L); // includes admin via isModerator()
    assertThat(stats.getAdmins()).isEqualTo(1L);
  }

  @Test
  void getSystemStatistics_emptyWhenNotAdmin() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    AdminService.AdminStatistics stats = adminService.getSystemStatistics(99L);
    assertThat(stats.getTotalUsers()).isZero();
  }

  @Test
  void freezeUser_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.freezeUser(99L, 2L, "spam", 10)).contains(false);

    asAdmin();
    when(sanctionService.freezeUser(1L, 2L, "spam", 10)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.freezeUser(1L, 2L, "spam", 10)).contains(true);
    verify(auditService).logAdminAction(any(), any(),
        eq(AdminActionType.USER_SANCTION), eq(AuditTargetType.USER), eq(2L), any());
  }

  @Test
  void banUser_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.banUser(99L, 2L, "spam", 7)).contains(false);

    asAdmin();
    when(sanctionService.banUser(1L, 2L, "spam", 7)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.banUser(1L, 2L, "spam", 7)).contains(true);
  }

  @Test
  void liftSanctions_paths() {
    when(userService.isUserAdmin(99L)).thenReturn(false);
    assertThat(adminService.liftSanctions(99L, 2L)).contains(false);

    asAdmin();
    when(sanctionService.liftSanctions(2L)).thenReturn(Optional.of(true));
    when(userService.findUserById(1L)).thenReturn(Optional.of(adminUser));
    assertThat(adminService.liftSanctions(1L, 2L)).contains(true);
  }

  @Test
  void adminStatistics_pojoSettersGetters() {
    AdminService.AdminStatistics s = new AdminService.AdminStatistics();
    s.setTotalUsers(10);
    s.setRegularUsers(7);
    s.setModerators(2);
    s.setAdmins(1);
    assertThat(s.getTotalUsers()).isEqualTo(10);
    assertThat(s.getRegularUsers()).isEqualTo(7);
    assertThat(s.getModerators()).isEqualTo(2);
    assertThat(s.getAdmins()).isEqualTo(1);
  }
}
