package com.mipt.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactController {

  @GetMapping(value = {"/", "/dashboard", "/create-ad", "/edit-ad", "/successful-create-ad", "/successful-edit-ad", "/edit-profile", "/profile", "/login", "/register", "/support", "/ad/{id}", "/error"})
  public String forwardToReact() {
    return "forward:/src/main/frontend/public/index.html";
  }

  @GetMapping("/notifications")
  public String notificationsRedirect() {
    return "redirect:/dashboard";
  }
}