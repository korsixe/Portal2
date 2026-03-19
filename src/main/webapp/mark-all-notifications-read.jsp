<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.notifications.NotificationService" %>
<%
    try {
        String userIdStr = request.getParameter("userId");
        if (userIdStr != null && !userIdStr.trim().isEmpty()) {
            Long userId = Long.parseLong(userIdStr);
            NotificationService notificationService = new NotificationService();
            boolean success = notificationService.markAllAsRead(userId);

            if (success) {
                out.print("SUCCESS");
            } else {
                response.setStatus(500);
                out.print("ERROR: Failed to mark all as read");
            }
        } else {
            response.setStatus(400);
            out.print("ERROR: User ID is required");
        }
    } catch (Exception e) {
        response.setStatus(500);
        out.print("ERROR: " + e.getMessage());
    }
%>