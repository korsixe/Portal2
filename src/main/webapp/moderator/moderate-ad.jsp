<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.Announcement" %>
<%@ page import="com.mipt.portal.enums.AdStatus" %>
<%@ page import="com.mipt.portal.service.AnnouncementService" %>
<%@ page import="com.mipt.portal.service.NotificationService" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Optional" %>
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

            if (("reject".equals(action) || "delete".equals(action))
                && (reason == null || reason.trim().isEmpty())) {
                message = "Укажите причину";
                messageType = "error";
            } else {
                WebApplicationContext appContext =
                    WebApplicationContextUtils.getRequiredWebApplicationContext(application);
                AnnouncementService adsService = appContext.getBean(AnnouncementService.class);
                NotificationService notificationService = appContext.getBean(NotificationService.class);

                AdStatus newStatus;
                switch (action) {
                    case "approve":
                        newStatus = AdStatus.ACTIVE;
                        message = "Объявление одобрено";
                        break;
                    case "reject":
                        newStatus = AdStatus.REJECTED;
                        message = "Объявление отправлено на доработку";
                        break;
                    case "delete":
                        newStatus = AdStatus.DELETED;
                        message = "Объявление удалено";
                        break;
                    default:
                        newStatus = null;
                        message = "Неизвестное действие";
                        messageType = "error";
                }

                if (newStatus != null) {
                    Optional<Announcement> updated = adsService.changeStatus(adId, newStatus);
                    if (updated.isPresent()) {
                        notificationService.createNotification(
                            adId,
                            action,
                            reason,
                            moderatorEmail != null ? moderatorEmail : ""
                        );
                    } else {
                        message = "Объявление не найдено";
                        messageType = "error";
                    }
                }
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