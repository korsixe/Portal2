// ChangePasswordModal.jsx
import React, { useState } from 'react';

const ChangePasswordModal = ({ onClose, onChangePassword }) => {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const success = await onChangePassword(currentPassword, newPassword, confirmPassword);

    setLoading(false);
    if (success) {
      onClose();
    }
  };

  return (
      <div className="modal" onClick={(e) => e.target === e.currentTarget && onClose()}>
        <div className="modalContent">
          <span className="close" onClick={onClose}>&times;</span>
          <h3>🔐 Изменение пароля</h3>

          {error && (
              <div className="errorMessage" style={{ marginBottom: '15px' }}>
                {error}
              </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="formGroup">
              <label>Текущий пароль</label>
              <input
                  type="password"
                  required
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  disabled={loading}
                  placeholder="Введите текущий пароль"
              />
            </div>

            <div className="formGroup">
              <label>Новый пароль</label>
              <input
                  type="password"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  disabled={loading}
                  placeholder="Минимум 8 символов"
              />
            </div>

            <div className="formGroup">
              <label>Подтверждение нового пароля</label>
              <input
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={loading}
                  placeholder="Повторите новый пароль"
              />
            </div>

            <div className="buttonGroup">
              <button type="submit" className="btnPrimary" disabled={loading}>
                {loading ? 'Сохранение...' : 'Сохранить пароль'}
              </button>
              <button type="button" onClick={onClose} className="btnSecondary" disabled={loading}>
                Отмена
              </button>
            </div>
          </form>
        </div>
      </div>
  );
};

export default ChangePasswordModal;