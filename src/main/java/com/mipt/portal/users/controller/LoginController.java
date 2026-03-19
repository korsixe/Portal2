package com.mipt.portal.users.controller;

import com.mipt.portal.users.User;
import com.mipt.portal.users.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
public class LoginController {

  private final UserService userService;

  public LoginController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/login")
  public String login(@RequestParam String email,
      @RequestParam String password,
      HttpSession session,
      Model model) {

    Optional<User> result = userService.loginUser(email, password);

    if (result.isPresent()) {
      User loggedInUser = result.get();
      session.setAttribute("user", loggedInUser);
      session.setAttribute("userId", loggedInUser.getId());
      session.setAttribute("userName", loggedInUser.getName());
      session.setAttribute("userEmail", loggedInUser.getEmail());

      return "redirect:/dashboard";
    } else {
      model.addAttribute("message", "Invalid email or password");
      model.addAttribute("messageType", "error");
      return "login";
    }
  }
}
