package com.mipt.portal.user;

import com.mipt.portal.address.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
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
        .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
        .orElse(ResponseEntity.badRequest().build());
  }

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody LoginRequest request) {
    return userService.loginUser(request.getEmail(), request.getPassword())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable long id) {
    return userService.findUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }
}

class RegisterRequest {
  private String email;
  private String name;
  private String password;
  private String passwordAgain;
  private String address;
  private String studyProgram;
  private int course;

  // Геттеры и сеттеры
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getPasswordAgain() { return passwordAgain; }
  public void setPasswordAgain(String passwordAgain) { this.passwordAgain = passwordAgain; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public String getStudyProgram() { return studyProgram; }
  public void setStudyProgram(String studyProgram) { this.studyProgram = studyProgram; }

  public int getCourse() { return course; }
  public void setCourse(int course) { this.course = course; }
}

class LoginRequest {
  private String email;
  private String password;

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
}
