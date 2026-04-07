package com.mipt.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactController {

  @GetMapping(value = {"/", "/dashboard", "/create-ad", "/edit-ad", "/edit-profile", "/profile", "/login", "/register"})
  public String forwardToReact() {
    return "forward:/src/main/frontend/public/index.html";
  }
}