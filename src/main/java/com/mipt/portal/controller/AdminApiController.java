package com.mipt.portal.controller;

import com.mipt.portal.dto.AdminDashboardResponse;
import com.mipt.portal.dto.CoinManagementRequest;
import com.mipt.portal.dto.RoleManagementRequest;
import com.mipt.portal.dto.SanctionRequest;
import com.mipt.portal.dto.SimpleActionResponse;
import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.AdminActionAudit;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Role;
import com.mipt.portal.repository.AdminActionAuditRepository;
import com.mipt.portal.service.AdminService;
import com.mipt.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final UserService userService;
    private final AdminService adminService;
    private final AdminActionAuditRepository adminActionAuditRepository;

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        List<User> users = userService.getAllUsers();
        SystemStats stats = userService.buildSystemStats();
        return new AdminDashboardResponse(users, stats);
    }

    @GetMapping("/actions")
    public List<AdminActionAudit> actions() {
        return adminActionAuditRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/role")
    public SimpleActionResponse manageRole(@RequestBody RoleManagementRequest request, Authentication authentication) {
        Long adminId = resolveCurrentUserId(authentication);
        boolean success = false;
        String action = request.getAction() == null ? "" : request.getAction().toLowerCase();
        String role = request.getRole() == null ? "" : request.getRole().toUpperCase();

        if (Role.MODERATOR.name().equals(role)) {
            success = action.equals("assign")
                ? adminService.promoteToModerator(adminId, request.getTargetUserId()).orElse(false)
                : adminService.demoteFromModerator(adminId, request.getTargetUserId()).orElse(false);
        } else if (Role.ADMIN.name().equals(role)) {
            success = action.equals("assign")
                ? adminService.promoteToAdmin(adminId, request.getTargetUserId()).orElse(false)
                : adminService.demoteFromAdmin(adminId, request.getTargetUserId()).orElse(false);
        }

        return new SimpleActionResponse(success, success ? "Role updated" : "Role update failed");
    }

    @PostMapping("/coins")
    public SimpleActionResponse manageCoins(@RequestBody CoinManagementRequest request, Authentication authentication) {
        Long adminId = resolveCurrentUserId(authentication);
        boolean success = false;
        String action = request.getAction() == null ? "" : request.getAction().toLowerCase();

        if (action.equals("add")) {
            success = adminService.addCoinsToUser(adminId, request.getTargetUserId(), request.getAmount()).orElse(false);
        } else if (action.equals("deduct")) {
            success = adminService.deductCoinsFromUser(adminId, request.getTargetUserId(), request.getAmount()).orElse(false);
        }

        return new SimpleActionResponse(success, success ? "Coins updated" : "Coins update failed");
    }

    @PostMapping("/sanction")
    public SimpleActionResponse manageSanction(@RequestBody SanctionRequest request, Authentication authentication) {
        Long adminId = resolveCurrentUserId(authentication);
        boolean success = false;
        String type = request.getType() == null ? "" : request.getType().toLowerCase();
        int duration = request.getDuration() == null ? 0 : request.getDuration();

        if ("freeze".equals(type)) {
            success = adminService.freezeUser(adminId, request.getTargetUserId(), request.getReason(), duration).orElse(false);
        } else if ("ban".equals(type)) {
            success = adminService.banUser(adminId, request.getTargetUserId(), request.getReason(), duration).orElse(false);
        } else if ("lift".equals(type)) {
            success = adminService.liftSanctions(adminId, request.getTargetUserId()).orElse(false);
        }

        return new SimpleActionResponse(success, success ? "Sanction updated" : "Sanction update failed");
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

