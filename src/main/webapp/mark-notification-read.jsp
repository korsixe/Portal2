<%@ page contentType="text/plain" %>
<%@ page import="com.mipt.portal.notification.NotificationService" %>
<%
System.out.println("📧 ===== ПОМЕТКА УВЕДОМЛЕНИЯ ПРОЧИТАННЫМ =====");

try {
    String notificationIdParam = request.getParameter("notificationId");
    System.out.println("📧 Получен notificationId для пометки прочитанным: " + notificationIdParam);

    // Логируем все параметры
    java.util.Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String paramName = paramNames.nextElement();
        String paramValue = request.getParameter(paramName);
        System.out.println("📧 Параметр " + paramName + ": " + paramValue);
    }

    if (notificationIdParam != null && !notificationIdParam.isEmpty()) {
        Long notificationId = Long.parseLong(notificationIdParam);
        System.out.println("📧 Помечаем уведомление ID как прочитанное: " + notificationId);

        NotificationService service = new NotificationService();
        boolean success = service.markAsRead(notificationId);

        System.out.println("📧 Результат пометки прочитанным: " + success);

        if (success) {
            out.print("SUCCESS");
            System.out.println("✅ Уведомление " + notificationId + " помечено как прочитанное в БД");
        } else {
            out.print("ERROR");
            System.out.println("❌ Ошибка пометки уведомления " + notificationId + " как прочитанного");
        }
    } else {
        out.print("MISSING_ID");
        System.out.println("❌ Отсутствует notificationId параметр для пометки прочитанным");
    }
} catch (Exception e) {
    out.print("EXCEPTION: " + e.getMessage());
    System.err.println("❌ Исключение при пометке прочитанным: " + e.getMessage());
    e.printStackTrace();
}

System.out.println("📧 ===== ПОМЕТКА ПРОЧИТАННЫМ ЗАВЕРШЕНА =====");
%>