<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="com.mipt.portal.users.service.UserService" %>
<%@ page import="com.mipt.portal.users.service.OperationResult" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Boolean canEdit = (Boolean) session.getAttribute("canEditProfile");
    if (canEdit == null || !canEdit) {
        response.sendRedirect("edit-profile.jsp");
        return;
    }

    String message = "";
    String messageType = "";

    if ("POST".equalsIgnoreCase(request.getMethod()) && "update".equals(request.getParameter("action"))) {
        try {
            String name = request.getParameter("name");
            String address = request.getParameter("address");
            String studyProgram = request.getParameter("studyProgram");
            String courseStr = request.getParameter("course");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (name == null || name.trim().isEmpty()) {
                message = "❌ Имя не может быть пустым";
                messageType = "error";
            } else if (studyProgram == null || studyProgram.trim().isEmpty()) {
                message = "❌ Учебная программа не может быть пустой";
                messageType = "error";
            } else if (courseStr == null || courseStr.trim().isEmpty()) {
                message = "❌ Курс не может быть пустым";
                messageType = "error";
            } else {
                int course = Integer.parseInt(courseStr);

                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    if (!newPassword.equals(confirmPassword)) {
                        message = "❌ Новые пароли не совпадают";
                        messageType = "error";
                    } else if (newPassword.length() < 8) {
                        message = "❌ Пароль должен содержать минимум 8 символов";
                        messageType = "error";
                    } else {
                        user.setPassword(newPassword);
                    }
                }

                if (message.isEmpty()) {
                    user.setName(name.trim());
                    user.setAddress(address != null ? address.trim() : "");
                    user.setStudyProgram(studyProgram);
                    user.setCourse(course);

                    UserService userService = new UserService();
                    OperationResult<User> updateResult = userService.updateUser(user);

                    if (updateResult.isSuccess()) {
                        session.setAttribute("user", updateResult.getData());
                        session.removeAttribute("canEditProfile");

                        // ВАЖНОЕ ИЗМЕНЕНИЕ: редирект на dashboard с сообщением об успехе
                        session.setAttribute("successMessage", "✅ Профиль успешно обновлен!");
                        response.sendRedirect("dashboard.jsp");
                        return; // Важно: завершаем выполнение после редиректа
                    } else {
                        message = updateResult.getMessage();
                        messageType = "error";
                    }
                }
            }
        } catch (NumberFormatException e) {
            message = "❌ Неверный формат курса";
            messageType = "error";
        } catch (Exception e) {
            message = "❌ Ошибка при обновлении данных: " + e.getMessage();
            messageType = "error";
        }
    }

    // Сохраняем сообщения об ошибках и редиректим обратно на edit-profile.jsp
    // (только в случае ошибки)
    if (!message.isEmpty()) {
        session.setAttribute("updateMessage", message);
        session.setAttribute("updateMessageType", messageType);
        response.sendRedirect("edit-profile.jsp");
    }
%>