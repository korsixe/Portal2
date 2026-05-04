package com.mipt.portal.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LevenshteinSearchTest {

  @Test
  void distance_zeroForEqualStrings() {
    assertThat(LevenshteinSearch.levenshteinDistance("abc", "abc")).isZero();
  }

  @Test
  void distance_emptyAndNonEmpty() {
    assertThat(LevenshteinSearch.levenshteinDistance("", "abc")).isEqualTo(3);
    assertThat(LevenshteinSearch.levenshteinDistance("abc", "")).isEqualTo(3);
  }

  @Test
  void distance_oneSubstitution() {
    assertThat(LevenshteinSearch.levenshteinDistance("kitten", "sitten")).isEqualTo(1);
  }

  @Test
  void distance_classicKittenSitting() {
    assertThat(LevenshteinSearch.levenshteinDistance("kitten", "sitting")).isEqualTo(3);
  }

  @Test
  void distance_swapsOrderForLongerSource() {
    // The internal swap branch: when n > m, swap
    assertThat(LevenshteinSearch.levenshteinDistance("abcdef", "ab")).isEqualTo(4);
  }

  @Test
  void distance_throwsOnNull() {
    assertThatThrownBy(() -> LevenshteinSearch.levenshteinDistance(null, "x"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> LevenshteinSearch.levenshteinDistance("x", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void normalized_zeroForBothEmpty() {
    assertThat(LevenshteinSearch.normalizedLevenshteinDistance("", "")).isZero();
  }

  @Test
  void normalized_oneForCompletelyDifferent() {
    assertThat(LevenshteinSearch.normalizedLevenshteinDistance("a", "b")).isEqualTo(1.0);
  }

  @Test
  void similarity_oneForEqual() {
    assertThat(LevenshteinSearch.similarity("hello", "hello")).isEqualTo(1.0);
  }

  @Test
  void isSimilar_aboveThreshold() {
    assertThat(LevenshteinSearch.isSimilar("hello", "hallo", 0.7)).isTrue();
  }

  @Test
  void isSimilar_belowThreshold() {
    assertThat(LevenshteinSearch.isSimilar("hello", "world", 0.9)).isFalse();
  }

  @Test
  void fuzzyContains_findsAllWords() {
    assertThat(LevenshteinSearch.fuzzyContains("Big yellow car", "yellow car", 0.8)).isTrue();
  }

  @Test
  void fuzzyContains_returnsFalseForMissingWord() {
    assertThat(LevenshteinSearch.fuzzyContains("Big yellow car", "blue plane", 0.9)).isFalse();
  }

  @Test
  void fuzzyContains_handlesNullText() {
    assertThat(LevenshteinSearch.fuzzyContains(null, "x", 0.9)).isFalse();
  }

  @Test
  void fuzzyContains_handlesNullQuery() {
    assertThat(LevenshteinSearch.fuzzyContains("text", null, 0.9)).isFalse();
  }

  @Test
  void findSimilarWords_returnsSubset() {
    List<String> result = LevenshteinSearch.findSimilarWords(
        "color", List.of("color", "colour", "totally-different"), 0.7);
    assertThat(result).contains("color", "colour").doesNotContain("totally-different");
  }
}
