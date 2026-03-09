package com.mipt.portal.admin;

import com.mipt.portal.user.User;
import com.mipt.portal.user.UserService;
import com.mipt.portal.user.Role;
import com.mipt.portal.address.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Контроллер для инициализации системы и создания первого администратора.
 * Используется только для первоначальной настройки системы.
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemInitController {

    private final UserService userService;

    /**
     * Создает первого администратора в системе.
     */
    @PostMapping("/init-admin")
    public ResponseEntity<String> initializeFirstAdmin(@RequestParam String email,
                                                      @RequestParam String name,
                                                      @RequestParam String password,
                                                      @RequestParam String passwordConfirm) {
        try {
            // Проверяем, есть ли уже администраторы в системе
            if (!userService.getAllAdmins().isEmpty()) {
                log.warn("Attempt to initialize admin when admins already exist");
                return ResponseEntity.badRequest().body("В системе уже есть администраторы");
            }

            // Создаем базового пользователя
            Address defaultAddress = new Address(); // создаем пустой адрес
            Optional<User> userOpt = userService.registerUser(
                email, name, password, passwordConfirm,
                defaultAddress, "Администратор", 0
            );

            if (userOpt.isEmpty()) {
                log.error("Failed to create first admin user");
                return ResponseEntity.badRequest().body("Ошибка при создании пользователя-администратора");
            }

            User user = userOpt.get();

            // Назначаем роль администратора
            Optional<Boolean> assignResult = userService.assignAdminRole(user.getId());
            if (assignResult.isEmpty() || !assignResult.get()) {
                log.error("Failed to assign admin role to first admin user");
                return ResponseEntity.internalServerError().body("Ошибка при назначении роли администратора");
            }

            log.info("First admin user created successfully with email: {}", email);
            return ResponseEntity.ok("Первый администратор успешно создан");

        } catch (Exception e) {
            log.error("Error initializing first admin: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Внутренняя ошибка сервера");
        }
    }

    /**
     * Проверяет состояние системы - есть ли администраторы
     */
    @GetMapping("/status")
    public ResponseEntity<String> getSystemStatus() {
        try {
            int adminCount = userService.getAllAdmins().size();
            int moderatorCount = userService.getAllModerators().size();
            int totalUsers = userService.getAllUsers().size();

            String status = String.format(
                "Статус системы: Всего пользователей: %d, Администраторов: %d, Модераторов: %d",
                totalUsers, adminCount, moderatorCount
            );

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting system status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Ошибка получения статуса системы");
        }
    }

    /**
     * Проверяет, нужна ли инициализация системы (нет ли администраторов)
     */
    @GetMapping("/needs-init")
    public ResponseEntity<Boolean> needsInitialization() {
        try {
            boolean needsInit = userService.getAllAdmins().isEmpty();
            return ResponseEntity.ok(needsInit);
        } catch (Exception e) {
            log.error("Error checking if system needs initialization: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
