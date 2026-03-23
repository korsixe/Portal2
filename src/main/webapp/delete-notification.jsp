<%@ page contentType="text/plain" %>
<%@ page import="com.mipt.portal.service.NotificationService" %>
<%
try {
    String notificationIdParam = request.getParameter("notificationId");
    System.out.println("🗑️ Получен notificationId для удаления: " + notificationIdParam);

    if (notificationIdParam != null && !notificationIdParam.isEmpty()) {
        Long notificationId = Long.parseLong(notificationIdParam);
        System.out.println("🗑️ Удаляем уведомление ID: " + notificationId);

        NotificationService service = new NotificationService();
        boolean success = service.deleteNotification(notificationId);

        System.out.println("🗑️ Результат удаления из БД: " + success);

        if (success) {
            out.print("SUCCESS");
            System.out.println("✅ Уведомление " + notificationId + " удалено из БД");
        } else {
            out.print("ERROR");
            System.out.println("❌ Ошибка удаления уведомления " + notificationId + " из БД");
        }
    } else {
        out.print("MISSING_ID");
        System.out.println("❌ Отсутствует notificationId параметр для удаления");
    }
} catch (Exception e) {
    out.print("EXCEPTION: " + e.getMessage());
    System.err.println("❌ Исключение при удалении: " + e.getMessage());
    e.printStackTrace();
}

%>