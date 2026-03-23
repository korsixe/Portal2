<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.announcement.AnnouncementService" %>
<%@ page import="com.mipt.portal.announcement.Announcement" %>
<%@ page import="com.mipt.portal.announcement.AdvertisementStatus" %>
<%@ page import="com.mipt.portal.service.ModerationMessageService" %>
<%
    // Проверка авторизации модератора
    if (session.getAttribute("moderator") == null) {
        response.sendRedirect("login-moderator.jsp");
        return;
    }

    String action = request.getParameter("action");
    String adIdParam = request.getParameter("adId");
    String reason = request.getParameter("reason");
    String moderatorEmail = (String) session.getAttribute("moderatorEmail");
    String message = "Действие выполнено";
    String messageType = "success";

    if (action != null && adIdParam != null) {
        try {
            Long adId = Long.parseLong(adIdParam);
            AnnouncementService adsService = new AnnouncementService();
            Announcement ad = adsService.getAd(adId);

            if (ad != null) {
                if ("approve".equals(action) && (reason == null || reason.trim().isEmpty())) {
                    // Создаем уведомление об одобрении без причины
                    Long messageId = ModerationMessageService.createApprovalNotification(adId, moderatorEmail);
                    ad.setMessageId(messageId);
                    message = "Объявление одобрено";
                } else {
                    // Для остальных действий используем обычную логику
                    Long messageId = ModerationMessageService.logModerationAction(
                            adId,
                            action,
                            reason,
                            moderatorEmail
                    );
                    ad.setMessageId(messageId);

                    switch (action) {
                        case "reject":
                            message = "Объявление отправлено на доработку";
                            break;
                        case "delete":
                            message = "Объявление удалено";
                            break;
                    }
                }

                // Обновляем статус объявления
                switch (action) {
                    case "approve":
                        ad.setStatus(AdvertisementStatus.ACTIVE);
                        break;
                    case "reject":
                        ad.setStatus(AdvertisementStatus.DRAFT);
                        break;
                    case "delete":
                        ad.setStatus(AdvertisementStatus.DELETED);
                        break;
                }

                adsService.editAd(ad);
            } else {
                message = "Объявление не найдено";
                messageType = "error";
            }
        } catch (Exception e) {
            message = "Произошла ошибка: " + e.getMessage();
            messageType = "error";
        }
    } else {
        message = "Неверные параметры";
        messageType = "error";
    }

    response.sendRedirect("moderation-bord.jsp?message=" +
            java.net.URLEncoder.encode(message, "UTF-8") +
            "&messageType=" + messageType);
%>