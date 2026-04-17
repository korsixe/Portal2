import React, { useState, useEffect } from 'react';
import './Home.css';

const API_BASE = 'http://localhost:8080';

const CATEGORY_COLORS = {
  ELECTRONICS: '#d7eaf3',
  CLOTHING:    '#f8dfd1',
  HOME:        '#dceef4',
  AUTO:        '#f6e3b8',
  SERVICES:    '#dceddf',
  OTHER:       '#e8dbf6',
};

const CONDITION_COLORS = {
  NEW:    '#dceddf',
  USED:   '#f8dfd1',
  BROKEN: '#ffe4e4',
};

const categoryLabel = (c) => ({
  ELECTRONICS: 'Electronics', CLOTHING: 'Clothing', HOME: 'Home',
  AUTO: 'Auto', SERVICES: 'Services', OTHER: 'Other',
}[c] || c);

const conditionLabel = (c) => ({ NEW: 'New', USED: 'Used', BROKEN: 'Broken' }[c] || c);

const Home = () => {
    const [ads, setAds] = useState([]);
    const [filters, setFilters] = useState({
        searchQuery: '',
        minPrice: '',
        maxPrice: '',
        category: '',
        condition: ''
    });
    const [loading, setLoading] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [favorites, setFavorites] = useState(new Set());

    useEffect(() => {
        fetchAds();
        fetch(`${API_BASE}/api/users/me`, { credentials: 'include' })
            .then(r => {
                setIsLoggedIn(r.ok);
                if (r.ok) {
                    return fetch(`${API_BASE}/api/favorites`, { credentials: 'include' })
                        .then(fr => fr.ok ? fr.json() : [])
                        .then(ids => setFavorites(new Set(ids)));
                }
            })
            .catch(() => {});
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const toggleFavorite = async (e, adId) => {
        e.stopPropagation();
        if (!isLoggedIn) { window.location.href = '/login'; return; }
        try {
            const res = await fetch(`${API_BASE}/api/favorites/${adId}`, {
                method: 'POST', credentials: 'include'
            });
            if (res.ok) {
                const data = await res.json();
                setFavorites(prev => {
                    const next = new Set(prev);
                    data.liked ? next.add(adId) : next.delete(adId);
                    return next;
                });
            }
        } catch {}
    };

    const fetchAds = async () => {
        setLoading(true);
        try {
            const queryParams = new URLSearchParams({
                text: filters.searchQuery,
                minPrice: filters.minPrice,
                maxPrice: filters.maxPrice,
                category: filters.category,
                condition: filters.condition
            });

            const response = await fetch(`${API_BASE}/api/announcements/search?${queryParams}`);

            if (response.ok) {
                const data = await response.json();
                setAds(data);
            }
        } catch (error) {
            console.error('Network error:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterSubmit = (e) => {
        e.preventDefault();
        fetchAds();
    };

    const handleChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const handleReset = () => {
        const empty = { searchQuery: '', minPrice: '', maxPrice: '', category: '', condition: '' };
        setFilters(empty);
        setTimeout(() => fetchAds(), 0);
    };

    const formatPrice = (price) => {
        if (price === null || price === undefined) return 'Price on request';
        if (price === 0) return 'Free';
        if (price < 0) return 'Negotiable';
        return `${price.toLocaleString('ru-RU')} ₽`;
    };

    return (
        <div className="portal-wrap">
            <div className="portal-shell">

                {/* Top bar */}
                <header className="portal-topbar">
                    <a href="/" className="portal-brand">
                        <div className="portal-brand-mark"></div>
                        <span>PORTAL</span>
                    </a>

                    <form className="portal-search" onSubmit={handleFilterSubmit}>
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" strokeWidth="2" className="search-icon">
                            <circle cx="11" cy="11" r="7"/>
                            <path d="M20 20L17 17"/>
                        </svg>
                        <input
                            type="text"
                            placeholder="Search ads by title, category, location…"
                            name="searchQuery"
                            value={filters.searchQuery}
                            onChange={handleChange}
                        />
                    </form>

                    {isLoggedIn
                        ? <a href="/dashboard" className="topbar-link">Profile</a>
                        : <a href="/login"     className="topbar-link">Sign In</a>
                    }
                </header>

                {/* Body: sidebar + content */}
                <div className="portal-body">

                    {/* Filters sidebar */}
                    <aside className="portal-sidebar">
                        <h2 className="sidebar-title">Filters</h2>
                        <form onSubmit={handleFilterSubmit}>

                            <div className="filter-block">
                                <div className="filter-block-label">Price, ₽</div>
                                <div className="price-row">
                                    <input className="price-field" type="number" placeholder="From"
                                           name="minPrice" value={filters.minPrice} onChange={handleChange} />
                                    <input className="price-field" type="number" placeholder="To"
                                           name="maxPrice" value={filters.maxPrice} onChange={handleChange} />
                                </div>
                            </div>

                            <div className="filter-block">
                                <div className="filter-block-label">Category</div>
                                <select className="filter-select" name="category"
                                        value={filters.category} onChange={handleChange}>
                                    <option value="">All categories</option>
                                    <option value="ELECTRONICS">Electronics</option>
                                    <option value="CLOTHING">Clothing &amp; Shoes</option>
                                    <option value="HOME">Home &amp; Garden</option>
                                    <option value="AUTO">Auto</option>
                                    <option value="SERVICES">Services</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>

                            <div className="filter-block">
                                <div className="filter-block-label">Condition</div>
                                <div className="condition-options">
                                    {[
                                        { value: '',       label: 'All' },
                                        { value: 'NEW',    label: 'New' },
                                        { value: 'USED',   label: 'Used' },
                                        { value: 'BROKEN', label: 'Not working' },
                                    ].map(opt => (
                                        <label key={opt.value}
                                               className={`condition-opt${filters.condition === opt.value ? ' active' : ''}`}>
                                            <input type="radio" name="condition" value={opt.value}
                                                   checked={filters.condition === opt.value}
                                                   onChange={handleChange} />
                                            {opt.label}
                                        </label>
                                    ))}
                                </div>
                            </div>

                            <div className="filter-actions">
                                <button type="submit" className="btn-apply">Apply</button>
                                <button type="button" className="btn-reset" onClick={handleReset}>Reset</button>
                            </div>
                        </form>
                    </aside>

                    {/* Main content */}
                    <main className="portal-main">
                        <div className="main-head">
                            <h1 className="main-title">Listings</h1>
                            <span className="results-badge">{ads.length} found</span>
                        </div>

                        {loading ? (
                            <div className="loading-state">
                                <div className="spinner"></div>
                                <p>Loading ads…</p>
                            </div>
                        ) : ads.length === 0 ? (
                            <div className="empty-state">
                                <div className="empty-icon">🛍️</div>
                                <h3>No listings found</h3>
                                <p>Try adjusting your filters or check back later.</p>
                                <button className="btn-apply" onClick={handleReset} style={{ marginTop: 12 }}>
                                    Clear filters
                                </button>
                            </div>
                        ) : (
                            <div className="ads-grid">
                                {ads.map(ad => (
                                    <article
                                        key={ad.id}
                                        className="ad-card"
                                        onClick={() => window.open(`/ad/${ad.id}`, '_blank')}
                                    >
                                        <div className="ad-image-wrap">
                                            <img
                                                src={`${API_BASE}/ad-photo?adId=${ad.id}&photoIndex=0`}
                                                alt={ad.title || 'Ad photo'}
                                                onError={(e) => {
                                                    e.currentTarget.style.display = 'none';
                                                    e.currentTarget.nextSibling.style.display = 'flex';
                                                }}
                                            />
                                            <div className="ad-image-fallback"
                                                 style={{ background: CATEGORY_COLORS[ad.category] || '#f0e8df' }}>
                                                <span className="fallback-emoji">
                                                    {ad.category === 'ELECTRONICS' ? '💻' :
                                                     ad.category === 'CLOTHING'    ? '👕' :
                                                     ad.category === 'HOME'        ? '🏠' :
                                                     ad.category === 'AUTO'        ? '🚗' :
                                                     ad.category === 'SERVICES'    ? '🔧' : '📦'}
                                                </span>
                                            </div>
                                            {ad.condition && (
                                                <span className="ad-condition-badge"
                                                      style={{ background: CONDITION_COLORS[ad.condition] || '#f0e8df' }}>
                                                    {conditionLabel(ad.condition)}
                                                </span>
                                            )}
                                            <button
                                                className={`ad-like-btn${favorites.has(ad.id) ? ' liked' : ''}`}
                                                onClick={(e) => toggleFavorite(e, ad.id)}
                                                title={favorites.has(ad.id) ? 'Remove from favorites' : 'Add to favorites'}
                                            >
                                                <svg viewBox="0 0 24 24" width="18" height="18">
                                                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                                                </svg>
                                            </button>
                                        </div>

                                        <div className="ad-body">
                                            <div className="ad-price">{formatPrice(ad.price)}</div>
                                            <h3 className="ad-title">{ad.title}</h3>
                                            {ad.description && (
                                                <p className="ad-description">{ad.description}</p>
                                            )}
                                            <div className="ad-footer">
                                                {ad.category && (
                                                    <span className="ad-category-tag"
                                                          style={{ background: CATEGORY_COLORS[ad.category] || '#f0e8df' }}>
                                                        {categoryLabel(ad.category)}
                                                    </span>
                                                )}
                                                {ad.location && (
                                                    <span className="ad-location">
                                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                                                             stroke="currentColor" strokeWidth="2">
                                                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
                                                            <circle cx="12" cy="10" r="3"/>
                                                        </svg>
                                                        {ad.location}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </article>
                                ))}
                            </div>
                        )}
                    </main>
                </div>

                {/* CTA footer */}
                <section className="portal-cta">
                    <div className="cta-text">
                        <h3>Have something to sell?</h3>
                        <p>Post your first listing — it's free and takes 2 minutes.</p>
                    </div>
                    <a href="/create-ad" className="cta-link">Start selling →</a>
                </section>

            </div>
        </div>
    );
};

export default Home;
