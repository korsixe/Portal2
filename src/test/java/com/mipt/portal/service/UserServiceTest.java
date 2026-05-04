package com.mipt.portal.service;

import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.Address;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Role;
import com.mipt.portal.exception.InsufficientCoinsException;
import com.mipt.portal.repository.UserRepository;
import com.mipt.portal.util.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private UserValidator userValidator;
  @Mock
  private KafkaMessageService kafkaMessageService;

  @InjectMocks
  private UserService userService;

  private User makeUser(long id, String email) {
    User u = new User();
    u.setId(id);
    u.setEmail(email);
    u.setName("Ivan");
    u.setHashPassword("hashed");
    u.setSalt("salt");
    u.setAdList(new ArrayList<>());
    u.setRoles(new HashSet<>(Set.of(Role.USER)));
    return u;
  }

  // ---------- registerUser ----------

  @Test
  void registerUser_savesAndSendsEvent() {
    when(userValidator.validateEmail("user@phystech.edu")).thenReturn(true);
    when(userValidator.validateName("Ivan")).thenReturn(true);
    when(userValidator.validatePassword("password1")).thenReturn(true);
    when(userValidator.isPasswordStrong("password1")).thenReturn(true);
    when(userRepository.existsByEmail("user@phystech.edu")).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> {
      User u = inv.getArgument(0);
      u.setId(42L);
      return u;
    });

    Optional<User> result = userService.registerUser(
        "user@phystech.edu", "Ivan", "password1", "password1",
        new Address("Москва"), "Прикладная математика", 3);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(42L);
    assertThat(result.get().getRoles()).contains(Role.USER);
    verify(kafkaMessageService).sendUserEvent(eq("user.registered"), eq("42"), any());
  }

  @Test
  void registerUser_throwsWhenEmailEmpty() {
    assertThatThrownBy(() -> userService.registerUser(
        "  ", "Ivan", "password1", "password1", null, "PM", 3))
        .isInstanceOf(IllegalArgumentException.class);
    verify(userRepository, never()).save(any());
  }

  @Test
  void registerUser_throwsWhenPasswordsDoNotMatch() {
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userValidator.validateName(anyString())).thenReturn(true);
    when(userValidator.validatePassword(anyString())).thenReturn(true);
    when(userValidator.isPasswordStrong(anyString())).thenReturn(true);

    assertThatThrownBy(() -> userService.registerUser(
        "user@phystech.edu", "Ivan", "password1", "password2", null, "PM", 3))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Пароли не совпадают");
  }

  @Test
  void registerUser_throwsWhenEmailExists() {
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userValidator.validateName(anyString())).thenReturn(true);
    when(userValidator.validatePassword(anyString())).thenReturn(true);
    when(userValidator.isPasswordStrong(anyString())).thenReturn(true);
    when(userRepository.existsByEmail("user@phystech.edu")).thenReturn(true);

    assertThatThrownBy(() -> userService.registerUser(
        "user@phystech.edu", "Ivan", "password1", "password1", null, "PM", 3))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void registerUser_validatorThrowsPropagates() {
    when(userValidator.validateEmail(anyString()))
        .thenThrow(new IllegalArgumentException("bad email"));
    assertThatThrownBy(() -> userService.registerUser(
        "x@y.com", "Ivan", "password1", "password1", null, "PM", 3))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bad email");
  }

  // ---------- loginUser ----------

  @Test
  void loginUser_returnsUserMaskedAndSendsEvent() {
    User u = makeUser(1L, "user@phystech.edu");
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userRepository.findByEmail("user@phystech.edu")).thenReturn(Optional.of(u));
    when(passwordEncoder.matches("pwsalt", "hashed")).thenReturn(true);

    Optional<User> result = userService.loginUser("user@phystech.edu", "pw");

    assertThat(result).isPresent();
    assertThat(result.get().getHashPassword()).isNull();
    assertThat(result.get().getSalt()).isNull();
    verify(kafkaMessageService).sendUserEvent(eq("user.login"), eq("1"), any());
  }

  @Test
  void loginUser_emptyOnEmptyEmail() {
    assertThat(userService.loginUser("  ", "pw")).isEmpty();
    assertThat(userService.loginUser(null, "pw")).isEmpty();
  }

  @Test
  void loginUser_emptyOnInvalidEmailFormat() {
    when(userValidator.validateEmail(anyString()))
        .thenThrow(new IllegalArgumentException("bad"));
    assertThat(userService.loginUser("x@y.com", "pw")).isEmpty();
  }

  @Test
  void loginUser_emptyOnEmptyPassword() {
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    assertThat(userService.loginUser("u@phystech.edu", "")).isEmpty();
  }

  @Test
  void loginUser_emptyOnUnknownUser() {
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    assertThat(userService.loginUser("u@phystech.edu", "pw")).isEmpty();
  }

  @Test
  void loginUser_emptyOnBannedUser() {
    User u = makeUser(1L, "user@phystech.edu");
    u.setBannedUntil(Instant.now().plusSeconds(100));
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
    assertThat(userService.loginUser("user@phystech.edu", "pw")).isEmpty();
  }

  @Test
  void loginUser_emptyOnFrozenUser() {
    User u = makeUser(1L, "user@phystech.edu");
    u.setFrozenUntil(Instant.now().plusSeconds(100));
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
    assertThat(userService.loginUser("user@phystech.edu", "pw")).isEmpty();
  }

  @Test
  void loginUser_emptyOnInvalidPassword() {
    User u = makeUser(1L, "user@phystech.edu");
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    assertThat(userService.loginUser("user@phystech.edu", "pw")).isEmpty();
  }

  // ---------- updateUser ----------

  @Test
  void updateUser_savesNewFields() {
    User existing = makeUser(1L, "old@phystech.edu");
    User update = makeUser(1L, "old@phystech.edu");
    update.setName("Petr");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userValidator.validateName(anyString())).thenReturn(true);
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    Optional<User> result = userService.updateUser(update);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Petr");
    verify(kafkaMessageService).sendUserEvent(eq("user.updated"), anyString(), any());
  }

  @Test
  void updateUser_emptyWhenUserNotFound() {
    User update = makeUser(99L, "x@y.com");
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThat(userService.updateUser(update)).isEmpty();
  }

  @Test
  void updateUser_emptyWhenEmailTaken() {
    User existing = makeUser(1L, "old@phystech.edu");
    User update = makeUser(1L, "new@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.existsByEmail("new@phystech.edu")).thenReturn(true);
    assertThat(userService.updateUser(update)).isEmpty();
  }

  @Test
  void updateUser_throwsOnValidationFailure() {
    User existing = makeUser(1L, "old@phystech.edu");
    User update = makeUser(1L, "old@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userValidator.validateEmail(anyString()))
        .thenThrow(new IllegalArgumentException("bad"));
    assertThatThrownBy(() -> userService.updateUser(update))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateUser_changesPasswordWhenProvided() {
    User existing = makeUser(1L, "old@phystech.edu");
    User update = makeUser(1L, "old@phystech.edu");
    update.setHashPassword("password1!");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userValidator.validateName(anyString())).thenReturn(true);
    when(userValidator.validatePassword(anyString())).thenReturn(true);
    when(userValidator.isPasswordStrong(anyString())).thenReturn(true);
    when(passwordEncoder.encode(anyString())).thenReturn("newhash");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    Optional<User> result = userService.updateUser(update);
    assertThat(result).isPresent();
    assertThat(result.get().getHashPassword()).isEqualTo("newhash");
  }

  @Test
  void updateUser_emptyWhenInvalidNewPassword() {
    User existing = makeUser(1L, "old@phystech.edu");
    User update = makeUser(1L, "old@phystech.edu");
    update.setHashPassword("weak");
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userValidator.validateEmail(anyString())).thenReturn(true);
    when(userValidator.validateName(anyString())).thenReturn(true);
    when(userValidator.validatePassword(anyString()))
        .thenThrow(new IllegalArgumentException("short"));

    assertThat(userService.updateUser(update)).isEmpty();
  }

  // ---------- announcement list manipulation ----------

  @Test
  void addAnnouncementId_addsAndPersists() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    Optional<Boolean> ok = userService.addAnnouncementId(1L, 100L);
    assertThat(ok).contains(true);
    assertThat(u.getAdList()).contains(100L);
    verify(userRepository).save(u);
  }

  @Test
  void addAnnouncementId_idempotent() {
    User u = makeUser(1L, "u@phystech.edu");
    u.getAdList().add(100L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    Optional<Boolean> ok = userService.addAnnouncementId(1L, 100L);
    assertThat(ok).contains(true);
    verify(userRepository, never()).save(any());
  }

  @Test
  void addAnnouncementId_emptyWhenUserMissing() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.addAnnouncementId(1L, 100L)).isEmpty();
  }

  @Test
  void addAnnouncementId_initializesNullList() {
    User u = makeUser(1L, "u@phystech.edu");
    u.setAdList(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    assertThat(userService.addAnnouncementId(1L, 100L)).contains(true);
    assertThat(u.getAdList()).contains(100L);
  }

  @Test
  void deleteAnnouncementId_removesPresent() {
    User u = makeUser(1L, "u@phystech.edu");
    u.getAdList().add(100L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    Optional<Boolean> ok = userService.deleteAnnouncementId(1L, 100L);
    assertThat(ok).contains(true);
    assertThat(u.getAdList()).doesNotContain(100L);
  }

  @Test
  void deleteAnnouncementId_emptyWhenAdNotPresent() {
    User u = makeUser(1L, "u@phystech.edu");
    u.getAdList().add(50L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    assertThat(userService.deleteAnnouncementId(1L, 100L)).isEmpty();
  }

  @Test
  void deleteAnnouncementId_emptyWhenAdListEmpty() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    assertThat(userService.deleteAnnouncementId(1L, 100L)).isEmpty();
  }

  @Test
  void deleteAnnouncementId_emptyWhenUserMissing() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.deleteAnnouncementId(1L, 100L)).isEmpty();
  }

  // ---------- deleteUser ----------

  @Test
  void deleteUser_returnsTrueWhenExisted() {
    when(userRepository.existsById(1L)).thenReturn(true);
    Optional<Boolean> r = userService.deleteUser(1L);
    assertThat(r).contains(true);
    verify(userRepository).deleteById(1L);
    verify(kafkaMessageService).sendUserEvent(eq("user.deleted"), eq("1"), any());
  }

  @Test
  void deleteUser_emptyWhenAbsent() {
    when(userRepository.existsById(1L)).thenReturn(false);
    assertThat(userService.deleteUser(1L)).isEmpty();
    verify(userRepository, never()).deleteById(anyLong());
  }

  // ---------- find users ----------

  @Test
  void findUserById_masksSensitive() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    Optional<User> result = userService.findUserById(1L);
    assertThat(result).isPresent();
    assertThat(result.get().getHashPassword()).isNull();
    assertThat(result.get().getSalt()).isNull();
  }

  @Test
  void findUserById_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.findUserById(1L)).isEmpty();
  }

  @Test
  void findUserByEmail_masksSensitive() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findByEmail("u@phystech.edu")).thenReturn(Optional.of(u));
    Optional<User> result = userService.findUserByEmail("u@phystech.edu");
    assertThat(result).isPresent();
    assertThat(result.get().getHashPassword()).isNull();
  }

  @Test
  void findUserByEmail_emptyWhenAbsent() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    assertThat(userService.findUserByEmail("u@phystech.edu")).isEmpty();
  }

  @Test
  void existsByEmail_delegates() {
    when(userRepository.existsByEmail("u@phystech.edu")).thenReturn(true);
    assertThat(userService.existsByEmail("u@phystech.edu")).isTrue();
  }

  // ---------- updateUserRating ----------

  @Test
  void updateUserRating_invalidRange() {
    assertThat(userService.updateUserRating(1L, -0.1)).isEmpty();
    assertThat(userService.updateUserRating(1L, 5.1)).isEmpty();
  }

  @Test
  void updateUserRating_emptyWhenUserMissing() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.updateUserRating(1L, 4.0)).isEmpty();
  }

  @Test
  void updateUserRating_persists() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    Optional<Boolean> r = userService.updateUserRating(1L, 4.0);
    assertThat(r).contains(true);
    assertThat(u.getRating()).isEqualTo(4.0);
  }

  // ---------- toggleFavorite ----------

  @Test
  void toggleFavorite_addsAndReturnsTrue() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    boolean liked = userService.toggleFavorite(1L, 100L);
    assertThat(liked).isTrue();
    assertThat(u.getAdList()).contains(100L);
  }

  @Test
  void toggleFavorite_removesIfPresent() {
    User u = makeUser(1L, "u@phystech.edu");
    u.getAdList().add(100L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    boolean liked = userService.toggleFavorite(1L, 100L);
    assertThat(liked).isFalse();
    assertThat(u.getAdList()).doesNotContain(100L);
  }

  @Test
  void toggleFavorite_throwsWhenUserMissing() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.toggleFavorite(1L, 100L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toggleFavorite_handlesNullList() {
    User u = makeUser(1L, "u@phystech.edu");
    u.setAdList(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.toggleFavorite(1L, 1L)).isTrue();
  }

  @Test
  void getFavoriteIds_returnsList() {
    User u = makeUser(1L, "u@phystech.edu");
    u.getAdList().add(10L);
    u.getAdList().add(20L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.getFavoriteIds(1L)).containsExactly(10L, 20L);
  }

  @Test
  void getFavoriteIds_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.getFavoriteIds(1L)).isEmpty();
  }

  @Test
  void getFavoriteIds_emptyWhenAdListNull() {
    User u = makeUser(1L, "u@phystech.edu");
    u.setAdList(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.getFavoriteIds(1L)).isEmpty();
  }

  // ---------- getAllUsers / SystemStats ----------

  @Test
  void getAllUsers_masksSensitive() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findAll()).thenReturn(List.of(u));
    List<User> all = userService.getAllUsers();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).getHashPassword()).isNull();
  }

  @Test
  void buildSystemStats_correctlyCounts() {
    User regular = makeUser(1L, "r@phystech.edu");
    User mod = makeUser(2L, "m@phystech.edu");
    mod.addRole(Role.MODERATOR);
    User admin = makeUser(3L, "a@phystech.edu");
    admin.addRole(Role.ADMIN);
    User adminMod = makeUser(4L, "am@phystech.edu");
    adminMod.addRole(Role.ADMIN);
    adminMod.addRole(Role.MODERATOR);

    when(userRepository.findAll()).thenReturn(List.of(regular, mod, admin, adminMod));

    SystemStats stats = userService.buildSystemStats();
    assertThat(stats.getTotalUsers()).isEqualTo(4);
    assertThat(stats.getAdminCount()).isEqualTo(2);
    assertThat(stats.getModeratorCount()).isEqualTo(1);
    assertThat(stats.getRegularUserCount()).isEqualTo(1);
  }

  @Test
  void buildSystemStats_returnsZeroOnException() {
    when(userRepository.findAll()).thenThrow(new RuntimeException("db"));
    SystemStats stats = userService.buildSystemStats();
    assertThat(stats.getTotalUsers()).isZero();
  }

  // ---------- coins ----------

  @Test
  void addCoins_persistsAndSendsEvent() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    Optional<Boolean> r = userService.addCoins(1L, 50);
    assertThat(r).contains(true);
    assertThat(u.getCoins()).isEqualTo(50);
    verify(kafkaMessageService).sendUserEvent(eq("user.coins.added"), anyString(), any());
  }

  @Test
  void addCoins_emptyOnInvalidAmount() {
    assertThat(userService.addCoins(1L, 0)).isEmpty();
    assertThat(userService.addCoins(1L, -10)).isEmpty();
  }

  @Test
  void addCoins_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.addCoins(1L, 5)).isEmpty();
  }

  @Test
  void deductCoins_throwsInsufficientWhenLow() {
    User u = makeUser(1L, "u@phystech.edu");
    u.setCoins(5);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThatThrownBy(() -> userService.deductCoins(1L, 10))
        .isInstanceOf(InsufficientCoinsException.class);
  }

  @Test
  void deductCoins_succeeds() {
    User u = makeUser(1L, "u@phystech.edu");
    u.setCoins(100);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    Optional<Boolean> r = userService.deductCoins(1L, 30);
    assertThat(r).contains(true);
    assertThat(u.getCoins()).isEqualTo(70);
  }

  @Test
  void deductCoins_emptyOnInvalidAmount() {
    assertThat(userService.deductCoins(1L, 0)).isEmpty();
  }

  @Test
  void deductCoins_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.deductCoins(1L, 10)).isEmpty();
  }

  // ---------- roles ----------

  @Test
  void assignModeratorRole_addsRole() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.assignModeratorRole(1L)).contains(true);
    assertThat(u.getRoles()).contains(Role.MODERATOR);
  }

  @Test
  void assignModeratorRole_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.assignModeratorRole(1L)).isEmpty();
  }

  @Test
  void revokeModeratorRole_removesRole() {
    User u = makeUser(1L, "u@phystech.edu");
    u.addRole(Role.MODERATOR);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.revokeModeratorRole(1L)).contains(true);
    assertThat(u.getRoles()).doesNotContain(Role.MODERATOR);
  }

  @Test
  void revokeModeratorRole_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.revokeModeratorRole(1L)).isEmpty();
  }

  @Test
  void isUserModerator_trueIfRolePresent() {
    User u = makeUser(1L, "u@phystech.edu");
    u.addRole(Role.MODERATOR);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.isUserModerator(1L)).isTrue();
  }

  @Test
  void isUserModerator_falseIfMissing() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.isUserModerator(1L)).isFalse();
  }

  @Test
  void getAllModerators_filtersAndMasks() {
    User regular = makeUser(1L, "r@phystech.edu");
    User mod = makeUser(2L, "m@phystech.edu");
    mod.addRole(Role.MODERATOR);
    when(userRepository.findAll()).thenReturn(List.of(regular, mod));
    List<User> mods = userService.getAllModerators();
    assertThat(mods).hasSize(1);
    assertThat(mods.get(0).getId()).isEqualTo(2L);
    assertThat(mods.get(0).getHashPassword()).isNull();
  }

  @Test
  void assignAdminRole_addsRole() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.assignAdminRole(1L)).contains(true);
    assertThat(u.getRoles()).contains(Role.ADMIN);
  }

  @Test
  void assignAdminRole_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.assignAdminRole(1L)).isEmpty();
  }

  @Test
  void revokeAdminRole_removesRole() {
    User u = makeUser(1L, "u@phystech.edu");
    u.addRole(Role.ADMIN);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.revokeAdminRole(1L)).contains(true);
    assertThat(u.getRoles()).doesNotContain(Role.ADMIN);
  }

  @Test
  void revokeAdminRole_emptyWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.revokeAdminRole(1L)).isEmpty();
  }

  @Test
  void isUserAdmin_truePath() {
    User u = makeUser(1L, "u@phystech.edu");
    u.addRole(Role.ADMIN);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    assertThat(userService.isUserAdmin(1L)).isTrue();
  }

  @Test
  void isUserAdmin_falseWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.isUserAdmin(1L)).isFalse();
  }

  @Test
  void getAllAdmins_filters() {
    User regular = makeUser(1L, "r@phystech.edu");
    User admin = makeUser(2L, "a@phystech.edu");
    admin.addRole(Role.ADMIN);
    when(userRepository.findAll()).thenReturn(List.of(regular, admin));
    List<User> admins = userService.getAllAdmins();
    assertThat(admins).hasSize(1);
    assertThat(admins.get(0).getId()).isEqualTo(2L);
  }

  // ---------- changePassword / deleteAccount ----------

  @Test
  void changePassword_falseWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.changePassword(1L, "old", "new")).isFalse();
  }

  @Test
  void changePassword_falseWhenMismatch() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    assertThat(userService.changePassword(1L, "old", "new")).isFalse();
  }

  @Test
  void changePassword_truePersistsNewHash() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded-new");

    boolean ok = userService.changePassword(1L, "old", "new");
    assertThat(ok).isTrue();
    assertThat(u.getHashPassword()).isEqualTo("encoded-new");
  }

  @Test
  void deleteAccount_falseWhenAbsent() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    assertThat(userService.deleteAccount(1L, "pw")).isFalse();
  }

  @Test
  void deleteAccount_falseOnWrongPassword() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    assertThat(userService.deleteAccount(1L, "pw")).isFalse();
  }

  @Test
  void deleteAccount_trueOnCorrectPassword() {
    User u = makeUser(1L, "u@phystech.edu");
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    boolean r = userService.deleteAccount(1L, "pw");
    assertThat(r).isTrue();
    verify(userRepository, times(1)).delete(u);
  }

  @Test
  void getPasswordEncoder_returnsInjected() {
    assertThat(userService.getPasswordEncoder()).isSameAs(passwordEncoder);
  }
}
