package com.mipt.portal.controller.users;

import com.mipt.portal.entity.Address;
import com.mipt.portal.dto.*;
import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.mipt.portal.service.CustomUserDetailsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final CustomUserDetailsService userDetailsService;

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
    log.info("Received registration request for email: {}", request.getEmail());
    Address address = new Address(request.getAddress());
    return userService.registerUser(
            request.getEmail(),
            request.getName(),
            request.getPassword(),
            request.getPasswordAgain(),
            address,
            request.getStudyProgram(),
            request.getCourse()
        )
        .map(user -> {
          log.info("Successfully registered user: {}", request.getEmail());
          return ResponseEntity.status(HttpStatus.CREATED).body(user);
        })
        .orElseGet(() -> {
          log.warn("Failed to register user: {}", request.getEmail());
          return ResponseEntity.badRequest().build();
        });
  }

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody LoginRequest request, HttpSession session) {
    log.info("Received login request for email: {}", request.getEmail());
    return userService.loginUser(request.getEmail(), request.getPassword())
        .map(user -> {
          // Сохраняем пользователя в сессию
          session.setAttribute("user", user);
          session.setAttribute("userId", user.getId());
          session.setAttribute("userName", user.getName());
          session.setAttribute("userEmail", user.getEmail());

          // Сохраняем контекст безопасности для ролей
          UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
          securityContext.setAuthentication(auth);
          SecurityContextHolder.setContext(securityContext);
          session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

          log.info("Successfully logged in user: {}", request.getEmail());
          return ResponseEntity.ok(user);
        })
        .orElseGet(() -> {
          log.warn("Login failed for email: {}", request.getEmail());
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        });
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable long id) {
    log.info("Fetching user details for ID: {}", id);
    return userService.findUserById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> {
          log.warn("User not found with ID: {}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  /**
   * Получить текущего авторизованного пользователя
   * GET /api/users/me
   */
  @GetMapping("/me")
  public ResponseEntity<User> getCurrentUser(HttpSession session) {
    Long userId = (Long) session.getAttribute("userId");
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return userService.findUserById(userId)
        .map(user -> {
          user.setHashPassword(null);
          user.setSalt(null);
          session.setAttribute("user", user);
          session.setAttribute("userName", user.getName());
          session.setAttribute("userEmail", user.getEmail());
          return ResponseEntity.ok(user);
        })
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  /**
   * Смена пароля
   * POST /api/users/change-password
   * Body: { "currentPassword": "old", "newPassword": "new" }
   */
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(
      @RequestBody ChangePasswordRequest request,
      HttpSession session) {

    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
    }

    boolean changed = userService.changePassword(
        currentUser.getId(),
        request.getCurrentPassword(),
        request.getNewPassword()
    );

    if (changed) {
      log.info("Password changed for user: {}", currentUser.getEmail());
      return ResponseEntity.ok().body("Пароль успешно изменен");
    } else {
      return ResponseEntity.badRequest().body("Неверный текущий пароль");
    }
  }

  /**
   * Удаление аккаунта
   * DELETE /api/users/delete-account
   * Body: { "password": "user_password" }
   */
  @DeleteMapping("/delete-account")
  public ResponseEntity<?> deleteAccount(
      @RequestBody DeleteAccountRequest request,
      HttpSession session) {

    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
    }

    boolean deleted = userService.deleteAccount(currentUser.getId(), request.getPassword());

    if (deleted) {
      session.invalidate();
      log.info("Account deleted for user: {}", currentUser.getEmail());
      return ResponseEntity.ok().body("Аккаунт удален");
    } else {
      return ResponseEntity.badRequest().body("Неверный пароль");
    }
  }

  /**
   * Выход из системы
   * POST /api/logout
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpSession session) {
    session.invalidate();
    log.info("User logged out");
    return ResponseEntity.ok().body("Выход выполнен");
  }
}
