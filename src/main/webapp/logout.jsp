<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Завершаем сессию пользователя
    if (session != null) {
        session.invalidate();
    }

    // Перенаправляем на страницу успешного выхода
    request.getRequestDispatcher("/logout-success.jsp").forward(request, response);
%>