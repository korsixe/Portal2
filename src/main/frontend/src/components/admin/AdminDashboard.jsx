import React, { useEffect, useState } from 'react';
import { apiGet, apiPost } from '../../api';
import AccessDenied from '../AccessDenied';
import './AdminDashboard.css';

const ROLE_LABELS = {
  ADMIN: 'Администратор',
  MODERATOR: 'Модератор',
  USER: 'Пользователь'
};

function getRoleLabel(role) {
  if (!role) return '';
  if (typeof role === 'string') return ROLE_LABELS[role] || role;
  if (role.displayName) return role.displayName;
  return String(role.name || role);
}

function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState(null);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(true);
  const [accessDenied, setAccessDenied] = useState(false);

  const refreshDashboard = async () => {
    const data = await apiGet('/api/admin/dashboard');
    const orderedUsers = (data.users || []).slice().sort((a, b) => (a.id || 0) - (b.id || 0));
    setUsers(orderedUsers);
    setStats(data.stats || null);
  };

  useEffect(() => {
    let active = true;
    setLoading(true);
    apiGet('/api/admin/dashboard')
      .then((data) => {
        if (!active) return;
        const orderedUsers = (data.users || []).slice().sort((a, b) => (a.id || 0) - (b.id || 0));
        setUsers(orderedUsers);
        setStats(data.stats || null);
        setLoading(false);
      })
      .catch((err) => {
        if (!active) return;
        if (err && (err.status === 401 || err.status === 403)) {
          setAccessDenied(true);
        } else {
          setMessage('Не удалось загрузить панель администратора');
          setMessageType('error');
        }
        setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const submitRole = async (userId, role, action) => {
    try {
      const res = await apiPost('/api/admin/role', {
        targetUserId: userId,
        role,
        action
      });
      setMessage(res.message || 'Готово');
      setMessageType(res.success ? 'success' : 'error');
      await refreshDashboard();
    } catch (err) {
      console.error('Failed to update role:', err);
      setMessage('Не удалось обновить роль');
      setMessageType('error');
    }
  };

  const submitCoins = async (userId, action, amount) => {
    try {
      const res = await apiPost('/api/admin/coins', {
        targetUserId: userId,
        action,
        amount: Number(amount || 0)
      });
      setMessage(res.message || 'Готово');
      setMessageType(res.success ? 'success' : 'error');
      await refreshDashboard();
    } catch (err) {
      console.error('Failed to update coins:', err);
      setMessage('Не удалось обновить монеты');
      setMessageType('error');
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
        title="Доступ к админке запрещен"
        message="У вашей учетной записи нет прав администратора."
        actionLabel="В личный кабинет"
        actionHref="/dashboard"
      />
    );
  }

  if (loading) {
    return (
      <div className="admin-page">
        <h1>Панель администратора</h1>
        <div className="alert">Загрузка...</div>
      </div>
    );
  }

  return (
    <div className="admin-page">
      <h1>Панель администратора</h1>

      <div className="nav">
        <a className="btn" href="/">На главную</a>
        <a className="btn btn-primary" href="/dashboard">Личный кабинет</a>
        <a className="btn" href="/moderator/dashboard">История модерации</a>
        <button className="btn" type="button" onClick={handleLogout}>Выйти</button>
      </div>

      <div className="alert-slot">
        {message && <div className={`alert ${messageType || ''}`}>{message}</div>}
      </div>

      <div className="stats">
        <div className="stat-card">
          <h3>Всего пользователей</h3>
          <p>{stats?.totalUsers ?? 0}</p>
        </div>
        <div className="stat-card">
          <h3>Администраторов</h3>
          <p>{stats?.adminCount ?? 0}</p>
        </div>
        <div className="stat-card">
          <h3>Модераторов</h3>
          <p>{stats?.moderatorCount ?? 0}</p>
        </div>
      </div>

      <h2>Пользователи</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Имя</th>
            <th>Роли</th>
            <th>Монеты</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          {users.length === 0 && (
            <tr>
              <td colSpan="6">Пользователи не найдены</td>
            </tr>
          )}
          {users.map((user) => {
            const userRoles = new Set((user.roles || []).map(String));
            const isModerator = userRoles.has('MODERATOR') || userRoles.has('ADMIN');
            const isAdmin = userRoles.has('ADMIN');

            return (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.email}</td>
                <td>{user.name}</td>
                <td>
                  {(user.roles || []).map((role, index) => (
                    <span key={`${user.id}-role-${index}`}>
                      {getRoleLabel(role)}
                      <br />
                    </span>
                  ))}
                </td>
                <td>{user.coins}</td>
                <td>
                  <div className="actions">
                    <div>
                      <button
                        className="btn"
                        type="button"
                        onClick={() => submitRole(user.id, 'MODERATOR', isModerator ? 'revoke' : 'assign')}
                      >
                        {isModerator ? 'Снять модератора' : 'Назначить модератором'}
                      </button>
                    </div>
                    <div>
                      <button
                        className="btn btn-primary"
                        type="button"
                        onClick={() => submitRole(user.id, 'ADMIN', isAdmin ? 'revoke' : 'assign')}
                      >
                        {isAdmin ? 'Снять админа' : 'Назначить админом'}
                      </button>
                    </div>
                    <div className="inline-form">
                      <input
                        type="number"
                        min="1"
                        defaultValue="50"
                        id={`coins-add-${user.id}`}
                      />
                      <button
                        className="btn"
                        type="button"
                        onClick={() => submitCoins(user.id, 'add', document.getElementById(`coins-add-${user.id}`).value)}
                      >
                        + Монеты
                      </button>
                    </div>
                    <div className="inline-form">
                      <input
                        type="number"
                        min="1"
                        defaultValue="20"
                        id={`coins-deduct-${user.id}`}
                      />
                      <button
                        className="btn btn-danger"
                        type="button"
                        onClick={() => submitCoins(user.id, 'deduct', document.getElementById(`coins-deduct-${user.id}`).value)}
                      >
                        - Монеты
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export default AdminDashboard;

