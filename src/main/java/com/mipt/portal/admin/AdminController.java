package com.mipt.portal.admin;

import com.mipt.portal.users.User;
import com.mipt.portal.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.getAllUsers();
        long adminCount = users.stream().filter(User::isAdmin).count();
        long moderatorCount = users.stream().filter(User::isModerator).count();
        
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("moderatorCount", moderatorCount);
        
        return "admin/dashboard";
    }
}

