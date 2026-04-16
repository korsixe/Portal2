import React, { useState, useEffect } from 'react';
import './Login.css';
import Icon from './Icon';

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
      console.log('Not authenticated');
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
        setMessage('Login successful!');
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 1000);
      } else {
        setMessageType('error');
        setMessage('Invalid email or password');
      }
    } catch (error) {
      setMessageType('error');
      setMessage('Network error. Server unavailable.');
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
      console.error('Logout error:', error);
    }
  };

  if (isLoggedIn) {
    return (
        <div className="portal-container loginContainer">
          <div className="portal-logo">PORTAL</div>
          <div className="portal-subtitle">Sign In</div>
          <div className="button-group">
            <a href="/dashboard" className="btn btn-primary">Go to Dashboard</a>
            <a href="/" className="btn btn-secondary">Home</a>
            <button onClick={handleLogout} className="btn btn-danger">Sign Out</button>
          </div>
        </div>
    );
  }

  return (
      <div className="portal-container loginContainer">
        <div className="portal-logo">PORTAL</div>
        <div className="portal-subtitle">Sign In</div>

        {message && (
            <div className={`message ${messageType}`}>
              <Icon name={messageType === 'success' ? 'success' : 'error'} size={20} />
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
            <label htmlFor="password">Password</label>
            <input
                type="password"
                id="password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
            />
          </div>

          <div className="button-group">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
            <a href="/register" className="btn btn-secondary">Register</a>
          </div>
        </form>
      </div>
  );
};

export default Login;
