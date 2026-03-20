package com.mipt.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String home() {
    return "redirect:/index.jsp";
  }

  @GetMapping("/dashboard")
  public String dashboard() {
    return "redirect:/dashboard.jsp";
  }
}