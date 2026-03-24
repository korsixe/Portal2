package com.mipt.portal.service;

import com.mipt.portal.entity.ModerationMessage;
import com.mipt.portal.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Сервис для работы с сообщениями модерации
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationMessageService {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ModerationService moderationService;
    private final UserService userService;

    /**
     * Логирует действие модератора
     */
    public Optional<Long> logModerationAction(Long adId, String action, String reason, Long moderatorUserId) {
        try {
            if (!moderationService.isUserModerator(moderatorUserId)) {
                log.warn("Log moderation action failed - user {} is not a moderator", moderatorUserId);
                return Optional.empty();
            }

            Optional<User> moderatorOpt = userService.findUserById(moderatorUserId);
            if (moderatorOpt.isEmpty()) {
                log.warn("Log moderation action failed - moderator user not found: {}", moderatorUserId);
                return Optional.empty();
            }

            User moderator = moderatorOpt.get();

            log.info("📝 Объявление с Id {} обновлён статус: {} {}. Модератор: {} ({})",
                    adId,
                    action,
                    (reason != null && !reason.trim().isEmpty() ? " по причине: " + reason : " (без указания причины)"),
                    moderator.getName(),
                    moderator.getEmail());

            // Пока возвращаем заглушку
            Long messageId = System.currentTimeMillis(); // Временный ID
            log.info("✅ Сообщение модератора успешно обработано (ID: {})", messageId);

            return Optional.of(messageId);

        } catch (Exception e) {
            log.error("Error logging moderation action: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Проверяет права модератора на отправку сообщений
     */
    public boolean canSendModerationMessage(Long userId) {
        return moderationService.canSendModerationMessages(userId);
    }

    /**
     * Создает сообщение модерации (заглушка для будущей реализации)
     */
    public Optional<ModerationMessage> createModerationMessage(Long adId, Long moderatorUserId, String action, String reason) {
        try {
            if (!moderationService.isUserModerator(moderatorUserId)) {
                log.warn("Create moderation message failed - user {} is not a moderator", moderatorUserId);
                return Optional.empty();
            }

            Optional<User> moderatorOpt = userService.findUserById(moderatorUserId);
            if (moderatorOpt.isEmpty()) {
                return Optional.empty();
            }

            User moderator = moderatorOpt.get();

            // Создание объекта сообщения
            ModerationMessage message = new ModerationMessage();
            message.setId(System.currentTimeMillis()); // Временный ID
            message.setAdId(adId);
            message.setModeratorEmail(moderator.getEmail());
            message.setAction(action);
            message.setReason(reason != null ? reason : "");
            message.setCreatedAt(LocalDateTime.now());
            message.setIsRead(false);

            log.info("Moderation message created: {}", message);
            return Optional.of(message);

        } catch (Exception e) {
            log.error("Error creating moderation message: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
