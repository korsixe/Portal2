import React, { useState, useEffect } from 'react';
import './Login.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/users/me', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        setIsLoggedIn(true);
      }
    } catch (error) {
      console.log('Не авторизован');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include'
      });

      if (response.ok) {
        setMessageType('success');
        setMessage('✅ Вход выполнен успешно!');
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 1000);
      } else {
        setMessageType('error');
        setMessage('❌ Неверный email или пароль');
      }
    } catch (error) {
      setMessageType('error');
      setMessage('❌ Ошибка сети. Сервер недоступен.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/api/users/logout', {
        method: 'POST',
        credentials: 'include'
      });
      setIsLoggedIn(false);
      window.location.href = '/login';
    } catch (error) {
      console.error('Ошибка выхода:', error);
    }
  };

  if (isLoggedIn) {
    return (
        <div className="portal-container">
          <div className="portal-logo">PORTAL</div>
          <div className="portal-subtitle">Вход</div>
          <div className="button-group">
            <a href="/dashboard" className="btn btn-primary">Перейти в личный кабинет</a>
            <a href="/" className="btn btn-secondary">На главную</a>
            <button onClick={handleLogout} className="btn btn-danger">Выйти</button>
          </div>
        </div>
    );
  }

  return (
      <div className="portal-container">
        <div className="portal-logo">PORTAL</div>
        <div className="portal-subtitle">Вход</div>

        {message && (
            <div className={`message ${messageType}`}>
              {message}
            </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
                type="email"
                id="email"
                name="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="ivanov.ii@phystech.edu"
                required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Пароль</label>
            <input
                type="password"
                id="password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Введите ваш пароль"
                required
            />
          </div>

          <div className="button-group">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Вход...' : 'Войти'}
            </button>
            <a href="/register" className="btn btn-secondary">Регистрация</a>
          </div>
        </form>
      </div>
  );
};

export default Login;