import React, { useState, useEffect } from 'react';
import './Home.css';

const Home = () => {
    // Состояния для хранения данных и фильтров
    const [ads, setAds] = useState([]);
    const [filters, setFilters] = useState({
        searchQuery: '',
        minPrice: '',
        maxPrice: '',
        category: '',
        condition: ''
    });

    // Загружаем объявления при первом открытии страницы
    useEffect(() => {
        fetchAds();
    }, []);

    // Функция запроса к нашему REST API (Spring Boot)
    const fetchAds = async () => {
        try {
            // Формируем URL с параметрами фильтрации
            const queryParams = new URLSearchParams({
                text: filters.searchQuery,
                minPrice: filters.minPrice,
                maxPrice: filters.maxPrice,
                category: filters.category,
                condition: filters.condition
            });

            // Тот самый эндпоинт из твоей Postman коллекции!
            const response = await fetch(`http://localhost:8080/api/announcements/search?${queryParams}`);

            if (response.ok) {
                const data = await response.json();
                setAds(data); // Обновляем список на экране
            } else {
                console.error("Ошибка сервера при загрузке объявлений");
            }
        } catch (error) {
            console.error("Ошибка сети:", error);
        }
    };

    // Обработчик отправки формы поиска/фильтрации
    const handleFilterSubmit = (e) => {
        e.preventDefault();
        fetchAds();
    };

    // Обработчик изменения полей ввода
    const handleChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    return (
        <div className="home-container">
            {/* Шапка с поиском */}
            <div className="header">
                <div className="portal-logo">PORTAL</div>
                <div className="search-section">
                    <form className="search-form" onSubmit={handleFilterSubmit}>
                        <input
                            type="text"
                            className="search-input"
                            placeholder="🔍 Поиск объявлений..."
                            name="searchQuery"
                            value={filters.searchQuery}
                            onChange={handleChange}
                        />
                        <button type="submit" className="search-btn">Найти</button>
                    </form>
                </div>
                <div className="auth-buttons">
                    <a href="/login" className="btn btn-secondary">Войти</a>
                    <a href="/register" className="btn btn-primary">Регистрация</a>
                </div>
            </div>

            {/* Боковая панель с фильтрами */}
            <div className="filters-sidebar">
                <h2 className="filters-title">🔍 Фильтры</h2>
                <form onSubmit={handleFilterSubmit}>
                    <div className="filter-section">
                        <div className="filter-label">💰 Цена</div>
                        <div className="price-inputs">
                            <input type="number" className="price-input" placeholder="Цена от" name="minPrice" value={filters.minPrice} onChange={handleChange} />
                        </div>
                        <div className="price-inputs">
                            <input type="number" className="price-input" placeholder="Цена до" name="maxPrice" value={filters.maxPrice} onChange={handleChange} />
                        </div>
                    </div>

                    <div className="filter-section">
                        <div className="filter-label">📂 Категория</div>
                        <select className="filter-select" name="category" value={filters.category} onChange={handleChange}>
                            <option value="">Все категории</option>
                            <option value="ELECTRONICS">Электроника</option>
                            <option value="CLOTHING">Одежда и обувь</option>
                            <option value="HOME">Дом и сад</option>
                            <option value="AUTO">Автотовары</option>
                            <option value="SERVICES">Услуги</option>
                            <option value="OTHER">Другое</option>
                        </select>
                    </div>

                    <div className="filter-section">
                        <div className="filter-label">🔄 Состояние</div>
                        <div className="filter-options">
                            <label className="filter-option">
                                <input type="radio" name="condition" value="" checked={filters.condition === ''} onChange={handleChange} />
                                <span>Все состояния</span>
                            </label>
                            <label className="filter-option">
                                <input type="radio" name="condition" value="NEW" checked={filters.condition === 'NEW'} onChange={handleChange} />
                                <span>Новое</span>
                            </label>
                            <label className="filter-option">
                                <input type="radio" name="condition" value="USED" checked={filters.condition === 'USED'} onChange={handleChange} />
                                <span>Б/У</span>
                            </label>
                            <label className="filter-option">
                                <input type="radio" name="condition" value="BROKEN" checked={filters.condition === 'BROKEN'} onChange={handleChange} />
                                <span>Не работает</span>
                            </label>
                        </div>
                    </div>

                    <div className="filter-actions">
                        <button type="submit" className="btn-apply">Применить фильтры</button>
                        <button type="button" className="btn-reset" onClick={() => {
                            setFilters({searchQuery: '', minPrice: '', maxPrice: '', category: '', condition: ''});
                            fetchAds();
                        }}>Сбросить</button>
                    </div>
                </form>
            </div>

            {/* Основной контент (Сетка объявлений) */}
            <div className="main-content">
                <div className="content-header">
                    <h1 className="section-title">🎯 Объявления</h1>
                    <div className="results-count">Найдено: {ads.length} объявлений</div>
                </div>

                {ads.length === 0 ? (
                    <div className="no-ads">
                        <div className="no-ads-icon">📭</div>
                        <h3>Объявлений не найдено</h3>
                    </div>
                ) : (
                    <div className="ads-grid">
                        {ads.map(ad => (
                            <div key={ad.id} className="ad-card" onClick={() => window.location.href=`/ad/${ad.id}`}>
                                <div className="ad-title">{ad.title}</div>
                                <div className="ad-price">{ad.price > 0 ? `${ad.price} руб.` : (ad.price === 0 ? 'Бесплатно' : 'Договорная')}</div>
                                <div className="ad-meta">
                                    <span className="ad-category">{ad.category}</span>
                                    <span className="ad-condition">{ad.condition}</span>
                                </div>
                                <div className="ad-location">📍 {ad.location}</div>
                                <div className="ad-description">{ad.description}</div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Home;