package com.mipt.portal.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserValidatorTest {

  private UserValidator validator;

  @BeforeEach
  void setUp() {
    validator = new UserValidator();
  }

  @Test
  void validateEmail_acceptsCorrectPhystechEmail() {
    assertThat(validator.validateEmail("ivan.ivanov@phystech.edu")).isTrue();
    assertThat(validator.validateEmail("a1@phystech.edu")).isTrue();
  }

  @Test
  void validateEmail_rejectsNull() {
    assertThatThrownBy(() -> validator.validateEmail(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Почта обязательна");
  }

  @Test
  void validateEmail_rejectsTooShort() {
    assertThatThrownBy(() -> validator.validateEmail("a@b"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateEmail_rejectsUppercase() {
    assertThatThrownBy(() -> validator.validateEmail("Ivan.Ivanov@phystech.edu"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("нижнем регистре");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "ivan@gmail.com",
      "ivan@phystech.com",
      "@phystech.edu",
      ".ivan@phystech.edu",
      "ivan.@phystech.edu"
  })
  void validateEmail_rejectsInvalidPattern(String bad) {
    assertThatThrownBy(() -> validator.validateEmail(bad))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateName_acceptsSimpleName() {
    assertThat(validator.validateName("Ivan")).isTrue();
  }

  @Test
  void validateName_rejectsNull() {
    assertThatThrownBy(() -> validator.validateName(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateName_rejectsEmpty() {
    assertThatThrownBy(() -> validator.validateName(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateName_rejectsSpaces() {
    assertThatThrownBy(() -> validator.validateName("Ivan Ivanov"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("без пробелов");
  }

  @Test
  void validatePassword_acceptsRange() {
    assertThat(validator.validatePassword("password1")).isTrue();
    assertThat(validator.validatePassword("a".repeat(30))).isTrue();
  }

  @Test
  void validatePassword_rejectsNull() {
    assertThatThrownBy(() -> validator.validatePassword(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validatePassword_rejectsTooShort() {
    assertThatThrownBy(() -> validator.validatePassword("abc"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validatePassword_rejectsTooLong() {
    assertThatThrownBy(() -> validator.validatePassword("a".repeat(31)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void isPasswordStrong_acceptsStrong() {
    // upper(2) + lower(1) + digit(1.5) = 4.5, length>10 doesn't help here but enough
    assertThat(validator.isPasswordStrong("AbcdEf1234")).isTrue();
  }

  @Test
  void isPasswordStrong_acceptsWithSpecialChar() {
    assertThat(validator.isPasswordStrong("password1!")).isTrue();
  }

  @Test
  void isPasswordStrong_rejectsTooSimple() {
    assertThatThrownBy(() -> validator.isPasswordStrong("aaaaaa"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("слишком простой");
  }

  @Test
  void isPasswordStrong_acceptsLongLowercaseWithDigit() {
    // lower(1) + digit(1.5) + size>10 (1.5) = 4.0 — passes
    assertThat(validator.isPasswordStrong("abcdefghi1k")).isTrue();
  }
}
