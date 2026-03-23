package com.mipt.portal.users.controller;

import com.mipt.portal.users.User;
import com.mipt.portal.users.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.mipt.portal.service.CustomUserDetailsService;
import com.mipt.portal.users.Role;

@Controller
@RequestMapping("/users")
public class LoginController {

  private final UserService userService;
  private final CustomUserDetailsService userDetailsService;

  public LoginController(UserService userService, CustomUserDetailsService userDetailsService) {
    this.userService = userService;
    this.userDetailsService = userDetailsService;
  }

  @GetMapping("/login")
  public String showLoginPage(Model model) {
    if (model.containsAttribute("error")) {
      model.addAttribute("message", "Неверный email или пароль");
      model.addAttribute("messageType", "error");
    }
    return "redirect:/login.jsp";
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

      // Set Spring Security Context
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(auth);

      if (loggedInUser.getRoles().contains(Role.ADMIN)) {
        return "redirect:/admin/dashboard";
      } else if (loggedInUser.getRoles().contains(Role.MODERATOR)) {
        return "redirect:/moderator/dashboard";
      } else {
        return "redirect:/dashboard.jsp";
      }
    } else {
      model.addAttribute("message", "❌ Неверный email или пароль");
      model.addAttribute("messageType", "error");
      model.addAttribute("email", email);
      return "forward:/login.jsp";
    }
  }

  @GetMapping("/register")
  public String showRegisterPage() {
    return "redirect:/register.jsp";
  }
}