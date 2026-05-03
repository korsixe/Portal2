import React, { useCallback, useState, useEffect } from 'react';
import './EditProfile.css';
import { useI18n } from '../i18n/I18nProvider';
import YandexLocationPicker from './YandexLocationPicker.jsx';

const API_BASE_URL = 'http://localhost:8080';

const studyPrograms = [
  'ФПМИ', 'ВШПИ', 'ФРКТ', 'ЛФИ', 'ФАКТ',
  'ФЭФМ', 'ВШМ', 'КНТ', 'ФБМФ', 'ПИШ ФАЛТ', 'ВШСИ'
];

const EditProfile = () => {
  const { t } = useI18n();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [passwordData, setPasswordData] = useState({ newPassword: '', confirmPassword: '' });
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

  useEffect(() => {
    fetchCurrentUser();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const fetchCurrentUser = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/me`, {
        method: 'GET',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      });

      if (response.status === 401) {
        window.location.href = '/login.jsp';
        return;
      }

      if (!response.ok) throw new Error(`HTTP ${response.status}`);

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
    } catch {
      setMessage({ text: t('editProfile.serverError'), type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const changePassword = async (newPassword) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/change-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ currentPassword: user.password || '', newPassword })
      });
      if (response.ok) return { success: true, message: t('editProfile.passwordChanged') };
      const errorMsg = await response.text();
      return { success: false, message: errorMsg || t('editProfile.passwordChangeError') };
    } catch {
      return { success: false, message: t('editProfile.connectionError') };
    }
  };

  const updateProfile = async (profileData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/${user.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(profileData)
      });
      if (response.ok) {
        const updatedUser = await response.json();
        setUser(updatedUser);
        return { success: true, message: t('editProfile.profileUpdated') };
      }
      let errorMsg = t('editProfile.profileUpdateError');
      let fieldError = null;
      try {
        const errorData = await response.json();
        if (errorData?.message) { errorMsg = errorData.message; fieldError = errorData.field; }
      } catch {}
      return { success: false, message: errorMsg, field: fieldError };
    } catch {
      return { success: false, message: t('editProfile.connectionError') };
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (name === 'name') {
      if (!value.trim()) {
        setFieldErrors(prev => ({ ...prev, name: t('editProfile.nameEmpty') }));
      } else if (value.includes(' ')) {
        setFieldErrors(prev => ({ ...prev, name: t('editProfile.nameNoSpaces') }));
      } else {
        setFieldErrors(prev => ({ ...prev, name: '' }));
      }
    } else if (fieldErrors[name]) {
      setFieldErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({ ...prev, [name]: value }));
  };

  const handleAddressSelect = useCallback((address) => {
    setFormData((prev) => ({
      ...prev,
      addressFull: address,
      addressCity: '',
      addressStreet: '',
      addressHouseNumber: '',
      addressBuilding: ''
    }));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});

    if (!formData.name.trim()) {
      setFieldErrors(prev => ({ ...prev, name: t('editProfile.nameEmpty') }));
      setMessage({ text: t('editProfile.fillRequired'), type: 'error' });
      return;
    }

    if (passwordData.newPassword) {
      if (passwordData.newPassword !== passwordData.confirmPassword) {
        setFieldErrors(prev => ({ ...prev, confirmPassword: t('editProfile.newPasswordMismatch') }));
        setMessage({ text: t('editProfile.passwordMismatch'), type: 'error' });
        return;
      }
      if (passwordData.newPassword.length < 8) {
        setFieldErrors(prev => ({ ...prev, newPassword: t('editProfile.passwordTooShort') }));
        setMessage({ text: t('editProfile.passwordTooShort'), type: 'error' });
        return;
      }
    }

    setMessage({ text: t('editProfile.saving'), type: 'info' });

    if (passwordData.newPassword) {
      const passwordResult = await changePassword(passwordData.newPassword);
      if (!passwordResult.success) {
        setFieldErrors(prev => ({ ...prev, password: passwordResult.message }));
        setMessage({ text: passwordResult.message, type: 'error' });
        return;
      }
      setMessage({ text: t('editProfile.passwordChanged'), type: 'success' });
      setPasswordData({ newPassword: '', confirmPassword: '' });
    }

    const profileData = {
      name: formData.name.trim(),
      address: {
        fullAddress: formData.addressFull,
        city: formData.addressCity,
        street: formData.addressStreet,
        houseNumber: formData.addressHouseNumber,
        building: formData.addressBuilding
      },
      studyProgram: formData.studyProgram,
      course: parseInt(formData.course)
    };

    const profileResult = await updateProfile(profileData);
    if (profileResult.success) {
      setMessage({ text: t('editProfile.profileUpdated'), type: 'success' });
      setFieldErrors({});
      setTimeout(() => { window.location.href = '/dashboard'; }, 1500);
    } else {
      if (profileResult.field) {
        setFieldErrors(prev => ({ ...prev, [profileResult.field]: profileResult.message }));
      } else {
        setFieldErrors(prev => ({ ...prev, general: profileResult.message }));
      }
      setMessage({ text: profileResult.message, type: 'error' });
    }
  };

  const handleLogout = async () => {
    try {
      await fetch(`${API_BASE_URL}/api/users/logout`, { method: 'POST', credentials: 'include' });
    } catch {}
    window.location.href = '/login.jsp';
  };

  const checkPasswordsMatch = () => {
    if (passwordData.newPassword && passwordData.confirmPassword &&
        passwordData.newPassword !== passwordData.confirmPassword) return false;
    return true;
  };

  if (loading) {
    return (
      <div className="edit-container">
        <div className="loading">{t('editProfile.loading')}</div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="edit-container">
        <div className="message error">
          <p><strong>{t('editProfile.loadError')}</strong></p>
          <div className="button-group" style={{ marginTop: '20px' }}>
            <button onClick={fetchCurrentUser} className="btn btn-primary">
              {t('editProfile.tryAgain')}
            </button>
            <a href="/login.jsp" className="btn btn-secondary">
              {t('editProfile.goToLogin')}
            </a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="edit-container">
      <div className="portal-logo">PORTAL</div>
      <div className="page-title">{t('editProfile.title')}</div>

      <div className="current-info">
        <strong>{t('editProfile.email')}</strong> {user.email}<br />
        <strong>{t('editProfile.adCount')}</strong> {user.adList?.length || 0}
      </div>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">{t('editProfile.username')}</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            placeholder="ivanov"
            className={fieldErrors.name ? 'error-field' : ''}
            required
          />
          {fieldErrors.name && (
            <small style={{ color: '#dc3545', marginTop: '5px', display: 'block' }}>
              {fieldErrors.name}
            </small>
          )}
        </div>

        <div className="address-section">
          <h3>📍 {t('editProfile.addressSection')}</h3>
          <div className="form-group">
            <label>{t('editProfile.addressLabel')}</label>
            <div className="location-preview">
              <span className="location-preview-label">{t('editProfile.addressSelected')}</span>
              <span className="location-preview-value">
                {formData.addressFull || t('editProfile.addressNotSelected')}
              </span>
            </div>
            <YandexLocationPicker initialAddress={formData.addressFull} onAddressChange={handleAddressSelect} />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="studyProgram">{t('editProfile.studyProgram')}</label>
          <select id="studyProgram" name="studyProgram" value={formData.studyProgram} onChange={handleInputChange} required>
            {studyPrograms.map(program => (
              <option key={program} value={program}>{program}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="course">{t('editProfile.course')}</label>
          <select id="course" name="course" value={formData.course} onChange={handleInputChange} required>
            {[1, 2, 3, 4, 5, 6].map(num => (
              <option key={num} value={num}>{num}</option>
            ))}
          </select>
        </div>

        <div className="password-section">
          <h3>🔐 {t('editProfile.passwordSection')}</h3>
          <div className="form-group">
            <label htmlFor="newPassword">{t('editProfile.newPassword')}</label>
            <input
              type="password"
              id="newPassword"
              name="newPassword"
              value={passwordData.newPassword}
              onChange={handlePasswordChange}
              placeholder={t('editProfile.newPasswordPlaceholder')}
            />
          </div>
          <div className="form-group">
            <label htmlFor="confirmPassword">{t('editProfile.confirmPassword')}</label>
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
            {fieldErrors.confirmPassword && (
              <small style={{ color: '#dc3545', marginTop: '5px', display: 'block' }}>
                {fieldErrors.confirmPassword}
              </small>
            )}
            {!checkPasswordsMatch() && passwordData.confirmPassword && !fieldErrors.confirmPassword && (
              <small style={{ color: '#dc3545', marginTop: '5px', display: 'block' }}>
                {t('editProfile.passwordMismatch')}
              </small>
            )}
          </div>
        </div>

        {message.text && (
          <div className={`message ${message.type}`} style={{ marginBottom: '20px' }}>
            {message.text}
          </div>
        )}

        <div className="button-group">
          <button type="submit" className="btn btn-primary">{t('editProfile.saveBtn')}</button>
          <a href="/dashboard" className="btn btn-secondary">{t('editProfile.cancelBtn')}</a>
          <button type="button" onClick={handleLogout} className="btn btn-secondary">{t('editProfile.signOutBtn')}</button>
        </div>
      </form>
    </div>
  );
};

export default EditProfile;
