import React, { useEffect, useState } from 'react';
import { apiGet, apiPost } from '../../api';
import './ModeratorDashboard.css';

function ModeratorDashboard() {
  const [ads, setAds] = useState([]);
  const [stats, setStats] = useState(null);
  const [message, setMessage] = useState('');
  const [reasons, setReasons] = useState({});

  const load = () => {
    apiGet('/api/moderator/dashboard')
      .then((data) => {
        setAds(data.ads || []);
        setStats(data.stats || null);
      })
      .catch(() => setMessage('Failed to load moderator dashboard'));
  };

  useEffect(() => {
    load();
  }, []);

  const setReason = (adId, value) => {
    setReasons((prev) => ({ ...prev, [adId]: value }));
  };

  const act = async (path, adId) => {
    try {
      const res = await apiPost(path, {
        adId,
        reason: reasons[adId] || ''
      });
      setMessage(res.message);
      load();
    } catch (err) {
      setMessage('Action failed');
    }
  };

  return (
    <div className="moderator-page">
      <h1>Moderator dashboard</h1>
      {message && <div className="moderator-message">{message}</div>}

      {stats && (
        <div className="moderator-stats">
          <div className="moderator-card">
            <div className="moderator-card-title">Total users</div>
            <div className="moderator-card-value">{stats.totalUsers}</div>
          </div>
          <div className="moderator-card">
            <div className="moderator-card-title">Moderators</div>
            <div className="moderator-card-value">{stats.moderatorCount}</div>
          </div>
          <div className="moderator-card">
            <div className="moderator-card-title">Admins</div>
            <div className="moderator-card-value">{stats.adminCount}</div>
          </div>
        </div>
      )}

      <div className="moderator-list">
        {ads.length === 0 && <div>No pending ads</div>}
        {ads.map((ad) => (
          <div className="moderator-card-item" key={ad.id}>
            <h3>{ad.title}</h3>
            <div className="moderator-meta">ID: {ad.id}</div>
            <div className="moderator-meta">Price: {ad.price}</div>
            <p>{ad.description}</p>
            <input
              type="text"
              placeholder="Reason (optional)"
              value={reasons[ad.id] || ''}
              onChange={(e) => setReason(ad.id, e.target.value)}
            />
            <div className="moderator-actions">
              <button type="button" onClick={() => act('/api/moderator/approve', ad.id)}>Approve</button>
              <button type="button" onClick={() => act('/api/moderator/reject', ad.id)}>Reject</button>
              <button type="button" onClick={() => act('/api/moderator/delete', ad.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ModeratorDashboard;

