package com.mipt.portal.controller;

import com.mipt.portal.dto.ModerationActionRequest;
import com.mipt.portal.dto.ModerationHistoryResponse;
import com.mipt.portal.dto.ModeratorDashboardResponse;
import com.mipt.portal.dto.SimpleActionResponse;
import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.AdminActionAudit;
import com.mipt.portal.entity.Announcement;
import com.mipt.portal.entity.ModerationHistory;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.AdStatus;
import com.mipt.portal.repository.AdminActionAuditRepository;
import com.mipt.portal.repository.ModerationHistoryRepository;
import com.mipt.portal.service.AnnouncementService;
import com.mipt.portal.service.CommentService;
import com.mipt.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModeratorApiController {

    private final AnnouncementService announcementService;
    private final UserService userService;
    private final CommentService commentService;
    private final ModerationHistoryRepository moderationHistoryRepository;
    private final AdminActionAuditRepository adminActionAuditRepository;

    @GetMapping("/dashboard")
    public ModeratorDashboardResponse dashboard(Authentication authentication) {
        List<Announcement> pendingAds = announcementService.getPendingForModerator();
        SystemStats stats = userService.buildSystemStats();
        User moderator = resolveCurrentUser(authentication);
        return new ModeratorDashboardResponse(pendingAds, stats, moderator);
    }

    @PostMapping("/approve")
    public SimpleActionResponse approve(@RequestBody ModerationActionRequest request, Authentication authentication) {
        Long moderatorId = resolveCurrentUserId(authentication);
        boolean success = announcementService.changeStatus(request.getAdId(), AdStatus.ACTIVE, moderatorId, null).isPresent();
        return new SimpleActionResponse(success, success ? "Approved" : "Approve failed");
    }

    @PostMapping("/reject")
    public SimpleActionResponse reject(@RequestBody ModerationActionRequest request, Authentication authentication) {
        Long moderatorId = resolveCurrentUserId(authentication);
        boolean success = announcementService.changeStatus(request.getAdId(), AdStatus.REJECTED, moderatorId, request.getReason()).isPresent();
        return new SimpleActionResponse(success, success ? "Rejected" : "Reject failed");
    }

    @PostMapping("/delete")
    public SimpleActionResponse delete(@RequestBody ModerationActionRequest request, Authentication authentication) {
        Long moderatorId = resolveCurrentUserId(authentication);
        boolean success = announcementService.changeStatus(request.getAdId(), AdStatus.DELETED, moderatorId, request.getReason()).isPresent();
        return new SimpleActionResponse(success, success ? "Deleted" : "Delete failed");
    }

    @DeleteMapping("/comments/{id}")
    public SimpleActionResponse deleteComment(@PathVariable Long id) {
        boolean success;
        try {
            commentService.deleteComment(id);
            success = true;
        } catch (Exception e) {
            success = false;
        }
        return new SimpleActionResponse(success, success ? "Comment deleted" : "Comment delete failed");
    }

    @GetMapping("/history")
    public ModerationHistoryResponse history() {
        List<ModerationHistory> history = moderationHistoryRepository.findAllByOrderByCreatedAtDesc();
        List<AdminActionAudit> adminActions = adminActionAuditRepository.findAllByOrderByCreatedAtDesc();
        return new ModerationHistoryResponse(history, adminActions);
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        User user = resolveCurrentUser(authentication);
        return user == null ? null : user.getId();
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userService.findUserByEmail(authentication.getName()).orElse(null);
    }
}
