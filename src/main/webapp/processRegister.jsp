<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.mipt.portal.service.UserService" %>
<%@ page import="com.mipt.portal.entity.User" %>
<%@ page import="com.mipt.portal.entity.Address" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Optional" %>
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
        WebApplicationContext appContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(application);
        UserService userService = appContext.getBean(UserService.class);
        Address addressObj = new Address(address);
        Optional<User> userOpt = userService.registerUser(
            email,
            name,
            password,
            passwordAgain,
            addressObj,
            studyProgram,
            course
        );

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("user", user);
            response.sendRedirect("index.jsp?success=Регистрация прошла успешно! Добро пожаловать, " + name + "!");
        } else {
            response.sendRedirect("register.jsp?error=Ошибка регистрации. Проверьте введенные данные");
        }

    } catch (IllegalArgumentException e) {
        e.printStackTrace();
        response.sendRedirect("register.jsp?error=" + e.getMessage());
    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect("register.jsp?error=Произошла ошибка при регистрации");
    }
%>