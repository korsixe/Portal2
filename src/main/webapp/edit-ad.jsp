<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.announcement.Announcement" %>
<%@ page import="com.mipt.portal.announcement.Category" %>
<%@ page import="com.mipt.portal.announcement.Condition" %>
<%@ page import="com.mipt.portal.announcement.AdvertisementStatus" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>


<%
    // Сохраняем выбранные теги из параметров запроса
    String selectedTagsParam = request.getParameter("selectedTags");
    if (selectedTagsParam != null && !selectedTagsParam.isEmpty()) {
        request.setAttribute("savedSelectedTags", selectedTagsParam);
    }
%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    Announcement announcement = (Announcement) request.getAttribute("announcement");
    if (announcement == null) {
        response.sendRedirect("dashboard.jsp");
        return;
    }
    // Загружаем теги для редактирования
    if (request.getAttribute("availableTags") == null || request.getAttribute("currentTags") == null) {
        try {
            com.mipt.portal.announcementContent.tags.TagSelector tagSelector =
                    new com.mipt.portal.announcementContent.tags.TagSelector();
            java.util.List<java.util.Map<String, Object>> availableTags = tagSelector.getTagsWithValues();
            request.setAttribute("availableTags", availableTags);

            // Загружаем текущие теги объявления
            List<Map<String, Object>> currentTags = tagSelector.getTagsForAd(announcement.getId());
            request.setAttribute("currentTags", currentTags);

            System.out.println("✅ Set currentTags in request: " + (currentTags != null ? currentTags.size() : 0));

        } catch (Exception e) {
            System.err.println("Error loading tags in edit-ad.jsp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Обработка отображения поля цены
    int price = announcement.getPrice();
    String priceType = price == -1 ? "negotiable" : price == 0 ? "free" : "fixed";
    boolean showPrice = "fixed".equals(priceType);

    // Определяем текущую категорию и подкатегорию с приоритетом сохраненных данных
    String currentCategoryValue = null; // ИЗМЕНЕНО: переименовано
    String currentSubcategory = null;

    String categoryParam = request.getParameter("category");
    String subcategoryParam = request.getParameter("subcategory");

    if (categoryParam != null && !categoryParam.isEmpty()) {
        currentCategoryValue = categoryParam; // ИЗМЕНЕНО
    } else if (announcement.getCategory() != null) {
        currentCategoryValue = announcement.getCategory().getDisplayName(); // ИЗМЕНЕНО
    }

    if (subcategoryParam != null && !subcategoryParam.isEmpty()) {
        currentSubcategory = subcategoryParam;
    } else {
        currentSubcategory = announcement.getSubcategory();
    }
%>

<%!
    // Метод для преобразования DisplayName в имя из БД
    private String convertDisplayNameToDbName(String displayName) {
        if (displayName == null) return null;

        // Пример маппинга - адаптируйте под вашу базу данных
        java.util.Map<String, String> mapping = new java.util.HashMap<>();
        mapping.put("Автозапчасти", "autoparts");
        mapping.put("Электроника", "electronics");
        mapping.put("Недвижимость", "realestate");
        mapping.put("Автоговары", "autogoods");
        // Добавьте другие категории

        return mapping.getOrDefault(displayName, displayName);
    }
%>

<%
    // Сохранение состояния тегов между запросами
    String selectedTagsJson = request.getParameter("selectedTags");
    if (selectedTagsJson != null && !selectedTagsJson.trim().isEmpty()) {
        try {
            // Парсим JSON с тегами из параметра запроса
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> savedTags = mapper.readValue(selectedTagsJson, List.class);
            request.setAttribute("savedTags", savedTags);
        } catch (Exception e) {
            System.err.println("Error parsing saved tags: " + e.getMessage());
        }
    }
%>



<html>
<head>
    <title>Редактировать объявление • Portal</title>
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
            max-width: 800px;
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
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }

        .logo span {
            color: var(--success);
        }

        .card {
            background: white;
            border-radius: 20px;
            padding: 40px;
            box-shadow: var(--shadow-lg);
            border: 1px solid rgba(255,255,255,0.2);
        }

        .card-header {
            text-align: center;
            margin-bottom: 30px;
        }

        .card-title {
            font-size: 2rem;
            font-weight: 700;
            color: var(--dark);
            margin-bottom: 10px;
        }

        .card-subtitle {
            color: var(--gray);
            font-size: 1.1rem;
        }

        .ad-info {
            background: var(--light);
            border-radius: 15px;
            padding: 20px;
            margin-bottom: 25px;
            border-left: 4px solid var(--primary);
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }

        .info-item {
            display: flex;
            flex-direction: column;
        }

        .info-label {
            font-weight: 600;
            color: var(--dark);
            font-size: 0.9rem;
        }

        .info-value {
            color: var(--gray);
            font-size: 1rem;
        }

        .status-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
        }

        .status-draft {
            background: #fff3cd;
            color: #856404;
        }

        .status-moderation {
            background: #cce7ff;
            color: #004085;
        }

        .status-active {
            background: #d4edda;
            color: #155724;
        }

        .form-section {
            margin-bottom: 30px;
            padding: 25px;
            background: var(--light);
            border-radius: 15px;
            border-left: 4px solid var(--warning);
        }

        .section-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: var(--dark);
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: var(--dark);
        }

        .required::after {
            content: " *";
            color: var(--danger);
        }

        .form-control {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid var(--border);
            border-radius: 10px;
            font-size: 1rem;
            transition: all 0.3s ease;
            background: white;
        }

        .form-control:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
        }

        textarea.form-control {
            min-height: 120px;
            resize: vertical;
        }

        .radio-group {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            margin-top: 10px;
        }

        .radio-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 12px 20px;
            background: white;
            border: 2px solid var(--border);
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s ease;
            flex: 1;
            min-width: 120px;
        }

        .radio-item:hover {
            border-color: var(--primary);
        }

        .radio-item input[type="radio"] {
            margin: 0;
        }

        .radio-label {
            font-weight: 500;
            color: var(--dark);
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

        .btn-danger {
            background: var(--danger);
            color: white;
        }

        .btn-danger:hover {
            background: #e00;
            transform: translateY(-2px);
        }

        .form-actions {
            display: flex;
            gap: 15px;
            margin-top: 30px;
            flex-wrap: wrap;
        }

        .alert {
            padding: 15px 20px;
            border-radius: 10px;
            margin-bottom: 25px;
            font-weight: 500;
        }

        .alert-error {
            background: rgba(247, 37, 133, 0.1);
            border: 1px solid var(--danger);
            color: var(--danger);
        }

        .alert-success {
            background: rgba(76, 201, 240, 0.1);
            border: 1px solid var(--success);
            color: var(--success);
        }

        .tags-hint {
            font-size: 0.9rem;
            color: var(--gray);
            margin-top: 5px;
        }

        .edit-note {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
            border-left: 4px solid var(--warning);
        }

        /* Стили для системы тегов */
        .tag-row {
            display: flex;
            flex-direction: column;
            margin-bottom: 15px;
            padding: 15px;
            border: 2px solid var(--border);
            border-radius: 10px;
            background: white;
        }

        .tag-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .tag-name {
            font-weight: 600;
            color: var(--dark);
            font-size: 1.1em;
        }

        .tag-select {
            width: 100%;
            padding: 10px;
            border: 2px solid var(--border);
            border-radius: 8px;
            font-size: 1em;
            background: white;
            transition: all 0.3s ease;
        }

        .tag-select:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
        }

        .selected-tags-container {
            min-height: 50px;
            border: 2px solid var(--border);
            border-radius: 10px;
            padding: 15px;
            background: white;
            transition: all 0.3s ease;
        }

        .selected-tags-container:focus-within {
            border-color: var(--primary);
        }

        .no-tags-message {
            color: var(--gray);
            font-style: italic;
            text-align: center;
            padding: 10px;
        }

        .selected-tag {
            display: inline-flex;
            align-items: center;
            background: var(--primary);
            color: white;
            padding: 6px 12px;
            border-radius: 20px;
            margin: 5px;
            font-size: 0.9em;
        }

        .remove-tag-btn {
            background: none;
            border: none;
            color: white;
            margin-left: 8px;
            cursor: pointer;
            font-size: 1.2em;
            font-weight: bold;
            padding: 0;
            width: 18px;
            height: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }

        .remove-tag-btn:hover {
            background: rgba(255,255,255,0.2);
        }

        .photo-preview-container {
            margin-top: 15px;
            padding: 15px;
            background: var(--light);
            border-radius: 10px;
            border: 2px dashed var(--border);
        }

        .current-photos {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            margin-top: 15px;
        }

        .photo-item {
            position: relative;
            width: 120px;
            height: 120px;
        }

        .photo-item img {
            width: 100%;
            height: 100%;
            object-fit: contain;
            border-radius: 8px;
            border: 2px solid var(--border);
        }

        .photo-remove-btn {
            position: absolute;
            top: -8px;
            right: -8px;
            background: var(--danger);
            color: white;
            border: none;
            border-radius: 50%;
            width: 24px;
            height: 24px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        /* Адаптивность */
        @media (max-width: 768px) {
            body {
                padding: 10px;
            }

            .card {
                padding: 25px 20px;
            }

            .form-section {
                padding: 20px 15px;
            }

            .radio-group {
                flex-direction: column;
            }

            .form-actions {
                flex-direction: column;
            }

            .info-grid {
                grid-template-columns: 1fr;
            }

            .current-photos {
                justify-content: center;
            }
        }

        .icon {
            display: inline-block;
            width: 24px;
            height: 24px;
            text-align: center;
            line-height: 24px;
        }

        .selected-tag {
            display: inline-flex;
            align-items: center;
            background: var(--primary);
            color: white;
            padding: 6px 12px;
            border-radius: 20px;
            margin: 5px;
            font-size: 0.9em;
        }

        .remove-tag-btn {
            background: none;
            border: none;
            color: white;
            margin-left: 8px;
            cursor: pointer;
            font-size: 1.2em;
            font-weight: bold;
            padding: 0;
            width: 18px;
            height: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }

        .remove-tag-btn:hover {
            background: rgba(255,255,255,0.2);
        }

        .selected-tags-container {
            min-height: 50px;
            border: 2px solid var(--border);
            border-radius: 10px;
            padding: 15px;
            background: white;
            transition: all 0.3s ease;
            margin-bottom: 15px;
        }

        .selected-tags-container:focus-within {
            border-color: var(--primary);
        }

        .no-tags-message {
            color: var(--gray);
            font-style: italic;
            text-align: center;
            padding: 10px;
        }

    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo">Portal</div>
    </div>

    <div class="card">
        <div class="card-header">
            <h1 class="card-title">Редактировать объявление</h1>
        </div>

        <% if (request.getAttribute("success") != null) { %>
        <div class="alert alert-success">
            <span class="icon">✓</span> <%= request.getAttribute("success") %>
        </div>
        <% } %>

        <!-- Информация об объявлении -->
        <div class="ad-info">
            <h3 class="section-title">
                <span class="icon">📊</span> Текущие данные
            </h3>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Статус:</span>
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
                            default:
                                statusClass = "status-draft";
                        }
                    %>
                    <span class="status-badge <%= statusClass %>"><%= statusText %></span>
                </div>
                <div class="info-item">
                    <span class="info-label">Просмотры:</span>
                    <span class="info-value"><%= announcement.getViewCount() != null ? announcement.getViewCount() : 0 %></span>
                </div>
                <div class="info-item">
                    <span class="info-label">Создано:</span>
                    <span class="info-value"><%= announcement.getCreatedAt() != null ? announcement.getCreatedAt() : "Не указано" %></span>
                </div>
                <div class="info-item">
                    <span class="info-label">Обновлено:</span>
                    <span class="info-value"><%= announcement.getUpdatedAt() != null ? announcement.getUpdatedAt() : "Не указано" %></span>
                </div>
            </div>
        </div>

        <div class="edit-note">
            <strong>💡 Примечание:</strong>
            <% if (!announcement.canBeEdited()) { %>
            Это объявление нельзя редактировать в текущем статусе. Сначала измените статус на "Черновик".
            <% } else { %>
            Вы можете редактировать все поля объявления. После сохранения статус может измениться.
            <% } %>
        </div>

        <!-- Форма редактирования -->
        <form action="edit-ad" method="post" enctype="multipart/form-data">
            <input type="hidden" name="adId" value="<%= announcement.getId() %>">

            <input type="hidden" name="selectedTags" id="formSelectedTags" value="<%= selectedTagsParam != null ? selectedTagsParam : "" %>">

            <!-- Основная информация -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📝</span> Основная информация
                </h3>

                <div class="form-group">
                    <label for="title" class="required">Заголовок объявления</label>
                    <input type="text" id="title" name="title" class="form-control"
                           placeholder="Например: iPhone 13 Pro Max 256GB" required
                           value="<%= announcement.getTitle() != null ? announcement.getTitle() : "" %>"
                        <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                </div>

                <div class="form-group">
                    <label for="description" class="required">Описание</label>
                    <textarea id="description" name="description" class="form-control"
                              placeholder="Подробно опишите ваш товар или услугу..." required
                            <%= !announcement.canBeEdited() ? "disabled" : "" %>><%= announcement.getDescription() != null ? announcement.getDescription() : "" %></textarea>
                </div>
            </div>

            <!-- Категории -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📂</span> Категория
                </h3>

                <div class="form-group">
                    <label for="category" class="required">Основная категория</label>
                    <select id="category" name="category" class="form-control" required
                            <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                        <option value="">Выберите категорию</option>
                        <%
                            try {
                                com.mipt.portal.announcementContent.tags.CategorySelector categorySelector =
                                        new com.mipt.portal.announcementContent.tags.CategorySelector();
                                java.util.List<java.util.Map<String, Object>> categories = categorySelector.getAllCategories();

                                for (java.util.Map<String, Object> category : categories) {
                                    String categoryName = (String) category.get("name");
                                    boolean isSelected = categoryName.equals(currentCategoryValue); // ИЗМЕНЕНО
                        %>
                        <option value="<%= categoryName %>" <%= isSelected ? "selected" : "" %>>
                            <%= categoryName %>
                        </option>
                        <%
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading categories: " + e.getMessage());
                        %>
                        <option value="">Ошибка загрузки категорий</option>
                        <%
                            }
                        %>
                    </select>
                </div>


                <!-- Подкатегория -->
                <div class="form-group">
                    <label for="subcategory" class="required">Подкатегория</label>
                    <select id="subcategory" name="subcategory" class="form-control" required
                            <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                        <%
                            // ДЕБАГ 1: Что у нас есть на входе
                            System.out.println("=== DEBUG SUBCATEGORIES START ===");
                            System.out.println("currentCategoryValue: '" + currentCategoryValue + "'");
                            System.out.println("currentSubcategory: '" + currentSubcategory + "'");

                            if (currentCategoryValue == null || currentCategoryValue.isEmpty()) {
                                System.out.println(" currentCategoryValue is null or empty");
                        %>
                        <option value="">Сначала выберите категорию</option>
                        <%
                        } else {
                            try {
                                System.out.println("🔍 Ищем категорию в БД: '" + currentCategoryValue + "'");

                                // Загружаем категории и находим ID выбранной
                                com.mipt.portal.announcementContent.tags.CategorySelector categorySelector =
                                        new com.mipt.portal.announcementContent.tags.CategorySelector();
                                java.util.List<java.util.Map<String, Object>> allCategories = categorySelector.getAllCategories();

                                System.out.println("📊 Всего категорий в БД: " + allCategories.size());

                                Long categoryId = null;
                                boolean foundExactMatch = false;

                                // ДЕБАГ: Выводим все категории из БД
                                System.out.println("📋 Категории в БД:");
                                for (java.util.Map<String, Object> category : allCategories) {
                                    String catName = (String) category.get("name");
                                    Long catId = (Long) category.get("id");
                                    System.out.println("  - '" + catName + "' (ID: " + catId + ")");

                                    // Сравниваем с учетом возможных опечаток и пробелов
                                    if (catName != null && catName.equals(currentCategoryValue)) {
                                        categoryId = catId;
                                        foundExactMatch = true;
                                        System.out.println("✅ Точное совпадение найдено! ID: " + categoryId);
                                        break;
                                    }
                                }

                                // ДЕБАГ: Проверяем, что нашли
                                if (categoryId != null) {

                                    // Загружаем подкатегории
                                    com.mipt.portal.announcementContent.tags.SubcategorySelector subcategorySelector =
                                            new com.mipt.portal.announcementContent.tags.SubcategorySelector();
                                    java.util.List<java.util.Map<String, Object>> subcategories =
                                            subcategorySelector.getSubcategoriesByCategory(categoryId);


                                    if (subcategories != null && !subcategories.isEmpty()) {
                                        for (java.util.Map<String, Object> subcategory : subcategories) {
                                            String subcategoryName = (String) subcategory.get("name");
                                        }
                        %>
                        <option value="">Выберите подкатегорию</option>
                        <%
                            for (java.util.Map<String, Object> subcategory : subcategories) {
                                String subcategoryName = (String) subcategory.get("name");
                                boolean isSelected = subcategoryName.equals(currentSubcategory);

                                if (isSelected) {
                                    System.out.println("⭐ Подкатегория выбрана: '" + subcategoryName + "'");
                                }
                        %>
                        <option value="<%= subcategoryName %>" <%= isSelected ? "selected" : "" %>>
                            <%= subcategoryName %>
                        </option>
                        <%
                            }
                        } else {
                            System.out.println("⚠️ Нет доступных подкатегорий для categoryId: " + categoryId);
                        %>
                        <option value="">Нет доступных подкатегорий</option>
                        <%
                            }
                        } else {
                            System.out.println(" Категория не найдена в БД: '" + currentCategoryValue + "'");

                            // ДЕБАГ: Проверим, что есть в enum Category
                            System.out.println("🔍 Проверяем enum Category:");
                            for (Category cat : Category.values()) {
                                System.out.println("  - " + cat.name() + " -> '" + cat.getDisplayName() + "'");
                            }
                        %>
                        <option value="">Категория не найдена в БД</option>
                        <%
                            }
                        } catch (Exception e) {
                            System.err.println(" ERROR loading subcategories: " + e.getMessage());
                            e.printStackTrace();
                        %>
                        <option value="">Ошибка загрузки подкатегорий</option>
                        <%
                                }
                            }
                            System.out.println("=== DEBUG SUBCATEGORIES END ===");
                        %>
                    </select>
                </div>
            </div>

            <!-- Местоположение и состояние -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📍</span> Местоположение и состояние
                </h3>

                <div class="form-group">
                    <label for="location" class="required">Местоположение</label>
                    <input type="text" id="location" name="location" class="form-control"
                           placeholder="Например: Москва, центр" required
                           value="<%= announcement.getLocation() != null ? announcement.getLocation() : "" %>"
                        <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                </div>

                <div class="form-group">
                    <label class="required">Состояние товара</label>
                    <div class="radio-group">
                        <% for (Condition condition : Condition.values()) { %>
                        <label class="radio-item">
                            <input type="radio" name="condition"
                                   value="<%= condition.name() %>" required
                                <%= announcement.getCondition() == condition ? "checked" : "" %>
                                <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                            <span class="radio-label"><%= condition.getDisplayName() %></span>
                        </label>
                        <% } %>
                    </div>
                </div>
            </div>

            <!-- Цена -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">💰</span> Цена
                </h3>

                <div class="form-group">
                    <label class="required">Тип цены</label>
                    <div class="radio-group">
                        <label class="radio-item">
                            <input type="radio" name="priceType" value="negotiable"
                                <%= "negotiable".equals(priceType) ? "checked" : "" %>
                                <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                            <span class="radio-label">Договорная</span>
                        </label>
                        <label class="radio-item">
                            <input type="radio" name="priceType" value="free"
                                <%= "free".equals(priceType) ? "checked" : "" %>
                                <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                            <span class="radio-label">Бесплатно</span>
                        </label>
                        <label class="radio-item">
                            <input type="radio" name="priceType" value="fixed"
                                <%= "fixed".equals(priceType) ? "checked" : "" %>
                                <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                            <span class="radio-label">Указать цену</span>
                        </label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="price">Цена (руб.)</label>
                    <input type="number" id="price" name="price" class="form-control"
                           min="1" max="1000000000" placeholder="1000"
                           value="<%= price > 0 ? price : "" %>"
                        <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                    <div class="tags-hint">
                        <strong>Напишите цену, если выбрали пункт "Указать цену"</strong>
                    </div>
                </div>
            </div>

            <!-- Фотографии -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📷</span> Фотографии
                </h3>

                <!-- Текущие фотографии -->
                <% if (announcement.getPhotos() != null && !announcement.getPhotos().isEmpty()) { %>
                <div class="form-group">
                    <label>Текущие фотографии:</label>
                    <div class="current-photos">
                        <%
                            int photoCount = announcement.getPhotos() != null ? announcement.getPhotos().size() : 0;
                            for (int i = 0; i < photoCount; i++) { %>
                        <div class="photo-item" id="photo-<%= i %>" style="position: relative; display: inline-block; margin: 10px;">
                            <img src="ad-photo?adId=<%= announcement.getId() %>&photoIndex=<%= i %>"
                                 alt="Фото <%= i + 1 %>"
                                 style="width: 120px; height: 120px; object-fit: contain; border-radius: 8px; pointer-events: none;">

                            <!-- Кнопка удаления - кликабельна -->
                            <button type="button"
                                    class="photo-remove-btn"
                                    onclick="removePhoto(<%= announcement.getId() %>, <%= i %>); return false;">
                                ×
                            </button>

                        </div>
                        <% } %>
                    </div>
                </div>
                <% } %>

                <!-- Добавление новых фотографий -->
                <div class="form-group">
                    <label for="photos">Добавить новые фотографии</label>
                    <input type="file" id="photos" name="photos" class="form-control"
                           multiple accept="image/*" style="padding: 8px;"
                        <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                    <div class="tags-hint">
                        Добавленные файлы:
                    </div>
                </div>

                <!-- Контейнер для предпросмотра новых фотографий -->
                <div id="photoPreview" class="photo-preview-container" style="display: none;">
                    <div class="preview-note">
                        <strong>Предпросмотр новых фотографий:</strong> Выбранные фотографии появятся здесь.
                    </div>
                    <div id="previewImages" style="display: flex; flex-wrap: wrap; gap: 10px; margin-top: 15px;"></div>
                </div>
            </div>

            <!-- Теги -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">🏷️</span> Теги
                </h3>

                <!-- ДЛЯ ОТЛАДКИ -->
                <%
                    List<Map<String, Object>> debugCurrentTags = (List<Map<String, Object>>) request.getAttribute("currentTags");
                    System.out.println("DEBUG in JSP - currentTags: " + (debugCurrentTags != null ? debugCurrentTags.size() : "null"));
                    if (debugCurrentTags != null) {
                        for (Map<String, Object> tag : debugCurrentTags) {
                            System.out.println("DEBUG tag in JSP: " + tag);
                        }
                    }
                %>

                <!-- Скрытое поле для хранения выбранных тегов в JSON -->
                <input type="hidden" id="selectedTags" name="selectedTagsJson" value="">

                <!-- Контейнер для тегов с выпадающими списками -->
                <div class="form-group">
                    <label>Доступные теги:</label>
                    <div id="tagsContainer" class="tags-container">
                        <%
                            if (request.getAttribute("availableTags") != null) {
                                List<Map<String, Object>> availableTags = (List<Map<String, Object>>) request.getAttribute("availableTags");
                                List<Map<String, Object>> currentTags = (List<Map<String, Object>>) request.getAttribute("currentTags");

                                // ДЛЯ ОТЛАДКИ
                                System.out.println("=== DEBUG TAGS ===");
                                System.out.println("Available tags: " + (availableTags != null ? availableTags.size() : 0));
                                System.out.println("Current tags: " + (currentTags != null ? currentTags.size() : 0));
                                if (currentTags != null) {
                                    for (Map<String, Object> tag : currentTags) {
                                        System.out.println("Current tag - tagId: " + tag.get("tagId") + ", valueId: " + tag.get("valueId"));
                                    }
                                }

                                if (availableTags != null && !availableTags.isEmpty()) {
                                    for (Map<String, Object> tag : availableTags) {
                                        String tagName = (String) tag.get("name");
                                        Long tagId = (Long) tag.get("id");
                                        List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
                        %>
                        <div class="tag-row" data-tag-id="<%= tagId %>">
                            <div class="tag-header">
                                <span class="tag-name"><%= tagName %></span>
                            </div>
                            <select class="tag-select" data-tag-id="<%= tagId %>" data-tag-name="<%= tagName %>"
                                    <%= !announcement.canBeEdited() ? "disabled" : "" %>>
                                <option value="">Не выбрано</option>
                                <%
                                    if (values != null && !values.isEmpty()) {
                                        for (Map<String, Object> value : values) {
                                            String valueName = (String) value.get("name");
                                            Long valueId = (Long) value.get("id");
                                            boolean isSelected = false;

                                            // Проверяем, выбран ли этот тег
                                            if (currentTags != null) {
                                                for (Map<String, Object> currentTag : currentTags) {
                                                    Long currentTagId = ((Number) currentTag.get("tagId")).longValue();
                                                    Long currentTagValueId = ((Number) currentTag.get("valueId")).longValue();

                                                    // ДЛЯ ОТЛАДКИ
                                                    if (currentTagId.equals(tagId) && currentTagValueId.equals(valueId)) {
                                                        System.out.println("FOUND SELECTED: tagId=" + tagId +
                                                                ", valueId=" + valueId + ", valueName=" + valueName);
                                                        isSelected = true;
                                                        break;
                                                    }

                                                    if (currentTagId.equals(tagId) && currentTagValueId.equals(valueId)) {
                                                        isSelected = true;
                                                        break;
                                                    }
                                                }
                                            }
                                %>
                                <option value="<%= valueId %>"
                                        data-value-name="<%= valueName %>"
                                        <%= isSelected ? "selected" : "" %>>
                                    <%= valueName %>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </div>
                        <%
                                    }
                                }
                            }
                        %>
                    </div>
                </div>
            </div>

            <!-- Действие после сохранения -->
            <% if (announcement.canBeEdited()) { %>
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">⚡</span> Действие после сохранения
                </h3>

                <div class="radio-group">
                    <label class="radio-item">
                        <input type="radio" name="action" value="draft" checked>
                        <span class="radio-label">Сохранить как черновик</span>
                    </label>
                    <label class="radio-item">
                        <input type="radio" name="action" value="publish">
                        <span class="radio-label">Опубликовать (отправить на модерацию)</span>
                    </label>
                </div>
            </div>
            <% } %>

            <!-- Кнопки действий -->
            <div class="form-actions">
                <a href="dashboard.jsp" class="btn btn-outline">
                    <span class="icon">←</span> Назад к списку
                </a>

                <% if (announcement.canBeEdited()) { %>
                <button type="submit" class="btn btn-primary">
                    <span class="icon">💾</span> Сохранить изменения
                </button>
                <% } else { %>
                <a href="edit-ad?action=toDraft&adId=<%= announcement.getId() %>" class="btn btn-warning">
                    <span class="icon">📝</span> Сделать черновиком
                </a>
                <% } %>

                <a href="delete-ad?adId=<%= announcement.getId() %>" class="btn btn-danger"
                   onclick="return confirm('Вы уверены, что хотите удалить это объявление?')">
                    <span class="icon">🗑️</span> Удалить
                </a>
            </div>
        </form>
    </div>
</div>
<script>

    function removePhoto(adId, photoIndex, event = null) {
        console.log('Удаление фото:', adId, photoIndex);

        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }

        if (!confirm('Удалить это фото?')) {
            return false;
        }

        let button;
        if (event) {
            button = event.target;
        } else {
            button = document.querySelector(`[onclick*="removePhoto(${adId}, ${photoIndex})"]`);
            if (!button) {
                button = document.getElementById('remove-photo-' + photoIndex);
            }
        }

        const originalText = button ? button.innerHTML : '×';
        if (button) {
            button.disabled = true;
            button.innerHTML = '...';
        }

        // Сохраняем ссылку на элемент ДО отправки запроса
        const photoElement = document.getElementById('photo-' + photoIndex);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/portal/delete-photo', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

        xhr.onload = function() {
            console.log('Статус ответа:', xhr.status);
            console.log('Ответ:', xhr.responseText);

            if (button) {
                button.disabled = false;
                button.innerHTML = originalText;
            }

            // === ИСПРАВЛЕННАЯ ПРОВЕРКА ===
            if (xhr.status === 200) {
                const response = xhr.responseText.trim();
                console.log('Обработанный ответ:', response);

                if (response === 'success') {
                    // ✅ MediaManager удалил фото из БД
                    console.log('✅ Фото удалено из БД, удаляем из DOM');

                    if (photoElement) {
                        // Плавное исчезновение
                        photoElement.style.transition = 'opacity 0.3s';
                        photoElement.style.opacity = '0';

                        setTimeout(() => {
                            // Удаляем элемент
                            photoElement.remove();
                            console.log('✅ Элемент удалён из DOM');

                            // Обновляем UI
                            updatePhotoIndexes(adId);

                        }, 300);
                    } else {
                        console.warn('Элемент не найден, перезагружаем страницу');
                        setTimeout(() => window.location.reload(), 500);
                    }
                } else {
                }
            } else {
            }
        };

        xhr.onerror = function() {
            if (button) {
                button.disabled = false;
                button.innerHTML = originalText;
            }
        };

        const params = 'adId=' + adId + '&photoIndex=' + photoIndex;
        console.log('Отправляем:', params);
        xhr.send(params);

        return false;
    }

    // === НОВАЯ ФУНКЦИЯ для переиндексации фото ===
    function updatePhotoIndexes(adId) {
        console.log('Обновляем индексы оставшихся фото...');

        const remaining = document.querySelectorAll('[id^="photo-"]');
        console.log('Найдено элементов:', remaining.length);

        remaining.forEach((el, newIndex) => {
            // Обновляем ID элемента
            const oldId = el.id;
            el.id = 'photo-' + newIndex;
            console.log(`Переименован: ${oldId} -> ${el.id}`);

            // Обновляем кнопку удаления
            const btn = el.querySelector('button');
            if (btn) {
                // Полностью перезаписываем обработчик
                btn.onclick = function(e) {
                    return removePhoto(adId, newIndex, e);
                };
                console.log(`Обновлена кнопка для индекса ${newIndex}`);
            }

            // Обновляем изображение если нужно
            const img = el.querySelector('img');
            if (img) {
                // Сохраняем оригинальный src в data-атрибут
                if (!img.dataset.originalSrc) {
                    img.dataset.originalSrc = img.src;
                }
                // Обновляем src с новым индексом
                const newSrc = img.dataset.originalSrc.replace(
                    /photoIndex=\d+/,
                    'photoIndex=' + newIndex
                );
                img.src = newSrc;
            }
        });

        // Также обновим скрытые поля формы если они есть
        const photoCountInput = document.querySelector('input[name="photoCount"]');
        if (photoCountInput) {
            photoCountInput.value = remaining.length;
        }

        console.log('✅ Переиндексация завершена');
    }

    document.addEventListener('DOMContentLoaded', () => {
        // === СИСТЕМА ТЕГОВ ===
        let selectedTags = [];

        // Инициализация тегов из текущих данных
        function initializeTags() {
            console.log('=== INITIALIZING TAGS ===');
            selectedTags = [];

            // Сначала обновляем селекты из данных
            updateSelectsFromDOM();

            // Затем загружаем из селектов
            loadTagsFromSelects();

            console.log('Initial selected tags:', selectedTags);
            updateHiddenFields();
            updateSelectedTagsDisplay();
        }

        // Обновление селектов на основе данных из DOM (уже проставленных сервером)
        function updateSelectsFromDOM() {
            const tagSelects = document.querySelectorAll('.tag-select');
            console.log('Updating selects from DOM, found:', tagSelects.length);

            tagSelects.forEach(select => {
                const tagId = select.getAttribute('data-tag-id');
                const currentValue = select.value;
                const selectedIndex = select.selectedIndex;

                console.log(`Select ${tagId}: value="${currentValue}", selectedIndex=${selectedIndex}`);

                if (currentValue && currentValue !== "" && selectedIndex > 0) {
                    const selectedOption = select.options[selectedIndex];
                    const valueName = selectedOption.getAttribute('data-value-name') || selectedOption.textContent;

                    console.log(`✅ Select ${tagId} has pre-selected value: ${currentValue} - ${valueName}`);
                }
            });
        }

        // Загрузка тегов из выпадающих списков
        function loadTagsFromSelects() {
            const tagSelects = document.querySelectorAll('.tag-select');
            console.log('Loading tags from selects:', tagSelects.length);

            tagSelects.forEach(select => {
                const tagId = parseInt(select.getAttribute('data-tag-id'));
                const tagName = select.getAttribute('data-tag-name');
                const valueId = select.value;
                const selectedIndex = select.selectedIndex;

                console.log(`Processing select: ${tagName} (ID: ${tagId}), value: ${valueId}, index: ${selectedIndex}`);

                // Проверяем, что значение выбрано (не пустое и не "Не выбрано")
                if (valueId && valueId !== "" && selectedIndex > 0) {
                    const selectedOption = select.options[selectedIndex];
                    const valueName = selectedOption.getAttribute('data-value-name') || selectedOption.textContent.trim();

                    // Проверяем, нет ли уже такого тега
                    const existingIndex = selectedTags.findIndex(tag => tag.tagId === tagId);
                    if (existingIndex === -1) {
                        selectedTags.push({
                            tagId: tagId,
                            tagName: tagName,
                            valueId: parseInt(valueId),
                            valueName: valueName
                        });
                    } else {
                    }
                } else {
                }
            });

            console.log('Total tags loaded from selects:', selectedTags.length);
        }

        // Обработчик изменения выпадающих списков тегов
        function setupTagSelectHandlers() {
            const tagSelects = document.querySelectorAll('.tag-select');
            console.log('Setting up handlers for selects:', tagSelects.length);

            tagSelects.forEach(select => {
                select.addEventListener('change', function() {
                    const tagId = parseInt(this.getAttribute('data-tag-id'));
                    const tagName = this.getAttribute('data-tag-name');
                    const valueId = this.value;
                    const selectedOption = this.options[this.selectedIndex];
                    const valueName = selectedOption?.getAttribute('data-value-name') || selectedOption?.textContent;

                    console.log('🔄 Tag changed:', { tagId, tagName, valueId, valueName });

                    // Находим индекс существующего тега
                    const existingIndex = selectedTags.findIndex(tag => tag.tagId === tagId);

                    if (valueId && valueId !== "" && this.selectedIndex > 0) {
                        // Обновляем или добавляем тег
                        const tagData = {
                            tagId: tagId,
                            tagName: tagName,
                            valueId: parseInt(valueId),
                            valueName: valueName
                        };

                        if (existingIndex !== -1) {
                            selectedTags[existingIndex] = tagData;
                            console.log('✅ Updated existing tag');
                        } else {
                            selectedTags.push(tagData);
                            console.log('✅ Added new tag');
                        }
                    } else {
                        // Удаляем тег если выбран "Не выбрано"
                        if (existingIndex !== -1) {
                            selectedTags.splice(existingIndex, 1);
                            console.log('🗑️ Removed tag');
                        }
                    }

                    updateHiddenFields();
                });
            });
        }

        // Обновление отображения выбранных тегов
        function updateSelectedTagsDisplay() {
            const container = document.getElementById('selectedTagsContainer');

            if (!container) {return;
            }

            if (selectedTags.length === 0) {
                container.innerHTML = '<div class="no-tags-message">Теги не выбраны</div>';
                console.log('No tags to display');
                return;
            }

            let html = '';
            selectedTags.forEach(tag => {
                html += `
                    <div class="selected-tag">
                        ${tag.tagName}: ${tag.valueName}
                        <button type="button" class="remove-tag-btn" onclick="removeSelectedTag(${tag.tagId})">
                            ×
                        </button>
                    </div>
                `;
            });

            container.innerHTML = html;
            console.log('Updated tags display with', selectedTags.length, 'tags');
        }

        // Удаление выбранного тега
        function removeSelectedTag(tagId) {
            console.log('Removing tag:', tagId);
            const index = selectedTags.findIndex(tag => tag.tagId === tagId);
            if (index !== -1) {
                selectedTags.splice(index, 1);

                // Сбрасываем соответствующий выпадающий список
                const select = document.querySelector(`.tag-select[data-tag-id="${tagId}"]`);
                if (select) {
                    select.value = '';
                    console.log('Reset select for tag:', tagId);
                }

                updateSelectedTagsDisplay();
                updateHiddenFields();
            }
        }

        // Обновление скрытых полей
        function updateHiddenFields() {
            const hiddenField = document.getElementById('selectedTags');
            const formHiddenField = document.getElementById('formSelectedTags');

            const tagsJson = JSON.stringify(selectedTags);
            if (hiddenField) hiddenField.value = tagsJson;
            if (formHiddenField) formHiddenField.value = tagsJson;

            updateSelectedTagsDisplay();

            console.log('💾 Updated hidden fields with:', selectedTags);
        }

        // === ОБРАБОТКА ПРЕДПРОСМОТРА ФОТОГРАФИЙ (НОВЫЕ ФОТО) ===
        const photoInput = document.getElementById('photos');
        const photoPreview = document.getElementById('photoPreview');
        const previewImages = document.getElementById('previewImages');

        if (photoInput) {
            photoInput.addEventListener('change', function(e) {
                const files = e.target.files;
                previewImages.innerHTML = '';

                if (files.length > 0) {
                    photoPreview.style.display = 'block';

                    for (let i = 0; i < files.length; i++) {
                        const file = files[i];
                        if (file.type.startsWith('image/')) {
                            const reader = new FileReader();

                            reader.onload = function(e) {
                                const imgContainer = document.createElement('div');
                                imgContainer.style.position = 'relative';
                                imgContainer.style.width = '100px';
                                imgContainer.style.height = '100px';

                                const img = document.createElement('img');
                                img.src = e.target.result;
                                img.style.width = '100%';
                                img.style.height = '100%';
                                img.style.objectFit = 'cover';
                                img.style.borderRadius = '8px';
                                img.style.border = '2px solid var(--border)';

                                const removeBtn = document.createElement('button');
                                removeBtn.type = 'button';
                                removeBtn.innerHTML = '×';
                                removeBtn.style.position = 'absolute';
                                removeBtn.style.top = '-8px';
                                removeBtn.style.right = '-8px';
                                removeBtn.style.background = 'var(--danger)';
                                removeBtn.style.color = 'white';
                                removeBtn.style.border = 'none';
                                removeBtn.style.borderRadius = '50%';
                                removeBtn.style.width = '20px';
                                removeBtn.style.height = '20px';
                                removeBtn.style.cursor = 'pointer';
                                removeBtn.style.fontSize = '12px';
                                removeBtn.style.fontWeight = 'bold';

                                removeBtn.addEventListener('click', function() {
                                    imgContainer.remove();
                                    updateFileInput(files, i);

                                    if (previewImages.children.length === 0) {
                                        photoPreview.style.display = 'none';
                                    }
                                });

                                imgContainer.appendChild(img);
                                imgContainer.appendChild(removeBtn);
                                previewImages.appendChild(imgContainer);
                            };

                            reader.readAsDataURL(file);
                        }
                    }
                } else {
                    photoPreview.style.display = 'none';
                }
            });
        }


        function updateFileInput(originalFiles, indexToRemove) {
            const dt = new DataTransfer();

            for (let i = 0; i < originalFiles.length; i++) {
                if (i !== indexToRemove) {
                    dt.items.add(originalFiles[i]);
                }
            }

            photoInput.files = dt.files;
        }
        // === КОНЕЦ ОБРАБОТКИ ФОТОГРАФИЙ ===


// Функция для переиндексации оставшихся фото
        function reindexPhotos() {
            const photoItems = document.querySelectorAll('.photo-item');
            photoItems.forEach((item, newIndex) => {
                // Обновляем ID
                item.id = 'photo-' + newIndex;

                // Обновляем кнопку удаления
                const button = item.querySelector('button');
                const adId = button.getAttribute('onclick').match(/\d+/)[0];
                button.setAttribute('onclick', `removePhoto(${adId}, ${newIndex})`);

                // Обновляем src изображения
                const img = item.querySelector('img');
                const currentSrc = img.src;
                const newSrc = currentSrc.replace(/photoIndex=\d+/, 'photoIndex=' + newIndex);
                img.src = newSrc;
                img.alt = 'Фото ' + (newIndex + 1);
            });
        }

// Функция для показа уведомлений
        function showNotification(message, type) {
            // Проверяем, нет ли уже уведомления
            const existingNotification = document.querySelector('.notification');
            if (existingNotification) {
                existingNotification.remove();
            }

            const notification = document.createElement('div');
            notification.className = 'notification';
            notification.textContent = message;
            notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 24px;
        background: ${type == 'success' ? '#4CAF50' : '#f72585'};
        color: white;
        border-radius: 8px;
        z-index: 9999;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        font-weight: 500;
        animation: slideIn 0.3s ease;
    `;

            document.body.appendChild(notification);

            // Автоматическое скрытие через 3 секунды
            setTimeout(() => {
                notification.style.animation = 'slideOut 0.3s ease';
                setTimeout(() => notification.remove(), 300);
            }, 3000);

            // Добавляем стили для анимации
            if (!document.querySelector('#notification-styles')) {
                const style = document.createElement('style');
                style.id = 'notification-styles';
                style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOut {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
                document.head.appendChild(style);
            }
        }

// Функция для обновления изображений после удаления (если нужно)
        function updatePhotoUrls(adId) {
            const images = document.querySelectorAll('.current-photos img');
            images.forEach((img, index) => {
                img.src = `ad-photo?adId=${adId}&photoIndex=${index}&t=${Date.now()}`;
                img.alt = `Фото ${index + 1}`;
            });
        }

        // Инициализация после полной загрузки DOM
        function initializePage() {
            console.log('=== STARTING PAGE INITIALIZATION ===');
            console.log('DOM readyState:', document.readyState);

            // Настраиваем обработчики сразу
            setupTagSelectHandlers();

            // Ждем немного чтобы браузер успел проставить selected значения
            setTimeout(() => {
                console.log('🕒 Initializing tags after timeout...');
                initializeTags();

                // Дополнительная проверка через секунду
                setTimeout(() => {
                    console.log('🕒 Final check...');
                    const finalCheckSelects = document.querySelectorAll('.tag-select');
                    finalCheckSelects.forEach(select => {
                        if (select.value && select.selectedIndex > 0) {
                            console.log(`Final - Select ${select.getAttribute('data-tag-id')}: ${select.value}`);
                        }
                    });
                }, 1000);

            }, 500); // Увеличиваем задержку
        }

        // Запускаем инициализацию
        initializePage();

        // Делаем функции глобальными для использования в onclick
        window.removeSelectedTag = removeSelectedTag;

        // Дополнительная инициализация если DOM менялся
        window.reinitializeTags = function() {
            console.log('🔄 Manual reinitialization of tags');
            initializeTags();
        };
    });
</script>
</body>
</html>