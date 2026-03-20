<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - Главная</title>
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

      .button-group {
        display: flex;
        flex-direction: column;
        gap: 20px;
        margin-top: 40px;
      }

      .btn {
        padding: 18px 30px;
        border: none;
        border-radius: 12px;
        font-size: 1.1rem;
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

      .features {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        margin-top: 40px;
        text-align: left;
      }

      .feature {
        padding: 15px;
        background: #f8f9fa;
        border-radius: 10px;
        border-left: 4px solid #667eea;
      }

      .feature h4 {
        color: #333;
        margin-bottom: 5px;
        font-size: 0.9rem;
      }

      .feature p {
        color: #666;
        font-size: 0.8rem;
      }

      .welcome-text {
        color: #555;
        line-height: 1.6;
        margin-bottom: 30px;
        font-size: 1rem;
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

        .features {
          grid-template-columns: 1fr;
        }

        .button-group {
          gap: 15px;
        }

        .btn {
          padding: 15px 25px;
          font-size: 1rem;
        }
      }
    </style>
</head>
<body>
<div class="portal-container">
    <div class="portal-logo">PORTAL</div>
    <div class="portal-subtitle">Физтех сообщество</div>

    <p class="welcome-text">
        Добро пожаловать в единое пространство для студентов МФТИ.
        Помогайте друг другу, обменивайтесь знаниями и находите единомышленников.
    </p>

    <div class="features">
        <div class="feature">
            <h4>🎓 Учеба</h4>
            <p>Помощь в учебе от других физтехов</p>
        </div>
        <div class="feature">
            <h4>🛒 Кампус-маркет</h4>
            <p>Покупка и продажа вещей на кампусе</p>
        </div>
        <div class="feature">
            <h4>👥 Сообщество</h4>
            <p>Общение с студентами и преподавателями</p>
        </div>
    </div>

    <div class="button-group">
        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Зарегистрироваться</a>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary">Войти</a>
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">На главную</a>
    </div>
</div>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach((btn, index) => {
      btn.style.animationDelay = (index * 0.1) + 's';
    });
  });
</script>
</body>
</html>