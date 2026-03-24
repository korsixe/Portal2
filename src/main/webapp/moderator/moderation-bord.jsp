<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - Кабинет модератора</title>
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
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        .header {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 30px;
            margin-bottom: 30px;
            text-align: center;
        }

        .portal-logo {
            font-size: 3rem;
            font-weight: 800;
            background: linear-gradient(135deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
        }

        .reason-modal-content .modal-icon,
        .reason-modal-content .modal-title {
            text-align: center;
            display: block;
            margin-left: auto;
            margin-right: auto;
        }

        .moderator-info {
            background: linear-gradient(135deg, #ff6b6b, #ee5a24);
            color: white;
            padding: 15px;
            border-radius: 15px;
            margin: 20px 0;
        }

        .content {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 30px;
            margin-bottom: 30px;
        }

        .section-title {
            color: #333;
            margin-bottom: 20px;
            font-size: 1.5rem;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }

        .stats-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            padding: 20px;
            border-radius: 15px;
            text-align: center;
        }

        .stat-number {
            font-size: 2rem;
            font-weight: bold;
            margin-bottom: 5px;
        }

        .stat-label {
            font-size: 0.9rem;
            opacity: 0.9;
        }

        .ads-list {
            display: grid;
            gap: 20px;
            margin-bottom: 30px;
        }

        .ad-card {
            border: 2px solid #e1e5e9;
            border-radius: 15px;
            padding: 20px;
            transition: all 0.3s ease;
            background: #f8f9fa;
            display: grid;
            grid-template-columns: 300px 1fr;
            gap: 25px;
            align-items: flex-start;
        }

        .ad-card:hover {
            border-color: #667eea;
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.1);
            transform: translateY(-2px);
        }

        .ad-photo-section {
            flex-shrink: 0;
        }

        .ad-photo-container {
            width: 300px;
            height: 250px;
            border-radius: 10px;
            overflow: hidden;
            border: 2px solid #e1e5e9;
            background: #f8f9fa;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }

        .ad-photo {
            width: 100%;
            height: 100%;
            object-fit: contain;
            background: white;
        }

        .photo-placeholder {
            font-size: 3rem;
            color: #ccc;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            gap: 10px;
        }

        .photo-counter {
            position: absolute;
            bottom: 10px;
            right: 10px;
            background: rgba(0,0,0,0.7);
            color: white;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 0.85rem;
            font-weight: 500;
        }

        .ad-content {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .ad-title {
            font-size: 1.4rem; 
            font-weight: 600;
            color: #333;
            margin-bottom: 15px; 
            line-height: 1.3;
        }

        .ad-meta {
            color: #666;
            font-size: 0.95rem; 
            margin-bottom: 15px;
            display: flex;
            flex-wrap: wrap;
            gap: 12px; 
        }

        .ad-meta span {
            background: #e9ecef;
            padding: 6px 12px; 
            border-radius: 8px; 
            font-size: 0.9rem;
        }

        .ad-price {
            font-size: 1.5rem; 
            font-weight: 700;
            color: #667eea;
            margin-bottom: 12px; 
            padding: 8px 0;
        }

        .ad-location {
            color: #666;
            font-size: 0.95rem; 
            margin-bottom: 12px; 
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .ad-description {
            color: #555;
            line-height: 1.6; /* Улучшено */
            margin-bottom: 15px;
            padding: 18px; 
            background: white;
            border-radius: 8px;
            border-left: 4px solid #667eea;
            flex: 1;
            min-height: 120px;
            max-height: 150px;
            overflow-y: auto;
            font-size: 0.95rem; 
        }

        .ad-description::-webkit-scrollbar {
            width: 8px; 
        }

        .ad-description::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 4px;
        }

        .ad-description::-webkit-scrollbar-thumb {
            background: #667eea;
            border-radius: 4px;
        }

        .ad-footer {
            margin-top: auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px; 
            padding-top: 12px;
            border-top: 1px solid #e1e5e9;
        }

        .ad-views {
            color: #666;
            font-size: 0.9rem; 
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .ad-date {
            color: #999;
            font-size: 0.9rem; 
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .moderation-actions {
            display: flex;
            gap: 12px; 
            flex-wrap: wrap;
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px solid #e1e5e9;
            grid-column: 1 / -1;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 1rem; 
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
            text-align: center;
            flex: 1;
            min-width: 140px; 
        }

        .btn-approve {
            background: #28a745;
            color: white;
        }

        .btn-approve:hover {
            background: #218838;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(40, 167, 69, 0.3);
        }

        .btn-reject {
            background: #ffc107;
            color: #212529;
        }

        .btn-reject:hover {
            background: #e0a800;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(255, 193, 7, 0.3);
        }

        .btn-delete {
            background: #dc3545;
            color: white;
        }

        .btn-delete:hover {
            background: #c82333;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(220, 53, 69, 0.3);
        }

        .btn-logout {
            background: #667eea;
            color: white;
            border: 2px solid #667eea;
        }

        .btn-logout:hover {
            background: #5a6fd8;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
        }

        .btn-home {
            background: #17a2b8;
            color: white;
            border: 2px solid #17a2b8;
        }

        .btn-home:hover {
            background: #138496;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(23, 162, 184, 0.3);
        }

        .empty-state {
            text-align: center;
            color: #666;
            padding: 60px 20px;
            font-size: 1.1rem;
        }

        .navigation {
            display: flex;
            justify-content: flex-end;
            margin-bottom: 20px;
            flex-wrap: wrap;
            gap: 10px;
        }

        .nav-group {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .status-badge {
            display: inline-block;
            padding: 6px 14px; 
            border-radius: 20px;
            font-size: 0.9rem; 
            font-weight: 600;
            margin-left: 12px; 
        }

        .status-pending {
            background: #fff3cd;
            color: #856404;
            border: 1px solid #ffeaa7;
        }

        /* Модальное окно */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            animation: fadeIn 0.3s ease;
        }

        .modal-content {
            background-color: white;
            margin: 15% auto;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.2);
            width: 400px;
            max-width: 90%;
            text-align: center;
            animation: slideIn 0.3s ease;
        }

        /* Стили для модального окна причины */
        .reason-modal-content {
            background-color: white;
            margin: 5% auto;
            padding: 30px;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.2);
            width: 500px;
            max-width: 90%;
            animation: slideIn 0.3s ease;
        }

        .reason-section {
            margin-bottom: 25px;
            text-align: left;
        }

        .reason-title {
            font-size: 1.1rem;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
            text-align: center;
        }

        .reason-buttons {
            display: grid;
            gap: 10px;
            margin-bottom: 20px;
        }

        .reason-btn {
            padding: 12px 15px;
            border: 2px solid #e1e5e9;
            border-radius: 8px;
            background: white;
            color: #333;
            font-size: 0.9rem;
            cursor: pointer;
            transition: all 0.3s ease;
            text-align: left;
        }

        .reason-btn:hover {
            border-color: #667eea;
            background-color: #f8f9ff;
            transform: translateY(-2px);
        }

        .reason-btn.selected {
            border-color: #667eea;
            background-color: #667eea;
            color: white;
        }

        .custom-reason-section {
            margin-top: 20px;
        }

        .custom-reason-input {
            width: 100%;
            padding: 12px;
            border: 2px solid #e1e5e9;
            border-radius: 8px;
            font-size: 0.9rem;
            resize: vertical;
            min-height: 80px;
            font-family: inherit;
        }

        .custom-reason-input:focus {
            outline: none;
            border-color: #667eea;
        }

        .reason-required {
            color: #dc3545;
            font-size: 0.8rem;
            margin-top: 5px;
            display: none;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @keyframes slideIn {
            from { transform: translateY(-50px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }

        .modal-icon {
            font-size: 3rem;
            margin-bottom: 15px;
        }

        .modal-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
        }

        .modal-message {
            color: #666;
            margin-bottom: 25px;
            line-height: 1.5;
        }

        .modal-actions {
            display: flex;
            gap: 15px;
            justify-content: center;
        }

        .modal-btn {
            padding: 12px 25px;
            border: none;
            border-radius: 10px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            min-width: 120px;
        }

        .modal-btn-confirm {
            background: #dc3545;
            color: white;
        }

        .modal-btn-confirm.approve {
            background: #28a745;
        }

        .modal-btn-confirm.reject {
            background: #ffc107;
            color: #212529;
        }

        .modal-btn-confirm:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }

        .modal-btn-cancel {
            background: #6c757d;
            color: white;
        }

        .modal-btn-cancel:hover {
            background: #5a6268;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
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

        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 25px;
            border-radius: 10px;
            color: white;
            font-weight: 600;
            z-index: 1001;
            animation: slideInRight 0.3s ease;
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }

        @keyframes slideInRight {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }

        .notification.success {
            background: #28a745;
        }

        .notification.error {
            background: #dc3545;
        }

        .notification.info {
            background: #17a2b8;
        }

        @media (max-width: 1024px) {
            .ad-card {
                grid-template-columns: 250px 1fr;
                gap: 20px;
            }

            .ad-photo-container {
                width: 250px;
                height: 200px;
            }
        }

        @media (max-width: 768px) {
            .navigation {
                flex-direction: column;
                align-items: center;
            }

            .nav-group {
                justify-content: center;
                width: 100%;
            }

            .moderation-actions {
                flex-direction: column;
            }

            .btn {
                width: 100%;
            }

            .stats-cards {
                grid-template-columns: 1fr;
            }

            .modal-actions {
                flex-direction: column;
            }

            .modal-btn {
                width: 100%;
            }

            .reason-buttons {
                grid-template-columns: 1fr;
            }

            .ad-card {
                grid-template-columns: 1fr;
                gap: 20px;
            }

            .ad-photo-container {
                width: 100%;
                height: 300px; /* Увеличено для мобильных */
            }

            .ad-title {
                font-size: 1.3rem;
            }
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
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="portal-logo">PORTAL</div>
        <div class="moderator-info">
            <h2>Кабинет модератора</h2>
            <p><%= session.getAttribute("moderatorEmail") != null ? session.getAttribute("moderatorEmail") : "Модератор" %></p>
        </div>
    </div>

    <div class="content">
        <c:if test="${not empty message}">
            <div class="notification ${messageType}" style="display:block; margin-bottom: 10px;">${message}</div>
        </c:if>

        <div class="navigation">
            <div class="nav-group">
                <a href="${pageContext.request.contextPath}/home.jsp" class="btn btn-primary">На главную</a>
                <a href="${pageContext.request.contextPath}/dashboard.jsp" class="btn btn-secondary">Личный кабинет</a>
                <a href="login-moderator.jsp?logout=true" class="btn btn-secondary">Выйти</a>
            </div>
        </div>

        <h2 class="section-title">Панель модерации</h2>

        <div class="stats-cards">
            <div class="stat-card">
                <div class="stat-number">${ads != null ? ads.size() : 0}</div>
                <div class="stat-label">Ожидают модерации</div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #1f2937, #111827);">
                <div class="stat-number">${stats.totalUsers}</div>
                <div class="stat-label">Всего пользователей</div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #2563eb, #1d4ed8);">
                <div class="stat-number">${stats.adminCount}</div>
                <div class="stat-label">Администраторов</div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #059669, #047857);">
                <div class="stat-number">${stats.moderatorCount}</div>
                <div class="stat-label">Модераторов</div>
            </div>
        </div>

        <c:if test="${not empty moderator}">
        <div class="moderator-info" style="background: linear-gradient(135deg, #6366f1, #8b5cf6); margin-bottom: 20px;">
            <h3 style="margin-bottom: 8px;">Ваш профиль (как у обычного пользователя)</h3>
            <p style="margin: 4px 0;">${moderator.name} — ${moderator.email}</p>
            <p style="margin: 4px 0;">Монеты: ${moderator.coins} · Роли:
                <c:forEach var="role" items="${moderator.roles}">
                    <span style="padding: 2px 8px; border-radius: 10px; background: rgba(255,255,255,0.15); margin-right: 6px;">${role.displayName}</span>
                </c:forEach>
            </p>
            <div style="margin-top: 8px; display:flex; gap:10px; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/dashboard.jsp" class="btn btn-home">Открыть личный кабинет</a>
                <a href="${pageContext.request.contextPath}/home.jsp" class="btn btn-secondary">Лента объявлений</a>
            </div>
        </div>
        </c:if>

        <h3 class="section-title">Объявления на модерации</h3>

        <c:if test="${empty ads}">
        <div class="empty-state">
            <div>📋</div>
            <p>Нет объявлений для модерации</p>
            <p style="margin-top: 10px; font-size: 0.9rem; opacity: 0.7;">Все объявления проверены и обработаны</p>
        </div>
        </c:if>

        <c:if test="${not empty ads}">
        <div class="ads-list">
            <c:forEach var="ad" items="${ads}">
            <div class="ad-card">
                <div class="ad-photo-section">
                    <div class="ad-photo-container">
                        <c:if test="${not empty ad.photoUrls}">
                             <img src="${ad.photoUrls[0]}" class="ad-photo">
                        </c:if>
                        <c:if test="${empty ad.photoUrls}">
                        <div class="photo-placeholder">
                            <span style="font-size: 3rem;">📷</span>
                            <span style="font-size: 0.9rem; margin-top: 5px;">Нет фото</span>
                        </div>
                        </c:if>
                    </div>
                </div>

                <div class="ad-content">
                    <div class="ad-title">
                        ${ad.title}
                        <span class="status-badge status-pending">На модерации</span>
                    </div>

                    <div class="ad-meta">
                        <span>Категория: ${ad.category.displayName}</span>
                        <span>Подкатегория: ${ad.subcategory}</span>
                        <span>Дата: <fmt:formatDate value="${ad.createdAtDate}" pattern="dd.MM.yyyy HH:mm" /></span>
                    </div>

                    <div class="ad-price">
                        ${ad.price} руб.
                    </div>

                    <div class="ad-location">
                        <span style="font-size: 1.1rem;">📍</span> ${ad.location}
                    </div>

                    <div class="ad-description">
                        ${ad.description}
                    </div>

                    <div class="ad-footer">
                        <div class="ad-views">
                            <span style="font-size: 1.1rem;">👁️</span> ${ad.viewCount} просмотров
                        </div>
                    </div>

                    <div class="moderation-actions">
                        <button type="button" class="btn btn-approve" data-action="approve" data-ad-id="${ad.id}" data-ad-title="${ad.title}">
                            Одобрить
                        </button>

                        <button type="button" class="btn btn-reject" data-action="reject" data-ad-id="${ad.id}" data-ad-title="${ad.title}">
                            Отозвать на доработку
                        </button>
                    </div>
                </div>
            </div>
            </c:forEach>
        </div>
        </c:if>
    </div>
</div>

<!-- Модальное окно подтверждения -->
<div id="confirmationModal" class="modal">
    <div class="modal-content">
        <div class="modal-icon" id="modalIcon">❓</div>
        <h3 class="modal-title" id="modalTitle">Подтверждение действия</h3>
        <p class="modal-message" id="modalMessage">Вы уверены, что хотите выполнить это действия?</p>
        <div class="modal-actions">
            <button type="button" class="modal-btn modal-btn-cancel" id="modalCancel">Отменить</button>
            <button type="button" class="modal-btn modal-btn-confirm" id="modalConfirm">Подтвердить</button>
        </div>
    </div>
</div>

<!-- Модальное окно выбора причины -->
<div id="reasonModal" class="modal">
    <div class="reason-modal-content">
        <div class="modal-icon" id="reasonModalIcon">📝</div>
        <h3 class="modal-title" id="reasonModalTitle">Выберите причину</h3>

        <div class="reason-section">
            <div class="reason-title" id="reasonSubtitle">Выберите одну из причин:</div>

            <!-- Причины для отзыва -->
            <div class="reason-buttons" id="rejectReasons" style="display: none;">
                <button type="button" class="reason-btn" data-reason="Неполная или некорректная информация">Неполная или некорректная информация</button>
                <button type="button" class="reason-btn" data-reason="Несоответствие категории, подкатегории, тегам">Несоответствие категории, подкатегории, тегам</button>
                <button type="button" class="reason-btn" data-reason="Нарушение правил платформы">Нарушение правил платформы</button>

            </div>

            <!-- Причины для удаления -->
            <div class="reason-buttons" id="deleteReasons" style="display: none;">
                <button type="button" class="reason-btn" data-reason="Нарушение правил платформы">Нарушение правил платформы</button>
                <button type="button" class="reason-btn" data-reason="Мошенничество или обман">Мошенничество или обман</button>
                <button type="button" class="reason-btn" data-reason="Нецензурная лексика, оскорбления">Нецензурная лексика, оскорбления</button>
                <button type="button" class="reason-btn" data-reason="Спам">Спам</button>
            </div>

            <div class="custom-reason-section">
                <div class="reason-title">Или введите свою причину:</div>
                <textarea class="custom-reason-input" id="customReasonInput" placeholder="Введите свою причину..."></textarea>
            </div>

            <div class="reason-required" id="reasonRequired">Пожалуйста, выберите причину или введите свою</div>
        </div>

        <div class="modal-actions">
            <button type="button" class="modal-btn modal-btn-cancel" id="reasonModalCancel">Отменить</button>
            <button type="button" class="modal-btn modal-btn-confirm" id="reasonModalConfirm">Продолжить</button>
        </div>
    </div>
</div>

<!-- Уведомление -->
<div id="notification" class="notification" style="display: none;"></div>

<script>
    const basePath = '${pageContext.request.contextPath}';
    const serverMessage = '<c:out value="${message}" />';
    const serverMessageType = '<c:out value="${messageType}" />';

    // Переменные для хранения текущего действия
    let currentAction = null;
    let currentAdId = null;
    let currentAdTitle = null;
    let currentReason = '';

    // Элементы модальных окон
    const modal = document.getElementById('confirmationModal');
    const reasonModal = document.getElementById('reasonModal');
    const modalIcon = document.getElementById('modalIcon');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const modalConfirm = document.getElementById('modalConfirm');
    const modalCancel = document.getElementById('modalCancel');

    const reasonModalIcon = document.getElementById('reasonModalIcon');
    const reasonModalTitle = document.getElementById('reasonModalTitle');
    const reasonSubtitle = document.getElementById('reasonSubtitle');
    const rejectReasons = document.getElementById('rejectReasons');
    const deleteReasons = document.getElementById('deleteReasons');
    const customReasonInput = document.getElementById('customReasonInput');
    const reasonRequired = document.getElementById('reasonRequired');
    const reasonModalConfirm = document.getElementById('reasonModalConfirm');
    const reasonModalCancel = document.getElementById('reasonModalCancel');

    const notification = document.getElementById('notification');

    // Тексты для разных действий
    const actionConfigs = {
        approve: {
            icon: '✅',
            title: 'Одобрение объявления',
            message: (title) => `Вы уверены, что хотите одобрить объявление "${title}"?`,
            confirmClass: 'approve',
            successMessage: 'Объявление успешно одобрено'
        },
        reject: {
            icon: '⚠️',
            title: 'Отозвать объявление',
            message: (title) => `Вы уверены, что хотите отозвать объявление "${title}" на доработку?`,
            confirmClass: 'reject',
            successMessage: 'Объявление отозвано на доработку'
        },
        delete: {
            icon: '🗑️',
            title: 'Удаление объявления',
            message: (title) => `Вы уверены, что хотите удалить объявление "${title}"? Это действие нельзя отменить.`,
            confirmClass: 'delete',
            successMessage: 'Объявление удалено'
        }
    };

    // Инициализация после загрузки DOM
    document.addEventListener('DOMContentLoaded', function() {
        console.log('DOM loaded, initializing moderation buttons...');

        // Обработчики для кнопок действий
        const actionButtons = document.querySelectorAll('[data-action]');
        console.log('Found action buttons:', actionButtons.length);

        actionButtons.forEach(button => {
            button.addEventListener('click', function() {
                currentAction = this.getAttribute('data-action');
                currentAdId = this.getAttribute('data-ad-id');
                currentAdTitle = this.getAttribute('data-ad-title');

                console.log('Button clicked:', { currentAction, currentAdId, currentAdTitle });

                if (currentAction === 'approve') {
                    // Для одобрения сразу показываем подтверждение
                    showConfirmationModal(currentAction, currentAdTitle);
                } else {
                    // Для отзыва и удаления показываем окно выбора причины
                    showReasonModal(currentAction, currentAdTitle);
                }
            });
        });

        // Обработчики для модальных окон
        modalConfirm.addEventListener('click', function() {
            console.log('Confirm button clicked, executing action...');
            executeAction();
        });

        modalCancel.addEventListener('click', closeModal);

        reasonModalConfirm.addEventListener('click', confirmWithReason);
        reasonModalCancel.addEventListener('click', closeReasonModal);

        // Обработчики для кнопок причин
        document.querySelectorAll('.reason-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                // Снимаем выделение со всех кнопок
                document.querySelectorAll('.reason-btn').forEach(b => b.classList.remove('selected'));
                // Выделяем текущую кнопку
                this.classList.add('selected');
                // Устанавливаем причину
                currentReason = this.getAttribute('data-reason');
                // Очищаем кастомное поле
                customReasonInput.value = '';
                reasonRequired.style.display = 'none';
            });
        });

        // Обработчик для кастомного ввода
        customReasonInput.addEventListener('input', function() {
            if (this.value.trim()) {
                // Снимаем выделение с кнопок
                document.querySelectorAll('.reason-btn').forEach(b => b.classList.remove('selected'));
                currentReason = this.value.trim();
                reasonRequired.style.display = 'none';
            }
        });

        // Закрытие модальных окон при клике вне их
        window.addEventListener('click', function(event) {
            if (event.target === modal) {
                closeModal();
            }
            if (event.target === reasonModal) {
                closeReasonModal();
            }
        });

        // Закрытие по ESC
        window.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                closeModal();
                closeReasonModal();
            }
        });

        // Показываем серверное сообщение, если есть
        if (serverMessage && serverMessage.trim().length > 0) {
            showNotification(serverMessage, serverMessageType || 'info');
        }
    });

    // Показать модальное окно подтверждения
    function showConfirmationModal(action, adTitle) {
        const config = actionConfigs[action];

        modalIcon.textContent = config.icon;
        modalTitle.textContent = config.title;
        modalMessage.textContent = config.message(adTitle);

        // Устанавливаем класс для кнопки подтверждения
        modalConfirm.className = 'modal-btn modal-btn-confirm';
        modalConfirm.classList.add(config.confirmClass);

        modal.style.display = 'block';
    }

    // Показать модальное окно выбора причины
    function showReasonModal(action, adTitle) {
        const config = actionConfigs[action];

        reasonModalIcon.textContent = config.icon;
        reasonModalTitle.textContent = config.title;

        // Показываем соответствующие причины
        if (action === 'reject') {
            rejectReasons.style.display = 'grid';
            deleteReasons.style.display = 'none';
        } else if (action === 'delete') {
            rejectReasons.style.display = 'none';
            deleteReasons.style.display = 'grid';
        }

        // Сбрасываем форму
        document.querySelectorAll('.reason-btn').forEach(btn => btn.classList.remove('selected'));
        customReasonInput.value = '';
        currentReason = '';
        reasonRequired.style.display = 'none';

        reasonModal.style.display = 'block';
    }

    // Закрыть модальное окно подтверждения
    function closeModal() {
        modal.style.display = 'none';
    }

    // Закрыть модальное окно причины
    function closeReasonModal() {
        reasonModal.style.display = 'none';
    }

    // Сбросить текущее действие
    function resetCurrentAction() {
        currentAction = null;
        currentAdId = null;
        currentAdTitle = null;
        currentReason = '';
    }

    // Подтвердить действие с причиной
    function confirmWithReason() {
        if (!currentReason && !customReasonInput.value.trim()) {
            reasonRequired.style.display = 'block';
            return;
        }

        // Если выбрана кастомная причина
        if (!currentReason && customReasonInput.value.trim()) {
            currentReason = customReasonInput.value.trim();
        }

        // Показываем финальное подтверждение
        showConfirmationModal(currentAction, currentAdTitle);
        closeReasonModal();
    }

    // Выполнить действие - ИСПРАВЛЕННАЯ ВЕРСИЯ
    function executeAction() {
        // Сохраняем значения в локальные переменные перед проверками
        const action = currentAction;
        const adId = currentAdId;
        const reason = currentReason;
        const adTitle = currentAdTitle;

        console.log('Executing action with data:', { action, adId, reason, adTitle });

        if (!action || !adId) {
            console.error('Missing action or adId', { action, adId, reason });
            showNotification('Ошибка: отсутствуют данные', 'error');
            return;
        }

        // Для reject и delete проверяем наличие причины
        if ((action === 'reject' || action === 'delete') && !reason) {
            showNotification('Пожалуйста, укажите причину', 'error');
            // Восстанавливаем значения для показа модального окна причины
            currentAction = action;
            currentAdId = adId;
            currentAdTitle = adTitle;
            showReasonModal(action, adTitle);
            return;
        }

        // Создаем и отправляем форму
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = basePath + '/moderator/' + action;
        form.style.display = 'none';

        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = action;

        const adIdInput = document.createElement('input');
        adIdInput.type = 'hidden';
        adIdInput.name = 'adId';
        adIdInput.value = adId;

        form.appendChild(actionInput);
        form.appendChild(adIdInput);

        // Добавляем причину только для reject и delete
        if (action === 'reject' || action === 'delete') {
            const reasonInput = document.createElement('input');
            reasonInput.type = 'hidden';
            reasonInput.name = 'reason';
            reasonInput.value = reason;
            form.appendChild(reasonInput);
        }

        document.body.appendChild(form);

        console.log('Form data to submit:', {
            action: action,
            adId: adId,
            reason: reason
        });

        // Показываем уведомление
        showNotification('Выполняется...', 'info');

        // Закрываем модальное окно
        closeModal();

        // Сбрасываем переменные ПОСЛЕ использования
        resetCurrentAction();

        // Отправляем форму
        setTimeout(() => {
            console.log('Submitting form...');
            form.submit();
        }, 500);
    }

    // Показать уведомление
    function showNotification(message, type) {
        notification.textContent = message;
        notification.className = `notification ${type}`;
        notification.style.display = 'block';

        // Автоматически скрыть через 3 секунды
        setTimeout(() => {
            notification.style.display = 'none';
        }, 3000);
    }

    // Обработка ошибок загрузки фото
    function handlePhotoError(img) {
        console.error('Error loading photo');
        img.style.display = 'none';
        const placeholder = img.nextElementSibling;
        if (placeholder && placeholder.classList.contains('photo-placeholder')) {
            placeholder.style.display = 'flex';
            placeholder.style.alignItems = 'center';
            placeholder.style.justifyContent = 'center';
            placeholder.style.flexDirection = 'column';
            placeholder.style.fontSize = '1.5rem';
            placeholder.innerHTML = '<span style="font-size: 3rem;"></span><span style="font-size: 0.9rem; margin-top: 10px;">Ошибка загрузки</span>';
        }
    }

    // Авто-обновление страницы каждые 30 секунд для проверки новых объявлений
    setTimeout(function() {
        window.location.reload();
    }, 30000);
</script>
</body>
</html>

<%!
    // Универсальная функция форматирования даты
    private String formatDate(Object dateObj) {
        if (dateObj == null) return "Не указано";

        try {
            if (dateObj instanceof java.time.Instant) {
                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                .withZone(java.time.ZoneId.systemDefault());
                return formatter.format((java.time.Instant) dateObj);
            } else if (dateObj instanceof java.sql.Timestamp) {
                java.text.SimpleDateFormat formatter =
                        new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
                return formatter.format((java.sql.Timestamp) dateObj);
            } else if (dateObj instanceof java.util.Date) {
                java.text.SimpleDateFormat formatter =
                        new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
                return formatter.format((java.util.Date) dateObj);
            } else {
                return dateObj.toString();
            }
        } catch (Exception e) {
            return dateObj.toString();
        }
    }
%>