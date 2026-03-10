package com.mipt.portal.profanityChecker;

import org.springframework.stereotype.Service;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ProfanityChecker {
  private final HttpClient client = HttpClient.newHttpClient();

  private static final Set<String> PROFANITY_WORDS = new HashSet<>(Arrays.asList(
    "бля", "блят", "бляд", "хуй", "хуё", "хуя", "хую", "хуе",
    "пизд", "еба", "ебл", "ёб", "пох", "долбоеб",
    "муд", "муда", "муде", "мудо", "мудё", "мудак", "мудил",
    "гандон", "гондон", "манде", "залуп", "дроч", "шлюх", "шлюш",
    "damn", "hell", "crap", "bitch", "ass", "shit", "fuck",
    "bastard", "piss", "dick", "cock", "pussy", "whore",
    "slut", "faggot", "douche", "cunt", "nigger", "nigga",
    "asshole", "motherfucker", "bullshit", "dammit", "fucking"
  ));

  public boolean containsProfanity(String text) {
    if (text == null || text.trim().isEmpty()) {
      return false;
    }

    boolean apiResult = checkWithAPI(text);
    if (apiResult) {
      return true;
    }
    return localProfanityCheck(text);
  }

  private boolean checkWithAPI(String text) {
    try {
      String encodedText = URLEncoder.encode(text, "UTF-8");
      String url = "https://www.purgomalum.com/service/containsprofanity?text=" + encodedText;

      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofSeconds(3))
        .GET()
        .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return Boolean.parseBoolean(response.body().trim());
    } catch (Exception e) {
      return false;
    }
  }

  private boolean localProfanityCheck(String text) {
    String lowerText = text.toLowerCase();

    String cleanText = lowerText.replaceAll("[^\\p{L}\\s]", " ");
    String[] words = cleanText.split("\\s+");

    for (String word : words) {
      if (word.length() >= 1 && PROFANITY_WORDS.contains(word)) {
        return true;
      }

      for (String profanity : PROFANITY_WORDS) {
        if (profanity.length() > 1 && word.contains(profanity)) {
          return true;
        }
      }
    }

    return false;
  }
}