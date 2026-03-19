<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.notifications.NotificationService" %>
<%@ page import="com.mipt.portal.moderator.message.ModerationMessage" %>
<%@ page import="com.mipt.portal.announcement.AdsService" %>
<%@ page import="com.mipt.portal.announcement.Announcement" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="java.util.ArrayList" %>
<%
    User user = (User) session.getAttribute("user");
    List<ModerationMessage> userNotifications = new ArrayList<>();
    int unreadCount = 0;

    if (user != null) {
        try {
            NotificationService notificationService = new NotificationService();
            userNotifications = notificationService.getUserNotifications(user.getId());
            unreadCount = notificationService.getUnreadCount(user.getId());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки уведомлений: " + e.getMessage());
        }
    }
%>

<div class="notification-container">
    <button class="notification-bell" onclick="toggleNotifications()">
        🔔
        <% if (unreadCount > 0) { %>
        <span class="notification-badge" id="notificationBadge"><%= unreadCount %></span>
        <% } %>
    </button>

    <div class="notification-dropdown" id="notificationDropdown">
        <div class="notification-header">
            <h4>Уведомления</h4>
            <% if (unreadCount > 0) { %>
            <button onclick="markAllAsRead()" class="btn-mark-all-read" style="padding: 5px 10px; font-size: 0.8rem;">
                Прочитать все
            </button>
            <% } %>
        </div>
        <div class="notification-list" id="notificationList">
            <% if (userNotifications.isEmpty()) { %>
            <div class="no-notifications">
                <div class="icon">🔔</div>
                <p>Нет уведомлений</p>
            </div>
            <% } else { %>
            <% for (ModerationMessage notification : userNotifications) {
                AdsService adService = new AdsService();
                Announcement ad = adService.getAd(notification.getAdId());
                String adTitle = (ad != null) ? ad.getTitle() : "Объявление";
                String notificationClass = Boolean.TRUE.equals(notification.getIsRead()) ? "notification-item read" : "notification-item unread";

                // Определяем тип уведомления
                boolean isApprovalNotification = "approve".equals(notification.getAction()) &&
                        (notification.getReason() == null || notification.getReason().isEmpty());
            %>
            <div class="<%= notificationClass %>"
                 data-notification-id="<%= notification.getId() %>"
                 data-ad-id="<%= notification.getAdId() %>">
                <div class="notification-icon">
                    <%= getActionIcon(notification.getAction(), isApprovalNotification) %>
                </div>
                <div class="notification-content">
                    <div class="notification-title">
                        <%= getNotificationTitle(notification.getAction(), isApprovalNotification) %>
                        <% if (!Boolean.TRUE.equals(notification.getIsRead())) { %>
                        <span class="unread-dot">●</span>
                        <% } %>
                    </div>
                    <div class="notification-message">
                        <%= getNotificationMessage(notification.getAction(), adTitle, isApprovalNotification) %>
                    </div>
                    <% if (notification.getReason() != null && !notification.getReason().isEmpty()) { %>
                    <div class="notification-reason">Причина: <%= notification.getReason() %></div>
                    <% } %>
                    <% if (isApprovalNotification) { %>
                    <% } %>
                    <div class="notification-time">

                        <%= formatNotificationDate(notification.getCreatedAt()) %>
                    </div>
                </div>
                <div class="notification-actions">
                    <button class="btn-delete"
                            data-notification-id="<%= notification.getId() %>"
                            title="Удалить уведомление">×</button>
                </div>
            </div>
            <% } %>
            <% } %>
        </div>
    </div>
</div>

<script>
    (function() {
        'use strict';

        document.addEventListener('click', function(event) {
            const target = event.target;

            if (target.closest('.notification-bell')) {
                toggleNotifications();
                event.stopPropagation();
                return;
            }

            // КНОПКА УДАЛЕНИЯ
            if (target.closest('.btn-delete')) {
                const btn = target.closest('.btn-delete');
                const notificationItem = btn.closest('.notification-item');

                console.log('🔍 Найдена кнопка удаления');
                console.log('🔍 notificationItem:', notificationItem);

                if (notificationItem) {
                    const notificationId = notificationItem.getAttribute('data-notification-id');
                    console.log('🔍 ID уведомления:', notificationId);
                    console.log('🔍 Все атрибуты:', notificationItem.attributes);
                }

                if (notificationItem) {
                    const notificationId = notificationItem.getAttribute('data-notification-id');
                    deleteNotification(notificationId);
                }
                event.stopPropagation();
                return;
            }

            // КЛИК ПО УВЕДОМЛЕНИЮ (но не по кнопке удаления)
            const notificationItem = target.closest('.notification-item');
            if (notificationItem && !target.closest('.btn-delete')) {
                const notificationId = notificationItem.getAttribute('data-notification-id');
                const adId = notificationItem.getAttribute('data-ad-id');

                if (notificationId && adId) {
                    handleNotificationClick(notificationId, adId, notificationItem);
                }
                event.preventDefault();
                event.stopPropagation();
                return;
            }

            // КНОПКА "ПРОЧИТАТЬ ВСЕ"
            if (target.closest('.btn-mark-all-read')) {
                markAllAsRead();
                event.stopPropagation();
                return;
            }

            // ЗАКРЫТИЕ ПРИ КЛИКЕ ВНЕ
            if (!target.closest('.notification-container')) {
                const dropdown = document.getElementById('notificationDropdown');
                if (dropdown && dropdown.style.display === 'block') {
                    dropdown.style.display = 'none';
                }
            }
        });

        function toggleNotifications() {
            const dropdown = document.getElementById('notificationDropdown');
            if (dropdown) {
                dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
            }
        }

        function handleNotificationClick(notificationId, adId, element) {
            // Проверяем, было ли уведомление уже прочитано
            const wasRead = element.classList.contains('read');

            // Если уведомление НЕ прочитано - помечаем его визуально и обновляем счетчик
            if (!wasRead) {
                element.classList.remove('unread');
                element.classList.add('read');
                const unreadDot = element.querySelector('.unread-dot');
                if (unreadDot) unreadDot.remove();
            }

            // Ждем завершения запроса перед переходом
            fetch('mark-notification-read.jsp', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'notificationId=' + notificationId
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('HTTP error: ' + response.status);
                    }
                    return response.text();
                })
                .then(text => {
                    console.log('✅ Сервер подтвердил пометку прочитанным:', text);

                    // Обновляем счетчик ТОЛЬКО если уведомление было непрочитанным
                    if (!wasRead) {
                        updateUnreadCount();
                    }

                    // Переходим только после успешной пометки
                    window.location.href = 'ad-details.jsp?id=' + adId;
                })
                .catch(error => {
                    console.error('❌ Ошибка при пометке прочитанным:', error);
                    // Все равно переходим, но с ошибкой
                    window.location.href = 'ad-details.jsp?id=' + adId;
                });
        }

        function updateUnreadCount() {
            const badge = document.getElementById('notificationBadge');
            if (badge) {
                const currentCount = parseInt(badge.textContent) || 0;
                const newCount = Math.max(0, currentCount - 1);

                if (newCount > 0) {
                    badge.textContent = newCount;
                } else {
                    badge.remove();
                    const markAllButton = document.querySelector('.btn-mark-all-read');
                    if (markAllButton) markAllButton.style.display = 'none';
                }
            }
        }

        function deleteNotification(notificationId) {
            console.log('🗑️ Начинаем удаление уведомления:', notificationId);

            // ИСПРАВЛЕННЫЙ СЕЛЕКТОР - используем правильный синтаксис
            const notificationElement = document.querySelector('[data-notification-id="' + notificationId + '"]');

            if (notificationElement) {
                console.log('🗑️ Элемент уведомления найден в DOM');

                // Проверяем, было ли уведомление непрочитанным
                const wasUnread = notificationElement.classList.contains('unread');

                // Удаляем элемент из DOM
                notificationElement.remove();
                console.log('🗑️ Элемент удален из DOM');

                // Обновляем счетчик только если удаляем непрочитанное
                if (wasUnread) {
                    console.log('🗑️ Удаляем непрочитанное уведомление, обновляем счетчик');
                    updateUnreadCount();
                }

                // Проверяем, остались ли уведомления
                const remainingNotifications = document.querySelectorAll('.notification-item');
                if (remainingNotifications.length === 0) {
                    console.log('🗑️ Уведомлений не осталось, показываем пустой список');
                    const notificationList = document.getElementById('notificationList');
                    if (notificationList) {
                        notificationList.innerHTML = `
                    <div class="no-notifications">
                        <div class="icon">🔔</div>
                        <p>Нет уведомлений</p>
                    </div>
                `;
                    }
                }

                // Отправляем запрос на удаление на сервер
                console.log('🗑️ Отправляем запрос на сервер для удаления:', notificationId);
                fetch('delete-notification.jsp', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'notificationId=' + notificationId
                })
                    .then(response => {
                        console.log('🗑️ Ответ от сервера получен, статус:', response.status);
                        if (!response.ok) {
                            throw new Error('HTTP error: ' + response.status);
                        }
                        return response.text();
                    })
                    .then(text => {
                        console.log('✅ Сервер подтвердил удаление:', text);
                        if (text.trim() !== 'SUCCESS') {
                            throw new Error('Server returned: ' + text);
                        }
                    })
                    .catch(error => {
                        console.error('❌ Ошибка при удалении уведомления:', error);
                        // Восстанавливаем элемент в случае ошибки
                        alert('Ошибка при удалении уведомления. Пожалуйста, обновите страницу.');
                        location.reload();
                    });
            } else {
                console.error('❌ Элемент уведомления не найден в DOM для ID:', notificationId);
                console.log('🔍 Все элементы с data-notification-id:');
                document.querySelectorAll('[data-notification-id]').forEach(el => {
                    console.log(' - ', el.getAttribute('data-notification-id'));
                });
            }
        }

        function markAllAsRead() {
            // Помечаем все визуально как прочитанные
            document.querySelectorAll('.notification-item.unread').forEach(item => {
                item.classList.remove('unread');
                item.classList.add('read');
                const dot = item.querySelector('.unread-dot');
                if (dot) dot.remove();
            });

            // Удаляем бейдж и кнопку
            const badge = document.getElementById('notificationBadge');
            if (badge) badge.remove();

            const markAllBtn = document.querySelector('.btn-mark-all-read');
            if (markAllBtn) markAllBtn.style.display = 'none';

            // Отправляем запрос на сервер для пометки всех как прочитанных
            fetch('mark-all-notifications-read.jsp', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'userId=<%= user != null ? user.getId() : "" %>'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('HTTP error: ' + response.status);
                    }
                    return response.text();
                })
                .then(text => {
                    console.log('✅ Все уведомления помечены как прочитанные:', text);
                })
                .catch(error => {
                    console.error('❌ Ошибка при пометке всех уведомлений:', error);
                    // В случае ошибки перезагружаем страницу для синхронизации
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                });
        }
    })();
</script>

<style>
    .notification-container {
        position: relative;
        display: inline-block;
    }

    .notification-bell {
        position: relative;
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        padding: 5px;
        transition: transform 0.2s;
    }

    .notification-bell:hover {
        transform: scale(1.1);
    }

    .notification-badge {
        position: absolute;
        top: -5px;
        right: -5px;
        background: #ff4444;
        color: white;
        border-radius: 50%;
        padding: 2px 6px;
        font-size: 0.7rem;
        min-width: 18px;
        text-align: center;
        font-weight: bold;
    }

    .notification-dropdown {
        display: none;
        position: absolute;
        right: 0;
        top: 100%;
        width: 400px;
        background: white;
        border: 1px solid #ddd;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        z-index: 1000;
        max-height: 500px;
        overflow-y: auto;
    }

    .notification-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px;
        border-bottom: 1px solid #eee;
        background: #f8f9fa;
        position: sticky;
        top: 0;
        z-index: 1;
    }

    .notification-header h4 {
        margin: 0;
        color: #333;
    }

    .btn-mark-all-read {
        background: #6c757d;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .btn-mark-all-read:hover {
        background: #5a6268;
    }

    .notification-list {
        max-height: 400px;
        overflow-y: auto;
    }

    .notification-item {
        display: flex;
        padding: 12px 15px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background-color 0.2s;
        position: relative;
    }

    .notification-item:hover {
        background-color: #f8f9fa;
    }

    .notification-item.unread {
        background-color: #f0f7ff;
        border-left: 3px solid #007bff;
    }

    .notification-item.read {
        opacity: 0.8;
        background-color: #fafafa;
    }

    .notification-icon {
        font-size: 1.2rem;
        margin-right: 12px;
        margin-top: 2px;
        flex-shrink: 0;
    }

    .notification-content {
        flex: 1;
        min-width: 0;
    }

    .notification-title {
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .unread-dot {
        color: #007bff;
        font-size: 1.2rem;
    }

    .notification-message {
        color: #666;
        margin-bottom: 4px;
        font-size: 0.9rem;
        word-wrap: break-word;
    }

    .notification-reason {
        color: #888;
        font-size: 0.85rem;
        font-style: italic;
        margin-bottom: 4px;
    }

    .notification-approval-info {
        color: #28a745;
        font-size: 0.85rem;
        font-weight: 500;
        margin-bottom: 4px;
        background: #f8fff9;
        padding: 4px 8px;
        border-radius: 4px;
        border-left: 3px solid #28a745;
    }

    .notification-time {
        color: #999;
        font-size: 0.8rem;
    }

    .notification-actions {
        margin-left: 10px;
        flex-shrink: 0;
    }

    .btn-delete {
        background: none;
        border: none;
        color: #999;
        cursor: pointer;
        font-size: 1.2rem;
        padding: 0 5px;
        border-radius: 3px;
        transition: all 0.2s;
    }

    .btn-delete:hover {
        color: #ff4444;
        background: #f8f9fa;
    }

    .no-notifications {
        text-align: center;
        padding: 30px 20px;
        color: #666;
    }

    .no-notifications .icon {
        font-size: 2rem;
        margin-bottom: 10px;
        opacity: 0.5;
    }
</style>

<%!
    private String getActionIcon(String action, boolean isApprovalNotification) {
        if (isApprovalNotification) {
            return "✅"; // Специальная иконка для одобрения
        }
        switch (action) {
            case "approve": return "✅";
            case "reject": return "⚠️";
            case "delete": return "❌";
            default: return "🔔";
        }
    }

    private String getNotificationTitle(String action, boolean isApprovalNotification) {
        if (isApprovalNotification) {
            return "Объявление одобрено!";
        }
        switch (action) {
            case "approve": return "Объявление одобрено";
            case "reject": return "Требуется доработка";
            case "delete": return "Объявление отклонено";
            default: return "Обновление статуса объявления";
        }
    }

    private String getNotificationMessage(String action, String adTitle, boolean isApprovalNotification) {
        if (isApprovalNotification) {
            return "Ваше объявление \"" + adTitle + "\" прошло модерацию и опубликовано";
        }
        switch (action) {
            case "approve": return "Ваше объявление \"" + adTitle + "\" было одобрено модератором";
            case "reject": return "Ваше объявление \"" + adTitle + "\" требует доработки";
            case "delete": return "Ваше объявление \"" + adTitle + "\" было отклонено модератором";
            default: return "Статус вашего объявления \"" + adTitle + "\" был изменен";
        }
    }

    private String formatNotificationDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        if (duration.toMinutes() < 1) {
            return "только что";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + " мин. назад";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + " ч. назад";
        } else {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return dateTime.format(formatter);
        }
    }
%>