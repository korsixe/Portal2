package com.mipt.portal.service;

import java.util.List;
import java.util.stream.Collectors;

public class LevenshteinSearch {

  public static int levenshteinDistance(CharSequence s, CharSequence t) {
    if (s == null || t == null) {
      throw new IllegalArgumentException("Strings must not be null");
    }

    int n = s.length();
    int m = t.length();

    if (n == 0) return m;
    if (m == 0) return n;

    if (n > m) {
      CharSequence tmp = s;
      s = t;
      t = tmp;
      n = m;
      m = t.length();
    }

    int[] p = new int[n + 1];
    int[] d = new int[n + 1];

    // Инициализация первой строки
    for (int i = 0; i <= n; i++) {
      p[i] = i;
    }

    for (int j = 1; j <= m; j++) {
      char t_j = t.charAt(j - 1);
      d[0] = j;

      for (int i = 1; i <= n; i++) {
        int cost = s.charAt(i - 1) == t_j ? 0 : 1;
        d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
      }
      int[] _d = p;
      p = d;
      d = _d;
    }

    return p[n];
  }

  public static double normalizedLevenshteinDistance(String s1, String s2) {
    int maxLength = Math.max(s1.length(), s2.length());
    if (maxLength == 0) return 0.0;

    int distance = levenshteinDistance(s1, s2);
    return (double) distance / maxLength;
  }

  public static double similarity(String s1, String s2) {
    return 1.0 - normalizedLevenshteinDistance(s1, s2);
  }

  public static boolean isSimilar(String s1, String s2, double similarityThreshold) {
    return similarity(s1, s2) >= similarityThreshold;
  }

  public static boolean fuzzyContains(String text, String query, double similarityThreshold) {
    if (text == null || query == null) return false;

    String[] textWords = text.toLowerCase().split("\\s+");
    String[] queryWords = query.toLowerCase().split("\\s+");

    for (String queryWord : queryWords) {
      boolean foundSimilar = false;

      for (String textWord : textWords) {
        if (isSimilar(textWord, queryWord, similarityThreshold)) {
          foundSimilar = true;
          break;
        }
      }

      if (!foundSimilar) {
        return false;
      }
    }

    return true;
  }

  public static List<String> findSimilarWords(String target, List<String> candidates, double similarityThreshold) {
    return candidates.stream()
        .filter(candidate -> isSimilar(candidate, target, similarityThreshold))
        .collect(Collectors.toList());
  }
}