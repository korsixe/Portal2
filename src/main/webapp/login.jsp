<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="com.mipt.portal.users.service.UserService" %>
<%@ page import="com.mipt.portal.users.service.OperationResult" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="java.util.Optional" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - Вход</title>
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

        input {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e1e5e9;
            border-radius: 10px;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        input:focus {
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

        .register-link {
            text-align: center;
            margin-top: 20px;
            color: #666;
        }

        .register-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }

        .register-link a:hover {
            text-decoration: underline;
        }

        .forgot-password {
            text-align: right;
            margin-top: -10px;
            margin-bottom: 20px;
        }

        .forgot-password a {
            color: #667eea;
            text-decoration: none;
            font-size: 0.9rem;
        }

        .forgot-password a:hover {
            text-decoration: underline;
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
    <div class="portal-subtitle">Вход</div>

    <%
        String message = "";
        String messageType = "";
        boolean loginSuccess = false;
        User loggedInUser = null;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            UserService userService = new UserService();
            Optional<User> result = userService.loginUser(email, password);

            if (result.isPresent()) {
                loggedInUser = result.get();
                message = "Login successful! Welcome, " + loggedInUser.getName() + "!";
                messageType = "success";
                loginSuccess = true;

                session.setAttribute("user", loggedInUser);
                session.setAttribute("userId", loggedInUser.getId());
                session.setAttribute("userName", loggedInUser.getName());
                session.setAttribute("userEmail", loggedInUser.getEmail());
            } else {
                message = "Invalid email or password. Please try again.";
                messageType = "error";
            }
        }
    %>

    <% if (!message.isEmpty()) { %>
    <div class="message <%= messageType %>">
        <%= message %>
    </div>
    <% } %>

    <% if (!loginSuccess) { %>
    <form method="POST" action="login.jsp">
        <div class="form-group">
            <label for="email">Email</label>
            <input type="email" id="email" name="email"
                   value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>"
                   placeholder="ivanov.ii@phystech.edu" required>
        </div>

        <div class="form-group">
            <label for="password">Пароль</label>
            <input type="password" id="password" name="password"
                   placeholder="Введите ваш пароль" required>
            <div class="forgot-password">
                <a href="forgot-password.jsp">Забыли пароль?</a>
            </div>
        </div>

        <div class="button-group">
            <button type="submit" class="btn btn-primary">Войти</button>
            <a href="register.jsp" class="btn btn-secondary">Регистрация</a>
        </div>
    </form>
    <% } else { %>
    <div class="button-group">
        <a href="dashboard.jsp" class="btn btn-primary">Перейти в личный кабинет</a>
        <a href="home.jsp" class="btn btn-secondary">На главную</a>
    </div>
    <% } %>
</div>

<script>
    // Добавляем небольшую анимацию при загрузке
    document.addEventListener('DOMContentLoaded', function() {
        const inputs = document.querySelectorAll('input');
        inputs.forEach((input, index) => {
            input.style.animationDelay = (index * 0.1) + 's';
        });
    });

    // Показываем/скрываем пароль (опционально)
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        // Можно добавить иконку для показа/скрытия пароля
        const togglePassword = document.createElement('span');
        togglePassword.innerHTML = '👁️';
        togglePassword.style.position = 'absolute';
        togglePassword.style.right = '15px';
        togglePassword.style.top = '50%';
        togglePassword.style.transform = 'translateY(-50%)';
        togglePassword.style.cursor = 'pointer';

        passwordInput.style.position = 'relative';
        passwordInput.parentElement.style.position = 'relative';
        passwordInput.parentElement.appendChild(togglePassword);

        togglePassword.addEventListener('click', function() {
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                togglePassword.innerHTML = '🔒';
            } else {
                passwordInput.type = 'password';
                togglePassword.innerHTML = '👁️';
            }
        });
    }
</script>
</body>
</html>