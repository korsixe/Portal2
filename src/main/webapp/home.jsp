<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mipt.portal.users.User" %>
<%@ page import="com.mipt.portal.announcement.AdsService" %>
<%@ page import="com.mipt.portal.announcement.Announcement" %>
<%@ page import="com.mipt.portal.announcement.AdsFilter" %>
<%@ page import="com.mipt.portal.announcement.Category" %>
<%@ page import="com.mipt.portal.announcement.Condition" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.sql.SQLException" %>
<%
    // Проверяем авторизацию пользователя
    User user = (User) session.getAttribute("user");

    // Получаем параметры фильтрации
    String categoryFilter = request.getParameter("category");
    String conditionFilter = request.getParameter("condition");
    String minPriceStr = request.getParameter("minPrice");
    String maxPriceStr = request.getParameter("maxPrice");
    String searchQuery = request.getParameter("searchQuery");

    Integer minPrice = null;
    Integer maxPrice = null;

    try {
        if (minPriceStr != null && !minPriceStr.isEmpty()) {
            minPrice = Integer.parseInt(minPriceStr);
        }
        if (maxPriceStr != null && !maxPriceStr.isEmpty()) {
            maxPrice = Integer.parseInt(maxPriceStr);
        }
    } catch (NumberFormatException e) {
        // Игнорируем неверные значения
    }

    // Получаем все активные объявления
    AdsService adsService = new AdsService();
    AdsFilter adsFilter = new AdsFilter(adsService.getAdsRepository());
    List<Long> recentAdsIds = null;
    try {
        recentAdsIds = adsService.getActiveAdIds();
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }

    if (searchQuery != null) {
        try {
            recentAdsIds = adsService.searchAdsByString(recentAdsIds, searchQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    if (categoryFilter != null && !categoryFilter.isEmpty()) {
        try {
            recentAdsIds = adsFilter.filterByCategory(recentAdsIds,
                    Category.valueOf(categoryFilter));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    if (conditionFilter != null && !conditionFilter.isEmpty()) {
        try {
            recentAdsIds = adsFilter.filterByCondition(recentAdsIds,
                    Condition.valueOf(conditionFilter));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    minPrice = minPrice == null ? -1 : minPrice;
    maxPrice = maxPrice == null ? 2000000000 : maxPrice;

    try {
        recentAdsIds = adsFilter.filterByPrice(recentAdsIds, minPrice, maxPrice);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }

    List<Announcement> recentAds = new ArrayList<>();

    for (long idAd : recentAdsIds) {
        recentAds.add(adsService.getAd(idAd));
    }
%>


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
        padding: 20px;
      }

      .home-container {
        max-width: 1400px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: 300px 1fr;
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
        grid-column: 1 / -1;
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

      .search-section {
        flex: 1;
        max-width: 600px;
        min-width: 300px;
      }

      .search-form {
        display: flex;
        gap: 10px;
      }

      .search-input {
        flex: 1;
        padding: 15px 20px;
        border: 2px solid #e1e5e9;
        border-radius: 12px;
        font-size: 1rem;
        transition: all 0.3s ease;
      }

      .search-input:focus {
        outline: none;
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }

      .search-btn {
        padding: 15px 25px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        border: none;
        border-radius: 12px;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
      }

      .search-btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
      }

      .auth-buttons {
        display: flex;
        gap: 10px;
      }

      /* Боковая панель с фильтрами */
      .filters-sidebar {
        background: white;
        border-radius: 20px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
        padding: 30px;
        height: fit-content;
        position: sticky;
        top: 20px;
      }

      .filters-title {
        font-size: 1.5rem;
        font-weight: 700;
        color: #333;
        margin-bottom: 25px;
        display: flex;
        align-items: center;
        gap: 10px;
      }

      .filter-section {
        margin-bottom: 30px;
        padding-bottom: 25px;
        border-bottom: 1px solid #e9ecef;
      }

      .filter-section:last-child {
        border-bottom: none;
        margin-bottom: 0;
      }

      .filter-label {
        font-size: 1.1rem;
        font-weight: 600;
        color: #333;
        margin-bottom: 15px;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .price-inputs {
        display: flex;
        gap: 10px;
        margin-bottom: 10px;
      }

      .price-input {
        flex: 1;
        padding: 12px 15px;
        border: 2px solid #e1e5e9;
        border-radius: 8px;
        font-size: 0.9rem;
        transition: all 0.3s ease;
      }

      .price-input:focus {
        outline: none;
        border-color: #667eea;
        box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
      }

      .filter-select {
        width: 100%;
        padding: 12px 15px;
        border: 2px solid #e1e5e9;
        border-radius: 8px;
        font-size: 0.9rem;
        background: white;
        cursor: pointer;
        transition: all 0.3s ease;
      }

      .filter-select:focus {
        outline: none;
        border-color: #667eea;
        box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
      }

      .filter-options {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }

      .filter-option {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        padding: 8px 0;
        transition: color 0.3s ease;
      }

      .filter-option:hover {
        color: #667eea;
      }

      .filter-option input[type="radio"] {
        margin: 0;
      }

      .filter-actions {
        display: flex;
        gap: 10px;
        margin-top: 20px;
      }

      .btn-apply {
        flex: 1;
        padding: 12px 20px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: white;
        border: none;
        border-radius: 8px;
        font-size: 0.9rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
      }

      .btn-apply:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      }

      .btn-reset {
        padding: 12px 20px;
        background: transparent;
        color: #666;
        border: 1px solid #ddd;
        border-radius: 8px;
        font-size: 0.9rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
      }

      .btn-reset:hover {
        background: #f8f9fa;
        border-color: #667eea;
        color: #667eea;
      }

      /* Основной контент */
      .main-content {
        background: white;
        border-radius: 20px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
        padding: 40px;
        min-height: 500px;
      }

      .content-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 30px;
        flex-wrap: wrap;
        gap: 15px;
      }

      .section-title {
        color: #333;
        font-size: 2rem;
        margin: 0;
      }

      .results-count {
        color: #666;
        font-size: 1rem;
      }

      /* Сетка объявлений */
      .ads-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 25px;
        margin-bottom: 40px;
      }

      .ad-card {
        background: #f8f9fa;
        border-radius: 15px;
        padding: 25px;
        border-left: 4px solid #667eea;
        transition: all 0.3s ease;
        cursor: pointer;
      }

      .ad-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      }

      .ad-title {
        font-size: 1.3rem;
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
        line-height: 1.3;
      }

      .ad-price {
        font-size: 1.5rem;
        font-weight: 700;
        color: #667eea;
        margin-bottom: 15px;
      }

      .ad-description {
        color: #666;
        line-height: 1.5;
        margin-bottom: 15px;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .ad-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 15px;
      }

      .ad-category {
        background: #667eea;
        color: white;
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 500;
      }

      .ad-condition {
        background: #28a745;
        color: white;
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 500;
      }

      .ad-location {
        color: #666;
        font-size: 0.9rem;
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        gap: 5px;
      }

      .ad-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 15px;
        padding-top: 15px;
        border-top: 1px solid #e9ecef;
      }

      .ad-date {
        color: #999;
        font-size: 0.8rem;
      }

      .ad-image {
          width: 100%;
          aspect-ratio: 4 / 3;
          border-radius: 12px;
          margin-bottom: 15px;
          overflow: hidden;
          background: #f8f9fa;
          display: flex;
          align-items: center;
          justify-content: center;
      }

      .ad-image img {
          width: 100%;
          height: 100%;
          object-fit: cover;
      }

      .no-image {
          font-size: 2rem;
          color: #ccc;
      }

      .ad-views {
        color: #666;
        font-size: 0.8rem;
        display: flex;
        align-items: center;
        gap: 5px;
      }

      /* Сообщение о пустом списке */
      .no-ads {
        text-align: center;
        padding: 60px 20px;
        color: #666;
        grid-column: 1 / -1;
      }

      .no-ads-icon {
        font-size: 4rem;
        margin-bottom: 20px;
        opacity: 0.5;
      }

      .no-ads h3 {
        font-size: 1.5rem;
        margin-bottom: 10px;
        color: #333;
      }

      .no-ads p {
        font-size: 1.1rem;
        line-height: 1.6;
        max-width: 500px;
        margin: 0 auto;
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

      /* Стили для автодополнения */
      .autocomplete-suggestions {
          position: absolute;
          top: 100%;
          left: 0;
          right: 0;
          background: white;
          border: 2px solid #667eea;
          border-top: none;
          border-radius: 0 0 12px 12px;
          max-height: 200px;
          overflow-y: auto;
          z-index: 1001;
          box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
          pointer-events: auto;
      }

      .autocomplete-suggestion {
          padding: 12px 20px;
          cursor: pointer;
          border-bottom: 1px solid #f0f0f0;
          transition: background-color 0.2s ease;
          pointer-events: auto;
          user-select: none;
      }

      .autocomplete-suggestion:hover {
          background-color: #f8f9fa;
      }

      .autocomplete-suggestion:last-child {
          border-bottom: none;
      }

      .autocomplete-suggestion strong {
          color: #667eea;
          font-weight: 600;
      }

      .search-section {
          flex: 1;
          max-width: 600px;
          min-width: 300px;
          position: relative;
      }

      .search-form {
          display: flex;
          gap: 10px;
          position: relative;
      }

      .autocomplete-suggestion.active {
          background-color: #667eea;
          color: white;
      }

      .autocomplete-suggestion.active strong {
          color: white;
          font-weight: bold;
      }


      @media (max-width: 1024px) {
          .home-container {
              grid-template-columns: 1fr;
              gap: 20px;
          }

          .filters-sidebar {
              position: static;
              order: 2;
          }

          .main-content {
              order: 1;
          }

          @media (max-width: 768px) {
              .header {
                  flex-direction: column;
                  text-align: center;
              }

              .search-section {
                  max-width: 100%;
              }

              .search-form {
                  flex-direction: column;
              }

              .auth-buttons {
                  justify-content: center;
              }

              .ads-grid {
                  grid-template-columns: 1fr;
              }

              .section-title {
                  font-size: 1.7rem;
              }

              .content-header {
                  flex-direction: column;
                  align-items: flex-start;
              }
          }

          @media (max-width: 480px) {
              .header {
                  padding: 20px;
              }

              .main-content {
                  padding: 25px 20px;
              }

              .filters-sidebar {
                  padding: 20px;
              }

              .portal-logo {
                  font-size: 2rem;
              }

              .btn {
                  padding: 10px 20px;
                  font-size: 0.9rem;
              }

              .price-inputs {
                  flex-direction: column;
              }
          }
      }
    </style>
</head>
<body>
<div class="home-container">
    <!-- Шапка с поиском -->
    <div class="header">
        <div class="portal-logo">PORTAL</div>

        <div class="search-section">
            <form class="search-form" method="GET" action="home.jsp" id="searchForm">
                <input type="text"
                       class="search-input"
                       placeholder="🔍 Поиск объявлений..."
                       name="searchQuery"
                       id="searchInput"
                       value="<%= request.getParameter("searchQuery") != null ? request.getParameter("searchQuery") : "" %>"
                       autocomplete="off">
                <button type="submit" class="search-btn">Найти</button>
                <div class="autocomplete-suggestions" id="autocompleteSuggestions" style="display: none;"></div>
            </form>
        </div>

        <div class="auth-buttons">
            <% if (user != null) { %>
            <a href="dashboard.jsp" class="btn btn-primary">Личный кабинет</a>
            <a href="logout.jsp" class="btn btn-secondary">Выйти</a>
            <% } else { %>
            <a href="login.jsp" class="btn btn-secondary">Войти</a>
            <a href="register.jsp" class="btn btn-primary">Регистрация</a>
            <% } %>
        </div>
    </div>

    <!-- Боковая панель с фильтрами -->
    <div class="filters-sidebar">
        <h2 class="filters-title">🔍 Фильтры</h2>

        <form id="filterForm" method="GET" action="">
            <!--Фильтр по поисковой строке-->
            <% if (searchQuery != null && !searchQuery.isEmpty()) { %>
            <input type="hidden" name="searchQuery" value="<%= searchQuery %>">
            <% } %>
            <!-- Фильтр по цене -->
            <div class="filter-section">
                <div class="filter-label">💰 Цена</div>
                <div class="price-inputs">
                    <input type="number"
                           class="price-input"
                           placeholder="Цена от"
                           name="minPrice"
                           value="<%= minPriceStr != null ? minPriceStr : "" %>">
                </div>
                <div class="price-inputs">
                    <input type="number"
                           class="price-input"
                           placeholder="Цена до"
                           name="maxPrice"
                           value="<%= maxPriceStr != null ? maxPriceStr : "" %>">
                </div>
                <div style="color: #666; font-size: 0.8rem; margin-top: 5px;">
                    Цена в рублях
                </div>
            </div>

            <!-- Фильтр по категории -->
            <div class="filter-section">
                <div class="filter-label">📂 Категория</div>
                <select class="filter-select" name="category">
                    <option value="">Все категории</option>
                    <% for (Category cat : Category.values()) { %>
                    <option value="<%= cat.name() %>"
                            <%= (categoryFilter != null && categoryFilter.equals(cat.name()))
                                    ? "selected" : "" %>>
                        <%= cat.getDisplayName() %>
                    </option>
                    <% } %>
                </select>
            </div>

            <!-- Фильтр по состоянию -->
            <div class="filter-section">
                <div class="filter-label">🔄 Состояние</div>
                <div class="filter-options">
                    <label class="filter-option">
                        <input type="radio" name="condition" value=""
                            <%= (conditionFilter == null || conditionFilter.isEmpty()) ? "checked" : "" %>>
                        <span>Все состояния</span>
                    </label>
                    <% for (Condition cond : Condition.values()) { %>
                    <label class="filter-option">
                        <input type="radio" name="condition" value="<%= cond.name() %>"
                            <%= (conditionFilter != null && conditionFilter.equals(cond.name())) ? "checked" : "" %>>
                        <span><%= cond.getDisplayName() %></span>
                    </label>
                    <% } %>
                </div>
            </div>

            <!-- Кнопки фильтрации -->
            <div class="filter-actions">
                <button type="submit" class="btn-apply">Применить фильтры</button>
                <button type="button" class="btn-reset" onclick="resetFilters()">Сбросить</button>
            </div>
        </form>
    </div>

    <!-- Основной контент -->
    <div class="main-content">
        <div class="content-header">
            <h1 class="section-title">🎯 Объявления</h1>
            <div class="results-count">
                Найдено: <%= recentAds.size() %> объявлений
            </div>
        </div>

        <% if (recentAds.isEmpty()) { %>
        <!-- Сообщение, если объявлений нет -->
        <div class="no-ads">
            <div class="no-ads-icon">📭</div>
            <h3>Объявлений не найдено</h3>
            <p><em>Попробуйте изменить параметры фильтрации или сбросить фильтры</em></p>
            <% if (user != null) { %>
            <a href="create-ad.jsp" class="btn btn-primary" style="margin-top: 20px;">
                + Создать объявление
            </a>
            <% } else { %>
            <p style="margin-top: 20px;">
                Войдите или зарегистрируйтесь, чтобы разместить объявление
            </p>
            <% } %>
        </div>
        <% } else { %>
        <!-- Сетка объявлений -->
        <div class="ads-grid">
            <% for (Announcement ad : recentAds) { %>
            <div class="ad-card" onclick="location.href='ad-details.jsp?id=<%= ad.getId() %>'">
                <div class="ad-title"><%= ad.getTitle() %>
                </div>

                <div class="ad-image">
                    <img src="<%= request.getContextPath() %>/ad-photo?adId=<%= ad.getId() %>&photoIndex=0&thumbnail=true"
                         alt="<%= ad.getTitle() %>"
                         onerror="this.style.display='none'; this.parentElement.innerHTML='<div class=\'no-image\'>📷</div>';">
                </div>

                <div class="ad-price">
                    <%= formatPrice(ad.getPrice()) %>
                </div>

                <div class="ad-meta">
                    <span class="ad-category"><%= ad.getCategory().getDisplayName() %></span>
                    <span class="ad-condition"><%= ad.getCondition().getDisplayName() %></span>
                </div>

                <div class="ad-location">
                    📍 <%= ad.getLocation() %>
                </div>

                <div class="ad-description">
                    <%= ad.getDescription() %>
                </div>

                <div class="ad-footer">
                    <div class="ad-date">
                        📅 <%= formatDate(ad.getCreatedAt()) %>
                    </div>
                    <div class="ad-views">
                        👁️ <%= ad.getViewCount() != null ? ad.getViewCount() : 0 %>
                    </div>
                </div>
            </div>
            <% } %>
        </div>

        <!-- Кнопка "Показать еще" -->
        <div style="text-align: center; margin-top: 30px;">
            <button class="btn btn-secondary" onclick="loadMoreAds()">
                📄 Показать еще объявления
            </button>
        </div>
        <% } %>
    </div>
</div>

<script>
    // Функция для загрузки дополнительных объявлений
    function loadMoreAds() {
        alert('Функция "Показать еще" будет реализована позже');
    }

    // Функция сброса фильтров
    function resetFilters() {
        document.getElementById('filterForm').reset();
        document.getElementById('filterForm').submit();
    }

    // Функция для автодополнения поиска
    function setupAutocomplete() {
        const searchInput = document.getElementById('searchInput');
        const suggestionsContainer = document.getElementById('autocompleteSuggestions');
        let currentRequest = null;

        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.trim();
            console.log('Autocomplete input:', query);

            // Отменяем предыдущий запрос
            if (currentRequest) {
                currentRequest.abort();
            }

            if (query.length < 2) {
                suggestionsContainer.style.display = 'none';
                return;
            }

            // Показываем индикатор загрузки
            suggestionsContainer.innerHTML = '<div class="autocomplete-suggestion">Поиск...</div>';
            suggestionsContainer.style.display = 'block';

            // Создаем новый AJAX запрос
            currentRequest = new XMLHttpRequest();

            // ВАЖНО: Добавляем timestamp для избежания кэширования
            const url = 'autocomplete.jsp?query=' + encodeURIComponent(query) + '&t=' + Date.now();
            console.log('Request URL:', url);

            currentRequest.open('GET', url);

            currentRequest.onreadystatechange = function() {
                if (currentRequest.readyState === 4) {
                    console.log('Response status:', currentRequest.status);
                    console.log('Response text:', currentRequest.responseText.substring(0, 100));

                    if (currentRequest.status === 200) {
                        try {
                            const suggestions = JSON.parse(currentRequest.responseText);
                            console.log('Parsed suggestions:', suggestions);

                            if (suggestions.length > 0) {
                                displaySuggestions(suggestions, query);
                            } else {
                                suggestionsContainer.innerHTML = '<div class="autocomplete-suggestion">Не найдено</div>';
                                suggestionsContainer.style.display = 'block';
                            }
                        } catch (e) {
                            console.error('JSON parse error:', e);
                            suggestionsContainer.style.display = 'none';
                        }
                    } else {
                        console.error('Request failed:', currentRequest.status);
                        suggestionsContainer.style.display = 'none';
                    }
                    currentRequest = null;
                }
            };

            currentRequest.onerror = function() {
                console.error('Network error');
                suggestionsContainer.style.display = 'none';
                currentRequest = null;
            };

            currentRequest.send();
        });

        // Обработка клавиш
        searchInput.addEventListener('keydown', function(e) {
            const visibleSuggestions = Array.from(suggestionsContainer.querySelectorAll('.autocomplete-suggestion'));
            const activeSuggestion = suggestionsContainer.querySelector('.autocomplete-suggestion.active');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (visibleSuggestions.length > 0) {
                    if (!activeSuggestion) {
                        visibleSuggestions[0].classList.add('active');
                    } else {
                        const currentIndex = visibleSuggestions.indexOf(activeSuggestion);
                        const nextIndex = (currentIndex + 1) % visibleSuggestions.length;
                        activeSuggestion.classList.remove('active');
                        visibleSuggestions[nextIndex].classList.add('active');
                    }
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (visibleSuggestions.length > 0 && activeSuggestion) {
                    const currentIndex = visibleSuggestions.indexOf(activeSuggestion);
                    const prevIndex = currentIndex > 0 ? currentIndex - 1 : visibleSuggestions.length - 1;
                    activeSuggestion.classList.remove('active');
                    visibleSuggestions[prevIndex].classList.add('active');
                }
            } else if (e.key === 'Enter' && activeSuggestion) {
                e.preventDefault();
                searchInput.value = activeSuggestion.textContent;
                suggestionsContainer.style.display = 'none';
                document.getElementById('searchForm').submit();
            } else if (e.key === 'Escape') {
                suggestionsContainer.style.display = 'none';
            }
        });

        // Обновленная функция displaySuggestions
        function displaySuggestions(suggestions, query) {
            suggestionsContainer.innerHTML = '';

            suggestions.forEach((suggestion, index) => {
                const div = document.createElement('div');
                div.className = 'autocomplete-suggestion';
                if (index === 0) div.classList.add('active');

                const lowerSuggestion = suggestion.toLowerCase();
                const lowerQuery = query.toLowerCase();
                const startIndex = lowerSuggestion.indexOf(lowerQuery);

                if (startIndex >= 0) {
                    const before = suggestion.substring(0, startIndex);
                    const match = suggestion.substring(startIndex, startIndex + query.length);
                    const after = suggestion.substring(startIndex + query.length);

                    div.innerHTML = before + '<strong>' + match + '</strong>' + after;
                } else {
                    div.textContent = suggestion;
                }

                div.addEventListener('mouseenter', function() {
                    suggestionsContainer.querySelectorAll('.autocomplete-suggestion.active')
                        .forEach(el => el.classList.remove('active'));
                    div.classList.add('active');
                });

                div.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    searchInput.value = suggestion;
                    suggestionsContainer.style.display = 'none';
                    setTimeout(() => {
                        document.getElementById('searchForm').submit();
                    }, 50);
                });

                suggestionsContainer.appendChild(div);
            });

            suggestionsContainer.style.display = 'block';
        }

        // Скрываем подсказки при клике вне поля
        document.addEventListener('click', function(e) {
            if (!searchInput.contains(e.target) && !suggestionsContainer.contains(e.target)) {
                suggestionsContainer.style.display = 'none';
            }
        });
    }

    // Анимация появления карточек и инициализация автодополнения
    document.addEventListener('DOMContentLoaded', function () {
        const cards = document.querySelectorAll('.ad-card');
        cards.forEach((card, index) => {
            card.style.animationDelay = (index * 0.1) + 's';
            card.style.animation = 'fadeInUp 0.6s ease-out forwards';
        });

        // Инициализация автодополнения
        setupAutocomplete();
    });

    // Добавляем стили для анимации
    const style = document.createElement('style');
    style.textContent = `
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

    .ad-card {
        opacity: 0;
    }
`;
    document.head.appendChild(style);
</script>
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
        if (instant == null) {
            return "Не указано";
        }
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        .withZone(java.time.ZoneId.systemDefault());
        return formatter.format(instant);
    }
%>