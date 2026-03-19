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

        UserService userService = new UserService();
        OperationResult<User> loginResult = userService.loginUser(user.getEmail(), currentPassword);

        if (loginResult.isSuccess()) {
            // Пароль верный, устанавливаем флаг в сессии
            session.setAttribute("canEditProfile", true);
            message = "✅ Пароль подтвержден. Теперь вы можете изменить данные.";
            messageType = "success";
        } else {
            message = "❌ Неверный пароль. Попробуйте снова.";
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
            max-width: 500px;
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

    <form method="POST" action="update-profile-handler.jsp">
        <input type="hidden" name="action" value="update">

        <div class="form-group">
            <label for="name">Имя пользователя</label>
            <input type="text" id="name" name="name"
                   value="<%= user.getName() %>"
                   placeholder="Введите ваше имя" required>
        </div>

        <div class="form-group">
            <label for="address">Адрес</label>
            <input type="text" id="address" name="address"
                   value="<%= user.getAddress() != null ? user.getAddress() : "" %>"
                   placeholder="Введите ваш адрес">
        </div>

        <div class="form-group">
            <label for="studyProgram">Учебная программа</label>
            <select id="studyProgram" name="studyProgram" required>
                <option value="">Выберите программу</option>
                <option value="Прикладная математика и информатика">Прикладная математика и информатика</option>
                <option value="Прикладная математика и физика">Прикладная математика и физика</option>
                <option value="Информатика и вычислительная техника">Информатика и вычислительная техника</option>
                <option value="Радиотехника">Радиотехника</option>
                <option value="Системный анализ и управление">Системный анализ и управление</option>
                <option value="Информационная безопасность">Информационная безопасность</option>
                <option value="Астрономия">Астрономия</option>
                <option value="Физика">Физика</option>
                <option value="Прикладная механика">Прикладная механика</option>
                <option value="Компьютерная безопасность">Компьютерная безопасность</option>
                <option value="Программная инженерия">Программная инженерия</option>
                <option value="Биотехнология">Биотехнология</option>
                <option value="Фундаментальная и прикладная химия">Фундаментальная и прикладная химия</option>
                <option value="Фундаментальная медицина">Фундаментальная медицина</option>
                <option value="Химия">Химия</option>
                <option value="Химическая физика и горение">Химическая физика и горение</option>
                <option value="Нанотехнологии и микросистемная техника">Нанотехнологии и микросистемная техника</option>
                <option value="Электроника и наноэлектроника">Электроника и наноэлектроника</option>
                <option value="Фотоника и оптоинформатика">Фотоника и оптоинформатика</option>
                <option value="Химия, физика и механика материалов">Химия, физика и механика материалов</option>
                <option value="Экономика">Экономика</option>
                <option value="Искусственный интеллект и машинное обучение">Искусственный интеллект и машинное обучение</option>
                <option value="Большие данные и распределенные системы">Большие данные и распределенные системы</option>
                <option value="Квантовые информационные технологии">Квантовые информационные технологии</option>
                <option value="Радиофотоника и квантовые коммуникации">Радиофотоника и квантовые коммуникации</option>
                <option value="Кибербезопасность телекоммуникационных систем">Кибербезопасность телекоммуникационных систем</option>
                <option value="Астрофизика и физика космоса">Астрофизика и физика космоса</option>
                <option value="Физика фундаментальных взаимодействий и космология">Физика фундаментальных взаимодействий и космология</option>
                <option value="Квантовая оптика и нанофотоника">Квантовая оптика и нанофотоника</option>
                <option value="Медицинская физика">Медицинская физика</option>
                <option value="Физика плазмы и управляемый термоядерный синтез">Физика плазмы и управляемый термоядерный синтез</option>
                <option value="Физика высоких энергий">Физика высоких энергий</option>
                <option value="Физика твердого тела и наноструктур">Физика твердого тела и наноструктур</option>
                <option value="Квантовые технологии и материалы">Квантовые технологии и материалы</option>
                <option value="Проектирование и технологии производства авиационно-космической техники">Проектирование и технологии производства авиационно-космической техники</option>
                <option value="Космические науки и технологии">Космические науки и технологии</option>
                <option value="Управление в технических системах">Управление в технических системах</option>
                <option value="Вычислительная механика и инженерия">Вычислительная механика и инженерия</option>
                <option value="Data Science">Data Science</option>
                <option value="Software Engineering и распределенные системы">Software Engineering и распределенные системы</option>
                <option value="Теоретическая информатика и компьютерные технологии">Теоретическая информатика и компьютерные технологии</option>
                <option value="Финансовые технологии и анализ данных">Финансовые технологии и анализ данных</option>
                <option value="Квантовые вычисления">Квантовые вычисления</option>
                <option value="Биомедицинская фотоника и биоинженерия">Биомедицинская фотоника и биоинженерия</option>
                <option value="Нейронауки и когнитивные науки">Нейронауки и когнитивные науки</option>
                <option value="Химическая биология и бионанотехнологии">Химическая биология и бионанотехнологии</option>
                <option value="Молекулярная и клеточная биомедицина">Молекулярная и клеточная биомедицина</option>
                <option value="Химическая физика и горение">Химическая физика и горение</option>
                <option value="Физика и химия наноструктур">Физика и химия наноструктур</option>
                <option value="Квантовая химия и молекулярное моделирование">Квантовая химия и молекулярное моделирование</option>
                <option value="Новые материалы и технологии">Новые материалы и технологии</option>
                <option value="Нанофотоника и квантовые материалы">Нанофотоника и квантовые материалы</option>
                <option value="Фотоника и оптоинформатика">Фотоника и оптоинформатика</option>
                <option value="Физика и технология новых материалов">Физика и технология новых материалов</option>
                <option value="Теоретическая физика">Теоретическая физика</option>
                <option value="Математическая физика">Математическая физика</option>
                <option value="Физика конденсированного состояния">Физика конденсированного состояния</option>
                <option value="Экономика и финансы">Экономика и финансы</option>
                <option value="Экономика и управление научными проектами и высокотехнологичными предприятиями">Экономика и управление научными проектами и высокотехнологичными предприятиями</option>
            </select>
        </div>

        <div class="form-group">
            <label for="course">Курс</label>
            <select id="course" name="course" required>
                <option value="">Выберите курс</option>
                <% for (int i = 1; i <= 6; i++) { %>
                <option value="<%= i %>" <%= i == user.getCourse() ? "selected" : "" %>><%= i %> курс</option>
                <% } %>
            </select>
        </div>
        <div class="button-group">
            <button type="submit" class="btn btn-primary">Сохранить изменения</button>
            <a href="dashboard.jsp" class="btn btn-secondary">Отмена</a>
        </div>
    </form>
    <% } %>
</div>

<script>

    // Обработчик отправки формы
    function handleFormSubmit() {
        // Показываем сообщение о сохранении
        showSaveMessage();

        // Продолжаем стандартную отправку формы
        return true;
    }

    // Функция для показа сообщения о сохранении
    function showSaveMessage() {
        // Создаем элемент для сообщения
        const messageDiv = document.createElement('div');
        messageDiv.innerHTML = `
        <div style="
            position: fixed;
            top: 20px;
            right: 20px;
            background: #4CAF50;
            color: white;
            padding: 15px 20px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
            z-index: 10000;
            display: flex;
            align-items: center;
            gap: 10px;
            font-weight: 500;
        ">
            <span style="font-size: 1.2em;">✓</span>
            <span>Изменения сохранены успешно!</span>
        </div>
    `;

        document.body.appendChild(messageDiv);

        // Автоматически скрываем через 3 секунды
        setTimeout(() => {
            messageDiv.remove();
        }, 3000);
    }
    // Добавляем проверку паролей в реальном времени
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