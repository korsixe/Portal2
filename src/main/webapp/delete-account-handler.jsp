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

    if ("POST".equalsIgnoreCase(request.getMethod()) && "deleteAccount".equals(request.getParameter("action"))) {
        try {
            String confirmPassword = request.getParameter("confirmPassword");

            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                message = "❌ Введите пароль для подтверждения";
                messageType = "error";
            } else {
                // Проверяем пароль
                UserService userService = new UserService();
                OperationResult<User> loginResult = userService.loginUser(user.getEmail(), confirmPassword);

                if (loginResult.isSuccess()) {
                    // Удаляем аккаунт
                    OperationResult<Boolean> deleteResult = userService.deleteUser(user.getId());

                    if (deleteResult.isSuccess()) {
                        // Завершаем сессию и перенаправляем на главную
                        session.invalidate();
                        response.sendRedirect("index.jsp?message=Аккаунт успешно удален");
                        return;
                    } else {
                        message = deleteResult.getMessage();
                        messageType = "error";
                    }
                } else {
                    message = "❌ Неверный пароль";
                    messageType = "error";
                }
            }
        } catch (Exception e) {
            message = "❌ Ошибка при удалении аккаунта: " + e.getMessage();
            messageType = "error";
        }
    }

    // Сохраняем сообщение в сессии для отображения на дашборде
    session.setAttribute("deleteMessage", message);
    session.setAttribute("deleteMessageType", messageType);

    // Перенаправляем обратно на дашборд
    response.sendRedirect("dashboard.jsp");
%>