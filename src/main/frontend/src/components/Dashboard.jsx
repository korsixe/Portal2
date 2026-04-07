import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import ChangePasswordModal from './ChangePasswordModal';

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [ads, setAds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [activeModal, setActiveModal] = useState(null);
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
    if (errorMessage) {
      const timer = setTimeout(() => setErrorMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [successMessage, errorMessage]);

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
        const activeAds = adsData.filter(ad => ad.status !== 'DELETED');
        setAds(activeAds);
      }
    } catch (error) {
      console.error('Ошибка загрузки данных:', error);
      setErrorMessage('Не удалось загрузить данные');
    } finally {
      setLoading(false);
    }
  };

  // Обработчик смены пароля (использует ChangePasswordRequest)
  const handlePasswordChanged = async (currentPassword, newPassword, confirmPassword) => {
    // Валидация
    if (newPassword !== confirmPassword) {
      setErrorMessage('❌ Пароли не совпадают!');
      return false;
    }
    if (newPassword.length < 8) {
      setErrorMessage('❌ Пароль должен содержать минимум 8 символов!');
      return false;
    }

    try {
      // Отправляем запрос в формате ChangePasswordRequest
      const response = await fetch('http://localhost:8080/api/users/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentPassword: currentPassword,
          newPassword: newPassword,
          confirmPassword: confirmPassword
        }),
        credentials: 'include'
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setSuccessMessage(data.message || '✅ Пароль успешно изменен!');
        // Обновляем пользователя в сессии
        if (data.user) {
          setUser(data.user);
        }
        return true;
      } else {
        setErrorMessage(data.message || '❌ Ошибка при смене пароля');
        return false;
      }
    } catch (error) {
      console.error('Ошибка:', error);
      setErrorMessage('❌ Ошибка при смене пароля');
      return false;
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
        setErrorMessage('Ошибка: ' + error);
      }
    } catch (error) {
      setErrorMessage('Ошибка при удалении аккаунта');
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
        loadUserData();
      } else {
        setErrorMessage('Ошибка при удалении');
      }
    } catch (error) {
      setErrorMessage('Ошибка при удалении');
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
    return statusMap[status] || 'statusDraft';
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
  const closeModals = () => setActiveModal(null);

  const formatUserAddress = (address) => {
    if (!address) return 'Не указан';
    return address.formattedAddress || address.fullAddress || address.address || 'Не указан';
  };

  if (loading) {
    return (
        <div className="loadingContainer">
          <div className="loader"></div>
          <p>Загрузка...</p>
        </div>
    );
  }

  if (!user) {
    return (
        <div className="errorContainer">
          <p>Не удалось загрузить данные пользователя</p>
          <button onClick={() => window.location.href = '/login'} className="btnPrimary">
            Войти снова
          </button>
        </div>
    );
  }

  const roleSet = new Set((user.roles || []).map((role) => String(role)));
  const isModerator = Boolean(user.moderator) || roleSet.has('MODERATOR') || roleSet.has('ADMIN');
  const isAdmin = Boolean(user.admin) || roleSet.has('ADMIN');

  return (
      <div className="dashboardContainer">
        {successMessage && (
            <div className="successMessage">
              <span className="successIcon">🎉</span>
              <span>{successMessage}</span>
            </div>
        )}

        {errorMessage && (
            <div className="errorMessage">
              <span className="errorIcon">❌</span>
              <span>{errorMessage}</span>
            </div>
        )}

        <div className="header">
          <div className="headerTop">
            <div className="portalLogo">PORTAL</div>
          </div>
        </div>

        <div className="headerBell">
          <div className="headerTopBell">
            <div className="notificationLeft">
              <button className="bellButton">🔔</button>
            </div>

            <div className="avatarCenter">
              <div className="avatarCircle">
                <span className="avatarIcon">👤</span>
                <div className="onlineStatus"></div>
              </div>
            </div>

            <div className="buttonsVertical">
              <button onClick={() => window.location.href = '/edit-profile'} className="btnPrimary">
                <span className="btnIcon">✏️</span>
                Редактировать профиль
              </button>
              <button onClick={() => openModal('account')} className="btnPrimary">
                <span className="btnIcon">⚙️</span>
                Управление аккаунтом
              </button>
            </div>
          </div>
        </div>

        <div className="profileActions">
          {isModerator && (
              <button onClick={() => window.location.href = '/moderator/dashboard'} className="btnModerator">
                Кабинет модератора
              </button>
          )}
          {isAdmin && (
              <button onClick={() => window.location.href = '/admin/dashboard'} className="btnAdmin">
                Админка
              </button>
          )}
        </div>

        <div className="stats">
          <div className="statCard">
            <div className="statNumber">{ads.length}</div>
            <div className="statLabel">Объявлений</div>
          </div>
          <div className="statCard">
            <div className="statNumber">{user.rating?.toFixed(1) || '0.0'}</div>
            <div className="statLabel">Рейтинг</div>
          </div>
          <div className="statCard">
            <div className="statNumber">{user.coins || 0}</div>
            <div className="statLabel">Коинов</div>
          </div>
          <div className="statCard">
            <div className="statNumber">{user.course || 1}</div>
            <div className="statLabel">Курс</div>
          </div>
        </div>

        <div className="userInfo">
          <div className="infoCard">
            <h3>👤 Основная информация</h3>
            <div className="infoItem">
              <span className="infoLabel">Имя:</span>
              <span className="infoValue">{user.name || 'Не указано'}</span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Email:</span>
              <span className="infoValue">{user.email}</span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Адрес:</span>
              <span className="infoValue">{formatUserAddress(user.address)}</span>
            </div>
          </div>

          <div className="infoCard">
            <h3>🎓 Учебная информация</h3>
            <div className="infoItem">
              <span className="infoLabel">Учебная программа:</span>
              <span className="infoValue">{user.studyProgram || 'Не указана'}</span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Курс:</span>
              <span className="infoValue">{user.course || 1} курс</span>
            </div>
          </div>

          <div className="infoCard">
            <h3>⭐ Рейтинг и коины</h3>
            <div className="infoItem">
              <span className="infoLabel">Рейтинг:</span>
              <span className="infoValue">
              <span className="ratingStars">
                {[...Array(5)].map((_, i) => (
                    <span key={i}>{i < Math.round(user.rating || 0) ? '★' : '☆'}</span>
                ))}
              </span>
              ({(user.rating || 0).toFixed(1)})
            </span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Коины:</span>
              <span className="infoValue coins">{user.coins || 0} 🪙</span>
            </div>
          </div>
        </div>

        <div className="adsSection">
          <h3>
            📋 Мои объявления
            <button onClick={() => window.location.href = '/create-ad'} className="btnSuccess">
              + Создать объявление
            </button>
          </h3>

          <div className="adList">
            {ads.length === 0 ? (
                <div className="noAds">
                  <h4>У вас пока нет объявлений</h4>
                  <p>Создайте первое объявление, чтобы начать продавать или обмениваться вещами!</p>
                </div>
            ) : (
                ads.map(ad => (
                    <div key={ad.id} className="adItem">
                      <div className="adTitle">{ad.title}</div>
                      <div className="adMeta">
                        <span className="adCategory">{getCategoryDisplayName(ad.category)}</span>
                        <span className="adCondition">{getConditionDisplayName(ad.condition)}</span>
                        <span className={`adStatus ${getStatusClass(ad.status)}`}>
                    {getStatusDisplayName(ad.status)}
                  </span>
                      </div>
                      <div className="adPrice">{formatPrice(ad.price)}</div>
                      <div className="adLocation">📍 {ad.location || 'Не указано'}</div>
                      <div className="adDescription">{ad.description}</div>
                      <div className="adViews">👁️ {ad.viewCount || 0} просмотров</div>
                      <div className="adDate">📅 {formatDate(ad.createdAt)}</div>
                      <div className="adActions">
                        <button onClick={() => window.location.href = `/edit-ad?id=${ad.id}`} className="btnEdit">
                          Редактировать
                        </button>
                        <button onClick={() => handleDeleteAd(ad.id)} className="btnDanger">
                          Удалить
                        </button>
                      </div>
                    </div>
                ))
            )}
          </div>
        </div>

        <div className="actionButtons">
          <button onClick={() => window.location.href = '/'} className="btnPrimary">
            На главную
          </button>
          <button onClick={handleLogout} className="btnSecondary">
            Выйти
          </button>
        </div>

        {/* Модальное окно управления аккаунтом */}
        {activeModal === 'account' && (
            <div className="modal" onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className="modalContent">
                <span className="close" onClick={closeModals}>&times;</span>
                <h3>🔧 Управление аккаунтом</h3>
                <div className="buttonGroup">
                  <button onClick={() => openModal('password')} className="btnPrimary">
                    Изменить пароль
                  </button>
                  <button onClick={() => openModal('delete')} className="btnDanger">
                    Удалить аккаунт
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Модальное окно смены пароля - используем отдельный компонент */}
        {activeModal === 'password' && (
            <ChangePasswordModal
                onClose={closeModals}
                onChangePassword={handlePasswordChanged}
            />
        )}

        {/* Модальное окно удаления аккаунта */}
        {activeModal === 'delete' && (
            <div className="modal" onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className="modalContent">
                <span className="close" onClick={closeModals}>&times;</span>
                <h3>🗑️ Удаление аккаунта</h3>
                <div className="warningBox">
                  <h4>⚠️ Внимание!</h4>
                  <p>Это действие необратимо. Все ваши данные, включая объявления, будут удалены без возможности восстановления.</p>
                </div>
                <p>Для подтверждения введите ваш пароль:</p>
                <form onSubmit={handleDeleteAccount}>
                  <div className="formGroup">
                    <label>Текущий пароль</label>
                    <input
                        type="password"
                        required
                        value={deletePassword}
                        onChange={(e) => setDeletePassword(e.target.value)}
                    />
                  </div>
                  <div className="buttonGroup">
                    <button type="submit" className="btnDanger">Удалить аккаунт</button>
                    <button type="button" onClick={closeModals} className="btnSecondary">Отмена</button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
};

export default Dashboard;
