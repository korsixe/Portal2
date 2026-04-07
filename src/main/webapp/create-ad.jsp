<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.enums.Condition" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>

<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    // Обработка отображения поля цены на сервере
    String priceType = request.getParameter("priceType");
    boolean showPrice = "fixed".equals(priceType);
    if (priceType == null) {
        priceType = "negotiable"; // значение по умолчанию
    }
%>

<html>
<head>
    <title>Создать объявление • Portal</title>
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

        .form-section {
            margin-bottom: 30px;
            padding: 25px;
            background: var(--light);
            border-radius: 15px;
            border-left: 4px solid var(--primary);
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


        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
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

        .alert-info {
            background: rgba(67, 97, 238, 0.1);
            border: 1px solid var(--primary);
            color: var(--primary);
        }

        .tags-hint {
            font-size: 0.9rem;
            color: var(--gray);
            margin-top: 5px;
        }

        .preview-note {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
            border-left: 4px solid var(--warning);
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
        }

        .icon {
            display: inline-block;
            width: 24px;
            height: 24px;
            text-align: center;
            line-height: 24px;
        }

        /* Стили для новой системы тегов */
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

        .no-tags-message{
            color: var(--gray);
            font-style: italic;
            text-align: center;
            padding: 10px;
        }

        .photo-preview-container {
            margin-top: 15px;
            padding: 15px;
            background: var(--light);
            border-radius: 10px;
            border: 2px dashed var(--border);
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
            <h1 class="card-title">Создать новое объявление</h1>
            <p class="card-subtitle">Заполните информацию о вашем товаре или услуге</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error">
            <span class="icon">⚠</span> <%= request.getAttribute("error") %>
        </div>
        <% } %>

        <% if (request.getAttribute("success") != null) { %>
        <div class="alert alert-success">
            <span class="icon">✓</span> <%= request.getAttribute("success") %>
        </div>
        <% } %>

        <!-- Основная форма для создания объявления -->
        <form action="create-ad" method="post" enctype="multipart/form-data">
            <!-- Основная информация -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📝</span> Основная информация
                </h3>

                <div class="form-group">
                    <label for="title" class="required">Заголовок объявления</label>
                    <input type="text" id="title" name="title" class="form-control"
                           placeholder="Например: iPhone 13 Pro Max 256GB" required
                           value="<%= request.getParameter("title") != null ? request.getParameter("title") : "" %>">
                </div>

                <div class="form-group">
                    <label for="description" class="required">Описание</label>
                    <textarea id="description" name="description" class="form-control"
                              placeholder="Подробно опишите ваш товар или услугу..." required><%=
                    request.getParameter("description") != null ? request.getParameter(
                            "description") : "" %></textarea>
                </div>
            </div>

            <!-- Категории -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📂</span> Категория
                </h3>

                <div class="form-group">
                    <label for="category" class="required">Основная категория</label>
                    <select id="category" name="category" class="form-control" required>
                        <option value="">Выберите категорию</option>
                        <%
                            // Получаем категории из атрибута request (переданы из контроллера)
                            List<Map<String, Object>> categories = (List<Map<String, Object>>) request.getAttribute("categories");
                            String currentCategoryParam = request.getParameter("category");

                            if (categories != null && !categories.isEmpty()) {
                                for (Map<String, Object> category : categories) {
                                    String categoryName = (String) category.get("name");
                                    boolean isSelected = categoryName.equals(currentCategoryParam);
                        %>
                        <option value="<%= categoryName %>" <%= isSelected ? "selected" : "" %>>
                            <%= categoryName %>
                        </option>
                        <%
                            }
                        } else {
                        %>
                        <option value="">Нет доступных категорий</option>
                        <%
                            }
                        %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="subcategory" class="required">Подкатегория</label>
                    <select id="subcategory" name="subcategory" class="form-control" required>
                        <%
                            String chosenCategory = request.getParameter("category");
                            List<Map<String, Object>> subcategories = (List<Map<String, Object>>) request.getAttribute("subcategories");
                            String currentSubcategoryParam = request.getParameter("subcategory");

                            if (chosenCategory == null || chosenCategory.isEmpty()) {
                        %>
                        <option value="">Сначала выберите категорию</option>
                        <%
                        } else if (subcategories == null || subcategories.isEmpty()) {
                        %>
                        <option value="">Нет доступных подкатегорий</option>
                        <%
                        } else {
                        %>
                        <option value="">Выберите подкатегорию</option>
                        <%
                            for (Map<String, Object> subcategory : subcategories) {
                                String subcategoryName = (String) subcategory.get("name");
                                boolean isSelected = subcategoryName.equals(currentSubcategoryParam);
                        %>
                        <option value="<%= subcategoryName %>" <%= isSelected ? "selected" : "" %>>
                            <%= subcategoryName %>
                        </option>
                        <%
                                }
                            }
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
                           value="<%= request.getParameter("location") != null ? request.getParameter("location") : "" %>">
                </div>

                <div class="form-group">
                    <label class="required">Состояние товара</label>
                    <div class="radio-group">
                        <% for (Condition condition : Condition.values()) { %>
                        <label class="radio-item">
                            <input type="radio" name="condition"
                                   value="<%= condition.name() %>" required
                                <%= (request.getParameter("condition") != null && request.getParameter("condition").equals(condition.name())) ? "checked" : "" %>>
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
                        <%
                            String currentPriceType = request.getParameter("priceType");
                            if (currentPriceType == null) {
                                currentPriceType = "negotiable";
                            }
                        %>
                        <label class="radio-item">
                            <input type="radio" name="priceType" value="negotiable"
                                <%= "negotiable".equals(currentPriceType) ? "checked" : "" %>>
                            <span class="radio-label">Договорная</span>
                        </label>
                        <label class="radio-item">
                            <input type="radio" name="priceType" value="free"
                                <%= "free".equals(currentPriceType) ? "checked" : "" %>>
                            <span class="radio-label">Бесплатно</span>
                        </label>
                        <label class="radio-item">
                            <input type="radio" name="priceType" value="fixed"
                                <%= "fixed".equals(currentPriceType) ? "checked" : "" %>>
                            <span class="radio-label">Указать цену</span>
                        </label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="price">Цена (руб.)</label>
                    <input type="number" id="price" name="price" class="form-control"
                           min="1" max="1000000000" placeholder="1000"
                           value="<%= request.getParameter("price") != null ? request.getParameter("price") : "" %>">
                    <div class="tags-hint">
                        <strong>Напишите цену, если выбрали пункт "Указать цену"</strong>
                    </div>
                </div>
            </div>

            <!-- Фотография (одно фото) -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">📷</span> Фотография
                </h3>

                <div class="form-group">
                    <label for="photo">Добавить фотографию</label>
                    <input type="file" id="photo" name="photo" class="form-control" accept="image/*">
                    <div class="tags-hint">
                        Добавьте фото товара (необязательно). Поддерживаются форматы: JPG, PNG, GIF.
                    </div>
                </div>

                <!-- Контейнер для предпросмотра -->
                <div id="photoPreview" class="photo-preview-container" style="display: none;">
                    <div id="previewImage" style="margin-top: 15px;"></div>
                </div>
            </div>


            <!-- Теги -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">🏷️</span> Теги
                </h3>

                <!-- Скрытое поле для хранения выбранных тегов в JSON -->
                <input type="hidden" id="selectedTags" name="selectedTags" value="<%= request.getParameter("selectedTags") != null ? request.getParameter("selectedTags") : "" %>">

                <!-- Контейнер для тегов с выпадающими списками -->
                <div class="form-group">
                    <label>Доступные теги:</label>
                    <div id="tagsContainer" class="tags-container">
                        <%
                            List<Map<String, Object>> availableTags = (List<Map<String, Object>>) request.getAttribute("tags");
                            if (availableTags != null && !availableTags.isEmpty()) {
                                for (Map<String, Object> tag : availableTags) {
                                    String tagName = (String) tag.get("name");
                                    Long tagId = ((Number) tag.get("id")).longValue();
                                    List<Map<String, Object>> values = (List<Map<String, Object>>) tag.get("values");
                        %>
                        <div class="tag-row" data-tag-id="<%= tagId %>">
                            <div class="tag-header">
                                <span class="tag-name"><%= tagName %></span>
                            </div>
                            <select class="tag-select" data-tag-id="<%= tagId %>" data-tag-name="<%= tagName %>">
                                <option value="">Не выбрано</option>
                                <%
                                    if (values != null && !values.isEmpty()) {
                                        for (Map<String, Object> value : values) {
                                            String valueName = (String) value.get("name");
                                            Long valueId = ((Number) value.get("id")).longValue();
                                %>
                                <option value="<%= valueId %>" data-value-name="<%= valueName %>">
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
                        } else {
                        %>
                        <div class="no-tags-message">Нет доступных тегов</div>
                        <%
                            }
                        %>
                    </div>
                </div>
            </div>

            <!-- Действие после создания -->
            <div class="form-section">
                <h3 class="section-title">
                    <span class="icon">⚡</span> Действие после создания
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

            <!-- Кнопки действий -->
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline">
                    <span class="icon">←</span> Отмена
                </a>

                <button type="submit" class="btn btn-primary">
                    <span class="icon">✓</span> Создать объявление
                </button>
            </div>
        </form>
    </div>
</div>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        const categorySelect = document.getElementById('category');
        const subcategorySelect = document.getElementById('subcategory');

        // При изменении категории перезагружаем страницу
        categorySelect.addEventListener('change', function() {
            submitFormWithCategory(this.value);
        });

        function submitFormWithCategory(categoryValue) {
            // Создаем временную форму для отправки данных
            const form = document.createElement('form');
            form.method = 'GET';
            form.action = '/create-ad';

            // Добавляем выбранную категорию
            addHiddenField(form, 'category', categoryValue);

            // Сохраняем другие поля формы
            const fieldsToSave = ['title', 'description', 'location', 'priceType', 'price'];
            fieldsToSave.forEach(fieldName => {
                const field = document.querySelector('[name="' + fieldName + '"]');
                if (field && (field.value || field.checked)) {
                    if (field.type === 'radio') {
                        const checkedRadio = document.querySelector('[name="' + fieldName + '"]:checked');
                        if (checkedRadio) {
                            addHiddenField(form, fieldName, checkedRadio.value);
                        }
                    } else {
                        addHiddenField(form, fieldName, field.value);
                    }
                }
            });

            // Сохраняем выбранные теги
            const selectedTagsField = document.getElementById('selectedTags');
            if (selectedTagsField && selectedTagsField.value) {
                addHiddenField(form, 'selectedTags', selectedTagsField.value);
            }

            document.body.appendChild(form);
            form.submit();
        }

        function addHiddenField(form, name, value) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = name;
            input.value = value;
            form.appendChild(input);
        }

        // Обработка типа цены
        const priceTypeRadios = document.querySelectorAll('input[name="priceType"]');
        const priceInput = document.getElementById('price');

        function updatePriceFieldVisibility() {
            const selectedType = document.querySelector('input[name="priceType"]:checked');
            if (selectedType && selectedType.value === 'fixed') {
                priceInput.style.display = 'block';
                priceInput.required = true;
            } else {
                priceInput.style.display = 'none';
                priceInput.required = false;
                if (priceInput) priceInput.value = '';
            }
        }

        // Инициализация обработчиков цены
        if (priceTypeRadios.length > 0) {
            priceTypeRadios.forEach(radio => {
                radio.addEventListener('change', updatePriceFieldVisibility);
            });
            updatePriceFieldVisibility();
        }

        // === СИСТЕМА ТЕГОВ С ВЫПАДАЮЩИМИ СПИСКАМИ ===
        let selectedTags = [];

        // Восстанавливаем выбранные теги если они есть
        const hiddenTagsField = document.getElementById('selectedTags');
        if (hiddenTagsField && hiddenTagsField.value) {
            try {
                selectedTags = JSON.parse(hiddenTagsField.value);
                restoreSelectedTags();
            } catch (e) {
                console.error('Error parsing saved tags:', e);
            }
        }

        // Обработчик изменения выпадающих списков тегов
        const tagSelects = document.querySelectorAll('.tag-select');
        tagSelects.forEach(select => {
            select.addEventListener('change', function() {
                const tagId = this.getAttribute('data-tag-id');
                const tagName = this.getAttribute('data-tag-name');
                const valueId = this.value;
                const valueName = this.options[this.selectedIndex]?.getAttribute('data-value-name');

                if (valueId && valueName) {
                    // Добавляем или обновляем тег
                    const existingIndex = selectedTags.findIndex(tag => tag.tagId == tagId);

                    if (existingIndex !== -1) {
                        // Обновляем существующий тег
                        selectedTags[existingIndex] = {
                            tagId: parseInt(tagId),
                            tagName: tagName,
                            valueId: parseInt(valueId),
                            valueName: valueName
                        };
                    } else {
                        // Добавляем новый тег
                        selectedTags.push({
                            tagId: parseInt(tagId),
                            tagName: tagName,
                            valueId: parseInt(valueId),
                            valueName: valueName
                        });
                    }
                } else {
                    // Удаляем тег если выбран "Не выбрано"
                    const index = selectedTags.findIndex(tag => tag.tagId == tagId);
                    if (index !== -1) {
                        selectedTags.splice(index, 1);
                    }
                }

                updateHiddenFields();
            });
        });

        // Восстановление выбранных тегов в выпадающих списках
        function restoreSelectedTags() {
            selectedTags.forEach(tag => {
                const select = document.querySelector(`.tag-select[data-tag-id="${tag.tagId}"]`);
                if (select) {
                    select.value = tag.valueId;
                }
            });
        }

        // Обновление скрытых полей
        function updateHiddenFields() {
            const hiddenField = document.getElementById('selectedTags');
            hiddenField.value = JSON.stringify(selectedTags);
        }

        // === ОБРАБОТКА ПРЕДПРОСМОТРА ФОТОГРАФИИ ===
        const photoInput = document.getElementById('photo');
        const photoPreview = document.getElementById('photoPreview');
        const previewImage = document.getElementById('previewImage');

        if (photoInput) {
            photoInput.addEventListener('change', function(e) {
                const file = e.target.files[0];

                if (previewImage) {
                    previewImage.innerHTML = '';
                }

                if (file && file.type.startsWith('image/')) {
                    if (photoPreview) photoPreview.style.display = 'block';

                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.style.width = '200px';
                        img.style.height = '200px';
                        img.style.objectFit = 'contain';
                        img.style.borderRadius = '8px';
                        img.style.border = '2px solid var(--border)';

                        if (previewImage) {
                            previewImage.appendChild(img);
                        }
                    };
                    reader.readAsDataURL(file);
                } else {
                    if (photoPreview) photoPreview.style.display = 'none';
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
    });
</script>
</body>
</html>