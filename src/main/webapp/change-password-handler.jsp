<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="com.mipt.portal.users.service.UserService" %>
<%@ page import="com.mipt.portal.users.service.OperationResult" %>
<%
    // Проверяем авторизацию
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String message = "";
    String messageType = "";

    if ("POST".equalsIgnoreCase(request.getMethod()) && "changePassword".equals(request.getParameter("action"))) {
        try {
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            // Валидация
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                message = "❌ Текущий пароль не может быть пустым";
                messageType = "error";
            } else if (newPassword == null || newPassword.trim().isEmpty()) {
                message = "❌ Новый пароль не может быть пустым";
                messageType = "error";
            } else if (!newPassword.equals(confirmPassword)) {
                message = "❌ Пароли не совпадают";
                messageType = "error";
            } else if (newPassword.length() < 8) {
                message = "❌ Пароль должен содержать минимум 8 символов";
                messageType = "error";
            } else {
                // Проверяем текущий пароль
                UserService userService = new UserService();
                OperationResult<User> loginResult = userService.loginUser(user.getEmail(), currentPassword);

                if (loginResult.isSuccess()) {
                    // Обновляем пароль
                    user.setPassword(newPassword);
                    OperationResult<User> updateResult = userService.updateUser(user);

                    if (updateResult.isSuccess()) {
                        // Обновляем пользователя в сессии
                        session.setAttribute("user", updateResult.getData());
                        message = "✅ Пароль успешно изменен!";
                        messageType = "success";
                    } else {
                        message = updateResult.getMessage();
                        messageType = "error";
                    }
                } else {
                    message = "❌ Неверный текущий пароль";
                    messageType = "error";
                }
            }
        } catch (Exception e) {
            message = "❌ Ошибка при изменении пароля: " + e.getMessage();
            messageType = "error";
        }
    }

    // Сохраняем сообщение в сессии для отображения на дашборде
    session.setAttribute("passwordMessage", message);
    session.setAttribute("passwordMessageType", messageType);

    // Перенаправляем обратно на дашборд
    response.sendRedirect("dashboard.jsp");
%>