<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.entity.Announcement" %>
<%@ page import="com.mipt.portal.entity.Comment" %>
<%@ page import="com.mipt.portal.entity.User" %>
<%@ page import="com.mipt.portal.service.AnnouncementService" %>
<%@ page import="com.mipt.portal.enums.Category" %>
<%@ page import="com.mipt.portal.enums.Condition" %>
<%@ page import="com.mipt.portal.service.ProfanityChecker" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>


<%
    // Получаем ID объявления из параметра
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
    WebApplicationContext appContext =
        WebApplicationContextUtils.getRequiredWebApplicationContext(application);
    AnnouncementService adsService = appContext.getBean(AnnouncementService.class);

    if (adIdParam != null && !adIdParam.trim().isEmpty()) {
        try {
            Long adId = Long.parseLong(adIdParam);

            // Загружаем объявление из БД
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT a.*, u.name as author_name FROM ads a " +
                                 "LEFT JOIN users u ON a.user_id = u.id WHERE a.id = ?")) {

                stmt.setLong(1, adId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    announcement = new Announcement();
                    announcement.setId(rs.getLong("id"));
                    announcement.setTitle(rs.getString("title"));
                    announcement.setDescription(rs.getString("description"));
                    announcement.setCategory(Category.values()[rs.getInt("category")]);
                    announcement.setSubcategory(rs.getString("subcategory"));
                    announcement.setCondition(Condition.values()[rs.getInt("condition")]);
                    announcement.setPrice(rs.getInt("price"));
                    announcement.setLocation(rs.getString("location"));
                    //announcement.setUserId(rs.getLong("user_id"));
                    announcement.setViewCount(rs.getInt("view_count"));
                    announcement.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    announcement.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());

                    authorName = rs.getString("author_name");

                    // Обрабатываем теги из JSONB
                    String tagsJson = rs.getString("tags");
                    List<String> tags = new ArrayList<>();
                    if (tagsJson != null && !tagsJson.equals("null") && !tagsJson.trim().isEmpty()) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();

                            if (tagsJson.startsWith("[")) {
                                Map<String, Object>[] tagArray = mapper.readValue(tagsJson, Map[].class);
                                for (Map<String, Object> tagObj : tagArray) {
                                    String valueName = (String) tagObj.get("valueName");
                                    if (valueName != null && !valueName.trim().isEmpty()) {
                                        tags.add(valueName.trim());
                                    }
                                }
                            } else if (tagsJson.startsWith("\"")) {
                                String decodedTags = mapper.readValue(tagsJson, String.class);
                                if (decodedTags.startsWith("[")) {
                                    Map<String, Object>[] tagArray = mapper.readValue(decodedTags, Map[].class);
                                    for (Map<String, Object> tagObj : tagArray) {
                                        String valueName = (String) tagObj.get("valueName");
                                        if (valueName != null && !valueName.trim().isEmpty()) {
                                            tags.add(valueName.trim());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // System.err.println("Ошибка при парсинге тегов: " + e.getMessage());
                            e.printStackTrace();

                            try {
                                if (tagsJson.startsWith("[") && tagsJson.endsWith("]")) {
                                    String[] simpleTags = tagsJson.substring(1, tagsJson.length() - 1).split(",");
                                    for (String tag : simpleTags) {
                                        String cleanedTag = tag.trim().replace("\"", "");
                                        if (!cleanedTag.isEmpty()) {
                                            tags.add(cleanedTag);
                                        }
                                    }
                                }
                            } catch (Exception e2) {
                                System.err.println("Fallback парсинг тоже не удался: " + e2.getMessage());
                            }
                        }
                    }

                    announcement.setTags(tags);

                    /*
                    // Получаем количество фото через AdsService
                    try {
                        List<byte[]> photos = adsService.getAdPhotosBytes(adId);
                        photoCount = photos != null ? photos.size() : 0;
                        System.out.println("✅ Loaded " + photoCount + " photos for ad " + adId);
                    } catch (Exception e) {
                        System.err.println(" Error loading photos: " + e.getMessage());
                        photoCount = 0;
                    }

                    // Увеличиваем счетчик просмотров
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE ads SET view_count = view_count + 1 WHERE id = ?")) {
                        updateStmt.setLong(1, adId);
                        updateStmt.executeUpdate();
                    }
                    */

                }
            }

            /*
            // Загружаем комментарии из БД
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM comments WHERE ad_id = ? ORDER BY created_at DESC")) {

                stmt.setLong(1, adId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // Получаем Timestamp и конвертируем в LocalDateTime
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    java.time.LocalDateTime createdAt = timestamp != null ?
                            timestamp.toLocalDateTime() : java.time.LocalDateTime.now();

                    Comment comment = new Comment(
                            rs.getLong("id"),
                            rs.getString("user_name"),
                            rs.getString("content"),
                            createdAt,
                            rs.getLong("ad_id")
                    );
                    comments.add(comment);
                }
            }
            */

        } catch (NumberFormatException e) {
            System.err.println("Неверный формат ID объявления: " + adIdParam);
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Общая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Обработка добавления нового комментария
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
                // Проверка на мат

                /*


                ProfanityChecker profanityChecker =
                        new ProfanityChecker();
                boolean hasProfanity = profanityChecker.containsProfanity(commentText);

                if (hasProfanity) {
                    request.setAttribute("profanityError", "Комментарий содержит недопустимые слова и не может быть сохранен.");
                } else {
                    try (Connection conn = getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                                 "INSERT INTO comments (ad_id, user_id, user_name, content, created_at) VALUES (?, ?, ?, ?, ?)")) {

                        stmt.setLong(1, announcement.getId());
                        stmt.setLong(2, user.getId());
                        stmt.setString(3, user.getName());
                        stmt.setString(4, commentText.trim());
                        stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

                        int affectedRows = stmt.executeUpdate();
                        System.out.println("Rows affected: " + affectedRows);

                        if (affectedRows > 0) {
                            System.out.println("Comment saved successfully");

                            try (PreparedStatement commentStmt = conn.prepareStatement(
                                    "SELECT * FROM comments WHERE ad_id = ? ORDER BY created_at DESC")) {
                                commentStmt.setLong(1, announcement.getId());
                                ResultSet rs = commentStmt.executeQuery();

                                comments.clear();
                                while (rs.next()) {
                                    Timestamp timestamp = rs.getTimestamp("created_at");
                                    java.time.LocalDateTime createdAt = timestamp != null ?
                                            timestamp.toLocalDateTime() : java.time.LocalDateTime.now();

                                    Comment comment = new Comment(
                                            rs.getLong("id"),
                                            rs.getString("user_name"),
                                            rs.getString("content"),
                                            createdAt,
                                            rs.getLong("ad_id")
                                    );
                                    comments.add(comment);
                                }
                                System.out.println("Comments reloaded: " + comments.size());
                            }

                            request.setAttribute("clearComment", "true");
                            request.setAttribute("success", "Комментарий успешно добавлен!");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка при создании комментария: " + e.getMessage());
                        e.printStackTrace();
                        request.setAttribute("error", "Ошибка при сохранении комментария: " + e.getMessage());
                    }
                }

                 */
            }
        }
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

        /* Шапка */
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

        /* Основной контент */
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

        /* Заголовок объявления */
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

        /* Секции */
        .section-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #333;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        /* Фотографии */
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

        .thumbnail{
            border-color: #667eea;
            box-shadow: 0 0 0 2px #667eea;
        }

        .thumbnail img {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }

        /* Описание */
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

        /* Теги */
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
            background: #e9ecef;
            color: #495057;
            padding: 6px 12px;
            border-radius: 15px;
            font-size: 0.9rem;
            font-weight: 500;
        }

        /* Информация */
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

        /* Кнопки действий */
        .action-buttons {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
        }

        /* Комментарии */
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

        /* Кнопки */
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

        /* Анимации */
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

        .tags-container {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
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
    </style>
</head>
<body>
<div class="container">

    <!-- Шапка -->
    <div class="header">
        <div class="portal-logo">PORTAL</div>
        <div class="auth-buttons">
            <%
                User user = sessionUser;
                if (user != null) {
            %>
            <a href="dashboard.jsp" class="btn btn-primary">Личный кабинет</a>
            <a href="logout.jsp" class="btn btn-secondary">Выйти</a>
            <% } else { %>
            <a href="login.jsp" class="btn btn-secondary">Войти</a>
            <a href="register.jsp" class="btn btn-primary">Регистрация</a>
            <% } %>
            <a href="${pageContext.request.contextPath}/home.jsp" class="btn btn-secondary">На главную</a>
        </div>
    </div>

    <% if (announcement == null) { %>
    <!-- Сообщение об ошибке -->
    <div class="ad-card fade-in">
        <div style="text-align: center; padding: 60px 20px;">
            <div style="font-size: 4rem; margin-bottom: 20px; opacity: 0.5;">🔍</div>
            <h2 style="color: #333; margin-bottom: 15px;">Объявление не найдено</h2>
            <p style="color: #666; margin-bottom: 30px;">Запрошенное объявление не существует или было удалено.</p>
            <a href="${pageContext.request.contextPath}/home.jsp" class="btn btn-primary">Вернуться к объявлениям</a>
        </div>
    </div>
    <% } else { %>
    <!-- Основной контент -->
    <div class="main-content">
        <!-- Левая колонка - информация об объявлении -->
        <div class="ad-card fade-in">
            <!-- Заголовок и цена -->
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

            <!-- Фотографии -->
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
                <!-- Основное фото с навигацией -->
                <div class="main-photo">
                    <img id="mainPhoto"
                         src="<%= request.getContextPath() %>/ad-photo?adId=<%= announcement.getId() %>&photoIndex=0&t=<%= System.currentTimeMillis() %>"
                         alt="Фото объявления"
                         onerror="handlePhotoError(this)"
                         style="display: block;">
                    <div class="photo-placeholder" style="display: none;">📷</div>
                    <div class="photo-counter">
                        <span id="currentPhoto">1</span> / <%= photoCount %>
                    </div>
                    <% if (photoCount > 1) { %>
                    <div class="photo-navigation">
                        <button class="nav-btn" onclick="prevPhoto()" id="prevBtn" disabled>❮</button>
                        <button class="nav-btn" onclick="nextPhoto()" id="nextBtn" <%= photoCount > 1 ? "" : "disabled" %>>❯</button>
                    </div>
                    <% } %>
                </div>

                <!-- Миниатюры -->
                <% if (photoCount > 1) { %>
                <div class="photo-thumbnails">
                    <% for (int i = 0; i < photoCount; i++) { %>
                    <div class="thumbnail <%= i == 0 ? "active" : "" %>"
                         onclick="showPhoto(<%= i %>)"
                         data-index="<%= i %>">
                        <img src="<%= request.getContextPath() %>/ad-photo?adId=<%= announcement.getId() %>&photoIndex=<%= i %>&t=<%= System.currentTimeMillis() %>"
                             alt="Миниатюра <%= i + 1 %>"
                             onerror="handleThumbnailError(this)"
                             loading="lazy">
                    </div>
                    <% } %>
                </div>
                <% } %>
                <% } %>
            </div>

            <!-- Описание -->
            <div class="ad-description">
                <h3 class="section-title">📝 Описание</h3>
                <div class="description-text">
                    <%= announcement.getDescription() != null ?
                            announcement.getDescription() : "Описание отсутствует" %>
                </div>
            </div>

            <!-- Теги -->
            <%
                List<String> tags = announcement.getTags();
                boolean hasTags = tags != null && !tags.isEmpty() && !(tags.size() == 1 && tags.get(0).isEmpty());
            %>
            <% if (hasTags) { %>
            <div class="tags-section">
                <h3 class="section-title">🏷️ Теги</h3>
                <div class="tags-container">
                    <% for (String tag : tags) {
                        if (tag != null && !tag.trim().isEmpty()) {
                    %>
                    <span class="tag">#<%= tag.trim() %></span>
                    <% } } %>
                </div>
            </div>
            <% } %>

            <!-- Дополнительная информация -->
            <div class="ad-info">
                <h3 class="section-title">ℹ️ Информация</h3>
                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">Автор</span>
                        <span class="info-value">👤 <%= authorName %></span>
                    </div>
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

            <!-- Кнопки действий -->
            <div class="action-buttons">
                <a href="${pageContext.request.contextPath}/home.jsp" class="btn btn-secondary">← Назад к объявлениям</a>
                <% if (user != null) { %>
                <button onclick="contactSeller()" class="btn btn-primary">📞 Связаться с продавцом</button>
                <% } else { %>
                <a href="login.jsp" class="btn btn-primary">🔐 Войдите, чтобы связаться</a>
                <% } %>
            </div>
        </div>

        <!-- Правая колонка - комментарии -->
        <div class="comments-section fade-in">
            <h3 class="section-title">💬 Комментарии (<%= comments.size() %>)</h3>

            <% if (request.getAttribute("profanityError") != null) { %>
            <div style="background: rgba(247, 37, 133, 0.1); border: 1px solid #f72585; color: #f72585; padding: 15px; border-radius: 10px; margin-bottom: 20px; font-weight: 500;">
                ⚠️ <%= request.getAttribute("profanityError") %>
            </div>
            <% } %>

            <!-- Форма добавления комментария -->
            <% if (user != null) { %>
            <div class="comment-form">
                <form id="commentForm" method="POST" action="ad-details.jsp?id=<%= announcement.getId() %>">
                    <input type="hidden" name="action" value="addComment">
                    <textarea name="commentText" id="commentText" class="comment-input"
                              placeholder="Напишите ваш комментарий..." required><%= request.getAttribute("clearComment") != null ? "" : "" %></textarea>

                    <% if (request.getAttribute("success") != null) { %>
                    <div style="color: green; margin-bottom: 10px; font-weight: 500;">
                        ✅ <%= request.getAttribute("success") %>
                    </div>
                    <% } %>

                    <% if (request.getAttribute("error") != null) { %>
                    <div style="color: red; margin-bottom: 10px; font-weight: 500;">
                         <%= request.getAttribute("error") %>
                    </div>
                    <% } %>

                    <button type="submit" class="btn btn-primary" onclick="console.log('Кнопка нажата!')">Добавить комментарий</button>
                </form>
            </div>
            <% } else { %>
            <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 25px;">
                <p style="color: #666; margin-bottom: 15px;">Войдите, чтобы оставить комментарий</p>
                <a href="login.jsp" class="btn btn-primary">Войти</a>
            </div>
            <% } %>

            <!-- Список комментариев -->
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
                        <span class="comment-author"><%= comment.getAuthor() %></span>
                        <span class="comment-date"><%= formatCommentDate(comment.getCreatedAt()) %></span>
                    </div>
                    <div class="comment-text">
                        <%= comment.getText() %>
                    </div>
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

            // Обновляем основное фото через сервлет
            const mainPhoto = document.getElementById('mainPhoto');
            const timestamp = new Date().getTime();

            // Используем contextPath из JSP
            const contextPath = '<%= request.getContextPath() %>';
            const newSrc = contextPath + '/ad-photo?adId=' + adId + '&photoIndex=' + index + '&t=' + timestamp;

            console.log('Loading photo:', newSrc);
            mainPhoto.style.opacity = '0.5';

            // Создаем новое изображение для предзагрузки
            const tempImg = new Image();
            tempImg.onload = function() {
                mainPhoto.src = newSrc;
                mainPhoto.style.opacity = '1';
                mainPhoto.style.display = 'block';

                const placeholder = document.querySelector('.main-photo .photo-placeholder');
                if (placeholder) placeholder.style.display = 'none';
            };
            tempImg.onerror = function() {
                console.error('Error loading photo:', newSrc);
                handlePhotoError(mainPhoto);
            };
            tempImg.src = newSrc;

            document.getElementById('currentPhoto').textContent = index + 1;

            // Обновляем активную миниатюру
            document.querySelectorAll('.thumbnail').forEach((thumb, i) => {
                thumb.classList.toggle('active', i === index);
            });

            // Обновляем состояние кнопок навигации
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

    // Улучшенная обработка ошибок загрузки фото
    function handlePhotoError(img) {
        console.error('Error loading main photo');
        img.style.display = 'none';
        const placeholder = document.querySelector('.main-photo .photo-placeholder');
        if (placeholder) {
            placeholder.style.display = 'flex';
            placeholder.style.flexDirection = 'column';
            placeholder.innerHTML = '<div style="text-align: center; margin-top: 10px; font-size: 1rem; color: #666;">Ошибка загрузки фото</div>';
        }
    }

    function handleThumbnailError(img) {
        console.error('Error loading thumbnail');
        img.style.display = 'none';
        const thumbnail = img.parentElement;
        thumbnail.innerHTML = '';
        thumbnail.style.alignItems = 'center';
        thumbnail.style.justifyContent = 'center';
        thumbnail.style.fontSize = '1.2rem';
        thumbnail.style.color = '#ccc';
        thumbnail.style.background = '#f8f9fa';
    }

    // навигация
    document.addEventListener('DOMContentLoaded', function() {
        if (totalPhotos > 0) {
            updateNavigationButtons();

            if (totalPhotos > 1) {
                const nextImg = new Image();
                nextImg.src = '/ad-photo?adId=' + adId + '&photoIndex=1&t=' + new Date().getTime();
            }
        }

        // обработка клавиш клавиатуры
        document.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowLeft') prevPhoto();
            if (e.key === 'ArrowRight') nextPhoto();
        });
    });
</script>
<%@ include file="profanity-check.jsp" %>
</body>
</html>

<%!
    // Вспомогательные методы для форматирования

    private String formatPrice(int price) {
        if (price == -1) {
            return "Договорная";
        } else if (price == 0) {
            return "Бесплатно";
        } else {
            return String.format("%,d руб.", price);
        }
    }

    private String formatDate(java.time.Instant instant) {
        if (instant == null) return "Не указано";
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        .withZone(java.time.ZoneId.systemDefault());
        return formatter.format(instant);
    }

    private String formatCommentDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Не указано";
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return formatter.format(dateTime);
    }

    // Метод для получения соединения с БД
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/myproject",
                "myuser",
                "mypassword"
        );
    }
%>