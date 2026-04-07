import React, { useEffect, useMemo, useState } from 'react';
import { apiGet, apiPost } from '../../api';
import AccessDenied from '../AccessDenied';
import './ModeratorDashboard.css';

const API_BASE = 'http://localhost:8080';

const CATEGORY_LABELS = {
  ELECTRONICS: 'Электроника',
  CLOTHING: 'Одежда',
  BOOKS: 'Книги',
  FURNITURE: 'Мебель',
  SPORTS: 'Спорт',
  OTHER: 'Другое'
};

const ACTION_CONFIGS = {
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

const REJECT_REASONS = [
  'Неполная или некорректная информация',
  'Несоответствие категории, подкатегории, тегам',
  'Нарушение правил платформы'
];

const DELETE_REASONS = [
  'Нарушение правил платформы',
  'Мошенничество или обман',
  'Нецензурная лексика, оскорбления',
  'Спам'
];

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  const options = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  };
  return date.toLocaleString('ru-RU', options);
}

function getCategoryLabel(category) {
  return CATEGORY_LABELS[category] || category || '—';
}

function ModeratorDashboard() {
  const [ads, setAds] = useState([]);
  const [stats, setStats] = useState(null);
  const [moderator, setModerator] = useState(null);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(true);
  const [accessDenied, setAccessDenied] = useState(false);

  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [reasonModalOpen, setReasonModalOpen] = useState(false);
  const [currentAction, setCurrentAction] = useState(null);
  const [currentAd, setCurrentAd] = useState(null);
  const [currentReason, setCurrentReason] = useState('');
  const [customReason, setCustomReason] = useState('');
  const [reasonRequired, setReasonRequired] = useState(false);
  const [historyOpen, setHistoryOpen] = useState({});
  const [historyData, setHistoryData] = useState({});

  const reasons = useMemo(() => {
    if (currentAction === 'reject') return REJECT_REASONS;
    if (currentAction === 'delete') return DELETE_REASONS;
    return [];
  }, [currentAction]);

  const load = () => {
    setLoading(true);
    apiGet('/api/moderator/dashboard')
      .then((data) => {
        setAds(data.ads || []);
        setStats(data.stats || null);
        setModerator(data.moderator || null);
        setLoading(false);
      })
      .catch((err) => {
        if (err && (err.status === 401 || err.status === 403)) {
          setAccessDenied(true);
        } else {
          setMessage('Не удалось загрузить кабинет модератора');
          setMessageType('error');
        }
        setLoading(false);
      });
  };

  useEffect(() => {
    load();
    const interval = setInterval(() => {
      load();
    }, 30000);
    return () => clearInterval(interval);
  }, []);

  const showNotification = (text, type) => {
    setMessage(text);
    setMessageType(type || 'info');
    setTimeout(() => {
      setMessage('');
      setMessageType('');
    }, 3000);
  };

  const openConfirm = (action, ad) => {
    setCurrentAction(action);
    setCurrentAd(ad);
    setConfirmModalOpen(true);
  };

  const openReasonModal = (action, ad) => {
    setCurrentAction(action);
    setCurrentAd(ad);
    setCurrentReason('');
    setCustomReason('');
    setReasonRequired(false);
    setReasonModalOpen(true);
  };

  const closeConfirm = () => {
    setConfirmModalOpen(false);
  };

  const closeReason = () => {
    setReasonModalOpen(false);
  };

  const handleActionClick = (action, ad) => {
    if (action === 'approve') {
      openConfirm(action, ad);
    } else {
      openReasonModal(action, ad);
    }
  };

  const confirmWithReason = () => {
    if (!currentReason && !customReason.trim()) {
      setReasonRequired(true);
      return;
    }
    const reason = currentReason || customReason.trim();
    setCurrentReason(reason);
    closeReason();
    openConfirm(currentAction, currentAd);
  };

  const executeAction = async () => {
    if (!currentAction || !currentAd) return;
    const action = currentAction;
    const adId = currentAd.id;

    try {
      const res = await apiPost(`/api/moderator/${action}`, {
        adId,
        reason: action === 'approve' ? null : currentReason
      });
      showNotification(ACTION_CONFIGS[action].successMessage, res.success ? 'success' : 'error');
      closeConfirm();
      setCurrentAction(null);
      setCurrentAd(null);
      setCurrentReason('');
      load();
    } catch (err) {
      console.error('Failed to execute action:', err);
      showNotification('Не удалось выполнить действие', 'error');
      closeConfirm();
    }
  };

  const toggleHistory = async (adId) => {
    setHistoryOpen((prev) => ({ ...prev, [adId]: !prev[adId] }));
    if (historyData[adId]) return;
    try {
      const data = await apiGet(`/api/announcements/${adId}/history`);
      setHistoryData((prev) => ({ ...prev, [adId]: data || [] }));
    } catch (err) {
      console.error('Failed to fetch history:', err);
      setHistoryData((prev) => ({ ...prev, [adId]: [] }));
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/api/users/logout', {
        method: 'POST',
        credentials: 'include'
      });
    } catch (error) {
      console.error('Logout failed:', error);
    }
    globalThis.location.href = '/login';
  };

  if (accessDenied) {
    return (
      <AccessDenied
        title="Доступ к модерации запрещен"
        message="У вашей учетной записи нет прав модератора."
        actionLabel="В личный кабинет"
        actionHref="/dashboard"
      />
    );
  }

  if (loading) {
    return (
      <div className="moderator-page">
        <div className="container">
          <div className="header">
            <div className="portal-logo">PORTAL</div>
          </div>
          <div className="content">
            <div className="message info">Загрузка...</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="moderator-page">
      <div className="container">
        <div className="header">
          <div className="portal-logo">PORTAL</div>
          <div className="moderator-info">
            <h2>Кабинет модератора</h2>
            <p>{moderator?.email || 'Модератор'}</p>
          </div>
          <div className="nav-group header-nav">
            <a href="/" className="btn btn-primary">На главную</a>
            <a href="/dashboard" className="btn btn-secondary">Личный кабинет</a>
            <button type="button" className="btn btn-secondary" onClick={handleLogout}>Выйти</button>
          </div>
        </div>

        <div className="content">
          {message && (
            <div className={`notification ${messageType}`} style={{ display: 'block', marginBottom: '10px' }}>
              {message}
            </div>
          )}

          <h2 className="section-title">Панель модерации</h2>

          <div className="stats-cards">
            <div className="stat-card">
              <div className="stat-number">{ads.length}</div>
              <div className="stat-label">Ожидают модерации</div>
            </div>
            <div className="stat-card dark-card">
              <div className="stat-number">{stats?.totalUsers ?? 0}</div>
              <div className="stat-label">Всего пользователей</div>
            </div>
            <div className="stat-card blue-card">
              <div className="stat-number">{stats?.adminCount ?? 0}</div>
              <div className="stat-label">Администраторов</div>
            </div>
            <div className="stat-card green-card">
              <div className="stat-number">{stats?.moderatorCount ?? 0}</div>
              <div className="stat-label">Модераторов</div>
            </div>
          </div>

          {moderator && (
            <div className="moderator-info profile-info">
              <h3>Ваш профиль (как у обычного пользователя)</h3>
              <p>{moderator.name} — {moderator.email}</p>
              <p>
                Монеты: {moderator.coins} · Роли:{' '}
                {(moderator.roles || []).map((role, index) => (
                  <span key={`${moderator.id}-role-${index}`} className="role-chip">
                    {typeof role === 'string' ? role : role.displayName || role.name}
                  </span>
                ))}
              </p>
              <div className="profile-actions">
                <a href="/dashboard" className="btn btn-home">Открыть личный кабинет</a>
                <a href="/" className="btn btn-secondary">Лента объявлений</a>
              </div>
            </div>
          )}

          <h3 className="section-title">Объявления на модерации</h3>

          {ads.length === 0 && (
            <div className="empty-state">
              <div>📋</div>
              <p>Нет объявлений для модерации</p>
              <p className="empty-sub">Все объявления проверены и обработаны</p>
            </div>
          )}

          {ads.length > 0 && (
            <div className="ads-list">
              {ads.map((ad) => (
                <div className="ad-card" key={ad.id}>
                  <div className="ad-photo-section">
                    <div className="ad-photo-container">
                      <img
                        src={`${API_BASE}/ad-photo?adId=${ad.id}&photoIndex=0`}
                        className="ad-photo"
                        alt={ad.title}
                        onError={(e) => {
                          e.currentTarget.style.display = 'none';
                          const parent = e.currentTarget.parentElement;
                          if (parent) {
                            parent.innerHTML = '<div class="photo-placeholder"><span style="font-size:3rem">📷</span><span style="font-size:0.9rem;margin-top:5px">Нет фото</span></div>';
                          }
                        }}
                      />
                    </div>
                  </div>

                  <div className="ad-content">
                    <div className="ad-title">
                      {ad.title}
                      <span className="status-badge status-pending">На модерации</span>
                    </div>

                    <div className="ad-meta">
                      <span>Категория: {getCategoryLabel(ad.category)}</span>
                      <span>Подкатегория: {ad.subcategory || '—'}</span>
                      <span>Дата: {formatDate(ad.createdAt)}</span>
                    </div>

                    <div className="ad-price">{ad.price} руб.</div>

                    <div className="ad-location">
                      <span style={{ fontSize: '1.1rem' }}>📍</span> {ad.location || 'Не указано'}
                    </div>

                    <div className="ad-description">{ad.description}</div>

                    <div className="ad-footer">
                      <div className="ad-views">
                        <span style={{ fontSize: '1.1rem' }}>👁️</span> {ad.viewCount || 0} просмотров
                      </div>
                    </div>

                    <div className="moderation-actions">
                      <button
                        type="button"
                        className="btn btn-approve"
                        onClick={() => handleActionClick('approve', ad)}
                      >
                        Одобрить
                      </button>

                      <button
                        type="button"
                        className="btn btn-reject"
                        onClick={() => handleActionClick('reject', ad)}
                      >
                        Отозвать на доработку
                      </button>

                      <button
                        type="button"
                        className="btn btn-secondary"
                        style={{ marginLeft: 'auto' }}
                        onClick={() => toggleHistory(ad.id)}
                      >
                        История модерации
                      </button>
                    </div>

                    {historyOpen[ad.id] && (
                      <div className="history-box" style={{ display: 'block' }}>
                        {historyData[ad.id] && historyData[ad.id].length > 0 ? (
                          historyData[ad.id].map((item, index) => (
                            <div className="history-entry" key={`${ad.id}-history-${index}`}>
                              <div>
                                <strong>{item.fromStatus || '—'}</strong> → <strong>{item.toStatus}</strong>
                              </div>
                              <div className="history-meta">
                                {formatDate(item.createdAt)} {item.moderatorId ? `Модератор: ${item.moderatorId}` : ''}
                              </div>
                              {item.reason && <div>{item.reason}</div>}
                            </div>
                          ))
                        ) : (
                          <div className="history-entry">Пока нет записей</div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {confirmModalOpen && currentAction && currentAd && (
        <div className="modal" style={{ display: 'block' }}>
          <div className="modal-content">
            <div className="modal-icon">{ACTION_CONFIGS[currentAction].icon}</div>
            <h3 className="modal-title">{ACTION_CONFIGS[currentAction].title}</h3>
            <p className="modal-message">{ACTION_CONFIGS[currentAction].message(currentAd.title)}</p>
            <div className="modal-actions">
              <button type="button" className="modal-btn modal-btn-cancel" onClick={closeConfirm}>Отменить</button>
              <button
                type="button"
                className={`modal-btn modal-btn-confirm ${ACTION_CONFIGS[currentAction].confirmClass}`}
                onClick={executeAction}
              >
                Подтвердить
              </button>
            </div>
          </div>
        </div>
      )}

      {reasonModalOpen && currentAction && currentAd && (
        <div className="modal" style={{ display: 'block' }}>
          <div className="reason-modal-content">
            <div className="modal-icon">📝</div>
            <h3 className="modal-title">Выберите причину</h3>

            <div className="reason-section">
              <div className="reason-title">Выберите одну из причин:</div>
              <div className="reason-buttons" style={{ display: 'grid' }}>
                {reasons.map((reason) => (
                  <button
                    key={reason}
                    type="button"
                    className={`reason-btn ${currentReason === reason ? 'selected' : ''}`}
                    onClick={() => {
                      setCurrentReason(reason);
                      setCustomReason('');
                      setReasonRequired(false);
                    }}
                  >
                    {reason}
                  </button>
                ))}
              </div>

              <div className="custom-reason-section">
                <div className="reason-title">Или введите свою причину:</div>
                <textarea
                  className="custom-reason-input"
                  placeholder="Введите свою причину..."
                  value={customReason}
                  onChange={(event) => {
                    setCustomReason(event.target.value);
                    setCurrentReason('');
                    setReasonRequired(false);
                  }}
                />
              </div>

              {reasonRequired && (
                <div className="reason-required" style={{ display: 'block' }}>
                  Пожалуйста, выберите причину или введите свою
                </div>
              )}
            </div>

            <div className="modal-actions">
              <button type="button" className="modal-btn modal-btn-cancel" onClick={closeReason}>Отменить</button>
              <button type="button" className="modal-btn modal-btn-confirm" onClick={confirmWithReason}>Продолжить</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ModeratorDashboard;
