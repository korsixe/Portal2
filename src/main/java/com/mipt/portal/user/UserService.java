package com.mipt.portal.user;

import com.mipt.portal.address.Address;
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

  @Transactional
  public Optional<User> registerUser(String email, String name, String password,
      String passwordAgain, Address address, String studyProgram, int course) {
    try {
      if (email == null || email.trim().isEmpty()) {
        log.warn("Registration failed - email is empty");
        return Optional.empty();
      }

      try {
        userValidator.validateEmail(email);
        userValidator.validateName(name);
        userValidator.validatePassword(password);
        userValidator.isPasswordStrong(password);
      } catch (IllegalArgumentException e) {
        log.warn("Validation failed: {}", e.getMessage());
        return Optional.empty();
      }

      if (!password.equals(passwordAgain)) {
        log.warn("Registration failed - passwords do not match");
        return Optional.empty();
      }

      if (userRepository.existsByEmail(email)) {
        log.warn("Registration failed - email already exists: {}", email);
        return Optional.empty();
      }

      User user = new User();
      user.setEmail(email);
      user.setName(name);
      user.setHashPassword(passwordEncoder.encode(password));
      user.setSalt(UUID.randomUUID().toString().substring(0, 10));
      user.setAddress(address);
      user.setStudyProgram(studyProgram);
      user.setCourse(course);
      user.setRating(0.0);
      user.setCoins(0);
      user.setAdList(new ArrayList<>());
      user.setModerator(false);

      Optional<User> savedUserOpt = userRepository.save(user);
      if (savedUserOpt.isEmpty()) {
        return Optional.empty();
      }
      User savedUser = savedUserOpt.get();

      log.info("User registered successfully: {}", email);

      savedUser.setHashPassword(null);
      savedUser.setSalt(null);

      return Optional.of(savedUser);

    } catch (Exception e) {
      log.error("Error during user registration: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public Optional<User> loginUser(String email, String password) {
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

      if (!passwordEncoder.matches(password, user.getHashPassword())) {
        log.warn("Login failed - invalid password for user: {}", email);
        return Optional.empty();
      }

      log.info("User logged in successfully: {}", email);

      // Очищаем чувствительные данные
      user.setHashPassword(null);
      user.setSalt(null);

      return Optional.of(user);

    } catch (Exception e) {
      log.error("Error during user login: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Transactional
  public Optional<User> updateUser(User user) {
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
        return Optional.empty();
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
          existing.setHashPassword(passwordEncoder.encode(user.getHashPassword()));
        } catch (IllegalArgumentException e) {
          log.warn("Update failed - invalid password: {}", e.getMessage());
          return Optional.empty();
        }
      }

      Optional<User> updatedUserOpt = userRepository.save(existing);
      if (updatedUserOpt.isEmpty()) {
        return Optional.empty();
      }
      User updatedUser = updatedUserOpt.get();
      log.info("User updated successfully: {}", user.getEmail());


      updatedUser.setHashPassword(null);
      updatedUser.setSalt(null);

      return Optional.of(updatedUser);

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
      userRepository.save(user);
      log.info("User {} rating updated to {}", userId, newRating);
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error updating user rating: {}", e.getMessage(), e);
      return Optional.empty();
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
      return List.of(); // Возвращаем пустой список в случае ошибки
    }
  }

  public boolean existsByEmail(String email) {
    try {
      return userRepository.existsByEmail(email);
    } catch (Exception e) {
      log.error("Error checking if email exists: {}", e.getMessage(), e);
      return false;
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
        return Optional.empty();
      }

      userRepository.save(user);
      log.info("Deducted {} coins from user {}. New balance: {}", coinsToDeduct, userId, user.getCoins());
      return Optional.of(true);

    } catch (Exception e) {
      log.error("Error deducting coins: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }
}