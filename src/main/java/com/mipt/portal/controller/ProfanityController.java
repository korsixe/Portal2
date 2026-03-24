package com.mipt.portal.controller;

import com.mipt.portal.service.ProfanityChecker;
import com.mipt.portal.dto.ProfanityCheckRequest;
import com.mipt.portal.dto.ProfanityCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/profanity")
@RequiredArgsConstructor
public class ProfanityController {

  private final ProfanityChecker profanityChecker;

  @PostMapping("/check")
  public ResponseEntity<ProfanityCheckResponse> checkProfanity(
    @RequestBody(required = false) ProfanityCheckRequest request,
    @RequestParam(required = false) String text) {

    String textToCheck = null;

    if (request != null && request.getText() != null) {
      textToCheck = request.getText();
    } else if (text != null) {
      textToCheck = text;
    }

    boolean hasProfanity = profanityChecker.containsProfanity(textToCheck);

    return ResponseEntity.ok(new ProfanityCheckResponse(hasProfanity));
  }

  @PostMapping("/check-form")
  public ResponseEntity<ProfanityCheckResponse> checkProfanityForm(
    @RequestParam String text) {

    boolean hasProfanity = profanityChecker.containsProfanity(text);
    return ResponseEntity.ok(new ProfanityCheckResponse(hasProfanity));
  }

  @GetMapping("/check")
  public ResponseEntity<ProfanityCheckResponse> checkProfanityGet(
    @RequestParam String text) {

    boolean hasProfanity = profanityChecker.containsProfanity(text);
    return ResponseEntity.ok(new ProfanityCheckResponse(hasProfanity));
  }
}