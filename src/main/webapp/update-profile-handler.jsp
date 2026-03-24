<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.User" %>
<%@ page import="com.mipt.portal.service.UserService" %>
<%@ page import="com.mipt.portal.entity.Address" %>
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
            String addressFull = request.getParameter("addressFull");
            String addressCity = request.getParameter("addressCity");
            String addressStreet = request.getParameter("addressStreet");
            String addressHouseNumber = request.getParameter("addressHouseNumber");
            String addressBuilding = request.getParameter("addressBuilding");
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

                // Проверка пароля
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    if (!newPassword.equals(confirmPassword)) {
                        message = "❌ Новые пароли не совпадают";
                        messageType = "error";
                    } else if (newPassword.length() < 8) {
                        message = "❌ Пароль должен содержать минимум 8 символов";
                        messageType = "error";
                    } else {
                        user.setHashPassword(newPassword);
                    }
                }

                if (message.isEmpty()) {
                    Address address = new Address(addressFull);
                    if (addressCity != null && !addressCity.isEmpty()) {
                        address.setCity(addressCity);
                    }
                    if (addressStreet != null && !addressStreet.isEmpty()) {
                        address.setStreet(addressStreet);
                    }
                    if (addressHouseNumber != null && !addressHouseNumber.isEmpty()) {
                        address.setHouseNumber(addressHouseNumber);
                    }
                    if (addressBuilding != null && !addressBuilding.isEmpty()) {
                        address.setBuilding(addressBuilding);
                    }

                    // Обновляем данные
                    user.setName(name.trim());
                    user.setAddress(address);
                    user.setStudyProgram(studyProgram);
                    user.setCourse(course);

                    // Получаем Spring контекст и бин UserService
                    ServletContext servletContext = request.getServletContext();
                    WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

                    if (springContext == null) {
                        message = "❌ Ошибка инициализации приложения";
                        messageType = "error";
                    } else {
                        UserService userService = springContext.getBean(UserService.class);

                        Optional<User> existingUser = userService.findUserById(user.getId());
                        if (existingUser.isPresent() && (newPassword == null || newPassword.trim().isEmpty())) {
                            user.setHashPassword(existingUser.get().getHashPassword());
                            user.setSalt(existingUser.get().getSalt());
                        }

                        Optional<User> updateResult = userService.updateUser(user);

                        if (updateResult.isPresent()) {
                            User updatedUser = updateResult.get();
                            session.setAttribute("user", updatedUser);
                            session.removeAttribute("canEditProfile");
                            session.setAttribute("successMessage", "✅ Профиль успешно обновлен!");
                            response.sendRedirect("dashboard.jsp");
                            return;
                        } else {
                            message = "❌ Ошибка при обновлении профиля. Попробуйте позже.";
                            messageType = "error";
                        }
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

    if (!message.isEmpty()) {
        session.setAttribute("updateMessage", message);
        session.setAttribute("updateMessageType", messageType);
        response.sendRedirect("edit-profile.jsp");
    }
%>