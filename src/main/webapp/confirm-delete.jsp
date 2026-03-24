<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.Announcement" %>
<%@ page import="com.mipt.portal.enums.Category" %>
<%@ page import="com.mipt.portal.enums.Condition" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    Announcement ad = (Announcement) request.getAttribute("ad");
    if (ad == null) {
        response.sendRedirect("dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Подтверждение удаления • Portal</title>
    <style>
      :root {
        --primary: #4361ee;
        --primary-dark: #3a56d4;
        --secondary: #7209b7;
        --danger: #f72585;
        --warning: #f8961e;
        --light: #f8f9fa;
        --dark: #212529;
        --gray: #6c757d;
        --border: #e9ecef;
        --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
      }

      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        min-height: 100vh;
        padding: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .container {
        max-width: 600px;
        width: 100%;
      }

      .header {
        text-align: center;
        margin-bottom: 30px;
      }

      .logo {
        font-size: 2.5rem;
        font-weight: 700;
        color: white;
        margin-bottom: 10px;
        text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
      }

      .page-title {
        color: white;
        font-size: 1.8rem;
        margin-bottom: 10px;
      }

      .confirmation-card {
        background: white;
        border-radius: 20px;
        padding: 40px;
        box-shadow: var(--shadow-lg);
        text-align: center;
      }

      .warning-icon {
        font-size: 4rem;
        color: var(--danger);
        margin-bottom: 20px;
      }

      .confirmation-title {
        font-size: 1.8rem;
        font-weight: 700;
        color: var(--dark);
        margin-bottom: 15px;
      }

      .confirmation-message {
        color: var(--gray);
        font-size: 1.1rem;
        margin-bottom: 30px;
        line-height: 1.6;
      }

      .ad-preview {
        background: var(--light);
        border-radius: 15px;
        padding: 25px;
        margin: 25px 0;
        text-align: left;
        border-left: 4px solid var(--danger);
      }

      .ad-title {
        font-size: 1.4rem;
        font-weight: 600;
        color: var(--dark);
        margin-bottom: 15px;
      }

      .ad-details {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 15px;
        margin-bottom: 15px;
      }

      .detail-item {
        display: flex;
        flex-direction: column;
      }

      .detail-label {
        font-weight: 600;
        color: var(--gray);
        font-size: 0.9rem;
        margin-bottom: 5px;
      }

      .detail-value {
        color: var(--dark);
        font-weight: 500;
      }

      .ad-price {
        font-size: 1.5rem;
        font-weight: 700;
        color: var(--primary);
        margin: 10px 0;
      }

      .ad-description {
        color: var(--gray);
        line-height: 1.5;
        margin-top: 15px;
        padding-top: 15px;
        border-top: 1px solid var(--border);
      }

      .consequences {
        background: #fff3cd;
        border: 1px solid #ffeaa7;
        color: #856404;
        padding: 20px;
        border-radius: 10px;
        margin: 25px 0;
        text-align: left;
      }

      .consequences h4 {
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        gap: 10px;
      }

      .consequences ul {
        list-style: none;
        padding-left: 0;
      }

      .consequences li {
        padding: 5px 0;
        display: flex;
        align-items: center;
        gap: 10px;
      }

      .consequences li:before {
        content: "⚠️";
        font-size: 0.9rem;
      }

      .action-buttons {
        display: flex;
        gap: 15px;
        justify-content: center;
        margin-top: 30px;
      }

      .btn {
        padding: 15px 30px;
        border: none;
        border-radius: 10px;
        font-size: 1.1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        min-width: 150px;
      }

      .btn-danger {
        background: linear-gradient(135deg, var(--danger), #e00);
        color: white;
        box-shadow: 0 4px 15px rgba(247, 37, 133, 0.3);
      }

      .btn-danger:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(247, 37, 133, 0.4);
      }

      .btn-outline {
        background: transparent;
        color: var(--gray);
        border: 2px solid var(--border);
      }

      .btn-outline:hover {
        border-color: var(--primary);
        color: var(--primary);
        transform: translateY(-2px);
      }

      .icon {
        display: inline-block;
        width: 24px;
        height: 24px;
        text-align: center;
        line-height: 24px;
      }

      @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-5px); }
        75% { transform: translateX(5px); }
      }

      .shake {
        animation: shake 0.5s ease-in-out;
      }

      @keyframes fadeIn {
        from { opacity: 0; transform: translateY(20px); }
        to { opacity: 1; transform: translateY(0); }
      }

      .fade-in {
        animation: fadeIn 0.6s ease;
      }

      /* Адаптивность */
      @media (max-width: 768px) {
        body {
          padding: 10px;
        }

        .confirmation-card {
          padding: 25px 20px;
        }

        .action-buttons {
          flex-direction: column;
        }

        .btn {
          width: 100%;
        }

        .ad-details {
          grid-template-columns: 1fr;
        }
      }

      @media (max-width: 480px) {
        .page-title {
          font-size: 1.5rem;
        }

        .confirmation-title {
          font-size: 1.5rem;
        }

        .warning-icon {
          font-size: 3rem;
        }
      }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo">PORTAL</div>
        <h1 class="page-title">Удаление объявления</h1>
    </div>

    <div class="confirmation-card fade-in">
        <div class="warning-icon shake">🗑️</div>

        <h2 class="confirmation-title">Подтвердите удаление</h2>
        <p class="confirmation-message">
            Вы собираетесь удалить объявление. Это действие нельзя отменить.
        </p>

        <!-- Превью объявления -->
        <div class="ad-preview">
            <h3 class="ad-title"><%= ad.getTitle() %></h3>

            <div class="ad-details">
                <div class="detail-item">
                    <span class="detail-label">Категория:</span>
                    <span class="detail-value"><%= ad.getCategory().getDisplayName() %></span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Состояние:</span>
                    <span class="detail-value"><%= ad.getCondition().getDisplayName() %></span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Местоположение:</span>
                    <span class="detail-value"><%= ad.getLocation() %></span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">Просмотры:</span>
                    <span class="detail-value"><%= ad.getViewCount() != null ? ad.getViewCount() : 0 %></span>
                </div>
            </div>

            <div class="ad-price">
                <%= formatPrice(ad.getPrice()) %>
            </div>

            <div class="ad-description">
                <strong>Описание:</strong><br>
                <%= ad.getDescription() %>
            </div>
        </div>

        <!-- Последствия удаления -->
        <div class="consequences">
            <h4><span class="icon">⚠️</span> Обратите внимание</h4>
            <ul>
                <li>Объявление будет удалено безвозвратно</li>
                <li>Все данные об объявлении будут утеряны</li>
                <li>Отменить это действие будет невозможно</li>
                <li>История просмотров и откликов будет удалена</li>
            </ul>
        </div>

        <!-- Кнопки действий -->
        <div class="action-buttons">
            <form action="confirm-delete" method="post" style="display: inline;">
                <input type="hidden" name="adId" value="<%= ad.getId() %>">
                <input type="hidden" name="confirm" value="yes">
                <button type="submit" class="btn btn-danger">
                    <span class="icon">🗑️</span> Да, удалить
                </button>
            </form>

            <a href="dashboard" class="btn btn-outline">
                <span class="icon">←</span> Отмена
            </a>
        </div>

        <div style="margin-top: 20px; color: var(--gray); font-size: 0.9rem;">
            ID объявления: #<%= ad.getId() %>
        </div>
    </div>
</div>

<script>
  // Добавляем анимацию при загрузке
  document.addEventListener('DOMContentLoaded', function() {
    const warningIcon = document.querySelector('.warning-icon');

    // Периодически повторяем анимацию тряски
    setInterval(() => {
      warningIcon.classList.remove('shake');
      void warningIcon.offsetWidth; // Trigger reflow
      warningIcon.classList.add('shake');
    }, 3000);
  });

  // Предотвращаем случайную отправку формы
  document.querySelector('form').addEventListener('submit', function(e) {
    const btn = this.querySelector('button[type="submit"]');
    btn.innerHTML = '<span class="icon">⏳</span> Удаление...';
    btn.disabled = true;
  });
</script>
</body>
</html>

<%!
    private String formatPrice(int price) {
        if (price == -1) {
            return "Договорная";
        } else if (price == 0) {
            return "Бесплатно";
        } else {
            return String.format("%,d руб.", price);
        }
    }
%>