<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

      @media (max-width: 480px) {
        .portal-container {
          padding: 40px 20px;
          margin: 20px;
        }

        .portal-logo {
          font-size: 2.8rem;
        }

        .row {
          flex-direction: column;
          gap: 20px;
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
        String message = (String) request.getAttribute("message");
        String messageType = (String) request.getAttribute("messageType");
        Boolean registrationSuccess = (Boolean) request.getAttribute("registrationSuccess");

        if (message == null) message = "";
        if (messageType == null) messageType = "";
        if (registrationSuccess == null) registrationSuccess = false;
    %>

    <% if (!message.isEmpty()) { %>
    <div class="message <%= messageType %>">
        <%= message.replace("\n", "<br>") %>
    </div>
    <% } %>

    <% if (!registrationSuccess) { %>
    <form method="POST" action="/register">
        <div class="form-group">
            <label for="email">Email *</label>
            <input type="email" id="email" name="email"
                   value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                   placeholder="ivanov.ii@phystech.edu" required>
        </div>

        <div class="form-group">
            <label for="name">Имя пользователя *</label>
            <input type="text" id="name" name="name"
                   value="<%= request.getAttribute("name") != null ? request.getAttribute("name") : "" %>"
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

        <div class="address-section">
            <h3>Адрес проживания</h3>

            <div class="form-group">
                <label for="addressFull">Полный адрес</label>
                <input type="text" id="addressFull" name="addressFull"
                       value="<%= request.getAttribute("addressFull") != null ? request.getAttribute("addressFull") : "" %>"
                       placeholder="г. Москва, ул. Примерная, д. 1">
            </div>

            <div class="row">
                <div class="form-group">
                    <label for="addressCity">Город</label>
                    <input type="text" id="addressCity" name="addressCity"
                           value="<%= request.getAttribute("addressCity") != null ? request.getAttribute("addressCity") : "" %>"
                           placeholder="Москва">
                </div>
                <div class="form-group">
                    <label for="addressStreet">Улица</label>
                    <input type="text" id="addressStreet" name="addressStreet"
                           value="<%= request.getAttribute("addressStreet") != null ? request.getAttribute("addressStreet") : "" %>"
                           placeholder="Примерная">
                </div>
            </div>

            <div class="row">
                <div class="form-group">
                    <label for="addressHouseNumber">Дом</label>
                    <input type="text" id="addressHouseNumber" name="addressHouseNumber"
                           value="<%= request.getAttribute("addressHouseNumber") != null ? request.getAttribute("addressHouseNumber") : "" %>"
                           placeholder="1">
                </div>
                <div class="form-group">
                    <label for="addressBuilding">Корпус</label>
                    <input type="text" id="addressBuilding" name="addressBuilding"
                           value="<%= request.getAttribute("addressBuilding") != null ? request.getAttribute("addressBuilding") : "" %>"
                           placeholder="2 (если есть)">
                </div>
            </div>
        </div>

        <div class="form-group">
            <label for="studyProgram">Учебная программа *</label>
            <select id="studyProgram" name="studyProgram" required>
                <option value="Не указывать">Не указывать</option>
                <option value="ФПМИ">ФПМИ</option>
                <option value="ВШПИ">ВШПИ</option>
                <option value="ФРКТ">ФРКТ</option>
                <option value="ЛФИ">ЛФИ</option>
                <option value="ФАКТ">ФАКТ</option>
                <option value="ФЭФМ">ФЭФМ</option>
                <option value="ВШМ">ВШМ</option>
                <option value="КНТ">КНТ</option>
                <option value="ФБМФ">ФБМФ</option>
                <option value="ПИШ ФАЛТ">ПИШ ФАЛТ</option>
                <option value="ВШСИ">ВШСИ</option>
            </select>
        </div>

        <div class="form-group">
            <label for="course">Курс *</label>
            <select id="course" name="course" required>
                <option value="Не указывать">Не указывать</option>
                <% for (int i = 1; i <= 6; i++) {
                    Integer currentCourse = (Integer) request.getAttribute("course");
                %>
                <option value="<%= i %>" <%= (currentCourse != null && currentCourse == i) ? "selected" : "" %>><%= i %> курс</option>
                <% } %>
            </select>
        </div>

        <div class="button-group">
            <button type="submit" class="btn btn-primary">Зарегистрироваться</button>
            <a href="/login" class="btn btn-secondary">Войти</a>
        </div>
    </form>
    <% } else { %>
    <div class="button-group">
        <a href="/login" class="btn btn-primary">Войти в аккаунт</a>
        <a href="/home" class="btn btn-secondary">На главную</a>
    </div>
    <% } %>
</div>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const inputs = document.querySelectorAll('input, select');
    inputs.forEach((input, index) => {
      input.style.animationDelay = (index * 0.1) + 's';
    });
  });

  const passwordInput = document.getElementById('password');
  if (passwordInput) {
    passwordInput.addEventListener('focus', function() {
      if (!this.getAttribute('data-hint-shown')) {
        this.setAttribute('placeholder', 'Введите надежный пароль');
        this.setAttribute('data-hint-shown', 'true');
      }
    });
  }

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