import React, { useState, useEffect } from 'react';
import './EditProfile.css';

const API_BASE_URL = 'http://localhost:8080';

const EditProfile = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [canEdit, setCanEdit] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [formData, setFormData] = useState({
    name: '',
    addressFull: '',
    addressCity: '',
    addressStreet: '',
    addressHouseNumber: '',
    addressBuilding: '',
    studyProgram: 'ФПМИ',
    course: '1'
  });

  const studyPrograms = [
    'ФПМИ', 'ВШПИ', 'ФРКТ', 'ЛФИ', 'ФАКТ',
    'ФЭФМ', 'ВШМ', 'КНТ', 'ФБМФ', 'ПИШ ФАЛТ', 'ВШСИ'
  ];

  useEffect(() => {
    fetchCurrentUser();
  }, []);

  const fetchCurrentUser = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/me`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (response.status === 401) {
        window.location.href = '/login.jsp';
        return;
      }

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const userData = await response.json();
      setUser(userData);

      setFormData({
        name: userData.name || '',
        addressFull: userData.address?.fullAddress || '',
        addressCity: userData.address?.city || '',
        addressStreet: userData.address?.street || '',
        addressHouseNumber: userData.address?.houseNumber || '',
        addressBuilding: userData.address?.building || '',
        studyProgram: userData.studyProgram || 'ФПМИ',
        course: userData.course?.toString() || '1'
      });

    } catch (error) {
      console.error('Ошибка загрузки пользователя:', error);
      setMessage({
        text: `❌ Ошибка соединения с сервером ${API_BASE_URL}. Убедитесь, что бэкенд запущен.`,
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordVerify = async (e) => {
    e.preventDefault();

    if (!passwordData.currentPassword) {
      setMessage({ text: '❌ Введите текущий пароль', type: 'error' });
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/users/change-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.currentPassword
        })
      });

      if (response.ok) {
        setCanEdit(true);
        setMessage({ text: '✅ Пароль подтвержден. Теперь вы можете изменить данные.', type: 'success' });
      } else {
        const errorText = await response.text();
        setMessage({ text: `❌ ${errorText || 'Неверный пароль'}`, type: 'error' });
      }
    } catch (error) {
      setMessage({ text: '❌ Ошибка при проверке пароля', type: 'error' });
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({ ...prev, [name]: value }));
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setMessage({ text: '❌ Имя не может быть пустым', type: 'error' });
      return;
    }

    if (passwordData.newPassword) {
      if (passwordData.newPassword !== passwordData.confirmPassword) {
        setMessage({ text: '❌ Новые пароли не совпадают', type: 'error' });
        return;
      }
      if (passwordData.newPassword.length < 8) {
        setMessage({ text: '❌ Пароль должен содержать минимум 8 символов', type: 'error' });
        return;
      }

      try {
        const passwordResponse = await fetch(`${API_BASE_URL}/api/users/change-password`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({
            currentPassword: passwordData.currentPassword,
            newPassword: passwordData.newPassword
          })
        });

        if (!passwordResponse.ok) {
          const errorMsg = await passwordResponse.text();
          setMessage({ text: `❌ ${errorMsg}`, type: 'error' });
          return;
        }

        setMessage({ text: '✅ Пароль успешно изменен!', type: 'success' });

        setTimeout(() => {
          setCanEdit(false);
          setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
          fetchCurrentUser();
        }, 2000);

        return;
      } catch (error) {
        setMessage({ text: '❌ Ошибка при смене пароля', type: 'error' });
        return;
      }
    }

    setMessage({ text: 'ℹ️ Нет изменений для сохранения', type: 'info' });
  };

  const handleCancelEdit = () => {
    setCanEdit(false);
    setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    fetchCurrentUser();
  };

  const handleLogout = async () => {
    try {
      await fetch(`${API_BASE_URL}/api/users/logout`, {
        method: 'POST',
        credentials: 'include'
      });
      window.location.href = '/login.jsp';
    } catch (error) {
      console.error('Ошибка выхода:', error);
      window.location.href = '/login.jsp';
    }
  };

  const checkPasswordsMatch = () => {
    if (passwordData.newPassword && passwordData.confirmPassword &&
        passwordData.newPassword !== passwordData.confirmPassword) {
      return false;
    }
    return true;
  };

  if (loading) {
    return (
        <div className="edit-container">
          <div className="loading">Загрузка...</div>
        </div>
    );
  }

  if (!user && !loading) {
    return (
        <div className="edit-container">
          <div className="message error">
            <p><strong>Не удалось загрузить профиль</strong></p>
            <p style={{ marginTop: '10px', fontSize: '0.9rem' }}>
              Проверьте что бэкенд запущен на {API_BASE_URL}
            </p>
            <div className="button-group" style={{ marginTop: '20px' }}>
              <button onClick={fetchCurrentUser} className="btn btn-primary">
                Попробовать снова
              </button>
              <a href="/login.jsp" className="btn btn-secondary">
                Перейти на страницу входа
              </a>
            </div>
          </div>
        </div>
    );
  }

  return (
      <div className="edit-container">
        <div className="portal-logo">PORTAL</div>
        <div className="page-title">Редактирование профиля</div>

        {message.text && (
            <div className={`message ${message.type}`}>
              {message.text}
            </div>
        )}

        {!canEdit ? (
            <div className="verification-section">
              <h3>🔒 Подтверждение личности</h3>
              <p>Для изменения данных профиля необходимо подтвердить ваш пароль</p>

              <form onSubmit={handlePasswordVerify}>
                <div className="form-group">
                  <label htmlFor="currentPassword">Текущий пароль</label>
                  <input
                      type="password"
                      id="currentPassword"
                      name="currentPassword"
                      value={passwordData.currentPassword}
                      onChange={handlePasswordChange}
                      placeholder="Введите ваш текущий пароль"
                      required
                  />
                </div>

                <div className="button-group">
                  <button type="submit" className="btn btn-primary">Подтвердить пароль</button>
                  <button type="button" onClick={handleLogout} className="btn btn-danger">Выйти</button>
                  <a href="/dashboard" className="btn btn-secondary">Отмена</a>
                </div>
              </form>
            </div>
        ) : (
            <>
              <div className="current-info">
                <strong>Текущий email:</strong> {user.email}<br />
                <strong>ID пользователя:</strong> {user.id}<br />
                <strong>Количество объявлений:</strong> {user.adList?.length || 0}
              </div>

              <form onSubmit={handleProfileUpdate}>
                <div className="form-group">
                  <label htmlFor="name">Имя пользователя *</label>
                  <input
                      type="text"
                      id="name"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      placeholder="Введите ваше имя"
                      required
                  />
                </div>

                <div className="address-section">
                  <h3>📍 Адрес проживания</h3>

                  <div className="form-group">
                    <label htmlFor="addressFull">Полный адрес</label>
                    <input
                        type="text"
                        id="addressFull"
                        name="addressFull"
                        value={formData.addressFull}
                        onChange={handleInputChange}
                        placeholder="г. Москва, ул. Примерная, д. 1"
                    />
                  </div>

                  <div className="row">
                    <div className="form-group">
                      <label htmlFor="addressCity">Город</label>
                      <input
                          type="text"
                          id="addressCity"
                          name="addressCity"
                          value={formData.addressCity}
                          onChange={handleInputChange}
                          placeholder="Москва"
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="addressStreet">Улица</label>
                      <input
                          type="text"
                          id="addressStreet"
                          name="addressStreet"
                          value={formData.addressStreet}
                          onChange={handleInputChange}
                          placeholder="Примерная"
                      />
                    </div>
                  </div>

                  <div className="row">
                    <div className="form-group">
                      <label htmlFor="addressHouseNumber">Дом</label>
                      <input
                          type="text"
                          id="addressHouseNumber"
                          name="addressHouseNumber"
                          value={formData.addressHouseNumber}
                          onChange={handleInputChange}
                          placeholder="1"
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="addressBuilding">Корпус</label>
                      <input
                          type="text"
                          id="addressBuilding"
                          name="addressBuilding"
                          value={formData.addressBuilding}
                          onChange={handleInputChange}
                          placeholder="2 (если есть)"
                      />
                    </div>
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="studyProgram">Учебная программа *</label>
                  <select
                      id="studyProgram"
                      name="studyProgram"
                      value={formData.studyProgram}
                      onChange={handleInputChange}
                      required
                  >
                    {studyPrograms.map(program => (
                        <option key={program} value={program}>
                          {program}
                        </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="course">Курс *</label>
                  <select
                      id="course"
                      name="course"
                      value={formData.course}
                      onChange={handleInputChange}
                      required
                  >
                    {[1, 2, 3, 4, 5, 6].map(num => (
                        <option key={num} value={num}>
                          {num} курс
                        </option>
                    ))}
                  </select>
                </div>

                <div className="password-section">
                  <h3>🔐 Смена пароля</h3>
                  <div className="form-group">
                    <label htmlFor="currentPassword">Текущий пароль (обязателен для смены пароля)</label>
                    <input
                        type="password"
                        id="currentPassword"
                        name="currentPassword"
                        value={passwordData.currentPassword}
                        onChange={handlePasswordChange}
                        placeholder="Введите текущий пароль"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="newPassword">Новый пароль (оставьте пустым, если не хотите менять)</label>
                    <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        value={passwordData.newPassword}
                        onChange={handlePasswordChange}
                        placeholder="Минимум 8 символов"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="confirmPassword">Подтверждение нового пароля</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={passwordData.confirmPassword}
                        onChange={handlePasswordChange}
                        style={{
                          borderColor: !checkPasswordsMatch() && passwordData.confirmPassword ? '#dc3545' :
                              passwordData.newPassword && passwordData.confirmPassword ? '#28a745' : ''
                        }}
                    />
                  </div>
                </div>

                <div className="button-group">
                  <button type="submit" className="btn btn-primary">Сохранить изменения</button>
                  <button type="button" onClick={() => window.location.href = '/dashboard'} className="btn btn-secondary">
                    Отмена
                  </button>
                  <button type="button" onClick={handleCancelEdit} className="btn btn-danger">
                    Отменить редактирование
                  </button>
                </div>
              </form>
            </>
        )}
      </div>
  );
};

export default EditProfile;