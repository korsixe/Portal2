<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.mipt.portal.users.service.UserService" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="java.sql.SQLException" %>
<%
    // Получаем данные из формы
    String email = request.getParameter("email");
    String name = request.getParameter("name");
    String password = request.getParameter("password");
    String passwordAgain = request.getParameter("passwordAgain");
    String address = request.getParameter("address");
    String studyProgram = request.getParameter("studyProgram");
    String courseStr = request.getParameter("course");

    int course = 1; // значение по умолчанию
    if (courseStr != null && !courseStr.trim().isEmpty()) {
        try {
            course = Integer.parseInt(courseStr);
        } catch (NumberFormatException e) {
            course = 1;
        }
    }

    // Валидация
    if (email == null || email.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
        response.sendRedirect("register.jsp?error=Заполните обязательные поля");
        return;
    }

    if (!password.equals(passwordAgain)) {
        response.sendRedirect("register.jsp?error=Пароли не совпадают");
        return;
    }

    if (password.length() < 6) {
        response.sendRedirect("register.jsp?error=Пароль должен содержать минимум 6 символов");
        return;
    }

    try {
        UserService userService = new UserService();
        User user = userService.registerUser(email, name, password, passwordAgain, address, studyProgram, course).getData();

        session.setAttribute("user", user);

        response.sendRedirect("index.jsp?success=Регистрация прошла успешно! Добро пожаловать, " + name + "!");

    } catch (IllegalArgumentException e) {
        e.printStackTrace();
        response.sendRedirect("register.jsp?error=" + e.getMessage());
    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect("register.jsp?error=Произошла ошибка при регистрации");
    }
%>