package com.mipt.portal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfanityCheckerTest {

  private ProfanityChecker checker;

  @BeforeEach
  void setUp() {
    checker = new ProfanityChecker();
  }

  @Test
  void containsProfanity_returnsFalseOnNull() {
    assertThat(checker.containsProfanity(null)).isFalse();
  }

  @Test
  void containsProfanity_returnsFalseOnBlank() {
    assertThat(checker.containsProfanity("   ")).isFalse();
  }

  @Test
  void containsProfanity_returnsFalseOnCleanText() {
    // The external API will likely time out or be unreachable → falls back to local
    assertThat(checker.containsProfanity("Привет, как дела?")).isFalse();
  }

  @Test
  void containsProfanity_findsLocalRussianWord() {
    assertThat(checker.containsProfanity("ты дроч на работе")).isTrue();
  }

  @Test
  void containsProfanity_findsEnglishWord() {
    assertThat(checker.containsProfanity("oh shit happens")).isTrue();
  }

  @Test
  void containsProfanity_findsSubstringMatch() {
    assertThat(checker.containsProfanity("долбоебище")).isTrue();
  }

  @Test
  void containsProfanity_stripsPunctuation() {
    assertThat(checker.containsProfanity("damn!!! что-то пошло не так")).isTrue();
  }
}
