<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.users.service.UserService" %>
<%@ page import="com.mipt.portal.users.service.OperationResult" %>
<%@ page import="com.mipt.portal.users.User" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - Регистрация</title>
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
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .portal-container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 60px 40px;
            text-align: center;
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
            font-size: 3.5rem;
            font-weight: 800;
            background: linear-gradient(135deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
            letter-spacing: 2px;
        }

        .portal-subtitle {
            color: #666;
            font-size: 1.2rem;
            margin-bottom: 40px;
            font-weight: 300;
        }

        .form-group {
            margin-bottom: 20px;
            text-align: left;
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

        .login-link {
            text-align: center;
            margin-top: 20px;
            color: #666;
        }

        .login-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }

        .login-link a:hover {
            text-decoration: underline;
        }

        .password-info {
            background: #f8f9fa;
            border: 1px solid #e1e5e9;
            border-radius: 8px;
            padding: 12px 15px;
            margin-bottom: 15px;
            font-size: 0.9rem;
            color: #555;
        }

        .password-info ul {
            list-style-type: none;
            padding-left: 0;
            margin: 8px 0 0 0;
        }

        .password-info li {
            margin-bottom: 4px;
            position: relative;
            padding-left: 15px;
        }

        .password-info li:before {
            content: "•";
            position: absolute;
            left: 0;
            color: #667eea;
        }

        /* Адаптивность */
        @media (max-width: 480px) {
            .portal-container {
                padding: 40px 20px;
                margin: 20px;
            }

            .portal-logo {
                font-size: 2.8rem;
            }

            .button-group {
                gap: 15px;
            }

            .btn {
                padding: 12px 20px;
                font-size: 0.95rem;
            }
        }
    </style>
</head>
<body>
<div class="portal-container">
    <div class="portal-logo">PORTAL</div>
    <div class="portal-subtitle">Регистрация</div>

    <%
        String message = "";
        String messageType = "";
        boolean registrationSuccess = false;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String email = request.getParameter("email");
            String name = request.getParameter("name");
            String password = request.getParameter("password");
            String passwordAgain = request.getParameter("passwordAgain");
            String address = request.getParameter("address");
            String studyProgram = request.getParameter("studyProgram");
            String courseStr = request.getParameter("course");
            int course = courseStr != null ? Integer.parseInt(courseStr) : 1;

            UserService userService = new UserService();
            OperationResult<User> result = userService.registerUser(
                    email, name, password, passwordAgain, address, studyProgram, course
            );

            if (result.isSuccess()) {
                message = result.getMessage();
                messageType = "success";
                registrationSuccess = true;
            } else {
                message = result.getMessage();
                messageType = "error";
            }
        }
    %>

    <% if (!message.isEmpty()) { %>
    <div class="message <%= messageType %>">
        <%= message.replace("\n", "<br>") %>
    </div>
    <% } %>

    <% if (!registrationSuccess) { %>
    <form method="POST" action="register.jsp">
        <div class="form-group">
            <label for="email">Email *</label>
            <input type="email" id="email" name="email"
                   value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>"
                   placeholder="ivanov.ii@phystech.edu" required>
        </div>

        <div class="form-group">
            <label for="name">Имя пользователя *</label>
            <input type="text" id="name" name="name"
                   value="<%= request.getParameter("name") != null ? request.getParameter("name") : "" %>"
                   placeholder="ivanov" required>
        </div>

        <div class="form-group">
            <label for="password">Пароль *</label>
            <div class="password-info">
                Пароль должен содержать:
                <ul>
                    <li>Строчные и заглавные буквы</li>
                    <li>Цифры и специальные символы: ! ? @ # $ % & * _ -</li>
                </ul>
                Длина пароля не менее 8 символов
            </div>
            <input type="password" id="password" name="password"
                   placeholder="Минимум 8 символов." required>
        </div>

        <div class="form-group">
            <label for="passwordAgain">Подтверждение пароля *</label>
            <input type="password" id="passwordAgain" name="passwordAgain"
                   placeholder="Повторите пароль" required>
        </div>

        <div class="form-group">
            <label for="studyProgram">Учебная программа *</label>
            <select id="studyProgram" name="studyProgram" required>
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
            <label for="course">Курс *</label>
            <select id="course" name="course" required>
                <option value="">Выберите курс</option>
                <% for (int i = 1; i <= 6; i++) { %>
                <option value="<%= i %>" <%= String.valueOf(i).equals(request.getParameter("course")) ? "selected" : "" %>><%= i %> курс</option>
                <% } %>
            </select>
        </div>

        <div class="form-group">
            <label for="address">Адрес</label>
            <input type="text" id="address" name="address"
                   value="<%= request.getParameter("address") != null ? request.getParameter("address") : "" %>"
                   placeholder="Общежитие, комната">
        </div>

        <div class="button-group">
            <button type="submit" class="btn btn-primary">Зарегистрироваться</button>
            <a href="login.jsp" class="btn btn-secondary">Войти</a>
        </div>
    </form>
    <% } else { %>
    <div class="button-group">
        <a href="login.jsp" class="btn btn-primary">Войти в аккаунт</a>
        <a href="home.jsp" class="btn btn-secondary">На главную</a>
    </div>
    <% } %>
</div>

<script>
    // Добавляем небольшую анимацию при загрузке
    document.addEventListener('DOMContentLoaded', function() {
        const inputs = document.querySelectorAll('input, select');
        inputs.forEach((input, index) => {
            input.style.animationDelay = (index * 0.1) + 's';
        });
    });

    // Показываем подсказку о пароле при фокусе
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('focus', function() {
            if (!this.getAttribute('data-hint-shown')) {
                this.setAttribute('placeholder', 'Введите надежный пароль');
                this.setAttribute('data-hint-shown', 'true');
            }
        });
    }

    // Проверка совпадения паролей в реальном времени
    const password = document.getElementById('password');
    const passwordAgain = document.getElementById('passwordAgain');

    if (password && passwordAgain) {
        function checkPasswords() {
            if (password.value !== passwordAgain.value) {
                passwordAgain.style.borderColor = '#dc3545';
            } else {
                passwordAgain.style.borderColor = '#28a745';
            }
        }

        password.addEventListener('input', checkPasswords);
        passwordAgain.addEventListener('input', checkPasswords);
    }
</script>
</body>
</html>