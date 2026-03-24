package com.mipt.portal.controller;

import com.mipt.portal.dto.CoinManagementRequest;
import com.mipt.portal.dto.RoleManagementRequest;
import com.mipt.portal.dto.SystemStats;
import com.mipt.portal.entity.User;
import com.mipt.portal.enums.Role;
import com.mipt.portal.service.AdminService;
import com.mipt.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.getAllUsers();
        SystemStats stats = userService.buildSystemStats();

        model.addAttribute("users", users);
        model.addAttribute("stats", stats);
        
        return "/admin/dashboard"; // абсолютное имя вида, чтобы избежать относительного /admin/admin/...
    }

    @PostMapping("/role")
    public String manageRole(@ModelAttribute RoleManagementRequest request,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
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

        redirectAttributes.addFlashAttribute("message", success ? "Роль обновлена" : "Не удалось изменить роль");
        redirectAttributes.addFlashAttribute("messageType", success ? "success" : "error");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/coins")
    public String manageCoins(@ModelAttribute CoinManagementRequest request,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Long adminId = resolveCurrentUserId(authentication);
        boolean success = false;
        String action = request.getAction() == null ? "" : request.getAction().toLowerCase();

        if (action.equals("add")) {
            success = adminService.addCoinsToUser(adminId, request.getTargetUserId(), request.getAmount()).orElse(false);
        } else if (action.equals("deduct")) {
            success = adminService.deductCoinsFromUser(adminId, request.getTargetUserId(), request.getAmount()).orElse(false);
        }

        redirectAttributes.addFlashAttribute("message", success ? "Баланс обновлен" : "Не удалось обновить баланс");
        redirectAttributes.addFlashAttribute("messageType", success ? "success" : "error");
        return "redirect:/admin/dashboard";
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
