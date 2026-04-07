import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './SupportChat.css';

const API_BASE = 'http://localhost:8080';

const formatDate = (value) => {
  if (!value) return '';
  return new Date(value).toLocaleString('ru-RU');
};

const SupportChat = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');
  const [feedback, setFeedback] = useState('');

  const loadMessages = async () => {
    setLoading(true);
    setFeedback('');
    try {
      const response = await fetch(`${API_BASE}/api/support/messages`, {
        credentials: 'include'
      });

      if (response.status === 401) {
        navigate('/login');
        return;
      }

      if (!response.ok) {
        setFeedback('Не удалось загрузить обращения');
        return;
      }

      const data = await response.json();
      setMessages(Array.isArray(data) ? data : []);
    } catch (e) {
      setFeedback(e.message || 'Ошибка загрузки обращений');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMessages();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const sendMessage = async (e) => {
    e.preventDefault();
    const trimmed = message.trim();
    if (!trimmed) return;

    setFeedback('');
    const response = await fetch(`${API_BASE}/api/support/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ message: trimmed })
    });

    if (response.status === 401) {
      navigate('/login');
      return;
    }

    if (!response.ok) {
      const text = await response.text();
      setFeedback(text || 'Ошибка отправки обращения');
      return;
    }

    setMessage('');
    await loadMessages();
  };

  return (
    <div className="supportPage">
      <div className="supportCard">
        <div className="supportHeader">
          <h1>Техподдержка</h1>
        </div>

        {feedback && <div className="supportFeedback">{feedback}</div>}

        <div className="supportMessages">
          {loading ? (
            <div className="empty">Загрузка...</div>
          ) : messages.length === 0 ? (
            <div className="empty">Пока нет обращений. Напишите первое сообщение.</div>
          ) : (
            messages.map((item) => (
              <div key={item.id} className="supportMessage">
                <div className="meta">
                  <strong>{item.userName}</strong>
                  <span>{formatDate(item.createdAt)}</span>
                </div>
                <p>{item.message}</p>
              </div>
            ))
          )}
        </div>

        <form className="supportForm" onSubmit={sendMessage}>
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="Опишите проблему или вопрос"
            required
          />
          <div className="supportActions">
            <button type="button" onClick={() => navigate('/dashboard')}>Назад в ЛК</button>
            <button type="submit">Отправить обращение</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SupportChat;

