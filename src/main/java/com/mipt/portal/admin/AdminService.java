package com.mipt.portal.admin;

import com.mipt.portal.users.User;
import com.mipt.portal.users.service.UserService;
import com.mipt.portal.moderator.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с функциями администратора.
 * Администратор имеет полные права на управление пользователями, модераторами и системой.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final ModerationService moderationService;

    /**
     * Проверяет, является ли пользователь администратором
     */
    public boolean isUserAdmin(Long userId) {
        return userService.isUserAdmin(userId);
    }

    /**
     * Получает информацию об администраторе
     */
    public Optional<User> getAdminInfo(Long userId) {
        try {
            Optional<User> userOpt = userService.findUserById(userId);

            if (userOpt.isPresent() && userOpt.get().isAdmin()) {
                return userOpt;
            }

            log.warn("Admin info not found for user: {}", userId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error getting admin info: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // ========== Управление пользователями ==========

    /**
     * Получает всех пользователей
     */
    public List<User> getAllUsers(Long adminUserId) {
        if (!isUserAdmin(adminUserId)) {
            log.warn("Access denied - user {} is not an admin", adminUserId);
            return List.of();
        }

        return userService.getAllUsers();
    }

    /**
     * Получает пользователя по ID
     */
    public Optional<User> getUserById(Long adminUserId, Long userId) {
        if (!isUserAdmin(adminUserId)) {
            log.warn("Access denied - user {} is not an admin", adminUserId);
            return Optional.empty();
        }

        return userService.findUserById(userId);
    }

    /**
     * Удаляет пользователя
     */
    public Optional<Boolean> deleteUser(Long adminUserId, Long userId) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Delete user failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            if (adminUserId.equals(userId)) {
                log.warn("Admin {} attempted to delete themselves", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.deleteUser(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} deleted by admin {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Обновляет рейтинг пользователя
     */
    public Optional<Boolean> updateUserRating(Long adminUserId, Long userId, double newRating) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Update rating failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.updateUserRating(userId, newRating);
            if (result.isPresent() && result.get()) {
                log.info("User {} rating updated to {} by admin {}", userId, newRating, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error updating user rating: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Добавляет монеты пользователю
     */
    public Optional<Boolean> addCoinsToUser(Long adminUserId, Long userId, int coins) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Add coins failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.addCoins(userId, coins);
            if (result.isPresent() && result.get()) {
                log.info("Admin {} added {} coins to user {}", adminUserId, coins, userId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error adding coins to user: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Снимает монеты у пользователя
     */
    public Optional<Boolean> deductCoinsFromUser(Long adminUserId, Long userId, int coins) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Deduct coins failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.deductCoins(userId, coins);
            if (result.isPresent() && result.get()) {
                log.info("Admin {} deducted {} coins from user {}", adminUserId, coins, userId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error deducting coins from user: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // ========== Управление ролями ==========

    /**
     * Назначает роль модератора пользователю
     */
    public Optional<Boolean> promoteToModerator(Long adminUserId, Long userId) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Promote moderator failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.assignModeratorRole(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} promoted to moderator by admin {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error promoting user to moderator: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Отзывает роль модератора у пользователя
     */
    public Optional<Boolean> demoteFromModerator(Long adminUserId, Long userId) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Demote moderator failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.revokeModeratorRole(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} demoted from moderator by admin {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error demoting user from moderator: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Назначает роль администратора пользователю
     */
    public Optional<Boolean> promoteToAdmin(Long adminUserId, Long userId) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Promote admin failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            if (adminUserId.equals(userId)) {
                log.warn("Admin {} attempted to promote themselves", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.assignAdminRole(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} promoted to admin by admin {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error promoting user to admin: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Отзывает роль администратора у пользователя (только для суперадминистраторов)
     */
    public Optional<Boolean> demoteFromAdmin(Long adminUserId, Long userId) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Demote admin failed - user {} is not an admin", adminUserId);
                return Optional.of(false);
            }

            // Проверяем, что не пытаемся отозвать роль у самого себя
            if (adminUserId.equals(userId)) {
                log.warn("Admin {} attempted to demote themselves", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.revokeAdminRole(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} demoted from admin by admin {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error demoting user from admin: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // ========== Просмотр информации о ролях ==========

    /**
     * Получает всех модераторов
     */
    public List<User> getAllModerators(Long adminUserId) {
        if (!isUserAdmin(adminUserId)) {
            log.warn("Access denied - user {} is not an admin", adminUserId);
            return List.of();
        }

        return userService.getAllModerators();
    }

    /**
     * Получает всех администраторов
     */
    public List<User> getAllAdmins(Long adminUserId) {
        if (!isUserAdmin(adminUserId)) {
            log.warn("Access denied - user {} is not an admin", adminUserId);
            return List.of();
        }

        return userService.getAllAdmins();
    }

    // ========== Системные функции ==========


    /**
     * Получает статистику пользователей системы
     */
    public AdminStatistics getSystemStatistics(Long adminUserId) {
        try {
            if (!isUserAdmin(adminUserId)) {
                log.warn("Access denied - user {} is not an admin", adminUserId);
                return new AdminStatistics();
            }

            List<User> allUsers = userService.getAllUsers();
            long totalUsers = allUsers.size();
            long moderators = allUsers.stream().filter(User::isModerator).count();
            long admins = allUsers.stream().filter(User::isAdmin).count();
            long regularUsers = totalUsers - moderators;

            AdminStatistics stats = new AdminStatistics();
            stats.setTotalUsers(totalUsers);
            stats.setModerators(moderators);
            stats.setAdmins(admins);
            stats.setRegularUsers(regularUsers);

            log.info("System statistics requested by admin {}", adminUserId);
            return stats;

        } catch (Exception e) {
            log.error("Error getting system statistics: {}", e.getMessage(), e);
            return new AdminStatistics();
        }
    }

    /**
     * Внутренний класс для статистики
     */
    public static class AdminStatistics {
        private long totalUsers;
        private long regularUsers;
        private long moderators;
        private long admins;

        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getRegularUsers() { return regularUsers; }
        public void setRegularUsers(long regularUsers) { this.regularUsers = regularUsers; }

        public long getModerators() { return moderators; }
        public void setModerators(long moderators) { this.moderators = moderators; }

        public long getAdmins() { return admins; }
        public void setAdmins(long admins) { this.admins = admins; }
    }
}
