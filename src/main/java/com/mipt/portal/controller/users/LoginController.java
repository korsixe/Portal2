package com.mipt.portal.controller.users;

import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
import com.mipt.portal.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.mipt.portal.service.CustomUserDetailsService;
import com.mipt.portal.enums.Role;

@Controller
@RequestMapping("/users")
public class LoginController {

  private final UserService userService;
  private final CustomUserDetailsService userDetailsService;
  private final AuditService auditService;

  public LoginController(UserService userService, CustomUserDetailsService userDetailsService, AuditService auditService) {
    this.userService = userService;
    this.userDetailsService = userDetailsService;
    this.auditService = auditService;
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
      HttpServletRequest request,
      Model model) {

    Optional<User> result = userService.loginUser(email, password);

    if (result.isPresent()) {
      User loggedInUser = result.get();
      session.setAttribute("user", loggedInUser);
      session.setAttribute("userId", loggedInUser.getId());
      session.setAttribute("userName", loggedInUser.getName());
      session.setAttribute("userEmail", loggedInUser.getEmail());

      // Set Spring Security Context and persist to session
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(auth);
      SecurityContextHolder.setContext(securityContext);
      session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

      if (loggedInUser.getRoles().contains(Role.ADMIN)) {
        auditAdminLogin(email, true, request);
        return "redirect:/admin/dashboard";
      } else if (loggedInUser.getRoles().contains(Role.MODERATOR)) {
        return "redirect:/moderator/dashboard";
      } else {
        return "redirect:/dashboard.jsp";
      }
    } else {
      // логируем только попытки админов
      userService.findUserByEmail(email)
          .filter(u -> u.getRoles().contains(Role.ADMIN))
          .ifPresent(u -> auditAdminLogin(email, false, request));
      model.addAttribute("message", "❌ Неверный email или пароль");
      model.addAttribute("messageType", "error");
      model.addAttribute("email", email);
      return "forward:/login.jsp";
    }
  }

  private void auditAdminLogin(String email, boolean success, HttpServletRequest request) {
    String ip = request.getRemoteAddr();
    String ua = request.getHeader("User-Agent");
    auditService.logAdminLogin(email, success, ip, ua);
  }

  @GetMapping("/register")
  public String showRegisterPage() {
    return "redirect:/register.jsp";
  }
}