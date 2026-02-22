package com.mipt.portal.user;

import org.springframework.stereotype.Component;
import java.util.List;

@Component  // Добавил @Component, чтобы Spring мог внедрять его в сервис
public class UserValidator {

  private static final List<String> STRENGTH_CRITERIA = List.of("!", "?", "@", "#", "$", "%", "&",
      "*", "_", "-");

  public boolean validateEmail(String email) {
    if (email == null || email.length() < 5) {
      throw new IllegalArgumentException("Почта обязательна.");
    }

    String emailPattern = "^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]@phystech\\.edu$";

    if (!email.equals(email.toLowerCase())) {
      throw new IllegalArgumentException("Почта должна быть в нижнем регистре!");
    }

    if (!email.matches(emailPattern)) {
      throw new IllegalArgumentException(
          "Неправильный формат почты. Пример физтех-почты: ivanov.ii@phystech.edu");
    }

    return true;
  }

  public boolean validateName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Имя не может быть пустым");
    } else if (name.contains(" ")) {
      throw new IllegalArgumentException("Имя должно быть без пробелов!");
    }
    return true;
  }

  public boolean validatePassword(String password) {
    if (password == null || password.length() < 8 || password.length() > 30) {
      throw new IllegalArgumentException("Длина пароля должна быть минимум 8 символов");
    }
    return true;
  }

  public boolean isPasswordStrong(String password) {
    double hasLower = 0, hasUpper = 0, hasDigit = 0, hasSpecialChar = 0, goodSize = 0;

    for (char i : password.toCharArray()) {
      if (Character.isLowerCase(i)) {
        hasLower = 1;
      }
      if (Character.isUpperCase(i)) {
        hasUpper = 2;
      }
      if (Character.isDigit(i)) {
        hasDigit = 1.5;
      }
      if (STRENGTH_CRITERIA.contains(String.valueOf(i))) {
        hasSpecialChar = 2;
      }
    }

    if (password.length() > 10) {
      goodSize = 1.5;
    }

    double strength = hasLower + hasUpper + hasDigit + hasSpecialChar + goodSize;

    if (strength < 4) {
      throw new IllegalArgumentException("Ваш пароль слишком простой!");
    }
    return true;
  }
}