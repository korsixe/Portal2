import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import ChangePasswordModal from './ChangePasswordModal';
import NotificationBell from './NotificationBell';
import Icon from './Icon';

const Dashboard = () => {
  const [user, setUser]               = useState(null);
  const [ads, setAds]                 = useState([]);
  const [favoriteAds, setFavoriteAds] = useState([]);
  const [bookedAds, setBookedAds]     = useState([]);
  const [loading, setLoading]         = useState(true);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage]     = useState('');
  const [activeModal, setActiveModal] = useState(null);
  const [deletePassword, setDeletePassword] = useState('');
  const [activeSection, setActiveSection]   = useState('all');
  const [adsOpen, setAdsOpen]         = useState(false);

  useEffect(() => { loadUserData(); }, []);

  useEffect(() => {
    if (successMessage) { const t = setTimeout(() => setSuccessMessage(''), 3000); return () => clearTimeout(t); }
  }, [successMessage]);

  useEffect(() => {
    if (errorMessage) { const t = setTimeout(() => setErrorMessage(''), 3000); return () => clearTimeout(t); }
  }, [errorMessage]);

  const loadUserData = async () => {
    setLoading(true);
    try {
      const userRes = await fetch('http://localhost:8080/api/users/me', { credentials: 'include' });
      if (!userRes.ok) { if (userRes.status === 401) window.location.href = '/login'; return; }
      setUser(await userRes.json());

      const adsRes = await fetch('http://localhost:8080/api/announcements/my', { credentials: 'include' });
      if (adsRes.ok) setAds((await adsRes.json()).filter(a => a.status !== 'DELETED'));

      const favRes = await fetch('http://localhost:8080/api/favorites/ads', { credentials: 'include' });
      if (favRes.ok) setFavoriteAds(await favRes.json());

      const bookedRes = await fetch('http://localhost:8080/api/v1/bookings/my', { credentials: 'include' });
      if (bookedRes.ok) setBookedAds(await bookedRes.json());
    } catch { setErrorMessage('Failed to load data'); }
    finally { setLoading(false); }
  };

  const handlePasswordChanged = async (currentPassword, newPassword, confirmPassword) => {
    if (newPassword !== confirmPassword) { setErrorMessage('Passwords do not match!'); return false; }
    if (newPassword.length < 8) { setErrorMessage('Password must be at least 8 characters!'); return false; }
    try {
      const res = await fetch('http://localhost:8080/api/users/change-password', {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
      });
      const data = await res.json();
      if (res.ok && data.success) { setSuccessMessage(data.message || 'Password changed!'); if (data.user) setUser(data.user); return true; }
      setErrorMessage(data.message || 'Error changing password'); return false;
    } catch { setErrorMessage('Error changing password'); return false; }
  };

  const handleDeleteAccount = async (e) => {
    e.preventDefault();
    if (!window.confirm('Are you sure? This cannot be undone.')) return;
    try {
      const res = await fetch('http://localhost:8080/api/users/delete-account', {
        method: 'DELETE', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: deletePassword }),
      });
      if (res.ok) { setSuccessMessage('Account deleted'); setTimeout(() => window.location.href = '/login', 2000); }
      else setErrorMessage('Error: ' + await res.text());
    } catch { setErrorMessage('Error deleting account'); }
  };

  const handleLogout = async () => {
    try { await fetch('http://localhost:8080/api/users/logout', { method: 'POST', credentials: 'include' }); }
    catch {}
    window.location.href = '/login';
  };

  const handleUnfavorite = async (e, adId) => {
    e.stopPropagation();
    try {
      const res = await fetch(`http://localhost:8080/api/favorites/${adId}`, { method: 'POST', credentials: 'include' });
      if (res.ok) setFavoriteAds(prev => prev.filter(a => a.id !== adId));
    } catch { setErrorMessage('Error updating favorites'); }
  };

  const handleDeleteAd = async (adId) => {
    if (!window.confirm('Delete this ad?')) return;
    try {
      const res = await fetch(`http://localhost:8080/api/announcements/${adId}`, { method: 'DELETE', credentials: 'include' });
      if (res.ok) { setSuccessMessage('Ad deleted'); loadUserData(); }
      else setErrorMessage('Error deleting ad');
    } catch { setErrorMessage('Error deleting ad'); }
  };

  const formatPrice = (price) => {
    if (price === -1) return 'Negotiable';
    if (price === 0)  return 'Free';
    return `${price.toLocaleString()} ₽`;
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('ru-RU') : '—';

  const formatAddress = (address) => {
    if (!address) return 'Not specified';
    if (typeof address === 'string') return address;
    if (address.fullAddress?.trim()) return address.fullAddress;
    return [address.city, address.street, address.houseNumber].filter(Boolean).join(', ') || 'Not specified';
  };

  const STATUS_LABEL = { ACTIVE: 'Active', DRAFT: 'Draft', UNDER_MODERATION: 'Moderation', ARCHIVED: 'Archived', DELETED: 'Deleted' };
  const STATUS_CLASS = { ACTIVE: 'statusActive', DRAFT: 'statusDraft', UNDER_MODERATION: 'statusModeration', ARCHIVED: 'statusArchived', DELETED: 'statusDeleted' };

  const activeAds      = ads.filter(a => a.status === 'ACTIVE');
  const moderationAds  = ads.filter(a => a.status === 'UNDER_MODERATION');
  const draftAds       = ads.filter(a => a.status === 'DRAFT');
  const archivedAds    = ads.filter(a => a.status === 'ARCHIVED');

  const visibleAds = activeSection === 'active'     ? activeAds
                   : activeSection === 'moderation' ? moderationAds
                   : activeSection === 'drafts'     ? draftAds
                   : activeSection === 'archive'    ? archivedAds
                   : activeSection === 'favorites'  ? favoriteAds
                   : activeSection === 'booked'     ? bookedAds
                   : ads;

  const sectionTitle = activeSection === 'active'     ? 'Active listings'
                     : activeSection === 'moderation' ? 'Under review'
                     : activeSection === 'drafts'     ? 'Drafts'
                     : activeSection === 'archive'    ? 'Archive'
                     : activeSection === 'favorites'  ? 'Favorites'
                     : activeSection === 'booked'     ? 'Booked'
                     : 'All listings';

  const openModal  = (name) => setActiveModal(name);
  const closeModal = ()     => setActiveModal(null);

  const selectSection = (section) => {
    setActiveSection(section);
    if (['active','drafts','archive','all','moderation'].includes(section)) setAdsOpen(true);
  };

  if (loading) return (
    <div className="loadingContainer">
      <div className="loader"></div>
      <p>Loading…</p>
    </div>
  );

  if (!user) return (
    <div className="loadingContainer">
      <p>Failed to load user data</p>
      <button onClick={() => window.location.href = '/login'} className="db-btn-primary" style={{ marginTop: 16 }}>
        Sign In Again
      </button>
    </div>
  );

  return (
    <div className="dash-wrap">
      <div className="dash-shell">

        {/* Toast messages */}
        {successMessage && <div className="db-toast db-toast-success">{successMessage}</div>}
        {errorMessage   && <div className="db-toast db-toast-error">{errorMessage}</div>}

        {/* Top bar */}
        <header className="dash-topbar">
          <a href="/" className="dash-brand">
            <div className="dash-brand-mark"></div>
            <span>PORTAL</span>
          </a>
          <div className="dash-topbar-right">
            <NotificationBell adIds={ads.map(a => a.id)} />
            <button className="db-btn-ghost" onClick={handleLogout}>Sign Out</button>
          </div>
        </header>

        <div className="dash-body">

          {/* ── Sidebar ── */}
          <aside className="dash-sidebar">

            {/* Avatar */}
            <div className="db-avatar-block">
              <div className="db-avatar">
                <Icon name="user" size={36} />
              </div>
              <div className="db-avatar-name">{user.name || 'User'}</div>
              <div className="db-avatar-email">{user.email}</div>
            </div>

            {/* About Me */}
            <div className="db-info-section">
              <div className="db-section-label">About Me</div>

              <div className="db-info-group">
                <div className="db-info-label">Basic info</div>
                <div className="db-info-row"><span>Name</span><span>{user.name || '—'}</span></div>
                <div className="db-info-row"><span>Email</span><span className="db-info-ellipsis">{user.email}</span></div>
                <div className="db-info-row"><span>Address</span><span className="db-info-ellipsis">{formatAddress(user.address)}</span></div>
              </div>

              <div className="db-info-group">
                <div className="db-info-label">Academic</div>
                <div className="db-info-row"><span>Program</span><span>{user.studyProgram || '—'}</span></div>
                <div className="db-info-row"><span>Course</span><span>{user.course ? `${user.course} year` : '—'}</span></div>
              </div>

              <div className="db-info-group">
                <div className="db-info-label">Rating &amp; Coins</div>
                <div className="db-info-row">
                  <span>Rating</span>
                  <span className="db-stars">
                    {[...Array(5)].map((_, i) => (
                      <span key={i} className={i < Math.round(user.rating || 0) ? 'star-filled' : 'star-empty'}>★</span>
                    ))}
                    <span className="db-rating-num">({(user.rating || 0).toFixed(1)})</span>
                  </span>
                </div>
                <div className="db-info-row">
                  <span>Coins</span>
                  <span className="db-coins">🪙 {user.coins || 0}</span>
                </div>
              </div>
            </div>

            {/* Nav */}
            <nav className="db-nav">

              {/* Listings collapsible */}
              <button
                className={`db-nav-item db-nav-group-toggle${adsOpen ? ' open' : ''}`}
                onClick={() => setAdsOpen(o => !o)}
              >
                <span>Listings</span>
                <span className="db-chevron">{adsOpen ? '▲' : '▼'}</span>
              </button>
              {adsOpen && (
                <div className="db-subnav">
                  <button className={`db-subnav-item${activeSection === 'all' ? ' active' : ''}`} onClick={() => selectSection('all')}>
                    All <span className="db-count">{ads.length}</span>
                  </button>
                  <button className={`db-subnav-item${activeSection === 'active' ? ' active' : ''}`} onClick={() => selectSection('active')}>
                    Active <span className="db-count">{activeAds.length}</span>
                  </button>
                  <button className={`db-subnav-item${activeSection === 'moderation' ? ' active' : ''}`} onClick={() => selectSection('moderation')}>
                    Under review <span className="db-count">{moderationAds.length}</span>
                  </button>
                  <button className={`db-subnav-item${activeSection === 'drafts' ? ' active' : ''}`} onClick={() => selectSection('drafts')}>
                    Drafts <span className="db-count">{draftAds.length}</span>
                  </button>
                  <button className={`db-subnav-item${activeSection === 'archive' ? ' active' : ''}`} onClick={() => selectSection('archive')}>
                    Archive <span className="db-count">{archivedAds.length}</span>
                  </button>
                </div>
              )}

              <button
                className={`db-nav-item${activeSection === 'favorites' ? ' active' : ''}`}
                onClick={() => selectSection('favorites')}
              >
                ♡ Favorites <span className="db-count">{favoriteAds.length}</span>
              </button>

              <button
                className={`db-nav-item${activeSection === 'booked' ? ' active' : ''}`}
                onClick={() => selectSection('booked')}
              >
                🔖 Booked <span className="db-count">{bookedAds.length}</span>
              </button>

              <a href="/" className="db-nav-item">
                Browse Marketplace
              </a>

              <a href="/edit-profile" className="db-nav-item">
                Edit Profile
              </a>

              <button className="db-nav-item" onClick={() => openModal('account')}>
                Account Settings
              </button>

              <a href="/support" className="db-nav-item db-nav-muted">
                Support
              </a>

              {user.moderator && (
                <button className="db-nav-item db-nav-role db-nav-moderator" onClick={() => window.location.href = '/moderator/dashboard'}>
                  Moderator Panel
                </button>
              )}
              {user.admin && (
                <button className="db-nav-item db-nav-role db-nav-admin" onClick={() => window.location.href = '/admin/dashboard'}>
                  Admin Panel
                </button>
              )}
            </nav>
          </aside>

          {/* ── Main content ── */}
          <main className="dash-main">
            <div className="dash-main-head">
              <h2>{sectionTitle}</h2>
              <button className="db-btn-primary" onClick={() => window.location.href = '/create-ad'}>
                + New listing
              </button>
            </div>

            {visibleAds.length === 0 ? (
              <div className="db-empty">
                <div className="db-empty-icon">📦</div>
                <h3>No listings here</h3>
                <p>
                  {activeSection === 'drafts'    ? 'You have no drafts.' :
                   activeSection === 'archive'   ? 'Your archive is empty.' :
                   activeSection === 'active'    ? 'No active listings yet.' :
                   activeSection === 'favorites' ? 'No saved listings yet. Click the heart on any ad!' :
                   activeSection === 'booked'    ? 'You have no booked items.' :
                   'Create your first listing!'}
                </p>
              </div>
            ) : (
              <div className="db-ad-grid">
                {visibleAds.map(ad => (
                  <div key={ad.id} className="db-ad-card" onClick={() => window.open(`/ad/${ad.id}`, '_blank')}>
                    <div className="db-ad-image">
                      <img
                        src={`http://localhost:8080/ad-photo?adId=${ad.id}&photoIndex=0`}
                        alt={ad.title}
                        onError={e => { e.currentTarget.style.display = 'none'; e.currentTarget.nextSibling.style.display = 'flex'; }}
                      />
                      <div className="db-ad-image-fallback" style={{ display: 'none' }}>📦</div>
                      <span className={`db-ad-status ${STATUS_CLASS[ad.status] || ''}`}>
                        {STATUS_LABEL[ad.status] || ad.status}
                      </span>
                      {activeSection === 'favorites' && (
                        <button
                          className="db-like-btn liked"
                          onClick={(e) => handleUnfavorite(e, ad.id)}
                          title="Remove from favorites"
                        >
                          <svg viewBox="0 0 24 24" width="16" height="16">
                            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                          </svg>
                        </button>
                      )}
                    </div>
                    <div className="db-ad-body">
                      <div className="db-ad-price">{formatPrice(ad.price)}</div>
                      <div className="db-ad-title">{ad.title}</div>
                      {ad.location && (
                        <div className="db-ad-location">
                          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
                          </svg>
                          {ad.location}
                        </div>
                      )}
                      <div className="db-ad-meta">
                        <span className="db-ad-views">👁 {ad.viewCount || 0}</span>
                        <span className="db-ad-date">{formatDate(ad.createdAt)}</span>
                      </div>
                      {activeSection !== 'favorites' && activeSection !== 'booked' && (
                        <div className="db-ad-actions" onClick={e => e.stopPropagation()}>
                          <button className="db-btn-edit" onClick={() => window.location.href = `/edit-ad?adId=${ad.id}`}>Edit</button>
                          <button className="db-btn-danger" onClick={() => handleDeleteAd(ad.id)}>Delete</button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </main>
        </div>
      </div>

      {/* Account modal */}
      {activeModal === 'account' && (
        <div className="db-modal" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="db-modal-content">
            <button className="db-modal-close" onClick={closeModal}>×</button>
            <h3>Account Settings</h3>
            <div className="db-modal-actions">
              <button className="db-btn-primary" onClick={() => openModal('password')}>Change Password</button>
              <button className="db-btn-danger"  onClick={() => openModal('delete')}>Delete Account</button>
            </div>
          </div>
        </div>
      )}

      {activeModal === 'password' && (
        <ChangePasswordModal onClose={closeModal} onChangePassword={handlePasswordChanged} />
      )}

      {activeModal === 'delete' && (
        <div className="db-modal" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="db-modal-content">
            <button className="db-modal-close" onClick={closeModal}>×</button>
            <h3>Delete Account</h3>
            <div className="db-warning-box">
              This action is irreversible. All your data and listings will be permanently deleted.
            </div>
            <form onSubmit={handleDeleteAccount}>
              <div className="db-form-group">
                <label>Confirm with your password</label>
                <input type="password" required value={deletePassword} onChange={e => setDeletePassword(e.target.value)} />
              </div>
              <div className="db-modal-actions">
                <button type="submit"  className="db-btn-danger">Delete Account</button>
                <button type="button" className="db-btn-secondary" onClick={closeModal}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
