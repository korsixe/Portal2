package com.mipt.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyAdminModeratorRedirectController {

    @GetMapping({
        "/admin/dashboard",
        "/admin/dashboard.jsp",
        "/admin/dashboard.html"
    })
    public String redirectAdminDashboard() {
        return "redirect:/admin";
    }

    @GetMapping({
        "/moderator/dashboard",
        "/moderator/moderation-bord.jsp"
    })
    public String redirectModeratorDashboard() {
        return "redirect:/moderator";
    }

    @GetMapping("/moderator/moderation-history.jsp")
    public String redirectModeratorHistory() {
        return "redirect:/moderator/history";
    }

    @GetMapping("/moderator/login-moderator.jsp")
    public String redirectModeratorLogin() {
        return "redirect:/login";
    }

    @GetMapping({
        "/admin",
        "/moderator",
        "/moderator/history"
    })
    public String forwardToSpa() {
        return "forward:/src/main/frontend/public/index.html";
    }
}
