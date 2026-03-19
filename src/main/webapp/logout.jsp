<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Завершаем сессию пользователя
    if (session != null) {
        session.invalidate();
    }

    // Перенаправляем на главную страницу с сообщением
    response.sendRedirect("home.jsp?message=Вы успешно вышли из аккаунта");
%>