import React, { useEffect, useState } from 'react';
import { apiGet, apiPost } from '../../api';
import './AdminDashboard.css';

function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    let active = true;
    apiGet('/api/admin/dashboard')
      .then((data) => {
        if (!active) return;
        setUsers(data.users || []);
        setStats(data.stats || null);
      })
      .catch(() => {
        if (!active) return;
        setMessage('Failed to load admin dashboard');
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
      setMessage(res.message);
    } catch (err) {
      setMessage('Role update failed');
    }
  };

  const submitCoins = async (userId, action, amount) => {
    try {
      const res = await apiPost('/api/admin/coins', {
        targetUserId: userId,
        action,
        amount: Number(amount || 0)
      });
      setMessage(res.message);
    } catch (err) {
      setMessage('Coins update failed');
    }
  };

  const submitSanction = async (userId, type, duration, reason) => {
    try {
      const res = await apiPost('/api/admin/sanction', {
        targetUserId: userId,
        type,
        duration: duration ? Number(duration) : null,
        reason
      });
      setMessage(res.message);
    } catch (err) {
      setMessage('Sanction update failed');
    }
  };

  return (
    <div className="admin-page">
      <h1>Admin dashboard</h1>
      {message && <div className="admin-message">{message}</div>}

      {stats && (
        <div className="admin-stats">
          <div className="admin-card">
            <div className="admin-card-title">Total users</div>
            <div className="admin-card-value">{stats.totalUsers}</div>
          </div>
          <div className="admin-card">
            <div className="admin-card-title">Admins</div>
            <div className="admin-card-value">{stats.adminCount}</div>
          </div>
          <div className="admin-card">
            <div className="admin-card-title">Moderators</div>
            <div className="admin-card-value">{stats.moderatorCount}</div>
          </div>
        </div>
      )}

      <table className="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Name</th>
            <th>Roles</th>
            <th>Coins</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => {
            const roles = (user.roles || []).map((role) => String(role));
            const isModerator = roles.includes('MODERATOR');
            const isAdmin = roles.includes('ADMIN');

            return (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.email}</td>
                <td>{user.name}</td>
                <td>{roles.join(', ')}</td>
                <td>{user.coins}</td>
                <td>
                  <div className="admin-actions">
                    <button
                      type="button"
                      onClick={() => submitRole(user.id, 'MODERATOR', isModerator ? 'revoke' : 'assign')}
                    >
                      {isModerator ? 'Revoke moderator' : 'Assign moderator'}
                    </button>
                    <button
                      type="button"
                      onClick={() => submitRole(user.id, 'ADMIN', isAdmin ? 'revoke' : 'assign')}
                    >
                      {isAdmin ? 'Revoke admin' : 'Assign admin'}
                    </button>
                    <div className="admin-inline">
                      <input
                        type="number"
                        min="1"
                        defaultValue="50"
                        id={`coins-add-${user.id}`}
                      />
                      <button
                        type="button"
                        onClick={() => submitCoins(user.id, 'add', document.getElementById(`coins-add-${user.id}`).value)}
                      >
                        Add coins
                      </button>
                    </div>
                    <div className="admin-inline">
                      <input
                        type="number"
                        min="1"
                        defaultValue="20"
                        id={`coins-deduct-${user.id}`}
                      />
                      <button
                        type="button"
                        onClick={() => submitCoins(user.id, 'deduct', document.getElementById(`coins-deduct-${user.id}`).value)}
                      >
                        Deduct coins
                      </button>
                    </div>
                    <div className="admin-inline">
                      <select id={`sanction-type-${user.id}`} defaultValue="freeze">
                        <option value="freeze">Freeze</option>
                        <option value="ban">Ban</option>
                        <option value="lift">Lift</option>
                      </select>
                      <input
                        type="number"
                        min="1"
                        placeholder="Duration"
                        id={`sanction-duration-${user.id}`}
                      />
                      <input
                        type="text"
                        placeholder="Reason"
                        id={`sanction-reason-${user.id}`}
                      />
                      <button
                        type="button"
                        onClick={() => {
                          const type = document.getElementById(`sanction-type-${user.id}`).value;
                          const duration = document.getElementById(`sanction-duration-${user.id}`).value;
                          const reason = document.getElementById(`sanction-reason-${user.id}`).value;
                          submitSanction(user.id, type, duration, reason);
                        }}
                      >
                        Apply
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

