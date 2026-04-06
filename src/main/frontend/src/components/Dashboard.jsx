import React, { useState, useEffect } from 'react';
import styles from './Dashboard.css';

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [ads, setAds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');
  const [activeModal, setActiveModal] = useState(null);

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [deletePassword, setDeletePassword] = useState('');

  // Загрузка данных
  useEffect(() => {
    loadUserData();
  }, []);

  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const loadUserData = async () => {
    setLoading(true);
    try {
      // Получаем данные пользователя
      const userResponse = await fetch('http://localhost:8080/api/users/me', {
        method: 'GET',
        credentials: 'include'
      });

      if (!userResponse.ok) {
        if (userResponse.status === 401) {
          window.location.href = '/login';
          return;
        }
        throw new Error('Ошибка загрузки пользователя');
      }
      const userData = await userResponse.json();
      setUser(userData);

      // Получаем объявления пользователя
      const adsResponse = await fetch('http://localhost:8080/api/announcements/my', {
        method: 'GET',
        credentials: 'include'
      });

      if (adsResponse.ok) {
        const adsData = await adsResponse.json();
        setAds(adsData);
      }
    } catch (error) {
      console.error('Ошибка загрузки данных:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert('❌ Пароли не совпадают!');
      return;
    }
    if (passwordData.newPassword.length < 8) {
      alert('❌ Пароль должен содержать минимум 8 символов!');
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/api/users/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword
        }),
        credentials: 'include'
      });

      if (response.ok) {
        setSuccessMessage('Пароль успешно изменен!');
        closeModals();
        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
      } else {
        const error = await response.text();
        alert('Ошибка: ' + error);
      }
    } catch (error) {
      alert('Ошибка при смене пароля');
    }
  };

  const handleDeleteAccount = async (e) => {
    e.preventDefault();
    const confirm = window.confirm('❗ Вы уверены, что хотите удалить аккаунт? Это действие нельзя отменить!');
    if (!confirm) return;

    try {
      const response = await fetch('http://localhost:8080/api/users/delete-account', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: deletePassword }),
        credentials: 'include'
      });

      if (response.ok) {
        setSuccessMessage('Аккаунт успешно удален');
        setTimeout(() => {
          window.location.href = '/login';
        }, 2000);
      } else {
        const error = await response.text();
        alert('Ошибка: ' + error);
      }
    } catch (error) {
      alert('Ошибка при удалении аккаунта');
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/api/users/logout', {
        method: 'POST',
        credentials: 'include'
      });
      window.location.href = '/login';
    } catch (error) {
      console.error('Ошибка выхода:', error);
      window.location.href = '/login';
    }
  };

  const handleDeleteAd = async (adId) => {
    if (!window.confirm('Вы уверены, что хотите удалить это объявление?')) return;

    try {
      const response = await fetch(`http://localhost:8080/api/announcements/${adId}`, {
        method: 'DELETE',
        credentials: 'include'
      });

      if (response.ok) {
        setSuccessMessage('Объявление удалено');
        loadUserData(); // Обновляем список
      } else {
        alert('Ошибка при удалении');
      }
    } catch (error) {
      alert('Ошибка при удалении');
    }
  };

  const getStatusClass = (status) => {
    const statusMap = {
      'ACTIVE': 'statusActive',
      'DRAFT': 'statusDraft',
      'UNDER_MODERATION': 'statusModeration',
      'ARCHIVED': 'statusArchived',
      'DELETED': 'statusDeleted'
    };
    return styles[statusMap[status]] || styles.statusDraft;
  };

  const formatPrice = (price) => {
    if (price === -1) return 'Договорная';
    if (price === 0) return 'Бесплатно';
    return `${price.toLocaleString()} руб.`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Не указано';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
  };

  const getCategoryDisplayName = (category) => {
    const categoryMap = {
      'ELECTRONICS': 'Электроника',
      'CLOTHING': 'Одежда',
      'BOOKS': 'Книги',
      'FURNITURE': 'Мебель',
      'SPORTS': 'Спорт',
      'OTHER': 'Другое'
    };
    return categoryMap[category] || category;
  };

  const getConditionDisplayName = (condition) => {
    const conditionMap = {
      'NEW': 'Новое',
      'LIKE_NEW': 'Как новое',
      'GOOD': 'Хорошее',
      'FAIR': 'Удовлетворительное',
      'POOR': 'Плохое'
    };
    return conditionMap[condition] || condition;
  };

  const getStatusDisplayName = (status) => {
    const statusMap = {
      'ACTIVE': 'Активно',
      'DRAFT': 'Черновик',
      'UNDER_MODERATION': 'На модерации',
      'ARCHIVED': 'Архивировано',
      'DELETED': 'Удалено'
    };
    return statusMap[status] || status;
  };

  const openModal = (modalName) => setActiveModal(modalName);
  const closeModals = () => {
    setActiveModal(null);
    setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    setDeletePassword('');
  };

  if (loading) {
    return (
        <div className={styles.loadingContainer}>
          <div className={styles.loader}></div>
          <p>Загрузка...</p>
        </div>
    );
  }

  if (!user) {
    return (
        <div className={styles.errorContainer}>
          <p>Не удалось загрузить данные пользователя</p>
          <button onClick={() => window.location.href = '/login'} className={styles.btnPrimary}>
            Войти снова
          </button>
        </div>
    );
  }

  return (
      <div className={styles.dashboardContainer}>
        {successMessage && (
            <div className={styles.successMessage}>
              <span className={styles.successIcon}>🎉</span>
              <span>{successMessage}</span>
            </div>
        )}

        <div className={styles.header}>
          <div className={styles.headerTop}>
            <div className={styles.portalLogo}>PORTAL</div>
          </div>
        </div>

        <div className={styles.headerBell}>
          <div className={styles.headerTopBell}>
            <div className={styles.notificationLeft}>
              <button className={styles.bellButton}>🔔</button>
            </div>

            <div className={styles.avatarCenter}>
              <div className={styles.avatarCircle}>
                <span className={styles.avatarIcon}>👤</span>
                <div className={styles.onlineStatus}></div>
              </div>
            </div>

            <div className={styles.buttonsVertical}>
              <button onClick={() => window.location.href = '/edit-profile'} className={styles.btnPrimary}>
                <span className={styles.btnIcon}>✏️</span>
                Редактировать профиль
              </button>
              <button onClick={() => openModal('account')} className={styles.btnPrimary}>
                <span className={styles.btnIcon}>⚙️</span>
                Управление аккаунтом
              </button>
            </div>
          </div>
        </div>

        <div className={styles.profileActions}>
          {user.moderator && (
              <button onClick={() => window.location.href = '/moderator/dashboard'} className={styles.btnModerator}>
                Кабинет модератора
              </button>
          )}
          {user.admin && (
              <button onClick={() => window.location.href = '/admin/dashboard'} className={styles.btnAdmin}>
                Админка
              </button>
          )}
        </div>

        <div className={styles.stats}>
          <div className={styles.statCard}>
            <div className={styles.statNumber}>{ads.length}</div>
            <div className={styles.statLabel}>Объявлений</div>
          </div>
          <div className={styles.statCard}>
            <div className={styles.statNumber}>{user.rating?.toFixed(1) || '0.0'}</div>
            <div className={styles.statLabel}>Рейтинг</div>
          </div>
          <div className={styles.statCard}>
            <div className={styles.statNumber}>{user.coins || 0}</div>
            <div className={styles.statLabel}>Коинов</div>
          </div>
          <div className={styles.statCard}>
            <div className={styles.statNumber}>{user.course || 1}</div>
            <div className={styles.statLabel}>Курс</div>
          </div>
        </div>

        <div className={styles.userInfo}>
          <div className={styles.infoCard}>
            <h3>👤 Основная информация</h3>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Имя:</span>
              <span className={styles.infoValue}>{user.name || 'Не указано'}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Email:</span>
              <span className={styles.infoValue}>{user.email}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Адрес:</span>
              <span className={styles.infoValue}>{user.address || 'Не указан'}</span>
            </div>
          </div>

          <div className={styles.infoCard}>
            <h3>🎓 Учебная информация</h3>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Учебная программа:</span>
              <span className={styles.infoValue}>{user.studyProgram || 'Не указана'}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Курс:</span>
              <span className={styles.infoValue}>{user.course || 1} курс</span>
            </div>
          </div>

          <div className={styles.infoCard}>
            <h3>⭐ Рейтинг и коины</h3>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Рейтинг:</span>
              <span className={styles.infoValue}>
              <span className={styles.ratingStars}>
                {[...Array(5)].map((_, i) => (
                    <span key={i}>{i < Math.round(user.rating || 0) ? '★' : '☆'}</span>
                ))}
              </span>
              ({(user.rating || 0).toFixed(1)})
            </span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Коины:</span>
              <span className={`${styles.infoValue} ${styles.coins}`}>{user.coins || 0} 🪙</span>
            </div>
          </div>
        </div>

        <div className={styles.adsSection}>
          <h3>
            📋 Мои объявления
            <button onClick={() => window.location.href = '/create-ad'} className={styles.btnSuccess}>
              + Создать объявление
            </button>
          </h3>

          <div className={styles.adList}>
            {ads.length === 0 ? (
                <div className={styles.noAds}>
                  <h4>У вас пока нет объявлений</h4>
                  <p>Создайте первое объявление, чтобы начать продавать или обмениваться вещами!</p>
                </div>
            ) : (
                ads.map(ad => (
                    <div key={ad.id} className={styles.adItem}>
                      <div className={styles.adTitle}>{ad.title}</div>
                      <div className={styles.adMeta}>
                        <span className={styles.adCategory}>{getCategoryDisplayName(ad.category)}</span>
                        <span className={styles.adCondition}>{getConditionDisplayName(ad.condition)}</span>
                        <span className={`${styles.adStatus} ${getStatusClass(ad.status)}`}>
                    {getStatusDisplayName(ad.status)}
                  </span>
                      </div>
                      <div className={styles.adPrice}>{formatPrice(ad.price)}</div>
                      <div className={styles.adLocation}>📍 {ad.location || 'Не указано'}</div>
                      <div className={styles.adDescription}>{ad.description}</div>
                      <div className={styles.adViews}>👁️ {ad.viewCount || 0} просмотров</div>
                      <div className={styles.adDate}>📅 {formatDate(ad.createdAt)}</div>
                      <div className={styles.adActions}>
                        <button onClick={() => window.location.href = `/edit-ad?id=${ad.id}`} className={styles.btnEdit}>
                          Редактировать
                        </button>
                        <button onClick={() => handleDeleteAd(ad.id)} className={styles.btnDanger}>
                          Удалить
                        </button>
                      </div>
                    </div>
                ))
            )}
          </div>
        </div>

        <div className={styles.actionButtons}>
          <button onClick={() => window.location.href = '/'} className={styles.btnPrimary}>
            На главную
          </button>
          <button onClick={handleLogout} className={styles.btnSecondary}>
            Выйти
          </button>
        </div>

        {activeModal === 'account' && (
            <div className={styles.modal} onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className={styles.modalContent}>
                <span className={styles.close} onClick={closeModals}>&times;</span>
                <h3>🔧 Управление аккаунтом</h3>
                <div className={styles.buttonGroup}>
                  <button onClick={() => openModal('password')} className={styles.btnPrimary}>
                    Изменить пароль
                  </button>
                  <button onClick={() => openModal('delete')} className={styles.btnDanger}>
                    Удалить аккаунт
                  </button>
                </div>
              </div>
            </div>
        )}

        {activeModal === 'password' && (
            <div className={styles.modal} onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className={styles.modalContent}>
                <span className={styles.close} onClick={closeModals}>&times;</span>
                <h3>🔐 Изменение пароля</h3>
                <form onSubmit={handleChangePassword}>
                  <div className={styles.formGroup}>
                    <label>Текущий пароль</label>
                    <input type="password" required value={passwordData.currentPassword} onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})} />
                  </div>
                  <div className={styles.formGroup}>
                    <label>Новый пароль</label>
                    <input type="password" required value={passwordData.newPassword} onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})} />
                  </div>
                  <div className={styles.formGroup}>
                    <label>Подтверждение нового пароля</label>
                    <input type="password" required value={passwordData.confirmPassword} onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})} />
                  </div>
                  <div className={styles.buttonGroup}>
                    <button type="submit" className={styles.btnPrimary}>Сохранить пароль</button>
                    <button type="button" onClick={closeModals} className={styles.btnSecondary}>Отмена</button>
                  </div>
                </form>
              </div>
            </div>
        )}

        {activeModal === 'delete' && (
            <div className={styles.modal} onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className={styles.modalContent}>
                <span className={styles.close} onClick={closeModals}>&times;</span>
                <h3>🗑️ Удаление аккаунта</h3>
                <div className={styles.warningBox}>
                  <h4>⚠️ Внимание!</h4>
                  <p>Это действие необратимо. Все ваши данные, включая объявления, будут удалены без возможности восстановления.</p>
                </div>
                <p>Для подтверждения введите ваш пароль:</p>
                <form onSubmit={handleDeleteAccount}>
                  <div className={styles.formGroup}>
                    <label>Текущий пароль</label>
                    <input type="password" required value={deletePassword} onChange={(e) => setDeletePassword(e.target.value)} />
                  </div>
                  <div className={styles.buttonGroup}>
                    <button type="submit" className={styles.btnDanger}>Удалить аккаунт</button>
                    <button type="button" onClick={closeModals} className={styles.btnSecondary}>Отмена</button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
};

export default Dashboard;