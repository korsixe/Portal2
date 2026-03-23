package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.users.User;
import com.mipt.portal.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
public class ModeratorController {

    private final AnnouncementService announcementService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        List<Announcement> pendingAds = announcementService.getPendingForModerator();
        model.addAttribute("ads", pendingAds);
        
        if (authentication != null) {
            String email = authentication.getName();
            Optional<User> user = userService.findUserByEmail(email);
            user.ifPresent(u -> model.addAttribute("moderator", u));
        }
        
        return "moderator/moderation-bord";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam Long adId) {
        announcementService.changeStatus(adId, AdStatus.ACTIVE);
        return "redirect:/moderator/dashboard";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long adId) {
        announcementService.changeStatus(adId, AdStatus.REJECTED);
        return "redirect:/moderator/dashboard";
    }
}


