package com.mipt.portal.service;

import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.Address;
import com.mipt.portal.enums.Role;
import com.mipt.portal.entity.User;
import com.mipt.portal.repository.UserRepository;
import com.mipt.portal.util.UserValidator;
import com.mipt.portal.dto.kafka.KafkaEventPayloads;
import com.mipt.portal.exception.InsufficientCoinsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserValidator userValidator;
  private final EmailService emailService;
  private final KafkaMessageService kafkaMessageService;

  @Transactional
  public Optional<User> registerUser(String email, String name, String password,
      String passwordAgain, Address address, String studyProgram, int course) {
    log.info("Attempting to register user with email: {}", email);
    try {
      if (email == null || email.trim().isEmpty()) {
        throw new IllegalArgumentException("Почта обязательна.");
      }

      userValidator.validateEmail(email);
      userValidator.validateName(name);
      userValidator.validatePassword(password);
      userValidator.isPasswordStrong(password);

      if (!password.equals(passwordAgain)) {
        throw new IllegalArgumentException("Пароли не совпадают.");
      }

      if (userRepository.existsByEmail(email)) {
        throw new IllegalArgumentException("Пользователь с таким email уже зарегистрирован.");
      }

      User user = new User();
      user.setEmail(email);
      user.setName(name);
      user.setSalt(UUID.randomUUID().toString().substring(0, 10));
      user.setHashPassword(passwordEncoder.encode(password + user.getSalt()));
      user.setAddress(address);
      user.setStudyProgram(studyProgram);
      user.setCourse(course);
      user.setRating(0.0);
      user.setCoins(0);
      user.setAdList(new ArrayList<>());
      user.addRole(Role.USER); // устанавливаем роль пользователя по умолчанию

      User savedUser = userRepository.save(user);

      log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), email);
      emailService.sendWelcome(savedUser.getEmail(), savedUser.getName());
      kafkaMessageService.sendUserEvent(
          "user.registered",
          String.valueOf(savedUser.getId()),
          new KafkaEventPayloads.UserRegistered(savedUser.getId(), savedUser.getEmail(), savedUser.getName())
      );
      return Optional.of(savedUser);

    } catch (IllegalArgumentException e) {
      log.warn("Registration validation failed: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Error during user registration: {}", e.getMessage(), e);
      throw new IllegalArgumentException("Ошибка при регистрации. Попробуйте позже.");
    }
  }

  public Optional<User> loginUser(String email, String password) {
    log.info("User login attempt with email: {}", email);
    try {
      if (email == null || email.trim().isEmpty()) {
        log.warn("Login failed - email is empty");
        return Optional.empty();
      }

      try {
        userValidator.validateEmail(email);
      } catch (IllegalArgumentException e) {
        log.warn("Login failed - invalid email format: {}", e.getMessage());
        return Optional.empty();
      }

      if (password == null || password.trim().isEmpty()) {
        log.warn("Login failed - password is empty");
        return Optional.empty();
      }

      Optional<User> userOpt = userRepository.findByEmail(email);
      if (userOpt.isEmpty()) {
        log.warn("Login failed - user not found: {}", email);
        return Optional.empty();
      }

      User user = userOpt.get();

      if (user.isBanned()) {
        log.warn("Login failed - user banned: {}", email);
        return Optional.empty();
      }

      if (user.isFrozen()) {
        log.warn("Login failed - user frozen until {}: {}", user.getFrozenUntil(), email);
        return Optional.empty();
      }

      if (!passwordEncoder.matches(password + user.getSalt(), user.getHashPassword())) {
        log.warn("Login failed - invalid password for user: {}", email);
        return Optional.empty();
      }

      log.info("User logged in successfully: {}", email);

      user.setHashPassword(null);
      user.setSalt(null);

      kafkaMessageService.sendUserEvent(
          "user.login",
          String.valueOf(user.getId()),
          new KafkaEventPayloads.UserLogin(user.getId(), user.getEmail())
      );
      return Optional.of(user);

    } catch (Exception e) {
      log.error("Error during user login: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<User> updateUser(User user) throws IllegalArgumentException {
    try {
      Optional<User> existingUser = userRepository.findById(user.getId());
      if (existingUser.isEmpty()) {
        log.warn("Update failed - user not found: {}", user.getId());
        return Optional.empty();
      }

      User existing = existingUser.get();

      if (!existing.getEmail().equals(user.getEmail())) {
        if (userRepository.existsByEmail(user.getEmail())) {
          log.warn("Update failed - email already exists: {}", user.getEmail());
          return Optional.empty();
        }
      }

      try {
        userValidator.validateEmail(user.getEmail());
        userValidator.validateName(user.getName());
      } catch (IllegalArgumentException e) {
        log.warn("Update failed - validation error: {}", e.getMessage());
        throw e;
      }

      existing.setEmail(user.getEmail());
      existing.setName(user.getName());
      existing.setAddress(user.getAddress());
      existing.setStudyProgram(user.getStudyProgram());
      existing.setCourse(user.getCourse());

      if (user.getHashPassword() != null && !user.getHashPassword().isEmpty()) {
        try {
          userValidator.validatePassword(user.getHashPassword());
          userValidator.isPasswordStrong(user.getHashPassword());
          existing.setSalt(UUID.randomUUID().toString().substring(0, 10));
          existing.setHashPassword(passwordEncoder.encode(user.getHashPassword() + existing.getSalt()));
        } catch (IllegalArgumentException e) {
          log.warn("Update failed - invalid password: {}", e.getMessage());
          return Optional.empty();
        }
      }

      User updatedUser = userRepository.save(existing);
      log.info("User updated successfully: {}", user.getEmail());
      kafkaMessageService.sendUserEvent(
          "user.updated",
          String.valueOf(updatedUser.getId()),
          new KafkaEventPayloads.UserUpdated(updatedUser.getId(), updatedUser.getEmail())
      );
      return Optional.of(updatedUser);

    } catch (IllegalArgumentException e) {
      // Пробрасываем ошибку валидации наружу
      throw e;
    } catch (Exception e) {
      log.error("Error during user update: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<Boolean> addAnnouncementId(Long userId, Long adId) {
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Add announcement failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();

      if (user.getAdList() == null) {
        user.setAdList(new ArrayList<>());
      }

      if (!user.getAdList().contains(adId)) {
        user.getAdList().add(adId);
        userRepository.save(user);
        log.info("Announcement {} added to user {}", adId, userId);
        kafkaMessageService.sendUserEvent(
            "user.announcement.added",
            String.valueOf(userId),
            new KafkaEventPayloads.UserAnnouncementChanged(userId, adId)
        );
      }

      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error adding announcement to user: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<Boolean> deleteAnnouncementId(Long userId, Long adId) {
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Delete announcement failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();

      if (user.getAdList() == null || user.getAdList().isEmpty()) {
        log.warn("Delete announcement failed - user has no announcements: {}", userId);
        return Optional.empty();
      }

      if (user.getAdList().contains(adId)) {
        user.getAdList().remove(adId);
        userRepository.save(user);
        log.info("Announcement {} removed from user {}", adId, userId);
        kafkaMessageService.sendUserEvent(
            "user.announcement.removed",
            String.valueOf(userId),
            new KafkaEventPayloads.UserAnnouncementChanged(userId, adId)
        );
        return Optional.of(true);
      } else {
        log.warn("Delete announcement failed - announcement {} not found for user {}", adId, userId);
        return Optional.empty();
      }

    } catch (Exception e) {
      log.error("Error deleting announcement from user: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<Boolean> deleteUser(long userId) {
    try {
      if (!userRepository.existsById(userId)) {
        log.warn("Delete failed - user not found: {}", userId);
        return Optional.empty();
      }

      userRepository.deleteById(userId);
      log.info("User deleted successfully: {}", userId);
      kafkaMessageService.sendUserEvent(
          "user.deleted",
          String.valueOf(userId),
          new KafkaEventPayloads.UserDeleted(userId)
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error deleting user: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public Optional<User> findUserById(long userId) {
    try {
      Optional<User> user = userRepository.findById(userId);
      if (user.isPresent()) {
        User foundUser = user.get();
        foundUser.setHashPassword(null);
        foundUser.setSalt(null);
        return Optional.of(foundUser);
      }
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error finding user by id: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public Optional<User> findUserByEmail(String email) {
    try {
      Optional<User> user = userRepository.findByEmail(email);
      if (user.isPresent()) {
        User foundUser = user.get();
        foundUser.setHashPassword(null);
        foundUser.setSalt(null);
        return Optional.of(foundUser);
      }
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error finding user by email: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<Boolean> updateUserRating(long userId, double newRating) {
    try {
      if (newRating < 0.0 || newRating > 5.0) {
        log.warn("Update rating failed - invalid rating: {}", newRating);
        return Optional.empty();
      }

      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Update rating failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      user.setRating(newRating);
      userRepository.save(user); // save возвращает User, игнорируем
      log.info("User {} rating updated to {}", userId, newRating);
      kafkaMessageService.sendUserEvent(
          "user.rating.updated",
          String.valueOf(userId),
          new KafkaEventPayloads.UserRatingUpdated(userId, newRating)
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error updating user rating: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public boolean toggleFavorite(Long userId, Long adId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    if (user.getAdList() == null) {
      user.setAdList(new ArrayList<>());
    }
    boolean liked;
    if (user.getAdList().contains(adId)) {
      user.getAdList().remove(adId);
      liked = false;
    } else {
      user.getAdList().add(adId);
      liked = true;
    }
    userRepository.save(user);
    kafkaMessageService.sendUserEvent(
        "user.favorite.toggled",
        String.valueOf(userId),
        new KafkaEventPayloads.UserFavoriteToggled(userId, adId, liked)
    );
    return liked;
  }

  public List<Long> getFavoriteIds(Long userId) {
    return userRepository.findById(userId)
        .map(u -> u.getAdList() != null ? u.getAdList() : List.<Long>of())
        .orElse(List.of());
  }

  public boolean existsByEmail(String email) {
    try {
      return userRepository.existsByEmail(email);
    } catch (Exception e) {
      log.error("Error checking if email exists: {}", e.getMessage(), e);
      return false;
    }
  }

  public List<User> getAllUsers() {
    try {
      List<User> users = userRepository.findAll();
      users.forEach(user -> {
        user.setHashPassword(null);
        user.setSalt(null);
      });
      return users;
    } catch (Exception e) {
      log.error("Error getting all users: {}", e.getMessage(), e);
      return List.of();
    }
  }

  /**
   * Собирает агрегированную статистику по ролям без двойного учета администраторов как модераторов.
   */
  public SystemStats buildSystemStats() {
    try {
      List<User> users = userRepository.findAll();

      long adminCount = users.stream()
          .filter(user -> user.getRoles().contains(Role.ADMIN))
          .count();

      long moderatorCount = users.stream()
          .filter(user -> user.getRoles().contains(Role.MODERATOR) && !user.getRoles().contains(Role.ADMIN))
          .count();

      long regularUsers = users.size() - adminCount - moderatorCount;
      if (regularUsers < 0) {
        regularUsers = 0;
      }

      return new SystemStats(users.size(), adminCount, moderatorCount, regularUsers);
    } catch (Exception e) {
      log.error("Error building system stats: {}", e.getMessage(), e);
      return new SystemStats(0, 0, 0, 0);
    }
  }

  @Transactional
  public Optional<Boolean> addCoins(long userId, int coinsToAdd) {
    try {
      if (coinsToAdd <= 0) {
        log.warn("Add coins failed - invalid amount: {}", coinsToAdd);
        return Optional.empty();
      }

      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Add coins failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      user.addCoins(coinsToAdd);
      userRepository.save(user);
      log.info("Added {} coins to user {}. New balance: {}", coinsToAdd, userId, user.getCoins());
      kafkaMessageService.sendUserEvent(
          "user.coins.added",
          String.valueOf(userId),
          new KafkaEventPayloads.UserCoinsChanged(userId, coinsToAdd, user.getCoins())
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error adding coins: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<Boolean> deductCoins(long userId, int coinsToDeduct) {
    try {
      if (coinsToDeduct <= 0) {
        log.warn("Deduct coins failed - invalid amount: {}", coinsToDeduct);
        return Optional.empty();
      }

      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Deduct coins failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      if (!user.spendCoins(coinsToDeduct)) {
        log.warn("Deduct coins failed - insufficient coins for user {}. Balance: {}, required: {}",
            userId, user.getCoins(), coinsToDeduct);
        throw new InsufficientCoinsException(userId, user.getCoins(), coinsToDeduct);
      }

      userRepository.save(user);
      log.info("Deducted {} coins from user {}. New balance: {}", coinsToDeduct, userId, user.getCoins());
      kafkaMessageService.sendUserEvent(
          "user.coins.deducted",
          String.valueOf(userId),
          new KafkaEventPayloads.UserCoinsChanged(userId, coinsToDeduct, user.getCoins())
      );
      return Optional.of(true);

    } catch (InsufficientCoinsException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error deducting coins: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Назначает роль модератора пользователю
   */
  @Transactional
  public Optional<Boolean> assignModeratorRole(Long userId) {
    log.info("Assigning moderator role to user ID: {}", userId);
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Assign moderator role failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      user.addRole(Role.MODERATOR);

      userRepository.save(user);
      log.info("Moderator role assigned successfully to user: {}", userId);
      kafkaMessageService.sendUserEvent(
          "user.role.changed",
          String.valueOf(userId),
          new KafkaEventPayloads.UserRoleChanged(userId, "MODERATOR", "assigned")
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error assigning moderator role to user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Убирает роль модератора у пользователя
   */
  @Transactional
  public Optional<Boolean> revokeModeratorRole(Long userId) {
    log.info("Revoking moderator role from user ID: {}", userId);
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Revoke moderator role failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      user.removeRole(Role.MODERATOR);

      userRepository.save(user);
      log.info("Moderator role revoked successfully from user: {}", userId);
      kafkaMessageService.sendUserEvent(
          "user.role.changed",
          String.valueOf(userId),
          new KafkaEventPayloads.UserRoleChanged(userId, "MODERATOR", "revoked")
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error revoking moderator role from user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Проверяет, является ли пользователь модератором
   */
  public boolean isUserModerator(Long userId) {
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      return userOpt.map(User::isModerator).orElse(false);
    } catch (Exception e) {
      log.error("Error checking moderator status: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * Получает всех пользователей с ролью модератора
   */
  public List<User> getAllModerators() {
    try {
      List<User> allUsers = userRepository.findAll();
      List<User> moderators = allUsers.stream()
          .filter(User::isModerator)
          .peek(user -> {
            user.setHashPassword(null);
            user.setSalt(null);
          })
          .toList();

      log.info("Found {} moderators", moderators.size());
      return moderators;
    } catch (Exception e) {
      log.error("Error getting all moderators: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * Назначает роль администратора пользователю
   */
  @Transactional
  public Optional<Boolean> assignAdminRole(Long userId) {
    log.info("Assigning admin role to user ID: {}", userId);
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Assign admin role failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      user.addRole(Role.ADMIN);

      userRepository.save(user);
      log.info("Admin role assigned successfully to user: {}", userId);
      kafkaMessageService.sendUserEvent(
          "user.role.changed",
          String.valueOf(userId),
          new KafkaEventPayloads.UserRoleChanged(userId, "ADMIN", "assigned")
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error assigning admin role to user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Убирает роль администратора у пользователя
   */
  @Transactional
  public Optional<Boolean> revokeAdminRole(Long userId) {
    log.info("Revoking admin role from user ID: {}", userId);
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        log.warn("Revoke admin role failed - user not found: {}", userId);
        return Optional.empty();
      }

      User user = userOpt.get();
      user.removeRole(Role.ADMIN);

      userRepository.save(user);
      log.info("Admin role revoked successfully from user: {}", userId);
      kafkaMessageService.sendUserEvent(
          "user.role.changed",
          String.valueOf(userId),
          new KafkaEventPayloads.UserRoleChanged(userId, "ADMIN", "revoked")
      );
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error revoking admin role from user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Проверяет, является ли пользователь администратором
   */
  public boolean isUserAdmin(Long userId) {
    try {
      Optional<User> userOpt = userRepository.findById(userId);
      return userOpt.map(User::isAdmin).orElse(false);
    } catch (Exception e) {
      log.error("Error checking admin status: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * Получает всех пользователей с ролью администратора
   */
  public List<User> getAllAdmins() {
    try {
      List<User> allUsers = userRepository.findAll();
      List<User> admins = allUsers.stream()
          .filter(User::isAdmin)
          .peek(user -> {
            user.setHashPassword(null);
            user.setSalt(null);
          })
          .toList();

      log.info("Found {} admins", admins.size());
      return admins;
    } catch (Exception e) {
      log.error("Error getting all admins: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * Смена пароля пользователя
   */
  public boolean changePassword(Long userId, String currentPassword, String newPassword) {
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();

    // Проверяем текущий пароль
    if (!passwordEncoder.matches(currentPassword, user.getHashPassword())) {
      return false;
    }

    // Устанавливаем новый пароль (захешированный)
    user.setHashPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    return true;
  }

  /**
   * Удаление аккаунта пользователя
   */
  public boolean deleteAccount(Long userId, String password) {
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();

    // Проверяем пароль
    if (!passwordEncoder.matches(password, user.getHashPassword())) {
      return false;
    }

    // Удаляем пользователя
    userRepository.delete(user);
    return true;
  }

  public PasswordEncoder getPasswordEncoder() {
    return this.passwordEncoder;
  }
}

