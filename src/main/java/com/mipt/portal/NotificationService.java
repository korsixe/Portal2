package com.mipt.portal;

//import com.mipt.portal.announcement.AdsRepository;
//import com.mipt.portal.moderator.message.ModerationMessageRepository;
//import com.mipt.portal.announcement.AdsService;
//import com.mipt.portal.announcement.Announcement;
//import com.mipt.portal.users.User;
//import com.mipt.portal.users.service.UserService;
import com.mipt.portal.moderator.message.ModerationMessage;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
  // Получаем уведомления пользователя с информацией о прочтении
  public List<ModerationMessage> getUserNotifications(Long userId) {
        /*
        List<ModerationMessage> notifications = new ArrayList<>();

        try {
            UserService userService = new UserService();
            List<Long> adIds = userService.findUserById(userId).getData().getAdList();
            AdsRepository adsRepository = new AdsRepository();
            List<Announcement> userAds = new ArrayList<>();

            for (int i = 0; i < adIds.size(); i++) {
              Announcement ad = adsRepository.getAdById(adIds.get(i));
              userAds.add(ad);
            }

            if (userAds.isEmpty()) {
                return notifications;
            }

            ModerationMessageRepository repository = new ModerationMessageRepository();

            for (Announcement ad : userAds) {
                List<ModerationMessage> moderationMessages = repository.getMessagesByAdId(ad.getId());
                notifications.addAll(moderationMessages);
            }
            repository.close();

        } catch (Exception e) {
            System.err.println("❌ Ошибка при загрузке уведомлений: " + e.getMessage());
            e.printStackTrace();
        }

        return notifications;
        */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    return new ArrayList<>();
  }

  // Получаем количество непрочитанных уведомлений
  public int getUnreadCount(Long userId) {
        /*
        try {
            ModerationMessageRepository repository = new ModerationMessageRepository();
            int count = repository.getUnreadCountForUser(userId);
            repository.close();
            return count;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении количества непрочитанных уведомлений: " + e.getMessage());
            return 0;
        }
        */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    return 0;
  }

  // Пометить уведомление как прочитанное
  public boolean markAsRead(Long notificationId) {
        /*
        try {
            System.out.println("🔔 Попытка пометить уведомление " + notificationId + " как прочитанное");
            ModerationMessageRepository repository = new ModerationMessageRepository();
            boolean success = repository.markAsRead(notificationId);
            repository.close();
            System.out.println("🔔 Результат пометки уведомления " + notificationId + ": " + success);
            return success;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отметке уведомления как прочитанного: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    return true;
  }

  // Пометить все уведомления как прочитанные
  public boolean markAllAsRead(Long userId) {
        /*
        try {
            ModerationMessageRepository repository = new ModerationMessageRepository();
            boolean success = repository.markAllAsReadForUser(userId);
            repository.close();
            return success;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при отметке всех уведомлений как прочитанных: " + e.getMessage());
            return false;
        }
        */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    return true;
  }

  // Удалить уведомление
  public boolean deleteNotification(Long notificationId) {
        /*
        try {
            ModerationMessageRepository repository = new ModerationMessageRepository();
            boolean success = repository.deleteNotification(notificationId);
            repository.close();
            return success;
        } catch (Exception e) {
            System.err.println("❌ Ошибка при удалении уведомления: " + e.getMessage());
            return false;
        }
        */

    // ВРЕМЕННАЯ ЗАГЛУШКА
    return true;
  }

  public String getActionText(String action) {
    switch (action) {
      case "approve": return "одобрено";
      case "reject": return "отправлено на доработку";
      case "delete": return "удалено";
      default: return "обработано";
    }
  }

  public String getActionIcon(String action) {
    switch (action) {
      case "approve": return "✅";
      case "reject": return "⚠️";
      case "delete": return "❌";
      default: return "🔔";
    }
  }

  public String getNotificationTitle(String action, String adTitle) {
    switch (action) {
      case "approve": return "Объявление одобрено";
      case "reject": return "Требуется доработка";
      case "delete": return "Объявление отклонено";
      default: return "Обновление статуса объявления";
    }
  }

  public String getNotificationMessage(String action, String adTitle) {
    switch (action) {
      case "approve": return "Ваше объявление \"" + adTitle + "\" было одобрено модератором";
      case "reject": return "Ваше объявление \"" + adTitle + "\" требует доработки";
      case "delete": return "Ваше объявление \"" + adTitle + "\" было отклонено модератором";
      default: return "Статус вашего объявления \"" + adTitle + "\" был изменен";
    }
  }

  public String getNotificationDate(LocalDateTime dateTime) {
    if (dateTime == null) return "";

    java.time.Duration duration = java.time.Duration.between(dateTime, LocalDateTime.now());

    if (duration.toMinutes() < 1) {
      return "только что";
    } else if (duration.toHours() < 1) {
      return duration.toMinutes() + " мин. назад";
    } else if (duration.toDays() < 1) {
      return duration.toHours() + " ч. назад";
    } else {
      java.time.format.DateTimeFormatter formatter =
        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
      return dateTime.format(formatter);
    }
  }
}