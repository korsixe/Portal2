<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.User" %>
<%@ page import="com.mipt.portal.service.UserService" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Optional" %>
<%
    Object sessionUserObj = session.getAttribute("user");
    User user = sessionUserObj instanceof User ? (User) sessionUserObj : null;
    if (sessionUserObj != null && user == null) {
        session.invalidate();
        response.sendRedirect("login.jsp");
        return;
    }
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
                WebApplicationContext appContext =
                    WebApplicationContextUtils.getRequiredWebApplicationContext(application);
                UserService userService = appContext.getBean(UserService.class);
                Optional<User> loginResult = userService.loginUser(user.getEmail(), confirmPassword);

                if (loginResult.isPresent()) {
                    Optional<Boolean> deleteResult = userService.deleteUser(user.getId());

                    if (deleteResult.isPresent() && deleteResult.get()) {
                        // Завершаем сессию и перенаправляем на главную
                        session.invalidate();
                        response.sendRedirect("index.jsp?message=Аккаунт успешно удален");
                        return;
                    } else {
                        message = "❌ Ошибка при удалении аккаунта";
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