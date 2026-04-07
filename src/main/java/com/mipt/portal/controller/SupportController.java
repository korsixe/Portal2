package com.mipt.portal.controller;

import com.mipt.portal.dto.SupportRequestCreateDto;
import com.mipt.portal.entity.SupportRequest;
import com.mipt.portal.entity.User;
import com.mipt.portal.service.SupportRequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

  private final SupportRequestService supportRequestService;

  @GetMapping("/messages")
  public ResponseEntity<?> getMyMessages(HttpSession session) {
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
    }

    List<SupportRequest> messages = supportRequestService.getByUserId(currentUser.getId());
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/messages")
  public ResponseEntity<?> sendMessage(
      @RequestBody @Valid SupportRequestCreateDto dto,
      HttpSession session) {
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
    }

    String userName = currentUser.getName();
    if (userName == null || userName.isBlank()) {
      userName = currentUser.getEmail() != null ? currentUser.getEmail() : "Пользователь";
    }

    SupportRequest created = supportRequestService.create(
        currentUser.getId(),
        userName,
        dto.getMessage().trim()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
}

