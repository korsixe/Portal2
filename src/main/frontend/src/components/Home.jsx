import React, { useState, useEffect } from 'react';
import './Home.css';
import Icon from './Icon';

const API_BASE = 'http://localhost:8080';
const FALLBACK_IMAGE = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='640' height='480'%3E%3Crect width='100%25' height='100%25' fill='%23E5EAF5'/%3E%3Ctext x='50%25' y='50%25' text-anchor='middle' dominant-baseline='middle' fill='%238458B3' font-size='28' font-family='Inter,sans-serif'%3ENo photo%3C/text%3E%3C/svg%3E";

const Home = () => {
    const [ads, setAds] = useState([]);
    const [filters, setFilters] = useState({
        searchQuery: '',
        minPrice: '',
        maxPrice: '',
        category: '',
        condition: ''
    });

    useEffect(() => {
        fetchAds();
    }, []);

    const fetchAds = async () => {
        try {
            const queryParams = new URLSearchParams({
                text: filters.searchQuery,
                minPrice: filters.minPrice,
                maxPrice: filters.maxPrice,
                category: filters.category,
                condition: filters.condition
            });

            const response = await fetch(`http://localhost:8080/api/announcements/search?${queryParams}`);

            if (response.ok) {
                const data = await response.json();
                setAds(data);
            } else {
                console.error("Server error while loading ads");
            }
        } catch (error) {
            console.error("Network error:", error);
        }
    };

    const handleFilterSubmit = (e) => {
        e.preventDefault();
        fetchAds();
    };

    const handleChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    return (
        <div className="home-container">
            {/* Header with search */}
            <div className="header">
                <div className="portal-logo">PORTAL</div>
                <div className="search-section">
                    <form className="search-form" onSubmit={handleFilterSubmit}>
                        <input
                            type="text"
                            className="search-input"
                            placeholder="Search ads..."
                            name="searchQuery"
                            value={filters.searchQuery}
                            onChange={handleChange}
                        />
                        <button type="submit" className="search-btn">Search</button>
                    </form>
                </div>
                <div className="auth-buttons">
                    <a href="/login" className="btn btn-secondary">Sign In</a>
                    <a href="/register" className="btn btn-primary">Register</a>
                </div>
            </div>

            {/* Sidebar with filters */}
            <div className="filters-sidebar">
                <h2 className="filters-title">
                    <Icon name="filter" size={24} />
                    Filters
                </h2>
                <form onSubmit={handleFilterSubmit}>
                    <div className="filter-section">
                        <div className="filter-label">
                            <Icon name="price" size={20} />
                            Price
                        </div>
                        <div className="price-inputs">
                            <input type="number" className="price-input" placeholder="From" name="minPrice" value={filters.minPrice} onChange={handleChange} />
                        </div>
                        <div className="price-inputs">
                            <input type="number" className="price-input" placeholder="To" name="maxPrice" value={filters.maxPrice} onChange={handleChange} />
                        </div>
                    </div>

                    <div className="filter-section">
                        <div className="filter-label">
                            <Icon name="category" size={20} />
                            Category
                        </div>
                        <select className="filter-select" name="category" value={filters.category} onChange={handleChange}>
                            <option value="">All categories</option>
                            <option value="ELECTRONICS">Electronics</option>
                            <option value="CLOTHING">Clothing & Shoes</option>
                            <option value="HOME">Home & Garden</option>
                            <option value="AUTO">Auto</option>
                            <option value="SERVICES">Services</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>

                    <div className="filter-section">
                        <div className="filter-label">
                            <Icon name="condition" size={20} />
                            Condition
                        </div>
                        <div className="filter-options">
                            <label className="filter-option">
                                <input type="radio" name="condition" value="" checked={filters.condition === ''} onChange={handleChange} />
                                <span>All conditions</span>
                            </label>
                            <label className="filter-option">
                                <input type="radio" name="condition" value="NEW" checked={filters.condition === 'NEW'} onChange={handleChange} />
                                <span>New</span>
                            </label>
                            <label className="filter-option">
                                <input type="radio" name="condition" value="USED" checked={filters.condition === 'USED'} onChange={handleChange} />
                                <span>Used</span>
                            </label>
                            <label className="filter-option">
                                <input type="radio" name="condition" value="BROKEN" checked={filters.condition === 'BROKEN'} onChange={handleChange} />
                                <span>Not working</span>
                            </label>
                        </div>
                    </div>

                    <div className="filter-actions">
                        <button type="submit" className="btn-apply">Apply Filters</button>
                        <button type="button" className="btn-reset" onClick={() => {
                            setFilters({searchQuery: '', minPrice: '', maxPrice: '', category: '', condition: ''});
                            fetchAds();
                        }}>Reset</button>
                    </div>
                </form>
            </div>

            {/* Main content (Ads grid) */}
            <div className="main-content">
                <div className="content-header">
                    <h1 className="section-title">
                        <Icon name="target" size={28} />
                        Ads
                    </h1>
                    <div className="results-count">Found: {ads.length} ads</div>
                </div>

                {ads.length === 0 ? (
                    <div className="no-ads">
                        <Icon name="empty-box" size={80} className="no-ads-icon" />
                        <h3>No ads found</h3>
                        <p>Try adjusting your search filters or check back later.</p>
                    </div>
                ) : (
                    <div className="ads-grid">
                        {ads.map(ad => (
                            <div key={ad.id} className="ad-card" onClick={() => window.location.href=`/ad/${ad.id}`}>
                                <div className="ad-image">
                                    <img
                                        src={`${API_BASE}/ad-photo?adId=${ad.id}&photoIndex=0`}
                                        alt={ad.title || 'ad-photo'}
                                        onError={(e) => {
                                            e.currentTarget.onerror = null;
                                            e.currentTarget.src = FALLBACK_IMAGE;
                                        }}
                                    />
                                </div>
                                <div className="ad-title">{ad.title}</div>
                                <div className="ad-price">{ad.price > 0 ? `${ad.price} RUB` : (ad.price === 0 ? 'Free' : 'Negotiable')}</div>
                                <div className="ad-meta">
                                    <span className="ad-category">{ad.category}</span>
                                    <span className="ad-condition">{ad.condition}</span>
                                </div>
                                <div className="ad-location">
                                    <Icon name="location" size={16} />
                                    {ad.location}
                                </div>
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
