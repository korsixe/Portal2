package com.mipt.portal.controller;

import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdminActionType;
import com.mipt.portal.enums.AuditTargetType;
import com.mipt.portal.service.AuditService;
import com.mipt.portal.service.CommentService;
import com.mipt.portal.service.UserService;
import com.mipt.portal.service.ModerationHistoryService;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
public class ModeratorController {

    private final AnnouncementService announcementService;
    private final UserService userService;
    private final CommentService commentService;
    private final AuditService auditService;
    private final ModerationHistoryService moderationHistoryService;

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

    @GetMapping("/history")
    public String history(Model model) {
        List<ModerationHistory> history = moderationHistoryService.getAll();
        Map<Long, String> moderatorNames = history.stream()
                .map(ModerationHistory::getModeratorId)
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> Map.entry(id, userService.findUserById(id).map(User::getName).orElse("ID: " + id)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        model.addAttribute("history", history);
        model.addAttribute("moderatorNames", moderatorNames);
        model.addAttribute("adminActions", auditService.getAdminActions());
        return "/moderator/moderation-history";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam Long adId, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long moderatorId = resolveCurrentUserId(authentication);
        announcementService.changeStatus(adId, AdStatus.ACTIVE, moderatorId, null);
        redirectAttributes.addFlashAttribute("message", "Объявление одобрено");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/moderator/dashboard";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam Long adId,
                         @RequestParam(required = false) String reason,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        Long moderatorId = resolveCurrentUserId(authentication);
        announcementService.changeStatus(adId, AdStatus.REJECTED, moderatorId, reason);
        redirectAttributes.addFlashAttribute("message", "Объявление отправлено на доработку" + (reason != null ? ": " + reason : ""));
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/moderator/dashboard";
    }

    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam Long commentId, Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean removed = false;
        try {
            removed = commentService.deleteComment(commentId);
            auditService.logAdminAction(resolveCurrentUserId(authentication), authentication != null ? authentication.getName() : null,
                    AdminActionType.COMMENT_DELETE, AuditTargetType.COMMENT, commentId, removed ? "Комментарий удален" : "Удаление не удалось");
        } catch (Exception e) {
            log.error("Failed to delete comment {}", commentId, e);
        }

        redirectAttributes.addFlashAttribute("message", removed ? "Комментарий удалён" : "Не удалось удалить комментарий");
        redirectAttributes.addFlashAttribute("messageType", removed ? "success" : "error");
        return "redirect:/moderator/dashboard";
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userService.findUserByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }
}
