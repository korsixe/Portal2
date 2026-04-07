package com.mipt.portal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdDetailsController {

  @GetMapping("/ad-details")
  public String legacyAdDetailsRedirect(@RequestParam("id") Long adId) {
    return "redirect:/ad/" + adId;
  }
}