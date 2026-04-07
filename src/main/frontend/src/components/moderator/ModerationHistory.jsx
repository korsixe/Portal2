import React, { useEffect, useState } from 'react';
import { apiGet } from '../../api';
import './ModerationHistory.css';

function ModerationHistory() {
  const [history, setHistory] = useState([]);
  const [adminActions, setAdminActions] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    apiGet('/api/moderator/history')
      .then((data) => {
        setHistory(data.history || []);
        setAdminActions(data.adminActions || []);
      })
      .catch(() => setMessage('Failed to load history'));
  }, []);

  return (
    <div className="history-page">
      <h1>Moderation history</h1>
      {message && <div className="history-message">{message}</div>}

      <h2>Ads history</h2>
      <table className="history-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Ad</th>
            <th>From</th>
            <th>To</th>
            <th>Moderator</th>
            <th>Time</th>
            <th>Reason</th>
          </tr>
        </thead>
        <tbody>
          {history.map((item, index) => (
            <tr key={`${item.id}-${index}`}>
              <td>{index + 1}</td>
              <td>{item.adId}</td>
              <td>{item.fromStatus || '-'}</td>
              <td>{item.toStatus}</td>
              <td>{item.moderatorId || '-'}</td>
              <td>{item.createdAt}</td>
              <td>{item.reason}</td>
            </tr>
          ))}
          {history.length === 0 && (
            <tr>
              <td colSpan="7">No entries</td>
            </tr>
          )}
        </tbody>
      </table>

      <h2>Admin actions</h2>
      <table className="history-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Action</th>
            <th>Target</th>
            <th>Details</th>
            <th>Actor</th>
            <th>Time</th>
          </tr>
        </thead>
        <tbody>
          {adminActions.map((item, index) => (
            <tr key={`${item.id}-${index}`}>
              <td>{index + 1}</td>
              <td>{item.actionType}</td>
              <td>{item.targetType} {item.targetId}</td>
              <td>{item.details}</td>
              <td>{item.actorEmail || item.actorId}</td>
              <td>{item.createdAt}</td>
            </tr>
          ))}
          {adminActions.length === 0 && (
            <tr>
              <td colSpan="6">No entries</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default ModerationHistory;

