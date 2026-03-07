package com.mipt.portal.moderator;

import com.mipt.portal.user.User;
import com.mipt.portal.user.UserService;
import com.mipt.portal.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с функциями модератора.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UserService userService;

    /**
     * Проверяет, является ли пользователь модератором
     */
    public boolean isUserModerator(Long userId) {
        return userService.isUserModerator(userId);
    }

    /**
     * Назначает роль модератора пользователю (только для администраторов)
     */
    public Optional<Boolean> promoteModerator(Long userId, Long adminUserId) {
        try {
            // Проверяем, что назначающий является модератором или администратором
            if (!userService.isUserModerator(adminUserId)) {
                log.warn("Promote moderator failed - user {} is not a moderator", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.assignModeratorRole(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} promoted to moderator by user {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error promoting user to moderator: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Отзывает роль модератора у пользователя (только для администраторов)
     */
    public Optional<Boolean> demoteModerator(Long userId, Long adminUserId) {
        try {
            // Проверяем, что отзывающий является модератором или администратором
            if (!userService.isUserModerator(adminUserId)) {
                log.warn("Demote moderator failed - user {} is not a moderator", adminUserId);
                return Optional.of(false);
            }

            Optional<Boolean> result = userService.revokeModeratorRole(userId);
            if (result.isPresent() && result.get()) {
                log.info("User {} demoted from moderator by user {}", userId, adminUserId);
                return Optional.of(true);
            }

            return Optional.of(false);

        } catch (Exception e) {
            log.error("Error demoting moderator: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Получает всех модераторов
     */
    public List<User> getAllModerators() {
        return userService.getAllModerators();
    }

    /**
     * Проверяет права доступа для модераторских действий
     */
    public boolean hasPermissionForAction(Long userId, String action) {
        try {
            if (!userService.isUserModerator(userId)) {
                log.warn("Permission denied - user {} is not a moderator", userId);
                return false;
            }

            // TODO: Здесь можно добавить более детальную проверку прав
            // разные типы модераторов с разными правами
            // Пока считаем, что все модераторы имеют одинаковые права

            log.debug("Permission granted for user {} to perform action: {}", userId, action);
            return true;

        } catch (Exception e) {
            log.error("Error checking permissions: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получает информацию о пользователе-модераторе
     */
    public Optional<User> getModeratorInfo(Long userId) {
        try {
            Optional<User> userOpt = userService.findUserById(userId);

            if (userOpt.isPresent() && userOpt.get().isModerator()) {
                return userOpt;
            }

            log.warn("Moderator info not found for user: {}", userId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error getting moderator info: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Проверяет, может ли пользователь модерировать объявления
     */
    public boolean canModerateAds(Long userId) {
        return hasPermissionForAction(userId, "MODERATE_ADS");
    }

    /**
     * Проверяет, может ли пользователь модерировать других пользователей
     */
    public boolean canModerateUsers(Long userId) {
        return hasPermissionForAction(userId, "MODERATE_USERS");
    }

    /**
     * Проверяет, может ли пользователь отправлять модераторские сообщения
     */
    public boolean canSendModerationMessages(Long userId) {
        return hasPermissionForAction(userId, "SEND_MODERATION_MESSAGES");
    }
}