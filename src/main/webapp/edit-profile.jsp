<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.User" %>
<%@ page import="com.mipt.portal.service.UserService" %>
<%@ page import="com.mipt.portal.entity.Address" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Optional" %>
<%
    // Проверяем авторизацию
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

    // Проверяем сообщения от обработчика обновления
    String updateMessage = (String) session.getAttribute("updateMessage");
    String updateMessageType = (String) session.getAttribute("updateMessageType");
    if (updateMessage != null) {
        message = updateMessage;
        messageType = updateMessageType;
        session.removeAttribute("updateMessage");
        session.removeAttribute("updateMessageType");
    }

    // Если форма отправлена для проверки пароля
    if ("POST".equalsIgnoreCase(request.getMethod()) && "verify".equals(request.getParameter("action"))) {
        String currentPassword = request.getParameter("currentPassword");

        // Получаем Spring контекст
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (springContext != null) {
            UserService userService = springContext.getBean(UserService.class);
            Optional<User> loginResult = userService.loginUser(user.getEmail(), currentPassword);

            if (loginResult.isPresent()) {
                // Пароль верный, устанавливаем флаг в сессии
                session.setAttribute("canEditProfile", true);
                message = "✅ Пароль подтвержден. Теперь вы можете изменить данные.";
                messageType = "success";
            } else {
                message = "❌ Неверный пароль. Попробуйте снова.";
                messageType = "error";
            }
        } else {
            message = "❌ Ошибка инициализации приложения";
            messageType = "error";
        }
    }

    // Если форма отправлена для обновления профиля
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
                    // Создаем объект Address
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

                    // Получаем Spring контекст
                    ServletContext servletContext = request.getServletContext();
                    WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

                    if (springContext == null) {
                        message = "❌ Ошибка инициализации приложения";
                        messageType = "error";
                    } else {
                        UserService userService = springContext.getBean(UserService.class);

                        // Сохраняем старый пароль, если новый не установлен
                        if (newPassword == null || newPassword.trim().isEmpty()) {
                            Optional<User> existingUser = userService.findUserById(user.getId());
                            if (existingUser.isPresent()) {
                                user.setHashPassword(existingUser.get().getHashPassword());
                                user.setSalt(existingUser.get().getSalt());
                            }
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

    // Проверяем, подтвержден ли пароль
    Boolean canEdit = (Boolean) session.getAttribute("canEditProfile");
    if (canEdit == null) {
        canEdit = false;
    }
%>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - Редактирование профиля</title>
    <style>
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        min-height: 100vh;
        padding: 20px;
        display: flex;
        justify-content: center;
        align-items: center;
      }

      .edit-container {
        background: white;
        border-radius: 20px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
        padding: 40px;
        max-width: 600px;
        width: 100%;
        animation: fadeInUp 0.8s ease-out;
      }

      @keyframes fadeInUp {
        from {
          opacity: 0;
          transform: translateY(30px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      .portal-logo {
        font-size: 2.5rem;
        font-weight: 800;
        background: linear-gradient(135deg, #667eea, #764ba2);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        text-align: center;
        margin-bottom: 10px;
      }

      .page-title {
        color: #666;
        font-size: 1.5rem;
        text-align: center;
        margin-bottom: 30px;
        font-weight: 300;
      }

      .form-group {
        margin-bottom: 20px;
      }

      label {
        display: block;
        margin-bottom: 8px;
        color: #333;
        font-weight: 500;
      }

      input, select {
        width: 100%;
        padding: 12px 15px;
        border: 2px solid #e1e5e9;
        border-radius: 10px;
        font-size: 1rem;
        transition: all 0.3s ease;
      }

      input:focus, select:focus {
        outline: none;
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }

      .address-section {
        background: #f8f9fa;
        border-radius: 10px;
        padding: 20px;
        margin-bottom: 20px;
      }

      .address-section h3 {
        margin-bottom: 15px;
        color: #667eea;
        font-size: 1.1rem;
      }

      .row {
        display: flex;
        gap: 15px;
        margin-bottom: 15px;
      }

      .row .form-group {
        flex: 1;
        margin-bottom: 0;
      }

      .readonly-field {
        background-color: #f8f9fa;
        color: #666;
        cursor: not-allowed;
      }

      .button-group {
        display: flex;
        flex-direction: column;
        gap: 15px;
        margin-top: 30px;
      }

      .btn {
        padding: 15px 25px;
        border: none;
        border-radius: 12px;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-block;
        text-align: center;
      }

      .btn-primary {
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
      }

      .btn-primary:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
      }

      .btn-secondary {
        background: transparent;
        color: #667eea;
        border: 2px solid #667eea;
      }

      .btn-secondary:hover {
        background: #667eea;
        color: white;
        transform: translateY(-2px);
      }

      .btn-danger {
        background: #dc3545;
        color: white;
      }

      .btn-danger:hover {
        background: #c82333;
        transform: translateY(-2px);
      }

      .message {
        padding: 15px;
        border-radius: 10px;
        margin-bottom: 20px;
        font-weight: 500;
        text-align: center;
      }

      .message.success {
        background: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
      }

      .message.error {
        background: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
      }

      .message.info {
        background: #cce7ff;
        color: #004085;
        border: 1px solid #b3d7ff;
      }

      .password-section {
        background: #f8f9fa;
        padding: 20px;
        border-radius: 10px;
        margin: 20px 0;
        border-left: 4px solid #667eea;
      }

      .password-section h3 {
        color: #333;
        margin-bottom: 15px;
        font-size: 1.2rem;
      }

      .verification-section {
        background: #fff3cd;
        padding: 20px;
        border-radius: 10px;
        margin: 20px 0;
        border-left: 4px solid #ffc107;
        text-align: center;
      }

      .current-info {
        background: #e9ecef;
        padding: 15px;
        border-radius: 8px;
        margin-bottom: 15px;
        font-size: 0.9rem;
      }

      .current-info strong {
        color: #333;
      }

      @media (max-width: 480px) {
        .edit-container {
          padding: 30px 20px;
          margin: 20px;
        }

        .portal-logo {
          font-size: 2.2rem;
        }

        .row {
          flex-direction: column;
          gap: 20px;
        }

        .btn {
          padding: 12px 20px;
          font-size: 0.95rem;
        }
      }
    </style>
</head>
<body>
<div class="edit-container">
    <div class="portal-logo">PORTAL</div>
    <div class="page-title">Редактирование профиля</div>

    <% if (!message.isEmpty()) { %>
    <div class="message <%= messageType %>">
        <%= message %>
    </div>
    <% } %>

    <% if (!canEdit) { %>
    <!-- Секция подтверждения пароля -->
    <div class="verification-section">
        <h3>🔒 Подтверждение личности</h3>
        <p>Для изменения данных профиля необходимо подтвердить ваш пароль</p>

        <form method="POST" action="edit-profile.jsp">
            <input type="hidden" name="action" value="verify">

            <div class="form-group">
                <label for="currentPassword">Текущий пароль</label>
                <input type="password" id="currentPassword" name="currentPassword"
                       placeholder="Введите ваш текущий пароль" required>
            </div>

            <div class="button-group">
                <button type="submit" class="btn btn-primary">Подтвердить пароль</button>
                <a href="dashboard.jsp" class="btn btn-secondary">Отмена</a>
            </div>
        </form>
    </div>
    <% } else { %>
    <div class="current-info">
        <strong>Текущий email:</strong> <%= user.getEmail() %><br>
        <strong>Количество объявлений:</strong> <%= user.getAdList() != null ? user.getAdList().size() : 0 %>
    </div>

    <form method="POST" action="edit-profile.jsp">
        <input type="hidden" name="action" value="update">

        <div class="form-group">
            <label for="name">Имя пользователя *</label>
            <input type="text" id="name" name="name"
                   value="<%= user.getName() != null ? user.getName() : "" %>"
                   placeholder="Введите ваше имя" required>
        </div>

        <div class="address-section">
            <h3>📍 Адрес проживания</h3>

            <div class="form-group">
                <label for="addressFull">Полный адрес</label>
                <input type="text" id="addressFull" name="addressFull"
                       value="<%= user.getAddress() != null && user.getAddress().getFullAddress() != null ? user.getAddress().getFullAddress() : "" %>"
                       placeholder="г. Москва, ул. Примерная, д. 1">
            </div>

            <div class="row">
                <div class="form-group">
                    <label for="addressCity">Город</label>
                    <input type="text" id="addressCity" name="addressCity"
                           value="<%= user.getAddress() != null && user.getAddress().getCity() != null ? user.getAddress().getCity() : "" %>"
                           placeholder="Москва">
                </div>
                <div class="form-group">
                    <label for="addressStreet">Улица</label>
                    <input type="text" id="addressStreet" name="addressStreet"
                           value="<%= user.getAddress() != null && user.getAddress().getStreet() != null ? user.getAddress().getStreet() : "" %>"
                           placeholder="Примерная">
                </div>
            </div>

            <div class="row">
                <div class="form-group">
                    <label for="addressHouseNumber">Дом</label>
                    <input type="text" id="addressHouseNumber" name="addressHouseNumber"
                           value="<%= user.getAddress() != null && user.getAddress().getHouseNumber() != null ? user.getAddress().getHouseNumber() : "" %>"
                           placeholder="1">
                </div>
                <div class="form-group">
                    <label for="addressBuilding">Корпус</label>
                    <input type="text" id="addressBuilding" name="addressBuilding"
                           value="<%= user.getAddress() != null && user.getAddress().getBuilding() != null ? user.getAddress().getBuilding() : "" %>"
                           placeholder="2 (если есть)">
                </div>
            </div>
        </div>

        <div class="form-group">
            <label for="studyProgram">Учебная программа *</label>
            <select id="studyProgram" name="studyProgram" required>
                <option value="ФПМИ" <%= "ФПМИ".equals(user.getStudyProgram()) ? "selected" : "" %>>ФПМИ</option>
                <option value="ВШПИ" <%= "ВШПИ".equals(user.getStudyProgram()) ? "selected" : "" %>>ВШПИ</option>
                <option value="ФРКТ" <%= "ФРКТ".equals(user.getStudyProgram()) ? "selected" : "" %>>ФРКТ</option>
                <option value="ЛФИ" <%= "ЛФИ".equals(user.getStudyProgram()) ? "selected" : "" %>>ЛФИ</option>
                <option value="ФАКТ" <%= "ФАКТ".equals(user.getStudyProgram()) ? "selected" : "" %>>ФАКТ</option>
                <option value="ФЭФМ" <%= "ФЭФМ".equals(user.getStudyProgram()) ? "selected" : "" %>>ФЭФМ</option>
                <option value="ВШМ" <%= "ВШМ".equals(user.getStudyProgram()) ? "selected" : "" %>>ВШМ</option>
                <option value="КНТ" <%= "КНТ".equals(user.getStudyProgram()) ? "selected" : "" %>>КНТ</option>
                <option value="ФБМФ" <%= "ФБМФ".equals(user.getStudyProgram()) ? "selected" : "" %>>ФБМФ</option>
                <option value="ПИШ ФАЛТ" <%= "ПИШ ФАЛТ".equals(user.getStudyProgram()) ? "selected" : "" %>>ПИШ ФАЛТ</option>
                <option value="ВШСИ" <%= "ВШСИ".equals(user.getStudyProgram()) ? "selected" : "" %>>ВШСИ</option>
            </select>
        </div>

        <div class="form-group">
            <label for="course">Курс *</label>
            <select id="course" name="course" required>
                <% for (int i = 1; i <= 6; i++) { %>
                <option value="<%= i %>" <%= i == user.getCourse() ? "selected" : "" %>><%= i %> курс</option>
                <% } %>
            </select>
        </div>

        <div class="password-section">
            <h3>🔐 Смена пароля</h3>
            <div class="form-group">
                <label for="newPassword">Новый пароль (оставьте пустым, если не хотите менять)</label>
                <input type="password" id="newPassword" name="newPassword" placeholder="Минимум 8 символов">
            </div>
            <div class="form-group">
                <label for="confirmPassword">Подтверждение нового пароля</label>
                <input type="password" id="confirmPassword" name="confirmPassword">
            </div>
        </div>

        <div class="button-group">
            <button type="submit" class="btn btn-primary">Сохранить изменения</button>
            <a href="dashboard.jsp" class="btn btn-secondary">Отмена</a>
            <a href="edit-profile.jsp?action=cancel" class="btn btn-danger">Отменить редактирование</a>
        </div>
    </form>
    <% } %>
</div>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');

    if (newPassword && confirmPassword) {
      function checkPasswords() {
        if (newPassword.value !== confirmPassword.value && confirmPassword.value !== '') {
          confirmPassword.style.borderColor = '#dc3545';
        } else {
          confirmPassword.style.borderColor = '#28a745';
        }
      }

      newPassword.addEventListener('input', checkPasswords);
      confirmPassword.addEventListener('input', checkPasswords);
    }
  });
</script>
</body>
</html>

<%
    // Обработка отзыва доступа
    if ("cancel".equals(request.getParameter("action"))) {
        session.removeAttribute("canEditProfile");
        response.sendRedirect("edit-profile.jsp");
    }
%>