package com.mipt.portal.controller;

import com.mipt.portal.dto.ChangePasswordRequest;
import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class ChangePasswordController {

  @Autowired
  private UserService userService;

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
      @RequestAttribute("user") User user) {

    // Проверяем текущий пароль
    Optional<User> loginResult = userService.loginUser(user.getEmail(), request.getCurrentPassword());

    if (loginResult.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "❌ Неверный текущий пароль"
      ));
    }

    // Валидация нового пароля
    if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "❌ Пароль должен содержать минимум 8 символов"
      ));
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "❌ Пароли не совпадают"
      ));
    }

    // Обновляем пароль
    User userToUpdate = new User();
    userToUpdate.setId(user.getId());
    userToUpdate.setHashPassword(request.getNewPassword());

    Optional<User> updateResult = userService.updateUser(userToUpdate);

    if (updateResult.isPresent()) {
      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "✅ Пароль успешно изменен!",
          "user", updateResult.get()
      ));
    } else {
      return ResponseEntity.status(500).body(Map.of(
          "success", false,
          "message", "❌ Не удалось обновить пароль"
      ));
    }
  }
}
