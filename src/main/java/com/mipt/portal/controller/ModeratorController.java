package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.CommentService;
import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.User;
import com.mipt.portal.service.UserService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final CommentService commentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        List<Announcement> pendingAds = announcementService.getPendingForModerator();
        model.addAttribute("ads", pendingAds);
        SystemStats stats = userService.buildSystemStats();
        model.addAttribute("stats", stats);
        
        if (authentication != null) {
            String email = authentication.getName();
            Optional<User> user = userService.findUserByEmail(email);
            user.ifPresent(u -> model.addAttribute("moderator", u));
        }
        
        return "/moderator/moderation-bord"; // абсолютное имя вида, чтобы избежать относительного /moderator/moderator/...
    }

    @PostMapping("/approve")
    public String approve(@RequestParam Long adId, RedirectAttributes redirectAttributes) {
        announcementService.changeStatus(adId, AdStatus.ACTIVE);
        redirectAttributes.addFlashAttribute("message", "Объявление одобрено");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/moderator/dashboard";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long adId,
                         @RequestParam(required = false) String reason,
                         RedirectAttributes redirectAttributes) {
        announcementService.changeStatus(adId, AdStatus.REJECTED);
        redirectAttributes.addFlashAttribute("message", "Объявление отправлено на доработку" + (reason != null ? ": " + reason : ""));
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/moderator/dashboard";
    }

    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam Long commentId, RedirectAttributes redirectAttributes) {
        boolean removed = false;
        try {
            //removed = commentService.deleteComment(commentId);
        } catch (Exception e) {
            log.error("Failed to delete comment {}", commentId, e);
        }

        redirectAttributes.addFlashAttribute("message", removed ? "Комментарий удалён" : "Не удалось удалить комментарий");
        redirectAttributes.addFlashAttribute("messageType", removed ? "success" : "error");
        return "redirect:/moderator/dashboard";
    }
}
