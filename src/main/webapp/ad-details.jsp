<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.Announcement" %>
<%@ page import="com.mipt.portal.entity.Comment" %>
<%@ page import="com.mipt.portal.entity.User" %>
<%@ page import="com.mipt.portal.service.AnnouncementService" %>
<%@ page import="com.mipt.portal.enums.Category" %>
<%@ page import="com.mipt.portal.enums.Condition" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.sql.*" %>

<%
    Object sessionUserObj = session.getAttribute("user");
    User sessionUser = sessionUserObj instanceof User ? (User) sessionUserObj : null;
    if (sessionUserObj != null && sessionUser == null) {
        session.invalidate();
    }

    String adIdParam = request.getParameter("id");
    Announcement announcement = null;
    List<Comment> comments = new ArrayList<>();
    String authorName = "Неизвестный пользователь";
    int photoCount = 0;

    WebApplicationContext appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(application);
    AnnouncementService announcementService = appContext.getBean(AnnouncementService.class);

    if (adIdParam != null && !adIdParam.trim().isEmpty()) {
        try {
            Long adId = Long.parseLong(adIdParam);
            announcement = announcementService.findById(adId);

            if (announcement != null) {
                authorName = announcementService.getAuthorName(announcement.getAuthorId());
                photoCount = announcementService.getPhotoCount(adId);
            }

        } catch (NumberFormatException e) {
            System.err.println("Неверный формат ID объявления: " + adIdParam);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    if ("POST".equalsIgnoreCase(request.getMethod()) && "addComment".equals(request.getParameter("action"))) {
        User user = sessionUser;

        if (user == null) {
            request.setAttribute("error", "Для добавления комментария необходимо авторизоваться");
        } else if (announcement == null) {
            request.setAttribute("error", "Объявление не найдено");
        } else {
            String commentText = request.getParameter("commentText");

            if (commentText == null || commentText.trim().isEmpty()) {
                request.setAttribute("error", "Комментарий не может быть пустым");
            } else {
                try {
                    announcementService.addComment(announcement.getId(), user.getId(), user.getName(), commentText.trim());
                    comments = announcementService.getCommentsByAdId(announcement.getId());
                    request.setAttribute("clearComment", "true");
                    request.setAttribute("success", "Комментарий успешно добавлен!");
                } catch (Exception e) {
                    System.err.println("Ошибка при создании комментария: " + e.getMessage());
                    request.setAttribute("error", "Ошибка при сохранении комментария");
                }
            }
        }
    } else if (announcement != null) {
        comments = announcementService.getCommentsByAdId(announcement.getId());
    }
%>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal - <%= announcement != null ? announcement.getTitle() : "Объявление" %></title>
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
            max-width: 1400px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: 1fr;
            gap: 30px;
        }

        .header {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 30px 40px;
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 20px;
        }

        .portal-logo {
            font-size: 2.5rem;
            font-weight: 800;
            background: linear-gradient(135deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            letter-spacing: 2px;
        }

        .auth-buttons {
            display: flex;
            gap: 10px;
        }

        .main-content {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 30px;
        }

        .ad-card {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 40px;
            margin-bottom: 0;
        }

        .comments-section {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            padding: 30px;
            height: fit-content;
            position: sticky;
            top: 20px;
        }

        .ad-header {
            margin-bottom: 30px;
            padding-bottom: 25px;
            border-bottom: 1px solid #e9ecef;
        }

        .ad-title {
            font-size: 2.2rem;
            font-weight: 700;
            color: #333;
            margin-bottom: 15px;
            line-height: 1.3;
        }

        .ad-price {
            font-size: 2rem;
            font-weight: 700;
            color: #667eea;
            margin-bottom: 20px;
        }

        .ad-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 15px;
        }

        .meta-badge {
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.9rem;
            font-weight: 500;
        }

        .category-badge {
            background: #667eea;
            color: white;
        }

        .condition-badge {
            background: #28a745;
            color: white;
        }

        .location-badge {
            background: #6c757d;
            color: white;
        }

        .author-badge {
            background: #17a2b8;
            color: white;
        }

        .section-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #333;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .photos-section {
            margin-bottom: 30px;
            padding-bottom: 25px;
            border-bottom: 1px solid #e9ecef;
        }

        .main-photo {
            width: 100%;
            height: 400px;
            background: #f8f9fa;
            border-radius: 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 15px;
            overflow: hidden;
            position: relative;
        }

        .main-photo img {
            width: 100%;
            height: 100%;
            object-fit: contain;
            background: white;
        }

        .photo-placeholder {
            font-size: 4rem;
            color: #ccc;
        }

        .photo-counter {
            position: absolute;
            top: 15px;
            right: 15px;
            background: rgba(0,0,0,0.7);
            color: white;
            padding: 5px 10px;
            border-radius: 15px;
            font-size: 0.8rem;
        }

        .photo-navigation {
            position: absolute;
            bottom: 15px;
            left: 50%;
            transform: translateX(-50%);
            display: flex;
            gap: 10px;
        }

        .nav-btn {
            background: rgba(0,0,0,0.7);
            color: white;
            border: none;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2rem;
        }

        .nav-btn:hover {
            background: rgba(0,0,0,0.9);
        }

        .nav-btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        .photo-thumbnails {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
            gap: 10px;
            margin-top: 15px;
        }

        .thumbnail {
            width: 80px;
            height: 80px;
            background: #f8f9fa;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            overflow: hidden;
            border: 3px solid transparent;
            transition: all 0.3s ease;
        }

        .thumbnail:hover {
            border-color: #667eea;
        }

        .thumbnail.active {
            border-color: #667eea;
            box-shadow: 0 0 0 2px #667eea;
        }

        .thumbnail img {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }

        .ad-description {
            margin-bottom: 30px;
            padding-bottom: 25px;
            border-bottom: 1px solid #e9ecef;
        }

        .description-text {
            color: #666;
            line-height: 1.6;
            font-size: 1.1rem;
        }

        .tags-section {
            margin-bottom: 30px;
            padding-bottom: 25px;
            border-bottom: 1px solid #e9ecef;
        }

        .tags-container {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
        }

        .tag {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            padding: 6px 12px;
            border-radius: 15px;
            font-size: 0.9rem;
            font-weight: 500;
            box-shadow: 0 2px 5px rgba(102, 126, 234, 0.3);
            transition: all 0.3s ease;
        }

        .tag:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(102, 126, 234, 0.4);
        }

        .ad-info {
            margin-bottom: 30px;
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 15px;
        }

        .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid #f1f3f4;
        }

        .info-label {
            color: #666;
            font-weight: 500;
        }

        .info-value {
            color: #333;
            font-weight: 600;
        }

        .action-buttons {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
        }

        .comment-form {
            margin-bottom: 25px;
            padding-bottom: 25px;
            border-bottom: 1px solid #e9ecef;
        }

        .comment-input {
            width: 100%;
            padding: 15px;
            border: 2px solid #e1e5e9;
            border-radius: 12px;
            font-size: 1rem;
            resize: vertical;
            min-height: 100px;
            margin-bottom: 15px;
            transition: all 0.3s ease;
            font-family: inherit;
        }

        .comment-input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .comments-list {
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        .comment-item {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 20px;
            border-left: 4px solid #667eea;
        }

        .comment-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .comment-author {
            font-weight: 600;
            color: #333;
        }

        .comment-date {
            color: #666;
            font-size: 0.9rem;
        }

        .comment-text {
            color: #555;
            line-height: 1.5;
        }

        .no-comments {
            text-align: center;
            padding: 40px 20px;
            color: #666;
        }

        .btn {
            padding: 12px 25px;
            border: none;
            border-radius: 10px;
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

        .fade-in {
            opacity: 0;
            animation: fadeInUp 0.6s ease-out forwards;
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

        @media (max-width: 1024px) {
            .main-content {
                grid-template-columns: 1fr;
                gap: 20px;
            }
            .comments-section {
                position: static;
                order: 2;
            }
            .ad-card {
                order: 1;
            }
        }

        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                text-align: center;
            }
            .auth-buttons {
                justify-content: center;
            }
            .ad-title {
                font-size: 1.8rem;
            }
            .ad-price {
                font-size: 1.6rem;
            }
            .action-buttons {
                flex-direction: column;
            }
            .main-photo {
                height: 300px;
            }
            .info-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 480px) {
            .header {
                padding: 20px;
            }
            .ad-card {
                padding: 25px 20px;
            }
            .comments-section {
                padding: 20px;
            }
            .portal-logo {
                font-size: 2rem;
            }
            .btn {
                padding: 10px 20px;
                font-size: 0.9rem;
            }
            .photo-thumbnails {
                grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
            }
            .thumbnail {
                width: 60px;
                height: 60px;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="portal-logo">PORTAL</div>
        <div class="auth-buttons">
            <% if (sessionUser != null) { %>
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-primary">Личный кабинет</a>
            <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary">Выйти</a>
            <% } else { %>
            <a href="<%= request.getContextPath() %>/login" class="btn btn-secondary">Войти</a>
            <a href="<%= request.getContextPath() %>/register" class="btn btn-primary">Регистрация</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/home" class="btn btn-secondary">На главную</a>
        </div>
    </div>

    <% if (announcement == null) { %>
    <div class="ad-card fade-in">
        <div style="text-align: center; padding: 60px 20px;">
            <div style="font-size: 4rem; margin-bottom: 20px; opacity: 0.5;">🔍</div>
            <h2 style="color: #333; margin-bottom: 15px;">Объявление не найдено</h2>
            <p style="color: #666; margin-bottom: 30px;">Запрошенное объявление не существует или было удалено.</p>
            <a href="<%= request.getContextPath() %>/home" class="btn btn-primary">Вернуться к объявлениям</a>
        </div>
    </div>
    <% } else { %>
    <div class="main-content">
        <div class="ad-card fade-in">
            <div class="ad-header">
                <h1 class="ad-title"><%= announcement.getTitle() %></h1>
                <div class="ad-price">
                    <%= formatPrice(announcement.getPrice()) %>
                </div>
                <div class="ad-meta">
                    <span class="meta-badge category-badge"><%= announcement.getCategory().getDisplayName() %></span>
                    <span class="meta-badge condition-badge"><%= announcement.getCondition().getDisplayName() %></span>
                    <span class="meta-badge location-badge">📍 <%= announcement.getLocation() %></span>
                    <span class="meta-badge author-badge">👤 <%= authorName %></span>
                </div>
            </div>

            <div class="photos-section">
                <h3 class="section-title">📷 Фотографии (<%= photoCount %>)</h3>
                <% if (photoCount == 0) { %>
                <div class="main-photo">
                    <div class="photo-placeholder">📷</div>
                    <div style="text-align: center; color: #666; position: absolute; bottom: 20px; width: 100%;">
                        <p>Фотографии отсутствуют</p>
                    </div>
                </div>
                <% } else { %>
                <div class="main-photo">
                    <img id="mainPhoto"
                         src="<%= request.getContextPath() %>/ad-photo?adId=<%= announcement.getId() %>&photoIndex=0"
                         alt="Фото объявления"
                         onerror="this.onerror=null; this.parentElement.innerHTML='<div class=\'photo-placeholder\'>📷<br/>Ошибка загрузки фото</div>';"
                style="max-width: 100%; max-height: 100%; object-fit: contain;">
            </div>
            <% } %>
        </div>

            <div class="ad-description">
                <h3 class="section-title">📝 Описание</h3>
                <div class="description-text"><%= announcement.getDescription() != null ? announcement.getDescription() : "Описание отсутствует" %></div>
            </div>

            <% if (announcement.getTags() != null && !announcement.getTags().isEmpty()) { %>
            <div class="tags-section">
                <h3 class="section-title">🏷️ Теги</h3>
                <div class="tags-container">
                    <% for (String tag : announcement.getTags()) { %>
                    <span class="tag">#<%= tag %></span>
                    <% } %>
                </div>
            </div>
            <% } %>

            <div class="ad-info">
                <h3 class="section-title">ℹ️ Информация</h3>
                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">Просмотры</span>
                        <span class="info-value">👁️ <%= announcement.getViewCount() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Создано</span>
                        <span class="info-value">📅 <%= formatDate(announcement.getCreatedAt()) %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Обновлено</span>
                        <span class="info-value">🔄 <%= formatDate(announcement.getUpdatedAt()) %></span>
                    </div>
                    <% if (announcement.getSubcategory() != null && !announcement.getSubcategory().isEmpty()) { %>
                    <div class="info-item">
                        <span class="info-label">Подкатегория</span>
                        <span class="info-value"><%= announcement.getSubcategory() %></span>
                    </div>
                    <% } %>
                </div>
            </div>

            <div class="action-buttons">
                <a href="<%= request.getContextPath() %>/home" class="btn btn-secondary">← Назад к объявлениям</a>
                <% if (sessionUser != null) { %>
                <button onclick="contactSeller()" class="btn btn-primary">📞 Связаться с продавцом</button>
                <% } else { %>
                <a href="<%= request.getContextPath() %>/login" class="btn btn-primary">🔐 Войдите, чтобы связаться</a>
                <% } %>
            </div>
        </div>

        <div class="comments-section fade-in">
            <h3 class="section-title">💬 Комментарии (<%= comments.size() %>)</h3>

            <% if (request.getAttribute("error") != null) { %>
            <div style="background: rgba(247, 37, 133, 0.1); border: 1px solid #f72585; color: #f72585; padding: 15px; border-radius: 10px; margin-bottom: 20px;">
                ⚠️ <%= request.getAttribute("error") %>
            </div>
            <% } %>

            <% if (request.getAttribute("success") != null) { %>
            <div style="background: rgba(76, 201, 240, 0.1); border: 1px solid #4cc9f0; color: #4cc9f0; padding: 15px; border-radius: 10px; margin-bottom: 20px;">
                ✅ <%= request.getAttribute("success") %>
            </div>
            <% } %>

            <% if (sessionUser != null) { %>
            <div class="comment-form">
                <form method="POST" action="<%= request.getContextPath() %>/ad-details">
                    <input type="hidden" name="id" value="<%= announcement.getId() %>">
                    <textarea name="commentText" class="comment-input" placeholder="Напишите ваш комментарий..." required></textarea>
                    <button type="submit" class="btn btn-primary">Добавить комментарий</button>
                </form>
            </div>
            <% } else { %>
            <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 25px;">
                <p style="color: #666; margin-bottom: 15px;">Войдите, чтобы оставить комментарий</p>
                <a href="<%= request.getContextPath() %>/login" class="btn btn-primary">Войти</a>
            </div>
            <% } %>

            <div class="comments-list">
                <% if (comments.isEmpty()) { %>
                <div class="no-comments">
                    <div style="font-size: 3rem; margin-bottom: 15px; opacity: 0.5;">💬</div>
                    <p>Пока нет комментариев</p>
                    <p style="font-size: 0.9rem;">Будьте первым, кто оставит комментарий!</p>
                </div>
                <% } else { %>
                <% for (Comment comment : comments) { %>
                <div class="comment-item">
                    <div class="comment-header">
                        <span class="comment-author"><%= comment.getUserName() %></span>
                        <span class="comment-date"><%= formatCommentDate(comment.getCreatedAt()) %></span>
                    </div>
                    <div class="comment-text"><%= comment.getContent() %></div>
                </div>
                <% } %>
                <% } %>
            </div>
        </div>
    </div>
    <% } %>
</div>

<script>
    let currentPhotoIndex = 0;
    const totalPhotos = <%= photoCount %>;
    const adId = <%= announcement != null ? announcement.getId() : 0 %>;

    function showPhoto(index) {
        if (index >= 0 && index < totalPhotos && adId > 0) {
            currentPhotoIndex = index;
            const mainPhoto = document.getElementById('mainPhoto');
            const contextPath = '<%= request.getContextPath() %>';
            mainPhoto.src = contextPath + '/ad-photo?adId=' + adId + '&photoIndex=' + index;
            document.getElementById('currentPhoto').textContent = index + 1;

            document.querySelectorAll('.thumbnail').forEach((thumb, i) => {
                thumb.classList.toggle('active', i === index);
            });
            updateNavigationButtons();
        }
    }

    function nextPhoto() {
        if (currentPhotoIndex < totalPhotos - 1) {
            showPhoto(currentPhotoIndex + 1);
        }
    }

    function prevPhoto() {
        if (currentPhotoIndex > 0) {
            showPhoto(currentPhotoIndex - 1);
        }
    }

    function updateNavigationButtons() {
        if (totalPhotos > 1) {
            const prevBtn = document.getElementById('prevBtn');
            const nextBtn = document.getElementById('nextBtn');
            if (prevBtn) prevBtn.disabled = currentPhotoIndex === 0;
            if (nextBtn) nextBtn.disabled = currentPhotoIndex === totalPhotos - 1;
        }
    }

    function contactSeller() {
        alert('Функция связи с продавцом будет доступна в ближайшее время');
    }

    document.addEventListener('DOMContentLoaded', function() {
        if (totalPhotos > 0) updateNavigationButtons();
        document.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowLeft') prevPhoto();
            if (e.key === 'ArrowRight') nextPhoto();
        });
    });
</script>
</body>
</html>

<%!
    private String formatPrice(int price) {
        if (price == -1) return "Договорная";
        if (price == 0) return "Бесплатно";
        return String.format("%,d руб.", price);
    }

    private String formatDate(java.time.Instant instant) {
        if (instant == null) return "Не указано";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(java.time.ZoneId.systemDefault());
        return formatter.format(instant);
    }

    private String formatCommentDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Не указано";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return formatter.format(dateTime);
    }
%>