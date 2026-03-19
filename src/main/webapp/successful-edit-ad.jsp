<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.announcement.Announcement" %>
<%@ page import="com.mipt.portal.announcement.AdvertisementStatus" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    Announcement announcement = (Announcement) request.getAttribute("announcement");
    String action = (String) request.getAttribute("action"); // "edit" или "statusChange"
%>
<html>
<head>
    <title>Объявление обновлено • Portal</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        :root {
            --primary: #4361ee;
            --primary-dark: #3a56d4;
            --secondary: #7209b7;
            --success: #4cc9f0;
            --danger: #f72585;
            --warning: #f8961e;
            --light: #f8f9fa;
            --dark: #212529;
            --gray: #6c757d;
            --border: #e9ecef;
            --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
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
            line-height: 1.6;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
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
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        }

        .logo span {
            color: var(--success);
        }

        .card {
            background: white;
            border-radius: 20px;
            padding: 40px;
            box-shadow: var(--shadow-lg);
            border: 1px solid rgba(255, 255, 255, 0.2);
            text-align: center;
        }

        .success-icon {
            font-size: 4rem;
            color: #28a745;
            margin-bottom: 20px;
        }

        .card-title {
            font-size: 2rem;
            font-weight: 700;
            color: var(--dark);
            margin-bottom: 15px;
        }

        .card-subtitle {
            color: var(--gray);
            font-size: 1.1rem;
            margin-bottom: 30px;
        }

        .announcement-info {
            background: var(--light);
            border-radius: 15px;
            padding: 25px;
            margin: 25px 0;
            text-align: left;
            border-left: 4px solid var(--success);
        }

        .info-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            padding: 8px 0;
            border-bottom: 1px solid var(--border);
        }

        .info-label {
            font-weight: 600;
            color: var(--dark);
        }

        .info-value {
            color: var(--gray);
        }

        .status-badge {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.9rem;
            font-weight: 600;
        }

        .status-draft {
            background: #fff3cd;
            color: #856404;
            border: 1px solid #ffeaa7;
        }

        .status-moderation {
            background: #cce7ff;
            color: #004085;
            border: 1px solid #b3d7ff;
        }

        .status-active {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .status-rejected {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .status-archived {
            background: #e2e3e5;
            color: #383d41;
            border: 1px solid #d6d8db;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 10px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin: 5px;
        }

        .btn-primary {
            background: linear-gradient(135deg, var(--primary), var(--secondary));
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(67, 97, 238, 0.3);
        }

        .btn-outline {
            background: transparent;
            color: var(--gray);
            border: 2px solid var(--border);
        }

        .btn-outline:hover {
            border-color: var(--primary);
            color: var(--primary);
        }

        .btn-success {
            background: #503eaf;
            color: white;
        }

        .btn-success:hover {
            background: #ab8ad1;
            transform: translateY(-2px);
        }

        .action-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
            flex-wrap: wrap;
            margin-top: 30px;
        }

        .next-steps {
            background: #e7f3ff;
            border-radius: 10px;
            padding: 20px;
            margin-top: 25px;
            text-align: left;
        }

        .next-steps h4 {
            color: var(--primary);
            margin-bottom: 10px;
        }

        .next-steps ul {
            list-style: none;
            padding-left: 0;
        }

        .next-steps li {
            padding: 5px 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .next-steps li:before {
            content: "✓";
            color: var(--success);
            font-weight: bold;
        }

        .changes-list {
            background: #fff3cd;
            border-radius: 10px;
            padding: 20px;
            margin: 20px 0;
            border-left: 4px solid var(--warning);
            text-align: left;
        }

        .changes-list h4 {
            color: #856404;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .changes-list ul {
            list-style-type: disc;
            padding-left: 20px;
        }

        .changes-list li {
            margin-bottom: 8px;
            color: #856404;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .fade-in {
            animation: fadeIn 0.6s ease;
        }

        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% {
                transform: translateY(0);
            }
            40% {
                transform: translateY(-10px);
            }
            60% {
                transform: translateY(-5px);
            }
        }

        .bounce {
            animation: bounce 2s infinite;
        }

        @keyframes pulse {
            0% {
                transform: scale(1);
            }
            50% {
                transform: scale(1.05);
            }
            100% {
                transform: scale(1);
            }
        }

        .pulse {
            animation: pulse 1.5s infinite;
        }

        .status-change-message {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #ab8ad1;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
            border-left: 4px solid #ab8ad1;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .status-change-message.warning {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            border-left: 4px solid #ffc107;
        }

        /* Адаптивность */
        @media (max-width: 768px) {
            body {
                padding: 10px;
            }

            .card {
                padding: 25px 20px;
            }

            .action-buttons {
                flex-direction: column;
            }

            .info-item {
                flex-direction: column;
                gap: 5px;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo">Portal<span></span></div>
    </div>

    <div class="card fade-in">
        <% if ("statusChange".equals(action)) { %>
        <div class="success-icon pulse">🔄</div>
        <h1 class="card-title">Статус объявления изменен!</h1>
        <p class="card-subtitle">Статус вашего объявления был успешно обновлен</p>
        <% } else { %>
        <div class="success-icon bounce">✅</div>
        <h1 class="card-title">Объявление успешно обновлено!</h1>
        <p class="card-subtitle">Изменения сохранены в системе</p>
        <% } %>

        <!-- Сообщение об изменении статуса -->
        <% if ("statusChange".equals(action) && announcement != null) { %>
        <div class="status-change-message <%= announcement.getStatus() == AdvertisementStatus.UNDER_MODERATION ? "warning" : "" %>">
            <span style="font-size: 1.5rem;">
                <% if (announcement.getStatus() == AdvertisementStatus.UNDER_MODERATION) { %>
                ⏳
                <% } else if (announcement.getStatus() == AdvertisementStatus.ACTIVE) { %>
                🎉
                <% } else if (announcement.getStatus() == AdvertisementStatus.DRAFT) { %>
                📝
                <% } %>
            </span>
            <div>
                <strong>
                    <% if (announcement.getStatus() == AdvertisementStatus.UNDER_MODERATION) { %>
                    Объявление отправлено на модерацию
                    <% } else if (announcement.getStatus() == AdvertisementStatus.ACTIVE) { %>
                    Объявление теперь активно и видно всем пользователям
                    <% } else if (announcement.getStatus() == AdvertisementStatus.DRAFT) { %>
                    Объявление сохранено как черновик
                    <% } %>
                </strong>
            </div>
        </div>
        <% } %>

        <% if (announcement != null) { %>
        <div class="announcement-info">
            <div class="info-item">
                <span class="info-label">ID объявления:</span>
                <span class="info-value">#<%= announcement.getId() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Заголовок:</span>
                <span class="info-value"><%= announcement.getTitle() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Категория:</span>
                <span class="info-value"><%= announcement.getCategory().getDisplayName() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Подкатегория:</span>
                <span class="info-value"><%= announcement.getSubcategory() != null ? announcement.getSubcategory() : "Не указана" %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Цена:</span>
                <span class="info-value">
                    <%
                        int price = announcement.getPrice();
                        if (price == -1) {
                            out.print("Договорная");
                        } else if (price == 0) {
                            out.print("Бесплатно");
                        } else {
                            out.print(String.format("%,d руб.", price));
                        }
                    %>
                </span>
            </div>
            <div class="info-item">
                <span class="info-label">Статус:</span>
                <span class="info-value">
                    <%
                        String statusClass = "";
                        String statusText = announcement.getStatus().getDisplayName();

                        switch (announcement.getStatus()) {
                            case DRAFT:
                                statusClass = "status-draft";
                                break;
                            case UNDER_MODERATION:
                                statusClass = "status-moderation";
                                break;
                            case ACTIVE:
                                statusClass = "status-active";
                                break;
                            case ARCHIVED:
                                statusClass = "status-archived";
                                break;
                            default:
                                statusClass = "status-draft";
                        }
                    %>
                    <span class="status-badge <%= statusClass %>"><%= statusText %></span>
                </span>
            </div>
            <div class="info-item">
                <span class="info-label">Дата обновления:</span>
                <span class="info-value"><%= new java.util.Date() %></span>
            </div>
        </div>
        <% } %>

        <!-- Список изменений (если предоставлен) -->
        <%
            java.util.List<String> changes = (java.util.List<String>) request.getAttribute("changes");
            if (changes != null && !changes.isEmpty()) {
        %>
        <div class="changes-list">
            <h4><span>📋</span> Внесенные изменения:</h4>
            <ul>
                <% for (String change : changes) { %>
                <li><%= change %></li>
                <% } %>
            </ul>
        </div>
        <% } %>

        <div class="next-steps">
            <h4>Что дальше?</h4>
            <ul>
                <% if (announcement != null) {
                    if (announcement.getStatus() == AdvertisementStatus.UNDER_MODERATION) {
                %>
                <li>Объявление отправлено на проверку модератору</li>
                <li>Обычно модерация занимает до 24 часов</li>
                <li>После одобрения объявление станет активным</li>
                <% } else if (announcement.getStatus() == AdvertisementStatus.ACTIVE) { %>
                <li>Объявление теперь видно всем пользователям</li>
                <li>Следите за откликами и сообщениями</li>
                <li>Вы можете редактировать объявление в любое время</li>
                <% } else if (announcement.getStatus() == AdvertisementStatus.DRAFT) { %>
                <li>Объявление сохранено как черновик</li>
                <li>Оно не видно другим пользователям</li>
                <li>Вы можете продолжить редактирование или опубликовать</li>
                <% } %>
                <li>Просматривайте статистику в личном кабинете</li>
                <li>Обновляйте информацию по мере необходимости</li>
                <% if (announcement != null && announcement.getStatus() != AdvertisementStatus.ACTIVE) { %>
                <li>Для публикации измените статус на "На модерации"</li>
                <% } %>
                <% } %>
            </ul>
        </div>

        <div class="action-buttons">
            <% if (announcement != null) { %>

            <% } %>
            <a href="dashboard.jsp" class="btn btn-primary">
                <span>📋</span> К моим объявлениям
            </a>
            <a href="home.jsp" class="btn btn-success">
                <span></span> На главную
            </a>
        </div>
    </div>
</div>

<script>
    // Автоматический редирект на dashboard через 30 секунд
    setTimeout(function () {
        window.location.href = 'dashboard.jsp';
    }, 30000);

    // Сообщение при попытке уйти со страницы
    window.addEventListener('beforeunload', function (e) {
        // Не блокируем переход, но можно добавить сообщение
        // e.preventDefault();
        // e.returnValue = 'Вы уверены, что хотите покинуть страницу? Изменения сохранены.';
    });

    // Плавная анимация при загрузке
    document.addEventListener('DOMContentLoaded', function() {
        const card = document.querySelector('.card');
        if (card) {
            card.classList.add('fade-in');
        }
    });
</script>
</body>
</html>
