package com.mipt.portal.users;

import com.mipt.portal.address.Address;
import com.mipt.portal.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

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
  public ResponseEntity<User> login(@RequestBody LoginRequest request) {
    log.info("Received login request for email: {}", request.getEmail());
    return userService.loginUser(request.getEmail(), request.getPassword())
        .map(user -> {
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
}
